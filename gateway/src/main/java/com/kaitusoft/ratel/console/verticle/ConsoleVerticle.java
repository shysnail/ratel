package com.kaitusoft.ratel.console.verticle;

import com.kaitusoft.ratel.ContextAttribute;
import com.kaitusoft.ratel.console.auth.LoginCheck;
import com.kaitusoft.ratel.console.model.ExecuteResult;
import com.kaitusoft.ratel.console.model.User;
import com.kaitusoft.ratel.core.common.Configuration;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.util.ResourceUtil;
import com.kaitusoft.ratel.util.StringUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/16
 *          <p>
 *          write description here
 */
public class ConsoleVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleVerticle.class);
    private int port;
    private boolean debug;
    private static String favicon = ResourceUtil.inJar(ConsoleVerticle.class) ? "../conf/favicon.ico" : "favicon.ico";
    private String uploadsTempDir;

    protected final String CONTENT_TYPE_HTML_UTF8 = "text/html;charset=UTF-8";

    private AppAction appAction;
    private ApiAction apiAction;
    private GroupAction groupAction;
    private UserAction userAction;
    private SystemAction systemAction;
    private NodeAction nodeAction;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        port = config().getInteger("port");
        debug = config().getBoolean("debug");
    }

    @Override
    public void start(Future future) throws Exception {
        appAction = new AppAction();
        apiAction = new ApiAction();
        groupAction = new GroupAction();
        userAction = new UserAction();
        systemAction = new SystemAction();
        nodeAction = new NodeAction();

        Router router = Router.router(vertx);
        router.route().handler(FaviconHandler.create(favicon));
        router.route().handler(BodyHandler.create());

        Handler cookieHandler = CookieHandler.create();
        Handler sessionHandler = null;
        if (vertx.isClustered()) {
            sessionHandler = SessionHandler.create(ClusteredSessionStore.create(vertx))
                    .setSessionCookieName(Configuration.SESSION_NAME);
        } else {
            sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx))
                    .setSessionCookieName(Configuration.SESSION_NAME);
        }
        router.route().handler(cookieHandler).handler(sessionHandler);

        router.route().failureHandler(failureContext -> {
            logger.error("error code:{}", failureContext.statusCode(), failureContext.failure());
            failureContext.response()
                    .setStatusCode(failureContext.statusCode())
                    .end("fail!!" + failureContext.statusCode());
        });

        if(debug)
            System.setProperty("io.vertx.ext.web.TemplateEngine.disableCache", "true");

        FreeMarkerTemplateEngine engine = FreeMarkerTemplateEngine.create(vertx);
        String tempRoot = getTemplateRoot();
        TemplateHandler tempHandler = TemplateHandler.create(engine, tempRoot, CONTENT_TYPE_HTML_UTF8);
        router.getWithRegex(".+\\.ftl").handler(tempHandler);

        router.getWithRegex(".+\\.html").handler(this::htmlRender);
        router.route().handler(context -> {
            String path = context.request().path();
            if(!StringUtils.isEmpty(path) && !"/".equalsIgnoreCase(path)){
                context.next();
                return;
            }

            wrapContext(context);
            context.reroute("/index.ftl");
        });
        router.route("/*").handler(StaticHandler.create("webapp/console").setCachingEnabled(!debug));

        Handler loginCheck = LoginCheck.create(new String[]{"/login"});

        router.post("/login").handler(userAction::login);
        router.get("/logout").handler(userAction::logout);

        router.get("/user").handler(userAction::find);
        router.post("/user").handler(userAction::add);
        router.get("/user/:account").handler(userAction::get);
        router.post("/user/changePassword").handler(userAction::changePassword);
        router.get("/user/:account/frozen").handler(userAction::frozen);
        router.get("/user/:account/unFrozen").handler(userAction::unFrozen);

        // 查看系统信息
        router.get("/status").handler(systemAction::status);
        router.get("/env").handler(systemAction::env);

        router.get("/app").handler(appAction::find);
        router.get("/app/:id").handler(appAction::get);
        router.get("/app/status/onNode").handler(appAction::allNodeStatus);

        router.post("/app").handler(loginCheck).handler(appAction::add);
        router.post("/app/:id").handler(loginCheck).handler(appAction::update);
        router.delete("/app/:id").handler(loginCheck).handler(appAction::delete);

        router.get("/app/:id/start").handler(loginCheck).handler(appAction::start);
        router.get("/app/:id/stop").handler(loginCheck).handler(appAction::stop);
        router.get("/app/:id/pause").handler(loginCheck).handler(appAction::pause);
        router.get("/app/:id/restart").handler(loginCheck).handler(appAction::restart);

        router.get("/app/:appId/api").handler(apiAction::find);
        router.get("/app/:appId/api/:id").handler(apiAction::get);
        router.get("/app/:appId/api/status/onNode").handler(apiAction::allNodeStatus);

        router.post("/app/:appId/api").handler(loginCheck).handler(apiAction::add);
        router.post("/app/:appId/api/:id").handler(loginCheck).handler(apiAction::update);
        router.delete("/app/:appId/api/:id").handler(loginCheck).handler(apiAction::delete);

        router.get("/app/:appId/api/:id/start").handler(loginCheck).handler(apiAction::start);
        router.get("/app/:appId/api/:id/stop").handler(loginCheck).handler(apiAction::stop);
        router.get("/app/:appId/api/:id/pause").handler(loginCheck).handler(apiAction::pause);
        router.get("/app/:appId/api/:id/resume").handler(loginCheck).handler(apiAction::resume);
        router.get("/app/:appId/api/:id/restart").handler(loginCheck).handler(apiAction::restart);


        if (vertx.isClustered()) {
            router.route("/cluster/*").handler(this::clusterFilter);

            router.get("/cluster/node").handler(nodeAction::nodes);
            router.get("/cluster/node/:nodeId/halt").handler(loginCheck).handler(nodeAction::halt);
            router.get("/cluster/node/:nodeId/restart").handler(loginCheck).handler(nodeAction::restart);
            router.get("/cluster/node/:nodeId/expel").handler(loginCheck).handler(nodeAction::removeNode);

            router.get("/cluster/node/:nodeId/app").handler(nodeAction::apps);
            router.get("/cluster/node/:nodeId/app/:appId/api").handler(nodeAction::apis);

            router.get("/cluster/group").handler(groupAction::find);
            router.get("/cluster/group/:id").handler(groupAction::get);

            router.post("/cluster/group").handler(loginCheck).handler(groupAction::add);
            router.post("/cluster/group/:id").handler(loginCheck).handler(groupAction::update);
            router.delete("/cluster/group/:id").handler(loginCheck).handler(groupAction::delete);
            router.post("/cluster/grouping").handler(loginCheck).handler(groupAction::grouping);


            router.get("/cluster/test").handler(this::clusterTest);

        }

        router.route().order(99999).handler(notfound -> {
            HttpServerResponse response = notfound.response();
            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end("not found");
        });

        vertx.createHttpServer().requestHandler(router::accept).listen(port, res -> {
            if (res.succeeded()) {
                future.complete();
            } else {
                future.fail(res.cause());
            }
        });

    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    private void wrapContext(RoutingContext context){
        Map<String, Object> data = new HashMap<>();
        data.put("name", Configuration.OFFICIAL_NAME);
        User user = context.session().get(ContextAttribute.SESSION_USER);
        if(user != null){
            JsonObject principal = user.principal();
            data.put("curUser", principal);
        }

        if(this.getVertx().isClustered()){
            data.put("cluster", true);
        }

        String domain = Configuration.DOMAIN;
        if(StringUtils.isEmpty(domain))
            domain = "//" + context.request().host();

        data.put("domain", domain);

        String path = context.request().path();

        int pageSplit = path.lastIndexOf('/');
//        String page = path.substring(1, path.indexOf(".")) + ".html";
        String page = path.substring(1);
        data.put("uri", page);

        // in 3.6+
        context.put("context", data);

        //in < 3.6
//        data.forEach((k, v) -> {
//            context.put(k, v);
//        });
    }

    private void htmlRender(RoutingContext context){
        wrapContext(context);

        String path = context.request().path();
        String realPage = path.substring(0, path.length() - 5) + ".ftl";
        context.reroute(realPage);
    }

    private void clusterTest(RoutingContext context) {
        logger.debug("test cluster msg:{}", vertx.isClustered());
        vertx.eventBus().publish("cluster.test", "sdfsdfsf");
        context.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(Json.encode(new ExecuteResult("ok")));
    }

    private void statusRealTime(RoutingContext context) {
        logger.debug("查看实时运行状态");
        JsonObject body = context.getBodyAsJson();
        HttpServerResponse response = context.response();
        JsonObject params = new JsonObject();
        params.put("size", context.request().getParam("size"));
        params.put("timestamp", context.request().getParam("timestamp"));
        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.STATUS_FIND), params, reply -> {
            if (reply.succeeded()) {
                logger.debug("获取实时运行状态 -> ok");
                response.setStatusCode(HttpResponseStatus.OK.code()).putHeader("Content-Type", "application/json").end(Json.encode(reply.result().body()));
            } else {
                logger.error("获取实时运行状态 -> failed:", reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader("Content-Type", "application/json").end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
            }
        });
    }

    private void clusterFilter(RoutingContext context) {
        HttpServerResponse response = context.response();

        if (!vertx.isClustered()) {
            logger.error("{}: -> failed!", context.request().absoluteURI());
            response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end("非集群运行");
            return;
        }

        context.next();
    }


    protected String getTemplateRoot() {
//        if (ResourceUtil.inJar(ConsoleVerticle.class)) {
//            FileResolver resolver = new FileResolver(vertx);
//            File file = resolver.resolveFile(new File(ResourceUtil.getPath("webapp/console")).getPath());
//            return "/" + file.getPath();
//        }

        return "webapp/console";
    }
}
