package com.kaitusoft.ratel.console.verticle;

import com.kaitusoft.ratel.cluster.ClusterVerticle;
import com.kaitusoft.ratel.console.model.ExecuteResult;
import com.kaitusoft.ratel.core.common.Event;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
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
 * @version 1.0.0, 2019/1/11
 *          <p>
 *          write description here
 */
public class NodeAction extends BaseAction {

    private static Logger logger = LoggerFactory.getLogger(NodeAction.class);

    protected void nodes(RoutingContext context) {
        logger.debug("获取所有 节点");
        HttpServerResponse response = context.response();

        String groupId = context.request().getParam("groupId");
        context.vertx().eventBus().<JsonArray>send(Event.formatInternalAddress(Event.CLUSTER_GET_NODES), groupId, reply -> {
            if (reply.succeeded()) {
                logger.debug("查找所有 节点:{} -> ok", groupId);
                JsonArray nodes = reply.result().body();
                if (nodes.size() == 0) {
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(Json.encode(nodes));
                    return;
                }

                StringBuilder groupIds = new StringBuilder();
                Map<String, List<JsonObject>> tmp = new HashMap<>();
                nodes.forEach(obj -> {
                    JsonObject json = (JsonObject) obj;
                    String gId = json.getString("groupId");
                    String nodeId = json.getString("nodeId");
                    if (nodeId.equals(ClusterVerticle.myNodeId))
                        json.put("isLocal", true);
                    groupIds.append(gId).append(",");
                    List<JsonObject> gNodes = tmp.get(gId);
                    if (gNodes == null) {
                        gNodes = new ArrayList<>();
                        tmp.put(gId, gNodes);
                    }
                    gNodes.add(json);

                });
                groupIds.deleteCharAt(groupIds.length() - 1);
                context.vertx().eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_GROUP_IDS), groupIds.toString(), groupReply -> {
                    if (groupReply.succeeded()) {
                        JsonArray groups = groupReply.result().body();
                        groups.forEach(g -> {
                            JsonObject groupJson = (JsonObject) g;
                            String gId = groupJson.getInteger("id").toString();
                            List<JsonObject> gNodes = tmp.get(gId);
                            if (gNodes != null) {
                                gNodes.forEach(node -> {
                                    node.put("groupName", groupJson.getString("name"));
                                });
                            }
                        });
                    } else {

                    }

                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(Json.encode(nodes));

                });

            } else {
                logger.error("查找所有 节点 -> failed!", reply.cause());

                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
            }
        });

    }

    public void halt(RoutingContext context) {
        String nodeId = context.request().getParam("nodeId");
        logger.debug("关闭节点:{}", nodeId);
        HttpServerResponse response = context.response();
//        List<String> targetNodes = new ArrayList<>(2);
//        targetNodes.add(nodeId);
//        ClusterVerticle.clusterMessage.<JsonObject>send(targetNodes, Event.formatAddress(Event.CLUSTER_HALT_NODE, nodeId),
//                new JsonObject().put("targetNodes", nodeId).put("commander", ClusterVerticle.myNodeId),
//                reply -> {
//            if (reply.succeeded()) {
//                logger.debug("关闭节点:{} -> ok", nodeId, reply.result());
//                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
//                        .end(Json.encode(new ExecuteResult("节点已关闭")));
//            } else {
//                logger.error("关闭节点:{} -> failed:", nodeId, reply.cause());
//                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
//                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
//                        .end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
//            }
//        });
        context.vertx().eventBus().<JsonObject>send(Event.formatAddress(Event.CLUSTER_HALT_NODE, nodeId), new JsonObject(), reply -> {
            if (reply.succeeded()) {
                logger.debug("关闭节点:{} -> ok", nodeId, reply.result().body());
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult("节点已关闭")));
            } else {
                logger.error("关闭节点:{} -> failed:", nodeId, reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
            }
        });
    }

    public void restart(RoutingContext context) {
        String nodeId = context.request().getParam("nodeId");
        logger.debug("重启节点:{}", nodeId);
        HttpServerResponse response = context.response();
//        List<String> targetNodes = new ArrayList<>(2);
//        targetNodes.add(nodeId);
//        ClusterVerticle.clusterMessage.<JsonObject>send(targetNodes, Event.formatAddress(Event.CLUSTER_RESTART_NODE, nodeId),
//                new JsonObject().put("targetNodes", nodeId).put("commander", ClusterVerticle.myNodeId),
//                reply -> {
//                    if (reply.succeeded()) {
//                        logger.debug("重启节点:{} -> ok", nodeId, reply.result());
//                        response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
//                                .end(Json.encode(new ExecuteResult("节点已关闭")));
//                    } else {
//                        logger.error("重启节点:{} -> failed:", nodeId, reply.cause());
//                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
//                                .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
//                                .end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
//                    }
//                });
        context.vertx().eventBus().<JsonObject>send(Event.formatAddress(Event.CLUSTER_RESTART_NODE, nodeId), nodeId, reply -> {
            if (reply.succeeded()) {
                logger.debug("重启节点:{} -> ok", nodeId, reply.result().body());
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult("节点已重启完毕")));
            } else {
                logger.error("重启节点:{} -> failed:", nodeId, reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
            }
        });
    }

    public void removeNode(RoutingContext context) {
        String nodeId = context.request().getParam("nodeId");
        logger.debug("驱逐节点:{}", nodeId);
        HttpServerResponse response = context.response();
        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.CLUSTER_GET_NODE), nodeId, res -> {
            if (res.succeeded()) {
                JsonObject node = res.result().body();

                Object data = nodeId;
                String address = Event.formatInternalAddress(Event.CLUSTER_EXPEL_NODE);
                if (node.getBoolean("online", false)) {
                    data = new JsonObject().put("expel", true);
                    address = Event.formatAddress(Event.CLUSTER_HALT_NODE, nodeId);
                }

                context.vertx().eventBus().<JsonObject>send(address, data, reply -> {
                    if (reply.succeeded()) {
                        logger.debug("驱逐节点:{} -> ok", nodeId, reply.result().body());
                        response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                                .end(Json.encode(new ExecuteResult("节点已移除")));
                    } else {
                        logger.error("驱逐节点:{} -> failed:", nodeId, reply.cause());
                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                                .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                                .end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
                    }
                });
            } else {
                logger.error("获取节点状态出错:", res.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, res.cause().getMessage())));
            }
        });

    }


    public void apps(RoutingContext context) {
        Vertx vertx = context.vertx();
        String nodeId = context.request().getParam("nodeId");
        logger.debug("查看节点 apps:{}", nodeId);
        HttpServerResponse response = context.response();

        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.CLUSTER_GET_NODE), nodeId, res -> {
            if (res.succeeded()) {
                JsonObject node = res.result().body();
                String groupId = node.getString("groupId");
                JsonObject params = new JsonObject();
                params.put("deploy_group", groupId);
                vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_APP_COND), params, apps -> {
                    if (apps.succeeded()) {
                        JsonArray appArray = apps.result().body();

                        Collection toNodes = new ArrayList();
                        toNodes.add(node.getString("nodeId"));
                        JsonObject body = new JsonObject();
                        body.put("commander", ClusterVerticle.myNodeId);
                        body.put("targetNodes", nodeId);
                        ClusterVerticle.clusterMessage.<JsonObject>send(toNodes, Event.formatAddress(Event.CLUSTER_NODE_APP_STATUS), body, clusterReply -> {
                            JsonArray nodeResult;
                            if (clusterReply.succeeded()) {
                                nodeResult = clusterReply.result().getJsonArray("nodes");
                            } else {
                                nodeResult = new JsonArray();
                            }
                            logger.debug("获取节点app运行状态:{}", nodeResult);
                            Map<String, Integer> runningApps = new HashMap<>();
                            for (int i = 0; i < nodeResult.size(); i++) {
                                JsonObject nodeDataJson = nodeResult.getJsonObject(i);
                                JsonObject data = nodeDataJson.getJsonObject(nodeId);
                                if (data == null)
                                    continue;
                                boolean nodeSuccess = data.getBoolean("success");
                                if (!nodeSuccess)
                                    return;

                                JsonArray nodeAppIds = data.getJsonArray("result");
                                for (int x = 0; x < nodeAppIds.size(); x++) {
                                    String nodeAppId = nodeAppIds.getString(x);
                                    runningApps.put(nodeAppId, 0);
                                }
                                break;
                            }

                            appArray.forEach(obj -> {
                                JsonObject appJson = (JsonObject) obj;
                                if (runningApps.get(appJson.getInteger("id").toString()) != null) {
                                    appJson.put("realRunning", true);
                                }
                                appJson.remove("parameter");
                            });

                            response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                                    .end(Json.encode(appArray));
                        });
                    } else {
                        logger.error("未能查找到对应app ", apps.cause());
                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                                .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                                .end(Json.encode(new ExecuteResult(false, apps.cause().getMessage())));
                    }
                });
            } else {
                logger.error("获取节点状态出错:", res.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, res.cause().getMessage())));
            }
        });
    }


    public void apis(RoutingContext context) {
        Vertx vertx = context.vertx();
        String nodeId = context.request().getParam("nodeId");
        String appId = context.request().getParam("appId");
        logger.debug("查看节点 app:{} - apis,  node:{}", appId, nodeId);
        HttpServerResponse response = context.response();

        JsonObject result = new JsonObject();

        Future<Void> futureApis = Future.future(apis -> {
            vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_API), appId, reply -> {
                if (!reply.succeeded()) {
                    logger.error("获取APP:{} 所有API -> failed!", appId, reply.cause());
                    apis.complete();
                    return;
                }

                JsonArray apiArray = reply.result().body();

                JsonObject body = new JsonObject();
                body.put("commander", ClusterVerticle.myNodeId);
                body.put("appId", appId);
                body.put("targetNodes", nodeId);
                ClusterVerticle.clusterMessage.<JsonObject>send(null, Event.formatAddress(Event.CLUSTER_NODE_API_STATUS), body, clusterReply -> {
                    JsonArray nodeResult;
                    if (clusterReply.succeeded()) {
                        nodeResult = clusterReply.result().getJsonArray("nodes");
                    } else {
                        nodeResult = new JsonArray();
                    }
                    logger.debug("获取节点 app:{} - apis 运行状态:{}", appId, nodeResult);

                    Map<String, Integer> runningApps = new HashMap<>();

                    for (int i = 0; i < nodeResult.size(); i++) {
                        JsonObject nodeDataJson = nodeResult.getJsonObject(i);
                        JsonObject data = nodeDataJson.getJsonObject(nodeId);
                        if (data == null)
                            continue;
                        boolean nodeSuccess = data.getBoolean("success");
                        if (!nodeSuccess)
                            return;

                        JsonArray nodeAppIds = data.getJsonArray("result");
                        for (int x = 0; x < nodeAppIds.size(); x++) {
                            String nodeAppId = nodeAppIds.getString(x);
                            runningApps.put(nodeAppId, 0);
                        }
                        break;
                    }

                    apiArray.forEach(obj -> {
                        JsonObject appJson = (JsonObject) obj;
                        if (runningApps.get(appJson.getInteger("id").toString()) != null) {
                            appJson.put("realRunning", true);
                        }
                        appJson.remove("parameter");
                    });

                    result.put("apis", apiArray);
                    apis.complete();
                });
            });

        });

        Future<Void> futureApp = Future.future(app -> {
            context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), appId, reply -> {
                if (reply.succeeded()) {
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
            if (res.succeeded()) {
                response
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(result));
            } else {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, res.cause().getMessage())));
            }
        });


    }

}
