package com.kaitusoft.ratel.core.verticle;

import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.common.ProtocolEnum;
import com.kaitusoft.ratel.core.handler.IpFilterHandler;
import com.kaitusoft.ratel.core.handler.SystemHandler;
import com.kaitusoft.ratel.core.model.*;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import com.kaitusoft.ratel.core.model.option.SessionOption;
import com.kaitusoft.ratel.core.model.option.UpstreamOption;
import com.kaitusoft.ratel.core.model.po.ApiOption;
import com.kaitusoft.ratel.core.model.po.AppOption;
import com.kaitusoft.ratel.handler.Processor;
import com.kaitusoft.ratel.util.StringUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author frog.w
 * @version 1.0.0, 2018/8/18
 *          <p>
 *          write description here
 */
public class Application extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private App app;

    private String[] ipBlanklist;

    /**
     * 用来做代理请求的httpClient;
     */
    private HttpClient httpClient;

    private Router httpRouter;

    /**
     * 如果需要启用ssl
     */
    private Router httpsRouter;

    private boolean openSsl;

    private Map<String, List<Route>> apiRoutes = new HashMap<>();
    private String hostPort;

//    private AppStatus appStatus = new AppStatus();


    @Override
    public void start(Future<Void> startFuture) throws Exception {

        try {
            AppOption appOption = config().mapTo(AppOption.class);
            app = new App(appOption);
            hostPort = ":" + app.getPort();
            openSsl = app.getSsl() != null && app.getSsl().getPort() > 0;
        } catch (Exception e) {
            logger.error("加载应用配置出错！", e);
            startFuture.fail(e);
            return;
        }

        if (app.getPreference().getIpBlacklist() != null) {
            List ips = new ArrayList();
            for (String ip : app.getPreference().getIpBlacklist()) {
                if (StringUtils.isIpV4(ip) || StringUtils.isIpV6(ip)) {
                    ips.add(ip);
                }
            }

            ipBlanklist = new String[ips.size()];
            ips.toArray(ipBlanklist);
        }


        logger.debug("启动应用-{} ...", app.getName());


//        Handler<AsyncResult<Void>> createResult = new Handler<AsyncResult<Void>>() {
//            @Override
//            public void handle(AsyncResult<Void> result) {
//                if (result.succeeded()) {
//                    logger.debug("启动应用{}-创建服务:{}, ssl:{} 完毕!", app.getName(), app.getProtocol(), openSsl);
//                    startFuture.complete();
//                } else {
//                    logger.error("启动应用{}-创建服务:{}, ssl:{} 出错!", app.getName(), app.getProtocol(), openSsl, result.cause());
//                    startFuture.fail(result.cause());
//                }
//            }
//        };

        List<Future> futureList = new ArrayList<>();
        if (app.getProtocol() == ProtocolEnum.HTTP_HTTPS) {
            futureList.add(Future.<Void>future(http -> {
                createHttpServer(app.getPort(), res -> {
                    if (res.succeeded()) {
                        http.complete();
                        logger.debug("http 网关已启动");
                    } else {
                        http.fail(res.cause());
                        logger.debug("http 网关启动失败", res.cause());
                    }
                });
            }));
            if (openSsl)
                futureList.add(Future.<Void>future(https -> {
                    createHttpsServer(app.getSsl().getPort(), res -> {
                        if (res.succeeded()) {
                            https.complete();
                            logger.debug("https 网关已启动");
                        } else {
                            https.fail(res.cause());
                            logger.debug("https 网关启动失败", res.cause());
                        }
                    });
                }));
        } else {
            startFuture.fail(MessageFormat.format("暂时不支持协议:{0}", app.getProtocol()));
        }

        CompositeFuture.all(futureList).setHandler(res -> {
            if (res.succeeded()) {
                logger.info("应用已启动");
                registerConsumers();

                JsonObject action = new JsonObject();
                action.put("app", app.getId());
                action.put("deployId", Application.this.deploymentID());

                vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.ACTION_APP_DEPLOYED), action);
//                appStatus.deployed(Application.this.deploymentID());
                startFuture.complete();
            } else {
                logger.error("启动应用 -> 失败:", res.cause().getMessage());
                startFuture.fail(res.cause());
            }
        });

    }

    public void stop() throws Exception {
//        appStatus.unDeploy();
        vertx.eventBus().<Integer>send(Event.formatInternalAddress(Event.ACTION_APP_UNDEPLOYED), app.getId());

        if(httpClient != null)
            httpClient.close();

        apiRoutes.clear();
        
    }

    private void registerConsumers() {
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.START_API, app.getId()), this::startApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.STOP_API, app.getId()), this::stopApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.PAUSE_API, app.getId()), this::pauseApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.RESUME_API, app.getId()), this::resumeApi);
    }

    protected void startApi(Message<JsonObject> message) {
        ApiOption apiOption = message.body().mapTo(ApiOption.class);
        Api api = null;
        try {
            api = new Api(app, apiOption);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("deploy path :{} -> failed!", apiOption, e);
            message.fail(500, e.getMessage());
        }

        ProxyOption proxyOption = api.getProxyOption();
        HttpClient useHttpClient = null;
        if (proxyOption.getProxyType() == ProxyOption.ProxyType.UPSTREAM) {
            UpstreamOption upstreamOption = (UpstreamOption) proxyOption;
            if (upstreamOption.getThreadType() == UpstreamOption.UpstreamThreadType.APP) {
                useHttpClient = getAppHttpClient(api.buildHttpClientOption(app.getProxyOption()));
            } else {
                useHttpClient = vertx.createHttpClient(api.buildHttpClientOption());
            }
        }

        api.buildProxy(useHttpClient);

        addRoutes(api, httpRouter);
        if (openSsl)
            addRoutes(api, httpsRouter);
        app.addDeployApi(api);

        message.reply(1);

    }

    protected void stopApi(Message<String> message) {
        String ids = message.body();
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            List<Route> routes = apiRoutes.get(id);
            if (routes == null)
                continue;
            routes.forEach(route -> {
                route.disable();
                route.remove();
            });
            app.unDeployApi(Integer.valueOf(id));
        }

        message.reply(idArray.length);
    }

    protected void pauseApi(Message<String> message){
        String ids = message.body();
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            Api api = app.getDeployApi(Integer.valueOf(id));
            api.setRunning(App.PAUSED);
        }

        message.reply(idArray.length);
    }

    protected void resumeApi(Message<String> message){
        String ids = message.body();
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            Api api = app.getDeployApi(Integer.valueOf(id));
            api.setRunning(App.RUNNING);
        }

        message.reply(idArray.length);
    }

    private Route newRoute(Api path, Preference preference, Router router) {
        Route route = router.route();
        mount(route, path);
        if (preference.getMethod() != null && preference.getMethod().length > 0) {
            for (HttpMethod method : preference.getMethod()) {
                route.method(method);
            }
        }
        return route;
    }

    private void addRoutes(Api path, Router router) {
        List<Route> routes = new ArrayList<>();

        Preference preference = path.getPreference();

        Route initRoute = newRoute(path, preference, router);
        initRoute.handler(new SystemHandler(vertx, app, path));
        routes.add(initRoute);

        Processor limit = preference.getAccessLimit();
        if (limit != null) {
            Route route = newRoute(path, preference, router);
            route.handler(limit);
            routes.add(route);
        }

        Processor auth = preference.getAuth();
        if (auth != null) {
            Route route = newRoute(path, preference, router);
            route.handler(auth);
            routes.add(route);
        }

        if (!StringUtils.isEmpty(preference.getDocRoot())) {
            //挂载自定义静态目录
        }

        Processor[] preProcessors = preference.getPreProcessors();
        if (preProcessors != null && preProcessors.length == 0) {
            for (Processor pro : preProcessors) {
                Route route = newRoute(path, preference, router);
                route.handler(pro);
                routes.add(route);
            }
        }

        Route proxy = newRoute(path, preference, router);
        proxy.handler(path.getProxy());
        routes.add(proxy);

        Processor[] postProcessors = preference.getPostProcessors();
        if (postProcessors != null && postProcessors.length == 0) {
            for (Processor pro : postProcessors) {
                Route route = newRoute(path, preference, router);
                route.handler(pro);
                routes.add(route);
            }
        }

        //ip
        //limit
        //auth
        //pre
        //mainProcess
        //post
//        Route mainRoute = router.route();
//        mount(mainRoute, path.getPath());
//        mainRoute.handler(path.getProxy());


        apiRoutes.put(path.getId().toString(), routes);
        logger.debug("path mounted -> {}", path);

    }

    private synchronized HttpClient getAppHttpClient(HttpClientOptions options) {
        if (httpClient == null)
            httpClient = Vertx.vertx().createHttpClient(options);
        return httpClient;
    }

    /**
     * 判断path类型，选择是否正则匹配
     *
     * @param path
     */
    private Route mount(Route route, Api path) {
        if (path.isRegexp())
            return route.pathRegex(path.getPath());

        if (path.isCapture())
            return route.pathRegex(path.getPath());

        return route.path(path.getPath());
    }

    private void createHttpsServer(int port, Handler<AsyncResult<Void>> result) {
        createHttpServer(true, port, result);
    }

    private void createHttpServer(int port, Handler<AsyncResult<Void>> result) {
        createHttpServer(false, port, result);
    }


    private void createHttpServer(boolean ssl, int port, Handler<AsyncResult<Void>> result) {
        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setPort(port);
        Router refRouter = null;
        if (ssl)
            refRouter = httpsRouter = Router.router(vertx);
        else
            refRouter = httpRouter = Router.router(vertx);

        refRouter.route().handler((context) -> {
            hostFilter(context, app.getVhost());
        });

        if (this.ipBlanklist != null && this.ipBlanklist.length > 0)
            refRouter.route().handler(new IpFilterHandler(this.ipBlanklist));

        refRouter.route().handler(CookieHandler.create());

        SessionOption sessionOption = app.getSessionOption();

        refRouter.route().handler((context) -> {
            HttpServerRequest request = context.request();
            if (request.method().equals(HttpMethod.POST) || request.method().equals(HttpMethod.PUT)) {
//                if(sessionOption != null && vertx.isClustered())
//                    context.request().bodyHandler(body -> {
//                        if (body != null)
//                            context.put(ContextAttribute.CTX_REQ_BODY, body);
//                    });

                context.request().setExpectMultipart(true);
            }
            context.next();
        });

        if (sessionOption != null) {

            SessionStore sessionStore;
            if (vertx.isClustered()) {
                sessionStore = ClusteredSessionStore.create(vertx);
            } else {
                sessionStore = LocalSessionStore.create(vertx);
            }
            SessionHandler sessionHandler = SessionHandler.create(sessionStore);
            sessionHandler.setSessionCookieName(sessionOption.getName());
            sessionHandler.setSessionTimeout(sessionOption.getInterval() * 1000);

            refRouter.route().handler(sessionHandler);
        }

        if (app.getCrossDomain() != null) {
            CrossDomain crossDomain = app.getCrossDomain();
            CorsHandler corsHandler = CorsHandler.create(crossDomain.getAllowedOrigin());
            if (crossDomain.getAllowedHeaders() != null) {
                corsHandler.allowedHeaders(crossDomain.getAllowedHeaders());
            }
            corsHandler.allowCredentials(crossDomain.isAllowCredentials());
            if (crossDomain.getExposedHeaders() != null) {
                corsHandler.exposedHeaders(crossDomain.getExposedHeaders());
            }
            if (crossDomain.getAllowedMethods() != null) {
                corsHandler.allowedMethods(crossDomain.getAllowedMethods());
            }
            corsHandler.maxAgeSeconds(crossDomain.getMaxAgeSeconds());
            refRouter.route().handler(corsHandler);
        }

        //挂载静态目录
        if (!StringUtils.isEmpty(app.getPreference().getDocRoot())) {
            refRouter.route().handler(StaticHandler.create(app.getPreference().getDocRoot()).setCachingEnabled(true));
        }

        routingCommonStatus();

        if (vertx.isNativeTransportEnabled()) {
            serverOptions.setTcpFastOpen(true).setTcpCork(true).setTcpQuickAck(true).setReusePort(true);
        }

        // 创建https服务器
        if (ssl) {
            serverOptions.setSsl(true);
            Ssl cert = app.getSsl();
            if (cert.getCertType() == Ssl.CertType.PEM) {
                serverOptions
                        .setPemKeyCertOptions(new PemKeyCertOptions().setCertPath(cert.getCertPath()).setKeyPath(cert.getKeyPath()));
            } else if (cert.getCertType() == Ssl.CertType.PFX) {
                serverOptions.setPfxKeyCertOptions(new PfxOptions().setPath(cert.getCertPath()).setPassword(cert.getKeyPath()));
            } else if (cert.getCertType() == Ssl.CertType.JKS) {
                serverOptions.setKeyStoreOptions(new JksOptions().setPath(cert.getCertPath()).setPassword(cert.getKeyPath()));
            }
//            else if(cert.getType() == Ssl.CertType.KEY){
//                serverOptions.setKeyCertOptions(new JksOptions().setPath(cert.getCertPath()).setPassword(cert.getKeyPath()).setValue(null));
//
//            }

            else {
                logger.error("不支持的证书类型:{}", cert.getCertType());
                result.handle(Future.failedFuture(MessageFormat.format("不支持的证书类型:{0}，目前支持 PEM，PFX，JKS", cert.getCertType())));
                return;
            }

        }

        refRouter.route().order(99999).handler(notfound -> {
            HttpServerResponse response = notfound.response();
            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end("not found");
        });

        try {
            HttpServer server = vertx.createHttpServer(serverOptions);

            server.requestHandler(refRouter::accept);

//            server.websocketHandler(ws -> {
//                logger.debug("ws connect: {}", ws.textHandlerID());
//                ws.frameHandler(frame -> {
//                    logger.debug("ws data: {}", frame.textData());
//                });
//            });

            server.listen(serverOptions.getPort(), startResult -> {
                if (startResult.succeeded()) {
                    System.err.println(MessageFormat.format("应用网关: {0} 已创建，端口: {1,number,0}，协议: {2}", app.getName(), serverOptions.getPort(), app.getProtocol()));
                    result.handle(Future.succeededFuture());
                } else {
                    System.err.println("创建应用网关失败 : " + startResult.cause());
                    result.handle(Future.failedFuture(startResult.cause()));
                }
            });


        } catch (Exception e) {
            logger.error("创建应用网关失败 : ", e);

        }

    }

    private boolean needFilterHost() {
        if (app.getVhost() == null || app.getVhost().length == 0)
            return false;

        for (String host : app.getVhost()) {
            if (host.equals("*"))
                return false;
        }

        return true;
    }

    private void hostFilter(RoutingContext context, String[] defineHosts) {
        String host = context.request().host();
        host = host.replace(hostPort, "");
        if (!hostMatch(host, defineHosts)) {
            context.response().end();
            return;
        }

        context.next();
    }

    public static boolean hostMatch(String host, String[] defineHosts) {
        if (defineHosts == null || defineHosts.length == 0)
            return true;

        boolean match = false;
        for (String ref : defineHosts) {
            if (ref.equals("*"))
                return true;

            match = StringUtils.isMatch(host, ref);
        }

        return match;
    }

    /**
     * 给404。500等等常规响应做路由配置
     */
    private void routingCommonStatus() {

    }

}
