package com.kaitusoft.ratel.console.verticle;

import com.kaitusoft.ratel.cluster.ClusterVerticle;
import com.kaitusoft.ratel.console.model.ExecuteResult;
import com.kaitusoft.ratel.core.common.Configuration;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.model.App;
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
public class AppAction extends BaseAction {

    private static Logger logger = LoggerFactory.getLogger(AppAction.class);

    protected void find(RoutingContext context) {
        logger.debug("获取所有APP");
        HttpServerResponse response = context.response();
        context.vertx().eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_ALL_APP), null, reply -> {
            if (!reply.succeeded()) {
                logger.error("查找所有app -> failed!", reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
                return;
            }

            JsonArray array = reply.result().body();

            if (array.size() == 0) {
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(array));
                return;
            }

            StringBuilder groupIds = new StringBuilder();
            Map<Integer, List<JsonObject>> tmp = new HashMap<>();
            array.forEach(obj -> {
                JsonObject json = (JsonObject) obj;

                String parameter = (String) json.remove("parameter");
                json.put("extendOption", new JsonObject(parameter));

                String appId = json.getInteger("id").toString();
                if (DeployVerticle.DEPLOY_APP.get(appId) != null)
                    json.put("running", true);

                if (DeployVerticle.RUNNING_APIS.get(appId) != null) {
                    json.put("runningApiNum", DeployVerticle.RUNNING_APIS.get(appId).size());
                }

                int gId = json.getInteger("deployGroup");
                String append = gId + ",";
                if (groupIds.indexOf(append) < 0)
                    groupIds.append(append);

                List<JsonObject> gNodes = tmp.get(gId);
                if (gNodes == null) {
                    gNodes = new ArrayList<>();
                    tmp.put(gId, gNodes);
                }
                gNodes.add(json);

            });


            context.vertx().eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_GROUP_IDS), groupIds.toString(), groupReply -> {
                if (groupReply.succeeded()) {
                    JsonArray groups = groupReply.result().body();
                    groups.forEach(g -> {
                        JsonObject groupJson = (JsonObject) g;
                        int gId = groupJson.getInteger("id");
                        List<JsonObject> gNodes = tmp.get(gId);
                        if (gNodes != null) {
                            gNodes.forEach(node -> {
                                node.put("groupName", groupJson.getString("name"));
                            });
                        }
                    });
                } else {

                }

                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(array));

            });

        });

    }


    protected void get(RoutingContext context) {
        String id = context.request().getParam("id");
        logger.debug("查看APP -> {}", id);
        HttpServerResponse response = context.response();

        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), id, reply -> {
            if (reply.succeeded()) {
                JsonObject object = reply.result().body();
//                if (DeployVerticle.DEPLOY_APP.get(id) != null)
//                    object.put("running", true);

                logger.debug("查看APP:{} -> ok", id);
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(object));
            } else {
                logger.error("查看APP:{} -> failed:", id, reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
            }
        });
    }


    protected void add(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        JsonObject parameter = body.getJsonObject("extendOption");
        body.remove("extendOption");
        body.put("parameter", parameter.toString());

        AppOption appOption = body.mapTo(AppOption.class);

        if(StringUtils.isEmpty(appOption.getVhost())){
            appOption.setVhost("*");
        }

        logger.debug("got app : {}", body);

        HttpServerResponse response = context.response();

        context.vertx().eventBus().<AppOption>send(Event.formatInternalAddress((appOption.getId() != null ? Event.UPDATE_APP : Event.ADD_APP)), appOption, new DeliveryOptions().setCodecName(Configuration.MODEL_CODEC), reply -> {
            if (reply.succeeded()) {
                logger.debug("创建APP -> ok");
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("ok")));
            } else {
                String error = reply.cause().getMessage();
                logger.error("创建APP -> failed", reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
            }
        });

    }

    protected void update(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        JsonObject parameter = body.getJsonObject("extendOption");
        body.remove("extendOption");
        body.put("parameter", parameter.toString());
        AppOption appOption = body.mapTo(AppOption.class);
        if(StringUtils.isEmpty(appOption.getVhost())){
            appOption.setVhost("*");
        }
        logger.debug("got app : {}", body);
        String appId = context.request().getParam("id");
        body.put("id", Integer.valueOf(appId));

        HttpServerResponse response = context.response();

        context.vertx().eventBus().<AppOption>send(Event.formatInternalAddress(Event.UPDATE_APP), appOption, new DeliveryOptions().setCodecName(Configuration.MODEL_CODEC), reply -> {
            if (reply.succeeded()) {
                logger.debug("更新APP -> ok");
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult("ok")));
            } else {
                String error = reply.cause().getMessage();
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
                logger.error("更新APP -> failed", error);
            }
        });


    }


    protected void delete(RoutingContext context) {
        Vertx vertx = context.vertx();
        String id = context.request().getParam("id");
        HttpServerResponse response = context.response();

        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), id, app -> {
            if (app.succeeded()) {
                JsonObject appJson = app.result().body();
                int originStat = appJson.getInteger("running");
                Future future = Future.future();
                future.setHandler(pre -> {
                    JsonObject result = (JsonObject) future.result();
                    if (result.getBoolean("success", false)) {
                        vertx.eventBus().<String>send(Event.formatInternalAddress(Event.DELETE_APP), id, reply -> {
                            if (reply.succeeded()) {
                                logger.debug("删除APP:{} -> ok", id);
                                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                                        .end(Json.encode(new ExecuteResult("ok")));
                                deleteAppStatus(context, id);
                            } else {
                                String error = reply.cause().getMessage();
                                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                                        .end(Json.encode(new ExecuteResult(false, error)));
                                logger.error("删除APP:{} -> failed", id, error);
                            }
                        });
                    } else {
                        String reason = result.getString("reason");
                        response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, reason)));
                        logger.debug("未能删除应用", reason);
                    }
                });

                if (originStat != App.STOPPED) {
                    JsonObject result = new JsonObject();
                    action(vertx, id, App.STOPPED, new int[]{App.STOPPED}, Event.STOP_APP, res -> {
                        if (res.succeeded()) {
                            JsonObject obj = (JsonObject) res.result();
                            if (obj == null) {
                                obj = new JsonObject();
                            }

                            if (obj.getBoolean("success", false)) {
                                result.put("success", true);

                            } else {
                                result.put("reason", obj.getString("reason"));
//                                future.complete(false);
                            }
                        } else {
                            result.put("reason", res.result());
//                            future.complete(false);
                        }
                        future.complete(result);
                    });
                }
            } else {
                logger.error("删除APP:{} -> failed， 获取应用信息出错", id, app.cause());
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, app.cause().getMessage())));
            }
        });

    }

    private void deleteAppStatus(RoutingContext context, String id) {
        JsonObject params = new JsonObject();
        params.put("appIds", id);
        context.vertx().eventBus().send(Event.formatInternalAddress(Event.STATUS_DELETE), params, reply -> {
            if (reply.succeeded()) {
                logger.info("清理废弃app状态残留完毕！");
            } else {
                logger.info("清理废弃app状态残留 -> failed", reply.cause());
            }
        });
    }


    /**
     * 启动app，
     * 1. 更新数据库中对应app状态
     * 2. 获取此app部署的组包含的节点。
     * 2. 集群情况下发布到应用所在的组，非集群状态下发布到本节点
     *
     * @param context
     */
    protected synchronized void start(RoutingContext context) {
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("id");
        logger.debug("启动app:{}", appId);
        HttpServerResponse response = context.response();

        action(vertx, appId, App.RUNNING, new int[]{App.RUNNING}, Event.START_APP, res -> {
            if (res.succeeded()) {
                JsonObject obj = (JsonObject) res.result();
                if (obj == null) {
                    obj = new JsonObject();
                }

                ExecuteResult result = new ExecuteResult();
                if (obj.getBoolean("success", false)) {
                    if (obj.getBoolean("fullSuccess", false)) {
                        result.setData("所有节点均启动完毕");
                    } else {
                        result.setData("部分节点启动完毕");
                    }
                } else {
                    result.setData(obj.getString("reason"));
                }

                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(result));
            } else {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, res.result())));
            }
        });
    }


    protected synchronized void stop(RoutingContext context) {
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("id");
        logger.debug("停止app:{}", appId);
        HttpServerResponse response = context.response();

        action(vertx, appId, App.STOPPED, new int[]{App.STOPPED}, Event.STOP_APP, res -> {
            if (res.succeeded()) {
                JsonObject obj = (JsonObject) res.result();
                if (obj == null) {
                    obj = new JsonObject();
                }

                ExecuteResult result = new ExecuteResult();
                if (obj.getBoolean("success", false)) {
                    if (obj.getBoolean("fullSuccess", false)) {
                        result.setData("所有节点均停止完毕");
                    } else {
                        result.setData("部分节点停止完毕");
                    }
                } else {
                    result.setData(obj.getString("reason"));
                }

                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(result));
            } else {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, res.result())));
            }
        });
    }

    /**
     * 暂停
     * 暂时不可用，不提供App暂停的功能，
     *
     * @param context
     */
    protected void pause(RoutingContext context) {
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("id");
        logger.debug("暂停app:{}", appId);
        HttpServerResponse response = context.response();

        //获取app，看其状态
        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), appId, app -> {
            if (app.succeeded()) {
                JsonObject appJson = app.result().body();
                int originStat = appJson.getInteger("running");
                if (originStat == App.STOPPED || originStat == App.PAUSED) {
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(Json.encode(new ExecuteResult("应用已暂停")));
                    return;
                }


            } else {
                logger.error("停止APP:{} -> failed:", appId, app.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, app.cause().getMessage())));
            }


        });
    }

    protected void restart(RoutingContext context) {
        Vertx vertx = context.vertx();
        String appId = context.request().getParam("id");
        logger.debug("重启app:{}", appId);
        HttpServerResponse response = context.response();
        action(vertx, appId, App.RUNNING, new int[]{App.PAUSED, App.STOPPED}, Event.RESTART_APP, res -> {
            if (res.succeeded()) {
                JsonObject obj = (JsonObject) res.result();
                if (obj == null) {
                    obj = new JsonObject();
                }

                ExecuteResult result = new ExecuteResult();
                if (obj.getBoolean("success", false)) {
                    if (obj.getBoolean("fullSuccess", false)) {
                        result.setData("所有节点均重启完毕");
                    } else {
                        result.setData("部分节点重启完毕");
                    }
                } else {
                    result.setData(obj.getString("reason"));
                }

                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(result));
            } else {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, res.result())));
            }
        });
    }

    private void action(Vertx vertx, String appId, int targetStatus, int[] stopStatus, String sendAddress, Handler<AsyncResult> handler) {
        JsonObject resultData = new JsonObject();
        //获取app，看其状态
        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), appId, app -> {
            if (app.succeeded()) {
                JsonObject appJson = app.result().body();
                int originStat = appJson.getInteger("running");
                boolean stop = false;
                for (int i = 0; stopStatus != null && i < stopStatus.length; i++) {
                    if (originStat == stopStatus[i]) {
                        stop = true;
                        break;
                    }
                }
                if (stop) {
                    resultData.put("success", false);
                    resultData.put("reason", "当前状态无法进行此操作");
                    handler.handle(Future.succeededFuture(resultData));
//                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
//                            .end(Json.encode(new ExecuteResult("APP已停止/暂停")));
                    return;
                }

                //更新app，设置成启动
                JsonObject updates = new JsonObject();
                updates.put("appId", appId);
                JsonObject prop = new JsonObject();
                prop.put("running", targetStatus);
                updates.put("prop", prop);

                vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.UPDATE_APP_PROP), updates, appUpdate -> {
                    if (appUpdate.succeeded()) {
                        //修改状态后，通知集群启动app
                        //首先拿到现在集群app所属组的所有节点
                        //然后统计所有的节点的响应
                        int deployGroup = appJson.getInteger("deployGroup");
                        JsonObject body = new JsonObject();
                        body.put("appId", appId);
                        body.put("commander", ClusterVerticle.myNodeId);
                        body.put("groupId", deployGroup);

                        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.CLUSTER_GET_NODES), String.valueOf(deployGroup), nodes -> {
                            Collection toNodes = new HashSet();
                            if (nodes.succeeded()) {
                                JsonArray allNodes = nodes.result().body();
                                allNodes.forEach(node -> {
                                    if (((JsonObject) node).getBoolean("online", false))
                                        toNodes.add(((JsonObject) node).getString("nodeId"));
                                });
                            }

                            ClusterVerticle.clusterMessage.<JsonObject>send(toNodes, Event.formatAddress(sendAddress), body, clusterReply -> {
                                if (clusterReply.succeeded()) {
                                    logger.debug("节点 {}，app {}", targetStatus, targetStatus);
                                    JsonObject reply = clusterReply.result();

                                    resultData.put("success", true);
//                                    ExecuteResult result = new ExecuteResult();
                                    if (reply.getBoolean("fullSuccess", false)) {
//                                        result.setData("所有节点均暂停完毕");
                                        resultData.put("fullSuccess", true);
                                    }
//                                    else {
//                                        result.setData("部分节点暂停完毕");
//                                    }
//                                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
//                                            .end(Json.encode(result));
                                    handler.handle(Future.succeededFuture(resultData));
                                } else {
                                    prop.put("running", originStat);
                                    vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.UPDATE_APP_PROP), updates, rollback -> {
                                        logger.debug("所有节点均{}失败，app无法{}", targetStatus, targetStatus);
//                                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
//                                                .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
//                                                .end(Json.encode(new ExecuteResult(false, "所有节点均停止失败，app无法停止")));
                                        handler.handle(Future.failedFuture(clusterReply.cause().getMessage()));
                                    });
                                }
                            });

                        });
                    } else {
                        logger.error("{}APP:{} -> failed:", targetStatus, appId, appUpdate.cause());
//                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
//                                .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
//                                .end(Json.encode(new ExecuteResult(false, appUpdate.cause().getMessage())));
                        handler.handle(Future.failedFuture(appUpdate.cause().getMessage()));
                    }
                });


            } else {
                logger.error("{}APP:{} -> failed:", targetStatus, appId, app.cause());
//                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
//                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
//                        .end(Json.encode(new ExecuteResult(false, app.cause().getMessage())));
                handler.handle(Future.failedFuture(app.cause().getMessage()));
            }

        });
    }

    /**
     * 获取每个节点app运行状态
     * 存储在json数组中，每个元素数据格式为 appId:[badnodes,...]
     *
     * @param context
     */
    protected void allNodeStatus(RoutingContext context) {
        Vertx vertx = context.vertx();
        String appIds = context.request().getParam("appIds");
        String[] appIdArray = appIds.split(",");
        Map<String, Set<Node>> appUnhealthyNodes = new HashMap<>();

        for (String appId : appIdArray) {
            appUnhealthyNodes.put(appId, new HashSet());
        }

        HttpServerResponse response = context.response();
        Map<Integer, Set<Integer>> appGroup = new HashMap<>();
        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_ALL_APP), appIds, app -> {
            if (app.succeeded()) {
                JsonArray appsJson = app.result().body();
                appsJson.forEach(obj -> {
                    JsonObject appJson = (JsonObject) obj;
                    int deployGroup = appJson.getInteger("deployGroup");
                    int id = appJson.getInteger("id");

                    Set<Integer> appIdSet = appGroup.get(deployGroup);
                    if (appIdSet == null) {
                        appIdSet = new HashSet();
                        appGroup.put(deployGroup, appIdSet);
                    }
                    appIdSet.add(id);
                });

                List<Future> allFuture = new ArrayList<>();
                appGroup.forEach((group, apps) -> {
                    Future future = Future.future(groupNodeApp -> {
                        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.CLUSTER_GET_NODES), String.valueOf(group), nodes -> {
                            Collection toNodes = new HashSet();
                            final Set<Node> allGroupNodes = new HashSet();
                            if (nodes.succeeded()) {
                                JsonArray allNodes = nodes.result().body();
                                allNodes.forEach(node -> {
                                    if (!((JsonObject) node).getBoolean("online", false))
                                        return;
                                    toNodes.add(((JsonObject) node).getString("nodeId"));
                                    ((JsonObject) node).remove("addTime");
                                    allGroupNodes.add(((JsonObject) node).mapTo(Node.class));
                                });
                            }
                            logger.debug("获取组:{} 包含的节点:{}", group, toNodes);

                            JsonObject body = new JsonObject();
                            body.put("commander", ClusterVerticle.myNodeId);

                            /*
                                拿到的数据结构为
                                node -> json(sucess:true/false, result:[appId1, appid2, appid3] ...)
                                每个组所有的节点，用户统计是否有节点未返回数据
                             */
                            ClusterVerticle.clusterMessage.<JsonObject>send(toNodes, Event.formatAddress(Event.CLUSTER_NODE_APP_STATUS), body, clusterReply -> {
                                JsonArray nodeResult;
                                if (clusterReply.succeeded()) {
                                    nodeResult = clusterReply.result().getJsonArray("nodes");
                                } else {
                                    nodeResult = new JsonArray();
                                }
                                logger.debug("获取节点app运行状态:{}", nodeResult);
                                /*
                                将每个节点对应的app数据转成每个app相对每个节点的运行状态
                                 */
                                nodeResult.forEach(nodeData -> {
                                    JsonObject nodeDataJson = (JsonObject) nodeData;
                                    nodeDataJson.forEach(e -> {
                                        String node = e.getKey();
                                        JsonObject data = (JsonObject) e.getValue();
                                        boolean nodeSuccess = data.getBoolean("success");
                                        if (!nodeSuccess)
                                            return;

                                        Node tmpNode = new Node();
                                        tmpNode.setNodeId(node);
                                        tmpNode.setHostname(data.getString("nodeName"));
                                        tmpNode.setGroupId(group.toString());
                                        allGroupNodes.remove(tmpNode);
                                        JsonArray nodeAppIds = data.getJsonArray("result");
                                        for (String appId : appIdArray) {
                                            boolean appRunInNode = false;
                                            for (int i = 0; i < nodeAppIds.size(); i++) {
                                                String nodeAppId = nodeAppIds.getString(i);
                                                if (appId.equalsIgnoreCase(nodeAppId)) {
                                                    appRunInNode = true;
                                                    break;
                                                }
                                            }

                                            if (!appRunInNode) {
                                                Set<Node> inNodes = appUnhealthyNodes.get(appId);
                                                inNodes.add(tmpNode);
                                            }
                                        }
                                    });
                                });

                            /*
                             *有节点未返回数据，表示无法获取该节点app运行状态
                             * 需要把这一组节点对应的app，全部记录不健康节点
                             */
                                if (allGroupNodes.size() > 0) {
                                    Set<Integer> appIdSets = appGroup.get(group);
                                    appIdSets.forEach(id -> {
                                        Set inNodes = appUnhealthyNodes.get(String.valueOf(id));
                                        inNodes.addAll(allGroupNodes);
                                    });
                                }

                                groupNodeApp.complete();
                            });
                        });

                    });

                    allFuture.add(future);

                });


                CompositeFuture.all(allFuture).setHandler(res -> {
                    ExecuteResult result = new ExecuteResult();
                    JsonObject obj = new JsonObject();
                    appUnhealthyNodes.forEach((k, v) -> {
                        if (v != null && v.size() > 0) {
                            obj.put(k, new JsonArray(new ArrayList(v)));
                        }
                    });
                    result.setData(obj);
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(Json.encode(result));
                });

            } else {
                logger.error("获取所有节点APP状态 -> failed:", app.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, app.cause().getMessage())));
            }
        });
    }
}
