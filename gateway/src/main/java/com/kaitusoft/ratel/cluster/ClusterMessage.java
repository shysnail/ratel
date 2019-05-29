package com.kaitusoft.ratel.cluster;

import com.kaitusoft.ratel.core.common.Event;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/4
 *          <p>
 *          write description here
 */
@Data
@ToString
public class ClusterMessage {
    public static final long DEFAULT_TIMEOUT = 60 * 1000;
    private static final Map<String, ClusterContext> CLUSTER_FUTURE = new ConcurrentHashMap<>(64, 0.75f, 2);
    private static Logger logger = LoggerFactory.getLogger(ClusterMessage.class);
    private Vertx vertx;

//    private String node;
//
//    private boolean deal;
//
//    private boolean success;
//
//    private Object result;

    public ClusterMessage(Vertx vertx) {
        this.vertx = vertx;
        vertx.eventBus().consumer(Event.formatAddress(Event.CLUSTER_CALLBACK, ClusterVerticle.myNodeId), this::clusterCallback);
    }

    public <T> void send(String address, JsonObject message) {
        send(address, message, new DeliveryOptions(), null);
    }

    public <T> void send(String address, JsonObject message, Handler<AsyncResult<T>> handler) {
        send(address, message, new DeliveryOptions().setSendTimeout(DEFAULT_TIMEOUT), handler);
    }

    public <T> void send(String address, JsonObject message, DeliveryOptions options) {
        send(null, address, message, options, null);
    }

    public <T> void send(String address, JsonObject message, DeliveryOptions options, Handler<AsyncResult<T>> handler) {
        send(null, address, message, options, handler);
    }

    public <T> void send(Collection<String> toNodes, String address, JsonObject message) {
        send(toNodes, address, message, new DeliveryOptions().setSendTimeout(DEFAULT_TIMEOUT), null);
    }

    public <T> void send(Collection<String> toNodes, String address, JsonObject message, Handler<AsyncResult<T>> handler) {
        send(toNodes, address, message, new DeliveryOptions().setSendTimeout(DEFAULT_TIMEOUT), handler);
    }

    public <T> void send(Collection<String> toNodes, String address, JsonObject message, DeliveryOptions options) {
        send(toNodes, address, message, options, null);
    }

    /**
     * @param toNodes 为空表示向全集群广播
     * @param address
     * @param message
     * @param options
     * @param handler
     * @param <T>
     */
    public <T> void send(Collection<String> toNodes, String address, JsonObject message, DeliveryOptions options, Handler<AsyncResult<T>> handler) {
        ClusterContext context = new ClusterContext();

        if (handler != null)
            context.future.setHandler(handler);

        String uuid = UUID.randomUUID().toString();

        message.put("msgId", uuid);

        if (handler != null) {
            CLUSTER_FUTURE.put(uuid, context);
            if (toNodes != null && toNodes.size() > 0) {
                context.targetNodes = new ArrayList(toNodes);
                context.noReplyNodes = new ConcurrentLinkedQueue(toNodes);
                context.timerId = vertx.setTimer(options.getSendTimeout(), task -> {
                    JsonObject result = context.result;
                    if (result.size() == 0) {
                        context.future.fail("no reply");
                    } else {
                        context.future.complete(context.result);
                    }
                });
            }
        }

        vertx.eventBus().publish(address, message, options);

    }

    private void clusterCallback(Message<JsonObject> message) {
        JsonObject data = message.body();
        logger.debug("cluster callback:{}", data);
        String uuid = (String) data.remove("msgId");
        ClusterContext context = CLUSTER_FUTURE.get(uuid);

        if (context == null) {
            logger.debug("收到无主或已过期广播，忽略");
            return;
        }

        String replyNode = (String) data.remove("node");
        context.result.getJsonArray("nodes").add(new JsonObject().put(replyNode, data));

        //为空表示向全集群广播
        if (context.noReplyNodes != null) {
            //是否有错误广播的消息
            boolean hasNode = context.noReplyNodes.remove(replyNode);

            //所有节点都完工
            if (context.noReplyNodes.size() == 0) {
                vertx.cancelTimer(context.timerId);
                CLUSTER_FUTURE.remove(uuid);
                context.result.put("fullSuccess", true);
                context.future.complete(context.result);
            }
        } else { //有任意一个回复信息，都表示成功执行了
            CLUSTER_FUTURE.remove(uuid);
            if (!vertx.isClustered()) {
                context.result.put("fullSuccess", true);
            }
            context.future.complete(context.result);
        }
    }

    @Data
    public static final class ClusterContext {
        protected Future future = Future.future();

//        private String uuid;
        protected List targetNodes;
        protected Queue noReplyNodes;
        protected JsonObject result = new JsonObject().put("fullSuccess", false).put("nodes", new JsonArray());
        private long timerId;
    }
}
