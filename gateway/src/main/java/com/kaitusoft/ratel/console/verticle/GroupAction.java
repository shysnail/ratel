package com.kaitusoft.ratel.console.verticle;

import com.kaitusoft.ratel.cluster.ClusterVerticle;
import com.kaitusoft.ratel.console.model.ExecuteResult;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.model.vo.Node;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/6
 *          <p>
 *          write description here
 */
public class GroupAction extends BaseAction {

    private static Logger logger = LoggerFactory.getLogger(GroupAction.class);


    protected void find(RoutingContext context) {
        logger.debug("获取所有 分组");
        HttpServerResponse response = context.response();
        context.vertx().eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_ALL_GROUP), null, reply -> {
            if (!reply.succeeded()) {
                logger.error("查找所有 分组 -> failed!", reply.cause());

                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
                return;
            }

            Map<String, List> groupNodes = new HashMap<>();

            context.vertx().eventBus().<JsonArray>send(Event.formatInternalAddress(Event.CLUSTER_GET_NODES), null, nodes -> {
                if (nodes.succeeded()) {
                    JsonArray allNodes = nodes.result().body();
                    allNodes.forEach(nodeJson -> {
                        Instant addTime = ((JsonObject) nodeJson).getInstant("addTime");
                        ((JsonObject) nodeJson).remove("addTime");
                        Node node = ((JsonObject) nodeJson).mapTo(Node.class);
                        node.setAddTime(addTime);
                        String gId = node.getGroupId();
                        List tmpNodes = groupNodes.get(gId);
                        if (tmpNodes == null) {
                            tmpNodes = new ArrayList();
                            groupNodes.put(gId, tmpNodes);
                        }
                        tmpNodes.add(node);
                    });
                } else {
                    logger.error("从集群获取节点组信息出错:", nodes.cause());
                }

                JsonArray array = reply.result().body();
                array.forEach(obj -> {
                    JsonObject json = (JsonObject) obj;
                    int groupId = json.getInteger("id");
                    List tmpNodes = groupNodes.get(groupId + "");
                    json.put("nodes", tmpNodes == null ? 0 : tmpNodes.size());
                });

                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(array));
            });

        });
    }

    protected void get(RoutingContext context) {
        String id = context.request().getParam("id");
        logger.debug("获取分组:{}", id);
        HttpServerResponse response = context.response();
        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_GROUP), id, reply -> {
            if (reply.succeeded()) {
                logger.debug("获取分组:{} -> ok", id);
                response.setStatusCode(HttpResponseStatus.OK.code()).end(Json.encode(reply.result().body()));
            } else {
                logger.error("获取分组:{} -> failed:", id, reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
            }
        });
    }

    protected void add(RoutingContext context) {
        JsonObject params = context.getBodyAsJson();
        logger.debug("添加分组");
        HttpServerResponse response = context.response();
        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.ADD_GROUP), params, reply -> {
            if (reply.succeeded()) {
                logger.debug("添加分组:{} -> ok", reply.result().body());
                response.setStatusCode(HttpResponseStatus.OK.code()).end(Json.encode(reply.result().body()));
            } else {
                logger.error("添加分组: -> failed:", reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
            }
        });
    }

    protected void update(RoutingContext context) {
        JsonObject params = context.getBodyAsJson();
        String id = context.request().getParam("id");
        params.put("id", Integer.valueOf(id));
        logger.debug("更新分组");
        HttpServerResponse response = context.response();
        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.UPDATE_GROUP), params, reply -> {
            if (reply.succeeded()) {
                logger.debug("更新分组:{} -> ok", id, reply.result().body());
                response.setStatusCode(HttpResponseStatus.OK.code()).end(Json.encode(reply.result().body()));
            } else {
                logger.error("更新分组:{} -> failed:", id, reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
            }
        });
    }

    protected void delete(RoutingContext context) {
        String ids = context.request().getParam("id");
        logger.debug("删除分组");
        HttpServerResponse response = context.response();
        context.vertx().eventBus().<String>send(Event.formatInternalAddress(Event.DELETE_GROUP), ids, reply -> {
            if (reply.succeeded()) {
                logger.debug("删除分组:{} -> ok", ids, reply.result().body());
                response.setStatusCode(HttpResponseStatus.OK.code()).end(Json.encode(reply.result().body()));
            } else {
                logger.error("删除分组:{} -> failed:", ids, reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
            }
        });
    }

    protected void grouping(RoutingContext context) {
        Vertx vertx = context.vertx();
        HttpServerResponse response = context.response();

        JsonObject param = context.getBodyAsJson();
        if (vertx.isClustered())
            param.put("commander", ClusterVerticle.myNodeId);

        logger.debug("分组:");

        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.CLUSTER_GET_NODES), null, nodes -> {
            Collection toNodes = new HashSet();
            if (nodes.succeeded()) {
                JsonArray allNodes = nodes.result().body();
                allNodes.forEach(node -> {
                    toNodes.add(((JsonObject) node).getString("nodeId"));
                });
            }

            ClusterVerticle.clusterMessage.<JsonObject>send(toNodes, Event.formatAddress(Event.CLUSTER_GROUPING), param, res -> {
                if (res.succeeded()) {
                    JsonObject reply = res.result();
                    logger.debug("分组: -> ok, {}", reply);

                    ExecuteResult result = new ExecuteResult();
                    if (reply.getBoolean("fullSuccess", false)) {
                        result.setData("已通知所有节点");
                    } else {
                        result.setData("已通知部分节点");
                    }
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(Json.encode(result));
                } else {
                    logger.error("分组: -> failed:", res.cause());
                    response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                            .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(res.cause().getMessage());
                }
            });
        });

    }

}
