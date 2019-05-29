package com.kaitusoft.ratel.core.verticle;

import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.common.StatusCode;
import com.kaitusoft.ratel.core.handler.IpFilterHandler;
import com.kaitusoft.ratel.core.handler.SystemHandler;
import com.kaitusoft.ratel.core.handler.VHostHandler;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author frog.w
 * @version 1.0.0, 2019/5/27
 *          <p>
 *          write description here
 */
public class ServerVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(ServerVerticle.class);

    private final Map<Integer, App> APPS = new ConcurrentHashMap<>(16, 0.75f, 4);

    /**
     * 有序集合，根据app vhost顺序进行匹配
     */
    private final Queue<App> MATH_QUEUE = new ConcurrentLinkedQueue<>();
//    private static final Map<Integer, Router> ROUTER_MAP = new HashMap<>();

    private final Map<Integer, HttpServer> SERVER = new HashMap<>();

    private final Map<Integer, Set> APP_ON_PORT = new HashMap<>();

    private final Map<String, Router> HOST_ROUTER_MAP = new ConcurrentHashMap<>(16, 0.75f, 4);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        registerConsumers();
        startFuture.complete();
    }

    public void stop() throws Exception {
        APPS.clear();
        APP_ON_PORT.clear();
        HOST_ROUTER_MAP.clear();

        stopServer(false);
        SERVER.clear();
    }

    private void registerConsumers() {
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.START_APP_ACT), this::startApp);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.STOP_APP_ACT), this::stopApp);

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.START_API_ACT), this::startApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.STOP_API_ACT), this::stopApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.PAUSE_API_ACT), this::pauseApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.RESUME_API_ACT), this::resumeApi);
    }

    private synchronized void stopApp(Message<String> message) {
        String ids = message.body();
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            App app = getApp(Integer.parseInt(id));
            if(app == null)
                continue;
            removeAppFromPort(app.getId());
            app.unDeployAllApi();

            APPS.remove(app.getId());
        }

        JsonObject result = new JsonObject();
        result.put("stoped", idArray.length);
        result.put("left", APPS.size());

        stopServer(true);

        message.reply(result);
    }

    private void stopServer(boolean filterIdle) {
        List<Integer> idles = new ArrayList<Integer>();
        APP_ON_PORT.forEach((k, apps) -> {
            if(!filterIdle || (filterIdle && (apps == null || apps.size() == 0))){
                idles.add(k);
            }
        });

        for(int idle : idles){
            HttpServer server = SERVER.get(idle);
            server.close(res -> {
                if(res.succeeded()) {
                    logger.debug("Server on {} closed!", idle);
                }else{
                    logger.error("Server on {} cant close", idle, res.cause());
                }
            });
        }
    }

    private void removeAppFromPort(int appId){
        APP_ON_PORT.forEach((k, apps) -> {
            apps.remove(appId);
        });
    }

    private synchronized void startApp(Message<JsonObject> message) {
        AppOption appOption = message.body().mapTo(AppOption.class);
        App app = getApp(appOption.getId());
        if(app == null) {
            try {
                app = new App(appOption);
                Router router = Router.router(vertx);
                app.setRouter(router);
                APPS.put(app.getId(), app);
            } catch (Exception e) {
                logger.error("parse app json error:", e);
                message.fail(StatusCode.SYS_ERROR, e.getMessage());
                return;
            }
        }

        List<Future> futureList = new ArrayList<>();

        int port = app.getPort();
        if(SERVER.get(port) == null) {
            futureList.add(Future.<Void>future(http -> {
                logger.debug("no Server run on port:{}, now create...", port);
                createHttpServer(port, res -> {
                    if (res.succeeded()) {
                        logger.debug("HTTP Server Created! port:{}", port);
                        SERVER.put(port, res.result());
                        http.complete();
                    } else {
                        logger.error("HTTP Server ERROR! port:{}", port);
                        http.fail(res.cause());
                    }

                });
            }));
        }

        final Ssl cert = app.getSsl();
        final int sslPort = cert != null ? cert.getPort() : -1;
        if(sslPort > 0 && SERVER.get(sslPort) == null){
            futureList.add(Future.<Void>future(https -> {
                logger.debug("no Server run on port:{}, now create...", sslPort);
                createHttpsServer(sslPort, cert, res -> {
                    if (res.succeeded()) {
                        logger.debug("HTTPS Server Created! port:{}", sslPort);
                        SERVER.put(sslPort, res.result());
                        https.complete();
                    } else {
                        logger.error("HTTPS Server ERROR! port:{}", sslPort);
                        https.fail(res.cause());
                    }
                });
            }));
        }

        CompositeFuture.all(futureList).setHandler(res -> {
            if (res.succeeded()) {
                logger.info("Server已启动 app:{}- port:{}, sslPort:{}", appOption.getId(), port, sslPort);
                message.reply(this.deploymentID());
            } else {
                logger.error("Server -> 启动失败:", res.cause().getMessage());
                message.fail(StatusCode.SYS_ERROR, res.cause().getMessage());
            }
        });
    }


    protected void startApi(Message<JsonObject> message) {
        ApiOption apiOption = message.body().mapTo(ApiOption.class);
        App app = getApp(apiOption.getAppId());
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
                useHttpClient = app.getHttpClient(buildHttpClientOption(app.getProxyOption()));
            } else {
                useHttpClient = vertx.createHttpClient(buildHttpClientOption((UpstreamOption)api.getProxyOption()));
            }
        }

        api.buildProxy(useHttpClient);

        route(api, app.getRouter());

        message.reply(1);
    }

    protected void stopApi(Message<JsonObject> message) {
        JsonObject body = message.body();
        String appId = body.getString("appId");
        String ids = body.getString("id");

        String[] idArray = ids.split(",");
        App app = getApp(Integer.parseInt(appId));

        for (String id : idArray) {
            app.unDeployApi(Integer.valueOf(id));
        }

        message.reply(idArray.length);
    }

    protected void pauseApi(Message<JsonObject> message){
        JsonObject body = message.body();
        String appId = body.getString("appId");
        String ids = body.getString("id");

        String[] idArray = ids.split(",");
        App app = getApp(Integer.parseInt(appId));

        for (String id : idArray) {
            Api api = app.getDeployApi(Integer.valueOf(id));
            api.setRunning(App.PAUSED);
        }

        message.reply(idArray.length);
    }

    protected void resumeApi(Message<JsonObject> message){
        JsonObject body = message.body();
        String appId = body.getString("appId");
        String ids = body.getString("id");

        String[] idArray = ids.split(",");
        App app = getApp(Integer.parseInt(appId));

        for (String id : idArray) {
            Api api = app.getDeployApi(Integer.valueOf(id));
            api.setRunning(App.RUNNING);
        }

        message.reply(idArray.length);
    }


    private App getApp(Integer appId) {
        return APPS.get(appId);
    }

    private void createHttpsServer(int port, Ssl cert, Handler<AsyncResult<HttpServer>> result) {
        createHttpServer(true, port, cert, result);
    }

    private void createHttpServer(int port, Handler<AsyncResult<HttpServer>> result) {
        createHttpServer(false, port, null, result);
    }

    private void createHttpServer(boolean ssl, int port, Ssl cert, Handler<AsyncResult<HttpServer>> result) {
        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setPort(port);
        Router refRouter = Router.router(vertx);

        refRouter.route().order(Integer.MAX_VALUE).handler(notfound -> {
            HttpServerResponse response = notfound.response();
            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end("not found");
        });

        // 创建https服务器
        if (ssl) {
            serverOptions.setSsl(true);
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

        if (vertx.isNativeTransportEnabled()) {
            serverOptions.setTcpFastOpen(true).setTcpCork(true).setTcpQuickAck(true).setReusePort(true);
        }

        try {
            HttpServer server = vertx.createHttpServer(serverOptions);
            server.requestHandler(request -> {
                String host = request.host();
                Router useRouter = HOST_ROUTER_MAP.get(host);
                if(useRouter != null){
                    useRouter.handle(request);
                }else{
                    Set<Map.Entry<Integer, App>> appSet =  APPS.entrySet();
                    for(Map.Entry<Integer, App> entry : appSet){
                        App app = entry.getValue();
                        if(app.match(host)) {
                            Router router = app.getRouter();
                            HOST_ROUTER_MAP.put(host, router);
                            router.handle(request);
                            return;
                        }
                    }

                    refRouter.handle(request);
                }
            });

            server.listen(serverOptions.getPort(), startResult -> {
                if (startResult.succeeded()) {
                    result.handle(Future.succeededFuture(server));
                } else {
                    result.handle(Future.failedFuture(startResult.cause()));
                }
            });
        } catch (Exception e) {
            logger.error("创建应用网关失败 : ", e);

        }

    }

    /**
     * host->
     * base routes
     * -> ip -> limit -> auth-> xss/sql .... security filters ->
     *
     * pre processors-> static/main proxy -> post processors
     *
     * @param path
     * @param router
     */
    private void route(Api path, Router router) {
        App app = path.getApp();
        List<Route> routes = new ArrayList<>();

        Preference preference = path.getPreference();
        HttpMethod[] routeMethods = preference.getMethod();

        Route route = newRoute(path, routeMethods, router);
        routes.add(route);

        routeBase(path.getApp(), route);

        route.handler(new SystemHandler(vertx, app, path));

        Processor limit = preference.getAccessLimit();
        if (limit != null) {
            route.handler(limit);
        }

        Processor auth = preference.getAuth();
        if (auth != null) {
            route.handler(auth);
        }

        Processor sqlFilter = preference.getSqlFilter();
        if(sqlFilter != null){
            route.handler(sqlFilter);
        }

        if (!StringUtils.isEmpty(preference.getDocRoot())) {
            //挂载自定义静态目录
        }

        Processor[] preProcessors = preference.getPreProcessors();
        if (preProcessors != null && preProcessors.length == 0) {
            for (Processor pro : preProcessors) {
                route.handler(pro);
            }
        }

        route.handler(path.getProxy());

        Processor[] postProcessors = preference.getPostProcessors();
        if (postProcessors != null && postProcessors.length == 0) {
            for (Processor pro : postProcessors) {
                route.handler(pro);
            }
        }

        app.addDeployApi(path, routes);

        logger.debug("path mounted -> {}", path);
    }

    private Route newRoute(Api path, HttpMethod[] methods, Router router) {
        Route route = router.route();
        mount(route, path);
        if (methods != null && methods.length > 0) {
            for (HttpMethod method : methods) {
                route.method(method);
            }
        }
        return route;
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


    private void routeBase(App app, Route route){
        String[] ipBlanklist = app.getPreference().getIpBlacklist();
        if (ipBlanklist != null && ipBlanklist.length > 0)
            route.handler(new IpFilterHandler(ipBlanklist));

        /**
         * 设置服务选项，如上传路径，请求体大小
         */
//        refRouter.route().handler(BodyHandler.create());

        SessionOption sessionOption = app.getSessionOption();
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
            route.handler(CookieHandler.create()).handler(sessionHandler);
        }

        route.handler(context -> {
            HttpServerRequest request = context.request();
            if (request.method().equals(HttpMethod.POST) || request.method().equals(HttpMethod.PUT)) {
                context.request().setExpectMultipart(true);
            }
            context.next();
        });

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
            route.handler(corsHandler);
        }

        //挂载静态目录
        if (!StringUtils.isEmpty(app.getPreference().getDocRoot())) {
            route.handler(StaticHandler.create(app.getPreference().getDocRoot()).setCachingEnabled(true));
        }

        routingCommonStatus(app, route);
    }

    /**
     * 给404。500等等常规响应做路由配置
     */
    private void routingCommonStatus(App app, Route route) {

    }


    private HttpClientOptions buildHttpClientOption(UpstreamOption upstreamOption) {
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        if (upstreamOption == null)
            return httpClientOptions;
        httpClientOptions.setMaxPoolSize(upstreamOption.getMaxPoolSize());
        httpClientOptions.setMaxInitialLineLength(upstreamOption.getMaxInitialLineLength());
        httpClientOptions.setMaxHeaderSize(upstreamOption.getMaxHeaderSize());
        httpClientOptions.setKeepAlive(upstreamOption.isKeepAlive());
        if(httpClientOptions.isKeepAlive())
            httpClientOptions.setKeepAliveTimeout(upstreamOption.getKeepAliveTimeout());
        httpClientOptions.setConnectTimeout(upstreamOption.getTimeout());
        httpClientOptions.setIdleTimeout(upstreamOption.getMaxIdleTimeout());
        httpClientOptions.setMaxWaitQueueSize(upstreamOption.getMaxWaitQueueSize());
//        if(upstreamOption.isKeepAlive()) {
//            httpClientOptions.setPipelining(true);
//            httpClientOptions.setPipeliningLimit(2);
//        }else{
//            httpClientOptions.setPipelining(false);
//        }
        httpClientOptions.setPipelining(false);
        //not verify host
        httpClientOptions.setVerifyHost(false);
        httpClientOptions.setTcpFastOpen(true);
        httpClientOptions.setTcpNoDelay(true);
        httpClientOptions.setTcpQuickAck(true);

        httpClientOptions.setReuseAddress(true);
        httpClientOptions.setReusePort(true);

        return httpClientOptions;
    }
}
