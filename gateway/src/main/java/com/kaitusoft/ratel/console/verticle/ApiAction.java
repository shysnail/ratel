package com.kaitusoft.ratel.console.verticle;

import com.kaitusoft.ratel.Result;
import com.kaitusoft.ratel.cluster.ClusterVerticle;
import com.kaitusoft.ratel.console.model.ExecuteResult;
import com.kaitusoft.ratel.core.common.Configuration;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.model.App;
import com.kaitusoft.ratel.core.model.option.*;
import com.kaitusoft.ratel.core.model.po.ApiOption;
import com.kaitusoft.ratel.core.model.po.AppOption;
import com.kaitusoft.ratel.core.model.vo.Node;
import com.kaitusoft.ratel.core.verticle.DeployVerticle;
import com.kaitusoft.ratel.util.StringUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/6
 *          <p>
 *          write description here
 */
public class ApiAction extends BaseAction {
    private static Logger logger = LoggerFactory.getLogger(ApiAction.class);



    protected void find(RoutingContext context) {
        String appId = context.request().getParam("appId");
        logger.debug("获取APP:{} 所有API", appId);
        HttpServerResponse response = context.response();
        Set<String> runningApis = DeployVerticle.RUNNING_APIS.get(appId);
        JsonObject result = new JsonObject();

        Future<Void> futureApis = Future.future(apis -> {
            context.vertx().eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_API), appId, reply -> {
                if (!reply.succeeded()) {
                    logger.error("获取APP:{} 所有API -> failed!", appId, reply.cause());
                    apis.complete();
                    return;
                }


                JsonArray array = reply.result().body();
                array.forEach(obj -> {
                    JsonObject apiJson = (JsonObject) obj;
                    apiJson.remove("parameter");

//                    apiJson.remove("running");
//                    String id = apiJson.getInteger("id").toString();
//                    if (runningApis != null) {
//                        if (runningApis.contains(id))
//                            apiJson.put("running", 1);
//                    }

                });
                result.put("apis", array);
                apis.complete();
            });
        });

        Future<Void> futureApp = Future.future(app -> {
            context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), appId, reply -> {
                if(reply.succeeded()){
                    JsonObject appJson = reply.result().body();
                    JsonObject appJsonVo = new JsonObject();
                    appJsonVo.put("id", appJson.getValue("id"));
                    appJsonVo.put("name", appJson.getValue("name"));
                    appJsonVo.put("port", appJson.getValue("port"));
                    result.put("app", appJsonVo);
                }
                app.complete();
            });
        });

        CompositeFuture.all(futureApis, futureApp).setHandler(res -> {
            if(res.succeeded()){
                response
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(result));
            }else{
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, res.cause().getMessage())));
            }
        });




    }


    protected void get(RoutingContext context) {
        String id = context.request().getParam("id");
        HttpServerResponse response = context.response();
        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_API), id, reply -> {
//            response(response, reply);
            Result result = null;
            if (reply.succeeded()) {
                JsonObject object = reply.result().body();
                Set<String> runningApis = DeployVerticle.RUNNING_APIS.get(object.getInteger("appId").toString());
                if (runningApis != null) {
                    if (runningApis.contains(id))
                        object.put("running", true);
                }
                result = new Result(HttpResponseStatus.OK.code(), object);
                logger.debug("请求 -> ok");
            } else {
                result = new Result(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), reply.cause().toString());
                logger.error("请求 -> failed:", result.getContent());
            }
            response.setStatusCode(result.getCode()).putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(result.toString());
        });
    }


    protected void add(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        String appId = context.request().getParam("appId");
        logger.debug("app:{} add api:{}", appId, body);

        HttpServerResponse response = context.response();

        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), appId, reply -> {
            if (reply.succeeded()) {

                wrapAppApiOption(reply.result().body(), body);

                ApiOption api = body.mapTo(ApiOption.class);

                context.vertx().eventBus().<ApiOption>send(Event.formatInternalAddress(Event.ADD_API), api, new DeliveryOptions().setCodecName(Configuration.MODEL_CODEC), apiReply -> {
                    if (apiReply.succeeded()) {
                        logger.debug("APP:{} 添加API -> ok", appId);
                        response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("ok")));
                        ;
                    } else {
                        String error = apiReply.cause().getMessage();
                        logger.error("APP:{} 添加API -> failed", appId, apiReply.cause());
                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
                    }
                });

            } else {
                String error = reply.cause().getMessage();
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
                logger.error("APP:{} 添加API -> failed", appId, error);
            }
        });
    }


    protected void delete(RoutingContext context) {
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("appId");
        String id = context.request().getParam("id");
        logger.debug("app:{} delete api:{}", appId, id);

        HttpServerResponse response = context.response();

        String[] ids = id.split(",");
        List<String> successIds = new ArrayList<>();
        List<Future> futures = new ArrayList<>();
        for(String apiId : ids){
            futures.add(Future.future(delete -> {
                //获取api，看其状态
                vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP_API), apiId, api -> {
                    if (api.succeeded()) {
                        JsonObject apiJson = api.result().body();
                        int originStat = apiJson.getInteger("running");

                        Future future = Future.future();
                        future.setHandler(pre -> {
                            JsonObject result = (JsonObject) future.result();
                            if(result.getBoolean("success", false)) {
                                context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.DELETE_API), id, reply -> {
                                    if (reply.succeeded()) {
                                        logger.debug("APP:{} deleteAPI:{} -> ok", appId, id);
                                        successIds.add(apiId);
//                                        response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("ok")));
                                    } else {
                                        String error = reply.cause().getMessage();
                                        logger.error("APP:{} deleteAPI:{} -> failed", appId, id, error);
//                                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
                                    }
                                    delete.complete();
                                });
                            }else{
                                String reason = result.getString("reason");
//                                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, reason)));
                                logger.debug("未能删除API", reason);
                                delete.complete();
                            }
                        });

                        if(originStat != App.STOPPED){
                            JsonObject result = new JsonObject();
                            action(vertx, appId, id, App.STOPPED, new int[]{App.STOPPED}, Event.STOP_API, res -> {
                                if(res.succeeded()){
                                    JsonObject obj = (JsonObject) res.result();
                                    if(obj == null){
                                        obj = new JsonObject();
                                    }

                                    if(obj.getBoolean("success", false)){
                                        result.put("success", true);

                                    }else{
                                        result.put("reason", obj.getString("reason"));
//                                future.complete(false);
                                    }
                                }else{
                                    result.put("reason", res.result());
//                            future.complete(false);
                                }
                                future.complete(result);
                            });
                        }

                    }else{
                        logger.error("删除API:{}-{} -> failed， 获取应用信息出错", appId, id, api.cause());
                        delete.complete();
                    }

                });
            }));
        }

        CompositeFuture.all(futures).setHandler(res -> {
            if(res.succeeded()){
                if(successIds.size() == ids.length){
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("指定api已删除")));
                }else{
                    String failedIds = "";
                    for(String apiId : ids){
                        if(!successIds.contains(apiId)){
                            failedIds += appId + ",";
                        }
                    }
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("指定api已删除，遗留 " + failedIds + " 无法删除")));
                }
            }else{
                logger.error("删除API:{}-{} -> failed", appId, id, res.cause());
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, res.cause().getMessage())));
            }
        });

    }

    protected void update(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        String appId = context.request().getParam("appId");
        String id = context.request().getParam("id");
        logger.debug("app:{} update api:{}", appId, id);
        body.put("appId", Integer.valueOf(appId));
        body.put("id", Integer.valueOf(id));
        HttpServerResponse response = context.response();

        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), appId, reply -> {
            if (reply.succeeded()) {

                wrapAppApiOption(reply.result().body(), body);

                ApiOption api = body.mapTo(ApiOption.class);

                context.vertx().eventBus().<ApiOption>send(id != null ? Event.formatInternalAddress(Event.UPDATE_API) : Event.formatInternalAddress(Event.ADD_API), api, new DeliveryOptions().setCodecName(Configuration.MODEL_CODEC), apiReply -> {
                    if (apiReply.succeeded()) {
                        logger.debug("APP:{} updateAPI:{} -> ok", appId, id);
                        response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("ok")));
                    } else {
                        String error = apiReply.cause().getMessage();
                        logger.error("APP:{} updateAPI:{} -> failed", appId, id, apiReply.cause());
                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
                    }
                });

            } else {
                String error = reply.cause().getMessage();
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
                logger.error("APP:{} 添加API -> failed", appId, error);
            }
        });

    }

    private void wrapAppApiOption(JsonObject appOption, JsonObject apiOption) {

        AppOption app = appOption.mapTo(AppOption.class);
        String parameter = app.getParameter();
        JsonObject extendJson = new JsonObject(parameter);
        AppExtendOption extendOption = extendJson.mapTo(AppExtendOption.class);
        PreferenceOption preferenceOption = extendOption.getPreferenceOption();

        JsonObject parameterJson = apiOption.getJsonObject("extendOption");
        apiOption.remove("extendOption");

        //proxy
        JsonObject upstreamOptionJson = parameterJson.getJsonObject("upstreamOption");
        if (upstreamOptionJson != null) {
            UpstreamOption appUpstreamOption = extendOption.getUpstreamOption();

            if ("APP".equalsIgnoreCase(upstreamOptionJson.getString("upstreamOptionType"))) {
                String threadType = upstreamOptionJson.getString("threadType");
                if (appUpstreamOption != null) {
                    upstreamOptionJson.put("timeout", appUpstreamOption.getTimeout());
                    upstreamOptionJson.put("retry", appUpstreamOption.getRetry());
                    upstreamOptionJson.put("maxContentLength", appUpstreamOption.getMaxContentLength());
                    upstreamOptionJson.put("maxInitialLineLength", appUpstreamOption.getMaxInitialLineLength());
                    upstreamOptionJson.put("maxHeaderSize", appUpstreamOption.getMaxHeaderSize());
                    upstreamOptionJson.put("keepAlive", appUpstreamOption.isKeepAlive());
                    upstreamOptionJson.put("keepAliveTimeout", appUpstreamOption.getKeepAliveTimeout());
                    upstreamOptionJson.put("passQueryString", appUpstreamOption.isPassQueryString());
                    upstreamOptionJson.put("passBody", appUpstreamOption.getPassBody().toJsonObject());

                    upstreamOptionJson.put("threadType", threadType); //自定义线程池类型
                    if (!threadType.equalsIgnoreCase(UpstreamOption.UpstreamThreadType.API.toString())) {
                        upstreamOptionJson.put("maxPoolSize", appUpstreamOption.getMaxPoolSize());
                    }
                } else {
                    upstreamOptionJson = null;
                }

            } else if ("NONE".equalsIgnoreCase(upstreamOptionJson.getString("upstreamOptionType"))) {
                upstreamOptionJson = null;
            }

            if ("APP".equalsIgnoreCase(upstreamOptionJson.getString("headerType"))) {
                upstreamOptionJson.put("appendHeaders", appUpstreamOption.getAppendHeaders());
                JsonArray rhs = new JsonArray();
                for (int i = 0; appUpstreamOption.getRemoveHeaders() != null && i < appUpstreamOption.getRemoveHeaders().length; i++) {
                    rhs.add(appUpstreamOption.getRemoveHeaders()[i]);
                }
                upstreamOptionJson.put("removeHeaders", rhs);
            }
            if ("NONE".equalsIgnoreCase(upstreamOptionJson.getString("headerType"))) {
                upstreamOptionJson.remove("appendHeaders");
                upstreamOptionJson.remove("removeHeaders");
            }
            upstreamOptionJson.remove("headerType");

            upstreamOptionJson.remove("upstreamOptionType");
            parameterJson.put("upstreamOption", upstreamOptionJson);
        }


        JsonObject preferenceJson = parameterJson.getJsonObject("preferenceOption");

        //静态服务
        String docRoot = preferenceJson.getString("root");
        if ("APP".equalsIgnoreCase(preferenceJson.getString("staticServerType"))) {
            if (preferenceOption != null) {
                docRoot = preferenceOption.getRoot();
            }
        } else if ("NONE".equalsIgnoreCase(preferenceJson.getString("staticServerType"))) {
            docRoot = null;
        }
        preferenceJson.remove("staticServerType");
        preferenceJson.put("root", docRoot);

        //流控
        JsonObject accessLimitJson = preferenceJson.getJsonObject("accessLimitOption");
        if ("APP".equalsIgnoreCase(preferenceJson.getString("accessLimitType"))) {
            AccessLimitOption appAL = extendOption.getPreferenceOption().getAccessLimitOption();
            if (appAL != null) {
                accessLimitJson = JsonObject.mapFrom(appAL);
            } else {
                accessLimitJson = null;
            }
        } else if ("NONE".equalsIgnoreCase(preferenceJson.getString("accessLimitType"))) {
            accessLimitJson = null;
        }
        preferenceJson.remove("accessLimitType");
        preferenceJson.put("accessLimitOption", accessLimitJson);

        JsonArray ipBlackListJson = preferenceJson.getJsonArray("ipBlacklist");
        String blacklistType = (String) preferenceJson.remove("ipBlacklistType");
        if ("APP".equalsIgnoreCase(blacklistType)) {
            String[] ipBlacklist = preferenceOption.getIpBlacklist();
            if (ipBlacklist != null) {
                ipBlackListJson = new JsonArray(Arrays.asList(ipBlacklist));
            } else {
                ipBlackListJson = null;
            }
        } else if ("NONE".equalsIgnoreCase(blacklistType)) {
            ipBlackListJson = null;
        }
        if (ipBlackListJson != null) {
            List<String> invalidIps = new ArrayList<>();
            for (int i = 0; i < ipBlackListJson.size(); i++) {
                String ip = ipBlackListJson.getString(i);
                if (!StringUtils.isIp(ip)) {
                    invalidIps.add(ip);
                }
            }
            for (String iip : invalidIps) {
                if (!StringUtils.isEmpty(iip))
                    ipBlackListJson.remove(iip);
            }
        }
        preferenceJson.remove("ipBlacklistType");
        preferenceJson.put("ipBlacklist", ipBlackListJson);


        JsonObject authOptionJson = preferenceJson.getJsonObject("authOption");
        if ("APP".equalsIgnoreCase(preferenceJson.getString("authType"))) {
            AuthOption appAuthOption = preferenceOption.getAuthOption();
            if (appAuthOption != null) {
                authOptionJson = JsonObject.mapFrom(appAuthOption);
            } else {
                authOptionJson = null;
            }
        } else if ("NONE".equalsIgnoreCase(preferenceJson.getString("authType"))) {
            authOptionJson = null;
        }
        preferenceJson.remove("authType");
        preferenceJson.put("authOption", authOptionJson);

        JsonArray preProcessorsJson = preferenceJson.getJsonArray("preProcessors");
        if ("APP".equalsIgnoreCase(preferenceJson.getString("preHandlerType"))) {
            List<EdgeProcessorOption> preProcessors = preferenceOption.getPreProcessors();
            if (preProcessors != null) {
                preProcessorsJson = new JsonArray(preProcessors);
            } else {
                preProcessorsJson = null;
            }
        } else if ("NONE".equalsIgnoreCase(preferenceJson.getString("preHandlerType"))) {
            preProcessorsJson = null;
        }
        preferenceJson.remove("preHandlerType");
        preferenceJson.put("preProcessors", preProcessorsJson);

        JsonArray postProcessorsJson = preferenceJson.getJsonArray("postProcessors");
        if ("APP".equalsIgnoreCase(preferenceJson.getString("postHandlerType"))) {
            List<EdgeProcessorOption> postProcessors = preferenceOption.getPostProcessors();
            if (postProcessors != null) {
                postProcessorsJson = new JsonArray(postProcessors);
            } else {
                postProcessorsJson = null;
            }
        } else if ("NONE".equalsIgnoreCase(preferenceJson.getString("postHandlerType"))) {
            postProcessorsJson = null;
        }
        preferenceJson.remove("postHandlerType");
        preferenceJson.put("postProcessors", postProcessorsJson);

        parameterJson.put("preferenceOption", preferenceJson);

        apiOption.put("parameter", parameterJson.toString());

    }

    protected synchronized void start(RoutingContext context) {
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("appId");
        String id = context.request().getParam("id");
        HttpServerResponse response = context.response();

        String[] ids = id.split(",");

        JsonObject result = new JsonObject();
        List<Future> futures = new ArrayList<>();
        for(String apiId : ids){
            futures.add(Future.future(future -> {
                action(vertx, appId, apiId, App.RUNNING, new int[]{App.RUNNING}, Event.START_API, res -> {
                    JsonObject success = new JsonObject();
                    if (res.succeeded()) {
                        JsonObject obj = (JsonObject) res.result();
                        if (obj == null) {
                            obj = new JsonObject();
                        }

                        if (obj.getBoolean("success", false)) {
                            success.put("success", true);
                            if (obj.getBoolean("fullSuccess", false)) {
                                success.put("fullSuccess", true);
                            }
                        } else {
                            success.put("reason", obj.getString("reason"));
                        }

                        result.put(appId, success);
                    } else {
                        result.put(appId, res.result());
                    }

                    future.complete();
                });
            }));

        }

        CompositeFuture.all(futures).setHandler(res -> {
            if(res.succeeded()){
                boolean allSuccess = true;
                String reason = "";
                for(String apiId : ids){
                    JsonObject success = result.getJsonObject(appId);
                    if(!success.getBoolean("success", false)){
                        allSuccess = false;
                        reason += apiId + ":" + success.getString("reason");
                    }
                }

                if(allSuccess){
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("指定api已启动")));
                }else{
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("指定api 未全部启动，遗留： " + reason)));
                }

            }else{
                logger.error("删除API:{}-{} -> failed", appId, id, res.cause());
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, res.cause().getMessage())));
            }
        });

    }

    protected synchronized void stop(RoutingContext context) {
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("appId");
        String id = context.request().getParam("id");
        HttpServerResponse response = context.response();

        String[] ids = id.split(",");

        JsonObject result = new JsonObject();
        List<Future> futures = new ArrayList<>();
        for(String apiId : ids){
            futures.add(Future.future(future -> {
                action(vertx, appId, apiId, App.STOPPED, new int[]{App.STOPPED}, Event.STOP_API, res -> {
                    JsonObject success = new JsonObject();
                    if (res.succeeded()) {
                        JsonObject obj = (JsonObject) res.result();
                        if (obj == null) {
                            obj = new JsonObject();
                        }

                        if (obj.getBoolean("success", false)) {
                            success.put("success", true);
                            if (obj.getBoolean("fullSuccess", false)) {
                                success.put("fullSuccess", true);
                            }
                        } else {
                            success.put("reason", obj.getString("reason"));
                        }

                        result.put(appId, success);
                    } else {
                        result.put(appId, res.result());
                    }

                    future.complete();
                });
            }));

        }

        CompositeFuture.all(futures).setHandler(res -> {
            if(res.succeeded()){
                boolean allSuccess = true;
                String reason = "";
                for(String apiId : ids){
                    JsonObject success = result.getJsonObject(appId);
                    if(!success.getBoolean("success", false)){
                        allSuccess = false;
                        reason += apiId + ":" + success.getString("reason");
                    }
                }

                if(allSuccess){
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("指定api已停止")));
                }else{
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("指定api 未全部停止，遗留： " + reason)));
                }

            }else{
                logger.error("删除API:{}-{} -> failed", appId, id, res.cause());
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, res.cause().getMessage())));
            }
        });

    }

    /**
     * 暂停
     * @param context
     */
    protected synchronized void pause(RoutingContext context){
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("appId");
        String id = context.request().getParam("id");
        HttpServerResponse response = context.response();

        String[] ids = id.split(",");

        JsonObject result = new JsonObject();
        List<Future> futures = new ArrayList<>();
        for(String apiId : ids){
            futures.add(Future.future(future -> {
                action(vertx, appId, id, App.PAUSED, new int[]{App.STOPPED, App.PAUSED}, Event.PAUSE_API, res -> {
                    JsonObject success = new JsonObject();
                    if (res.succeeded()) {
                        JsonObject obj = (JsonObject) res.result();
                        if (obj == null) {
                            obj = new JsonObject();
                        }

                        if (obj.getBoolean("success", false)) {
                            success.put("success", true);
                            if (obj.getBoolean("fullSuccess", false)) {
                                success.put("fullSuccess", true);
                            }
                        } else {
                            success.put("reason", obj.getString("reason"));
                        }

                        result.put(appId, success);
                    } else {
                        result.put(appId, res.result());
                    }

                    future.complete();
                });
            }));

        }

        CompositeFuture.all(futures).setHandler(res -> {
            if(res.succeeded()){
                boolean allSuccess = true;
                String reason = "";
                for(String apiId : ids){
                    JsonObject success = result.getJsonObject(appId);
                    if(!success.getBoolean("success", false)){
                        allSuccess = false;
                        reason += apiId + ":" + success.getString("reason");
                    }
                }

                if(allSuccess){
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("指定api已暂停")));
                }else{
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("指定api 未全部暂停，遗留： " + reason)));
                }

            }else{
                logger.error("暂停API:{}-{} -> failed", appId, id, res.cause());
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, res.cause().getMessage())));
            }
        });

    }

    /**
     * 暂停
     * @param context
     */
    protected synchronized void resume(RoutingContext context){
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("appId");
        String id = context.request().getParam("id");
        HttpServerResponse response = context.response();

        action(vertx, appId, id, App.RUNNING, new int[]{App.STOPPED, App.RUNNING}, Event.RESUME_API, res -> {
            if(res.succeeded()){
                JsonObject obj = (JsonObject) res.result();
                if(obj == null){
                    obj = new JsonObject();
                }

                ExecuteResult result = new ExecuteResult();
                if(obj.getBoolean("success", false)){
                    if(obj.getBoolean("fullSuccess", false)){
                        result.setData("所有节点均 恢复 完毕");
                    }else{
                        result.setData("部分节点 恢复 完毕");
                    }
                }else{
                    result.setData(obj.getString("reason"));
                }

                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(result));
            }else{
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, res.result())));
            }
        });
    }

    /**
     * 暂停
     * @param context
     */
    protected synchronized void restart(RoutingContext context){
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("appId");
        String id = context.request().getParam("id");
        HttpServerResponse response = context.response();

        action(vertx, appId, id, App.RUNNING, new int[]{App.STOPPED}, Event.RESTART_API, res -> {
            if(res.succeeded()){
                JsonObject obj = (JsonObject) res.result();
                if(obj == null){
                    obj = new JsonObject();
                }

                ExecuteResult result = new ExecuteResult();
                if(obj.getBoolean("success", false)){
                    if(obj.getBoolean("fullSuccess", false)){
                        result.setData("所有节点均 重启 完毕");
                    }else{
                        result.setData("部分节点 重启 完毕");
                    }
                }else{
                    result.setData(obj.getString("reason"));
                }

                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(result));
            }else{
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, res.result())));
            }
        });

    }

    private void action(Vertx vertx, String appId, String id, int targetStatus, int[] stopStatus, String sendAddress, Handler<AsyncResult> handler){
        JsonObject resultData = new JsonObject();
        //获取api，看其状态
        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP_API), id, api -> {
            if (api.succeeded()) {
                JsonObject apiJson = api.result().body();
                int originStat = apiJson.getInteger("running");
                boolean stop = false;
                for(int i = 0; stopStatus != null && i < stopStatus.length; i ++){
                    if(originStat == stopStatus[i]){
                        stop = true;
                        break;
                    }
                }
                if (stop) {
                    resultData.put("success", false);
                    resultData.put("reason", "当前状态无法进行此操作");
                    handler.handle(Future.succeededFuture(resultData));
                    return;
                }

                JsonObject updates = new JsonObject();
                updates.put("appId", appId);
                updates.put("apiId", id);
                JsonObject prop = new JsonObject();
                prop.put("running", targetStatus);
                updates.put("prop", prop);

                vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.UPDATE_API_PROP), updates, apiUpdate -> {
                    if (apiUpdate.succeeded()) {
                        //修改状态后，通知集群启动api
                        //首先拿到现在集群api所属组的所有节点
                        //然后统计所有的节点的响应
                        int deployGroup = apiJson.getInteger("deployGroup");
                        JsonObject body = new JsonObject();
                        body.put("appId", appId);
                        body.put("apiId", id);
                        body.put("commander", ClusterVerticle.myNodeId);
                        body.put("groupId", deployGroup);

                        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.CLUSTER_GET_NODES), String.valueOf(deployGroup), nodes -> {
                            Collection toNodes = new HashSet();
                            if (nodes.succeeded()) {
                                JsonArray allNodes = nodes.result().body();
                                allNodes.forEach(node -> {
                                    if(!((JsonObject) node).getBoolean("online", false))
                                        return;
                                    toNodes.add(((JsonObject) node).getString("nodeId"));
                                });
                            }
                            ClusterVerticle.clusterMessage.<JsonObject>send(toNodes, Event.formatAddress(sendAddress), body, clusterReply -> {
                                if (clusterReply.succeeded()) {
                                    logger.debug("节点暂停，api暂停");
                                    JsonObject reply = clusterReply.result();

                                    resultData.put("success", true);
                                    if (reply.getBoolean("fullSuccess", false)) {
                                        resultData.put("fullSuccess", true);
                                    }

                                    handler.handle(Future.succeededFuture(resultData));
                                } else {
                                    prop.put("running", originStat);
                                    vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.UPDATE_API_PROP), updates, rollback -> {
                                        logger.debug("api action->{}, 所有节点均失败", targetStatus);

                                        handler.handle(Future.failedFuture(clusterReply.cause().getMessage()));
                                    });
                                }
                            });
                        });

                    } else {
                        logger.info("api action->{}, app:{} - api:{} -> failed!", targetStatus, appId, id, apiUpdate.cause());

                        handler.handle(Future.failedFuture(apiUpdate.cause().getMessage()));
                    }
                });

            }else{
                logger.info("api action->{}, app:{} - api:{} -> failed!", targetStatus, appId, id, api.cause());

                handler.handle(Future.failedFuture(api.cause().getMessage()));

            }

        });
    }


    /**
     * 获取每个节点app运行状态
     * 存储在json数组中，每个元素数据格式为 appId:[badnodes,...]
     * @param context
     */
    protected void allNodeStatus(RoutingContext context){
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("appId");
        String apiIds = context.request().getParam("apiIds");
        String[] apiIdArray = apiIds.split(",");
        Map<String, Set<Node>> apiUnhealthyNodes = new HashMap<>();

        for(String apiId : apiIdArray){
            apiUnhealthyNodes.put(apiId, new HashSet());
        }

        HttpServerResponse response = context.response();
//        JsonObject params = new JsonObject();
//        params.put("ids", apiIds);
        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), appId, app -> {
            if (app.succeeded()) {
                JsonObject appJson = app.result().body();

                int deployGroup = appJson.getInteger("deployGroup");

                vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.CLUSTER_GET_NODES), String.valueOf(deployGroup), nodes -> {
                    Collection toNodes = new HashSet();
                    final Set<Node> allGroupNodes = new HashSet();
                    if (nodes.succeeded()) {
                        JsonArray allNodes = nodes.result().body();
                        allNodes.forEach(node -> {
                            if(!((JsonObject) node).getBoolean("online", false))
                                return;
                            toNodes.add(((JsonObject) node).getString("nodeId"));
                            ((JsonObject) node).remove("addTime");
                            allGroupNodes.add(((JsonObject) node).mapTo(Node.class));
                        });
                    }
                    logger.debug("获取组:{} 包含的节点:{}", deployGroup, toNodes);

                    JsonObject body = new JsonObject();
                    body.put("commander", ClusterVerticle.myNodeId);
                    body.put("appId", appId);

                    /*
                        拿到的数据结构为
                        node -> json(sucess:true/false, result:[appId1, appid2, appid3] ...)
                        每个组所有的节点，用户统计是否有节点未返回数据
                     */
                    ClusterVerticle.clusterMessage.<JsonObject>send(toNodes, Event.formatAddress(Event.CLUSTER_NODE_API_STATUS), body, clusterReply -> {
                        JsonArray nodeResult;
                        if (clusterReply.succeeded()) {
                            nodeResult = clusterReply.result().getJsonArray("nodes");
                        } else {
                            nodeResult = new JsonArray();
                        }
                        logger.debug("获取节点api运行状态:{}", nodeResult);
                        /*
                        将每个节点对应的app数据转成每个app相对每个节点的运行状态
                         */
                        nodeResult.forEach(nodeData -> {
                            JsonObject nodeDataJson = (JsonObject) nodeData;
                            nodeDataJson.forEach(e -> {
                                String node = e.getKey();
                                JsonObject data = (JsonObject) e.getValue();
                                boolean nodeSuccess = data.getBoolean("success");
                                if(!nodeSuccess)
                                    return;

                                Node tmpNode = new Node();
                                tmpNode.setNodeId(node);
                                tmpNode.setHostname(data.getString("nodeName"));
                                tmpNode.setGroupId(String.valueOf(deployGroup));
                                allGroupNodes.remove(tmpNode);
                                JsonArray nodeApiIds = data.getJsonArray("result");
                                for(String apiId : apiIdArray){
                                    boolean apiRunInNode = false;
                                    for(int i = 0; i < nodeApiIds.size(); i ++){
                                        String nodeApiId = nodeApiIds.getString(i);
                                        if(apiId.equalsIgnoreCase(nodeApiId)){
                                            apiRunInNode = true;
                                            break;
                                        }
                                    }

                                    if(!apiRunInNode){
                                        Set<Node> inNodes = apiUnhealthyNodes.get(apiId);
                                        inNodes.add(tmpNode);
                                    }
                                }
                            });
                        });

                    /*
                     *有节点未返回数据，表示无法获取该节点api运行状态
                     * 需要标记这些api对应节点全部不健康
                     */
                        if(allGroupNodes.size() > 0){
                            apiUnhealthyNodes.forEach((k, v) -> {
                                v.addAll(allGroupNodes);
                            });
                        }

                        ExecuteResult result = new ExecuteResult();
                        JsonObject obj = new JsonObject();
                        apiUnhealthyNodes.forEach((k, v)->{
                            if(v != null && v.size() > 0){
                                obj.put(k, new JsonArray(new ArrayList(v)));
                            }
                        });
                        result.setData(obj);
                        response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                                .end(Json.encode(result));

                    });
                });

            }else{
                logger.error("获取所有节点APP状态 -> failed:", app.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, app.cause().getMessage())));
            }
        });
    }

}
