package com.kaitusoft.ratel.console.verticle;

import com.kaitusoft.ratel.cluster.ClusterVerticle;
import com.kaitusoft.ratel.core.common.Env;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.util.StringUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/6
 *          <p>
 *          write description here
 */
public class SystemAction extends BaseAction {

    private static Logger logger = LoggerFactory.getLogger(SystemAction.class);


    protected void status(RoutingContext context) {
        logger.debug("查看运行状态");
        String node = context.request().getParam("node");
        if (StringUtils.isEmpty(node))
            node = ClusterVerticle.myNodeId;
        String size = context.request().getParam("size");
        if (!StringUtils.isNumric(size))
            size = "20";
        String app = context.request().getParam("app");

        JsonObject param = new JsonObject().put("node", node).put("size", Integer.parseInt(size)).put("app", app).put("timestamp", context.request().getParam("timestamp"));

        HttpServerResponse response = context.response();
        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.SYSTEM_INFO_GET), param, reply -> {
            if (reply.succeeded()) {
                logger.debug("获取运行状态 -> ok");
                response.setStatusCode(HttpResponseStatus.OK.code()).putHeader("Content-Type", "application/json").end(Json.encode(reply.result().body()));
            } else {
                String error = reply.cause().toString();
                logger.error("获取运行状态 -> failed:", reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
            }
        });
    }

    /**
     * 获取当前的配置环境信息
     *
     * @param context
     */
    protected void env(RoutingContext context) {
        logger.debug("获取环境信息");
        HttpServerResponse response = context.response();
        JsonObject env = new JsonObject();
        if (Env.groups.size() > 0) {
            JsonArray groups = new JsonArray();
            Env.groups.forEach(va -> {
                groups.add(JsonObject.mapFrom(va));
            });
            env.put("groups", groups);
        }

        if (Env.auths.size() > 0) {
            JsonArray auths = new JsonArray();
            Env.auths.forEach(va -> {
                auths.add(JsonObject.mapFrom(va));
            });
            env.put("auths", auths);
        }

        if (Env.preHandlers.size() > 0) {
            JsonArray eh = new JsonArray();
            Env.preHandlers.forEach(va -> {
                eh.add(JsonObject.mapFrom(va));
            });
            env.put("preHandlers", eh);
        }

        if (Env.postHandlers.size() > 0) {
            JsonArray eh = new JsonArray();
            Env.postHandlers.forEach(va -> {
                eh.add(JsonObject.mapFrom(va));
            });
            env.put("postHandlers", eh);
        }

        response.setStatusCode(HttpResponseStatus.OK.code()).end(env.toString());
    }
}
