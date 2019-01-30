package com.kaitusoft.ratel.cluster;

import com.kaitusoft.ratel.core.common.Event;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/17
 *          <p>
 *          write description here
 */
public class AppApiAction extends BaseClusterAction{

    public static final Logger logger = LoggerFactory.getLogger(AppApiAction.class);

    public AppApiAction(Vertx vertx) {
        super(vertx);
    }


    public void startApp(Message<JsonObject> message) {
        JsonObject data = message.body();
        String appId = data.getString("appId");
        String from = data.getString("commander");

        logger.debug("收到集群启动app:{}, from:{}, 命令: {}", appId, from, data);
        JsonObject replyMessage = buildCommonReply(data);
        if (!replyMessage.getBoolean("deal")) {
//            message.reply(replyMessage);
            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
            return;
        }

        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.START_APP), appId, reply -> {
            replyMessage.put("success", reply.succeeded());
            if (reply.succeeded()) {
                replyMessage.put("result", reply.result().body());
                logger.debug("集群启动app:{} -> ok", appId);
            } else {
                String error = reply.cause().getMessage();
                replyMessage.put("result", error);
                logger.error("集群启动app:{} -> failed", appId, reply.cause());
            }

            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);

        });

    }


    public void stopApp(Message<JsonObject> message) {
        JsonObject data = message.body();
        String appId = data.getString("appId");
        String from = data.getString("commander");

        logger.debug("收到集群停止app:{}, from:{}, 命令: {}", appId, from, data);

        JsonObject replyMessage = buildCommonReply(data);
        if (!replyMessage.getBoolean("deal")) {
//            message.reply(replyMessage);
            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
            return;
        }

        vertx.eventBus().<Object>send(Event.formatInternalAddress(Event.STOP_APP), appId, reply -> {
            replyMessage.put("success", reply.succeeded());
            if (reply.succeeded()) {
                replyMessage.put("result", reply.result().body());
                logger.debug("集群停止app:{} -> ok", appId);
            } else {
                String error = reply.cause().getMessage();
                replyMessage.put("result", error);
                logger.error("集群停止app:{} -> failed", appId, reply.cause());
            }

            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);

        });

    }

    public void restartApp(Message<JsonObject> message) {
        JsonObject data = message.body();
        String appId = data.getString("appId");
        String from = data.getString("commander");

        logger.debug("收到集群重启app:{}, from:{}, 命令: {}", appId, from, data);

        JsonObject replyMessage = buildCommonReply(data);
        if (!replyMessage.getBoolean("deal")) {
//            message.reply(replyMessage);
            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
            return;
        }

        vertx.eventBus().<Object>send(Event.formatInternalAddress(Event.RESTART_APP), appId, reply -> {
            replyMessage.put("success", reply.succeeded());
            if (reply.succeeded()) {
                replyMessage.put("result", reply.result().body());
                logger.debug("集群重启app:{} -> ok", appId);
            } else {
                String error = reply.cause().getMessage();
                replyMessage.put("result", error);
                logger.error("集群重启app:{} -> failed", appId, reply.cause());
            }

            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);

        });

    }

    public void startApi(Message<JsonObject> message) {
        JsonObject data = message.body();
        String appId = data.getString("appId");
        String apiId = data.getString("apiId");
        String from = data.getString("commander");
        logger.debug("收到集群启动app:{}-api:{}, from:{}, 命令: {}", appId, apiId, from, data);

        JsonObject replyMessage = buildCommonReply(data);
        if (!replyMessage.getBoolean("deal")) {
//            message.reply(replyMessage);
            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
            return;
        }

        JsonObject param = new JsonObject();
        param.put("appId", appId);
        param.put("id", apiId);
        vertx.eventBus().send(Event.formatInternalAddress(Event.START_API), param, reply -> {
            replyMessage.put("success", reply.succeeded());
            if (reply.succeeded()) {
                replyMessage.put("result", reply.result().body());
                logger.info("集群启动接口 app:{} - api:{} -> ok", appId, apiId);
            } else {
                String error = reply.cause().getMessage();
                replyMessage.put("result", error);
                logger.info("集群启动接口 app:{} - api:{} -> failed!", appId, apiId, reply.cause());
            }

            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);

        });

    }

    public void stopApi(Message<JsonObject> message) {
        JsonObject data = message.body();
        String appId = data.getString("appId");
        String apiId = data.getString("apiId");
        String from = data.getString("commander");
        logger.debug("收到集群停止app:{}-api:{}, from:{}, 命令: {}", appId, apiId, from, data);

        JsonObject replyMessage = buildCommonReply(data);
        if (!replyMessage.getBoolean("deal")) {
//            message.reply(replyMessage);
            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
            return;
        }

        JsonObject param = new JsonObject();
        param.put("appId", appId);
        param.put("id", apiId);
        vertx.eventBus().send(Event.formatInternalAddress(Event.STOP_API), param, reply -> {
            replyMessage.put("success", reply.succeeded());
            if (reply.succeeded()) {
                replyMessage.put("result", reply.result().body());
                logger.info("集群停止接口 app:{} - api:{} -> ok", appId, apiId);
            } else {
                String error = reply.cause().getMessage();
                replyMessage.put("result", error);
                logger.info("集群停止接口 app:{} - api:{} -> failed!", appId, apiId, reply.cause());
            }

            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);

        });
    }

    public void pauseApi(Message<JsonObject> message) {
        JsonObject data = message.body();
        String appId = data.getString("appId");
        String apiId = data.getString("apiId");
        String from = data.getString("commander");
        logger.debug("收到集群暂停 app:{}-api:{}, from:{}, 命令: {}", appId, apiId, from, data);

        JsonObject replyMessage = buildCommonReply(data);
        if (!replyMessage.getBoolean("deal")) {
//            message.reply(replyMessage);
            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
            return;
        }

        JsonObject param = new JsonObject();
        param.put("appId", appId);
        param.put("id", apiId);
        vertx.eventBus().send(Event.formatInternalAddress(Event.PAUSE_API), param, reply -> {
            replyMessage.put("success", reply.succeeded());
            if (reply.succeeded()) {
                replyMessage.put("result", reply.result().body());
                logger.info("集群暂停 接口 app:{} - api:{} -> ok", appId, apiId);
            } else {
                String error = reply.cause().getMessage();
                replyMessage.put("result", error);
                logger.info("集群暂停 接口 app:{} - api:{} -> failed!", appId, apiId, reply.cause());
            }

            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);

        });

    }

    public void resumeApi(Message<JsonObject> message) {
        JsonObject data = message.body();
        String appId = data.getString("appId");
        String apiId = data.getString("apiId");
        String from = data.getString("commander");
        logger.debug("收到集群 恢复运行 app:{}-api:{}, from:{}, 命令: {}", appId, apiId, from, data);

        JsonObject replyMessage = buildCommonReply(data);
        if (!replyMessage.getBoolean("deal")) {
//            message.reply(replyMessage);
            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
            return;
        }

        JsonObject param = new JsonObject();
        param.put("appId", appId);
        param.put("id", apiId);
        vertx.eventBus().send(Event.formatInternalAddress(Event.RESUME_API), param, reply -> {
            replyMessage.put("success", reply.succeeded());
            if (reply.succeeded()) {
                replyMessage.put("result", reply.result().body());
                logger.info("集群 恢复运行 接口 app:{} - api:{} -> ok", appId, apiId);
            } else {
                String error = reply.cause().getMessage();
                replyMessage.put("result", error);
                logger.info("集群 恢复运行 接口 app:{} - api:{} -> failed!", appId, apiId, reply.cause());
            }

            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);

        });
    }

    public void restartApi(Message<JsonObject> message) {
        JsonObject data = message.body();
        String appId = data.getString("appId");
        String apiId = data.getString("apiId");
        String from = data.getString("commander");
        logger.debug("收到集群重启 app:{}-api:{}, from:{}, 命令: {}", appId, apiId, from, data);

        JsonObject replyMessage = buildCommonReply(data);
        if (!replyMessage.getBoolean("deal")) {
//            message.reply(replyMessage);
            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
            return;
        }

        JsonObject param = new JsonObject();
        param.put("appId", appId);
        param.put("id", apiId);
        vertx.eventBus().send(Event.formatInternalAddress(Event.RESTART_API), param, reply -> {
            replyMessage.put("success", reply.succeeded());
            if (reply.succeeded()) {
                replyMessage.put("result", reply.result().body());
                logger.info("集群重启 接口 app:{} - api:{} -> ok", appId, apiId);
            } else {
                String error = reply.cause().getMessage();
                replyMessage.put("result", error);
                logger.info("集群重启 接口 app:{} - api:{} -> failed!", appId, apiId, reply.cause());
            }

            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);

        });

    }

    public void appStatus(Message<JsonObject> message) {
        JsonObject data = message.body();
        String from = data.getString("commander");
        logger.debug("收到集群获取app状态命令 from:{}, {}", from, data);
        JsonObject replyMessage = buildCommonReply(data);
        if (!replyMessage.getBoolean("deal")) {
//            message.reply(replyMessage);
            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
            return;
        }
        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.APP_STATUS), null, reply -> {
            replyMessage.put("success", reply.succeeded());
            if (reply.succeeded()) {
                replyMessage.put("result", reply.result().body());
                logger.info("获取本节点 app运行状态: -> ok");
            } else {
                String error = reply.cause().getMessage();
                replyMessage.put("result", error);
                logger.info("获取本节点 app运行状态: -> failed!", reply.cause());
            }

            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
        });

    }

    public void apiStatus(Message<JsonObject> message) {
        JsonObject data = message.body();
        String from = data.getString("commander");
        logger.debug("收到集群获取api状态命令from:{}, {}", from, data);

        JsonObject replyMessage = buildCommonReply(data);
        if (!replyMessage.getBoolean("deal")) {
//            message.reply(replyMessage);
            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);
            return;
        }
        String appId = data.getString("appId");
        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.API_STATUS), appId, reply -> {
            replyMessage.put("success", reply.succeeded());
            if (reply.succeeded()) {
                replyMessage.put("result", reply.result().body());
                logger.info("获取本节点app:{}  api 运行状态 -> ok", appId);
            } else {
                String error = reply.cause().getMessage();
                replyMessage.put("result", error);
                logger.info("获取本节点app:{}  api 运行状态 -> failed!", appId, reply.cause());
            }

            vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);

        });

    }

}
