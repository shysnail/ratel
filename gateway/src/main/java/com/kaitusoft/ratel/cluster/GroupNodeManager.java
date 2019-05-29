package com.kaitusoft.ratel.cluster;

import com.kaitusoft.ratel.Ratel;
import com.kaitusoft.ratel.core.common.Configuration;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.common.StatusCode;
import com.kaitusoft.ratel.core.model.vo.Node;
import com.kaitusoft.ratel.util.SerializeUtil;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/9
 *          <p>
 *          write description here
 */
public class GroupNodeManager {
    private static final Logger logger = LoggerFactory.getLogger(GroupNodeManager.class);
    private static String rootPath;
    //    public static final Map<String, Node> NODE_HOST_MAP = new ConcurrentHashMap<>();
    private JsonObject clusterConfig;
    private CuratorFramework client;
    private Vertx vertx;

    public GroupNodeManager(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.clusterConfig = config;

        rootPath = clusterConfig.getString("rootPath");
        if (!rootPath.startsWith("/"))
            rootPath = "/" + rootPath;

    }

    protected static String getPath(String base, Object... subs) {
        StringBuilder path = new StringBuilder(rootPath);
        path.append("/cluster");
        path.append(base.startsWith("/") ? base : ("/" + base));

        if (subs == null || subs.length == 0)
            return path.toString();

        for (Object sub : subs) {
            String dir = sub.toString();
            path.append(dir.startsWith("/") ? dir : ("/" + dir));
        }

        return path.toString();
    }

    protected void init() {
        JsonObject retry = clusterConfig.getJsonObject("retry");

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(retry.getInteger("initialSleepTimeMs", 1000), retry.getInteger("maxTimes", 3), retry.getInteger("maxSleepMs", 3000));
        client = CuratorFrameworkFactory.newClient(clusterConfig.getString("zookeeperHosts"), clusterConfig.getInteger("sessionTimeoutMs", 600000), clusterConfig.getInteger("connectionTimeoutMs", 3000), retryPolicy);
        client.start();
        try {
            checkEnv();
        } catch (Exception e) {
            logger.warn("组配置有误，可能因为无法连接到注册中心", e);
        }
//        loadNodes();
    }

    private void checkEnv() throws Exception {
        touch(getPath("/groups"), true);
        touch(getPath("/groups", 0), true);
    }

    protected void destroy() {
        client.close();
    }

    public void haltNode(Message<JsonObject> message) {
        JsonObject data = message.body();
        Future future = Future.future();
        future.setHandler(h -> {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                    Ratel.getInstance().halt();
                }
            }.start();
            message.reply(1);
        });

        boolean expel = data.getBoolean("expel", false);

        if (vertx.isClustered() && expel) {
            try {
                nodeLeft(ClusterVerticle.myNodeId, res -> {
                    future.complete();
                });
            } catch (Exception e) {
                logger.warn("clean node status from cluster error", e);
                future.complete();
            }
        } else {
            future.complete();
        }

    }

    public void restartNode(Message<Void> message) {
        Future future = Future.future();
        future.setHandler(h -> {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                    Ratel.getInstance().restart();
                }
            }.start();
            message.reply(1);
        });
        if (vertx.isClustered()) {
            try {
                nodeLeft(ClusterVerticle.myNodeId, res -> {
                    future.complete();
                });
            } catch (Exception e) {
                logger.warn("clean node status from cluster error", e);
                future.complete();
            }
        } else {
            future.complete();
        }
    }

    protected void node(Message<String> message) {
        String nodeId = message.body();
        try {
            Node node = this.getDataByNodeId(nodeId);
            if (node == null) {
                message.reply(null);
                return;
            }
            List<String> nodeIds = ClusterVerticle.getClusterManager(null).getNodes();
            if (nodeIds.contains(nodeId))
                node.setOnline(true);

            message.reply(JsonObject.mapFrom(node));
        } catch (Exception e) {
            message.fail(StatusCode.SYS_ERROR, e.getMessage());
        }
    }

    protected void nodes(Message<String> message) {
        String groupId = message.body();

        JsonArray nodes = new JsonArray();
//        NODE_HOST_MAP.forEach((k, v) -> {
//            if (StringUtils.isEmpty(groupId) || groupId.equalsIgnoreCase(v.getGroupId())) {
//                try {
//                    //有可能有节点意外关机，没来得及session退出，需要判断一下
//                    if(!exists(getPath("/nodes", v.getNodeId()))){
//                        removeNodeFromGroupByHostname(groupId, v.getHostname());
//                        return;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                nodes.add(JsonObject.mapFrom(v));
//                return;
//            }
//        });
        List<String> nodeIds = ClusterVerticle.getClusterManager(null).getNodes();
        List<String> children;
        if (groupId != null) {
            String[] groupIds = groupId.split(",");
            children = new ArrayList<>(groupIds.length);
            for (String id : groupIds) {
                children.add(id);
            }
        } else {
            try {
                children = client.getChildren().forPath(getPath("/groups"));
            } catch (Exception e) {
                logger.error("获取组配置目录出错", e);
                message.fail(StatusCode.SYS_ERROR, e.getMessage());
                return;
            }
        }
        try {
            for (String groupPath : children) {
                List<String> groupNodes = client.getChildren().forPath(getPath("/groups", groupPath));

                for (String hostname : groupNodes) {
                    String nodePath = getPath("/groups", groupPath, hostname);
                    Node node = getData(nodePath);
                    if (nodeIds.contains(node.getNodeId())) { //节点在线
                        node.setOnline(true);
                    } else {
                        if (node.isOnline()) {
                            node.setOnline(false);
                            saveData(nodePath, node);
                        }
                    }
                    nodes.add(JsonObject.mapFrom(node));
                }

            }
        } catch (Exception e) {
            logger.error("从节点获取数据出错", e);
            message.fail(StatusCode.SYS_ERROR, e.getMessage());
            return;
        }

        message.reply(nodes);
    }

    /**
     * 新增节点，需要把节点放到 对应的组里
     * 先判断这个节点是不是曾经存在，如果存在，更新一下nodeId，
     * 如果不存在，新建到默认组中
     *
     * @param message
     */
    protected void nodeAdd(Message<JsonObject> message) {
        String nodeId = message.body().getString("nodeId");
        String hostname = message.body().getString("hostname");
        Node node = new Node();
        node.setNodeId(nodeId);
        node.setHostname(hostname);
//        NODE_HOST_MAP.put(nodeId, node);
        String nodePath = getNodePathInGroup(hostname);

        String group = "0";
        try {
            if (StringUtils.isEmpty(nodePath)) {
                addNode2Group(group, hostname);
                node.setGroupId(group);
            } else {
                group = nodePath.replace(getPath("/groups") + "/", "").replace("/" + Configuration.hostname, "");
                node.setGroupId(group);
            }

            addNode2Group(group, hostname, node);

            logger.debug("node:{} -> host:{}", nodeId, hostname);

            message.reply(1);

        } catch (Exception e) {
            message.fail(500, e.getMessage());
        }
    }

    protected void expelNode(Message<String> message) {
        String nodeId = message.body();
        try {
            nodeLeft(nodeId, res -> {
                if (res.succeeded()) {
                    message.reply(1);
                } else {
                    message.reply(0);
                }
            });
        } catch (Exception e) {
            logger.error("节点不存在或者数据不存在", e);
            message.reply(0);
        }
    }

    protected void nodeLeft(Message<JsonObject> message) {
        JsonObject data = message.body();
        String nodeId = data.getString("nodeId");

        message.reply(1);
        return;

//        String nodeId = message.body().getString("nodeId");
//        try {
//            nodeLeft(nodeId, res -> {
//                if(res.succeeded()){
//                    message.reply(1);
//                }else{
//                    message.reply(0);
//                }
//            });
//        } catch (Exception e) {
//            logger.error("节点不存在或者数据不存在", e);
//            message.reply(0);
//        }
    }

    private void nodeLeft(String nodeId, Handler<AsyncResult> handler) throws Exception {
        Node node = getDataByNodeId(nodeId);
        if (node != null) {
            String hostname = node.getHostname();
            removeNodeFromGroupByHostname(node.getGroupId(), hostname);

//            if (isCommander)
            removeNodeStatus(nodeId, handler);
        }
    }

    protected void removeNodeStatus(String nodeId, Handler<AsyncResult> handler) {
        JsonObject params = new JsonObject();
        params.put("nodeId", nodeId);
        vertx.eventBus().send(Event.formatInternalAddress(Event.STATUS_DELETE), params, reply -> {
            if (reply.succeeded()) {
                logger.info("清理废弃节点状态残留完毕！");
                handler.handle(Future.succeededFuture());
            } else {
                logger.info("清理废弃节点状态残留 -> failed", reply.cause());
                handler.handle(Future.failedFuture(reply.cause()));
            }
        });
    }

    private void addNode2Group(String group, String hostname, Object data) throws Exception {
        String groupPath = getPath("/groups", group);
        touch(groupPath, true);

        String wholePath = getPath("/groups", group, hostname);
        touch(wholePath, true);

        if (data != null)
            client.setData().forPath(wholePath, SerializeUtil.serialize(data));
    }

    private void addNode2Group(String group, String hostname) throws Exception {
        addNode2Group(group, hostname, null);
    }

    protected void grouping(Message<JsonObject> message) {
        String nodeIds = message.body().getString("nodeIds");
        String toGroup = message.body().getString("groupId");
        String[] nodes = nodeIds.split(",");
        int success = 0;
        for (String nodeId : nodes) {
            if (nodeId.equals(ClusterVerticle.myNodeId)) {
                ClusterVerticle.myGroup = Integer.parseInt(toGroup);
            }
            try {
                //更改zk分组树
                Node node = getDataByNodeId(nodeId);
                if (node != null) {
                    String hostname = node.getHostname();
                    removeNodeFromGroupByHostname(node.getGroupId(), hostname);
                } else {
                    node = new Node();
                    node.setNodeId(nodeId);
                }

                node.setGroupId(toGroup);

                addNode2Group(toGroup, node.getHostname(), node);
                success++;
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn("分组节点:{} 发生错误", nodeId, e);
            }

        }

        message.reply(success);
//        vertx.eventBus().publish(Event.formatAddress(Event.CLUSTER_CALLBACK, from), replyMessage);

    }

    private void loadNodes() {

        initMyGroup();

        try {
            initMyPosition();
        } catch (Exception e) {
            logger.error("create group error", e);
            return;
        }

        initNodeHostMap();
        logger.debug("all node init ");
    }

    private void initNodeHostMap() {
        String groupBasePath = getPath("/groups");
        try {
            if (!exists(groupBasePath))
                return;

            GetChildrenBuilder childrenBuilder = client.getChildren();

            List<String> children = childrenBuilder.forPath(groupBasePath);
            for (String c : children) {
                logger.debug("group : {}", c);
                String path = getPath("/groups", c);
                List<String> nodes = childrenBuilder.forPath(path);

                for (String host : nodes) {
                    String wholePath = getPath("/groups", c, host);
                    byte[] data = client.getData().forPath(wholePath);
                    Node node = (Node) SerializeUtil.unserialize(data);
//                    if (node != null)
//                        NODE_HOST_MAP.put(node.getNodeId(), node);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    private void initMyGroup() {
        String group = getNodeGroup(Configuration.hostname);
        if (StringUtils.isEmpty(group)) {
            return;
        }
        ClusterVerticle.myGroup = Integer.parseInt(group);
    }

    private void initMyPosition() throws Exception {
        String groupBasePath = getPath("/groups");
        touch(groupBasePath, true);

        String defaultGroupPath = getPath("/groups", 0);

        touch(defaultGroupPath, true);

        String nodePath = getNodePathInGroup(Configuration.hostname);
        if (StringUtils.isEmpty(nodePath)) {
            //node 目前没有分配到组记录中
            client.create().forPath(getPath("/groups", 0, Configuration.hostname));
        } else {
            byte[] data = client.getData().forPath(nodePath);
            if (data != null && data.length > 0) {
                Node node = (Node) SerializeUtil.unserialize(data);
                if (node != null) {
                    String oldId = node.getNodeId(); //有可能集群管理还保留着上一次的id
                    if (!StringUtils.isEmpty(oldId) && exists(getPath("/nodes", oldId)))
                        try {
                            client.delete().forPath(getPath("/nodes", oldId));
                        } catch (Exception e) {
                            logger.warn("清除旧节点:{} -> 出错", oldId, e);
                        }
                }
            }
        }

        Node newNode = new Node();
        newNode.setNodeId(ClusterVerticle.myNodeId);
        newNode.setHostname(Configuration.hostname);
        newNode.setGroupId(ClusterVerticle.myGroup.toString());

        client.setData().forPath(getPath("/groups", ClusterVerticle.myGroup, Configuration.hostname), SerializeUtil.serialize(newNode));
    }

    private Node getData(String path) throws Exception {
        byte[] data = client.getData().forPath(path);
        Node node = (Node) SerializeUtil.unserialize(data);
        return node;
    }

    private void saveData(String path, Node node) throws Exception {
        byte[] data = SerializeUtil.serialize(node);
        client.setData().forPath(path, data);
    }

    private Node getDataByNodeId(String nodeId) throws Exception {
        String groupBasePath = getPath("/groups");

        if (!exists(groupBasePath))
            return null;

        GetChildrenBuilder childrenBuilder = client.getChildren();

        List<String> children = childrenBuilder.forPath(groupBasePath);
        for (String c : children) {
            logger.debug("group : {}", c);
            String path = getPath("/groups", c);
            List<String> nodes = childrenBuilder.forPath(path);

            for (String node : nodes) {
                String wholePath = getPath("/groups", c, node);
                Node nodeData = (Node) SerializeUtil.unserialize(client.getData().forPath(wholePath));
                if (nodeData == null)
                    continue;

                if (nodeId.equalsIgnoreCase(nodeData.getNodeId())) {
                    return nodeData;
                }

            }
        }

        return null;
    }

    protected void removeNode(String nodeId) throws Exception {
        removeNodeFromGroupByNodeId(nodeId);
    }

    private void removeNodeFromGroupByNodeId(String groupId, String nodeId) throws Exception {
        removeNodeFromGroup(groupId, nodeId, 2);
    }

    private void removeNodeFromGroupByHostname(String groupId, String hostname) throws Exception {
        removeNodeFromGroup(groupId, hostname, 1);
    }

    private void removeNodeFromGroupByNodeId(String nodeId) throws Exception {
        removeNodeFromGroup(null, nodeId, 2);
    }

    private void removeNodeFromGroupByHostname(String hostname) throws Exception {
        removeNodeFromGroup(null, hostname, 1);
    }

//    protected static String getNodeGroupFromPath(String path) {
//        if (StringUtils.isEmpty(path))
//            return null;
//
//        return path.replace(getPath("/groups") + "/", "").replace("/" + Configuration.hostname, "");
//    }

    /**
     * 根据数据类型删除节点
     *
     * @param groupId
     * @param value
     * @param type    1=hostname
     *                其它=nodeId
     */
    private void removeNodeFromGroup(String groupId, String value, int type) throws Exception {
        String groupBasePath = getPath("/groups");

        if (!exists(groupBasePath))
            return;

        GetChildrenBuilder childrenBuilder = client.getChildren();

        List<String> children = childrenBuilder.forPath(groupBasePath);
        for (String c : children) {
            logger.debug("group : {}", c);
            //非目标组
            if (StringUtils.isEmpty(groupId) || !groupId.equalsIgnoreCase(c))
                continue;
            String path = getPath("/groups", c);
            List<String> nodes = childrenBuilder.forPath(path);

            for (String node : nodes) {
                String wholePath = getPath("/groups", c, node);

                if (type == 1) {
                    if (value.equals(node)) {
                        client.delete().forPath(wholePath);
                        break;
                    }
                } else {
                    Node nodeData = (Node) SerializeUtil.unserialize(client.getData().forPath(wholePath));
                    if (value.equalsIgnoreCase(nodeData.getNodeId())) {
                        client.delete().forPath(wholePath);
                        break;
                    }
                }
            }
        }
    }

    private boolean exists(String path) throws Exception {
        ExistsBuilder existsBuilder = client.checkExists();
        if (existsBuilder.forPath(path) == null)
            return false;
        return true;
    }

    private boolean touch(String path, boolean createNotExists) throws Exception {
        ExistsBuilder existsBuilder = client.checkExists();
        if (existsBuilder.forPath(path) == null) {
            if (createNotExists) {
                logger.debug("path :{} not exists , need create ", path);
                client.create().forPath(path);
                return true;
            }
            return false;
        }
        return true;
    }


    private String getNodeGroup(String hostname) {
        try {
            GetChildrenBuilder childrenBuilder = client.getChildren();
            String groupBasePath = getPath("/groups");
            touch(groupBasePath, true);
            List<String> children = childrenBuilder.forPath(groupBasePath);
            for (String c : children) {
                logger.debug("group : {}", c);
                String path = getPath("/groups", c);
                List<String> nodes = childrenBuilder.forPath(path);

                for (String node : nodes) {
                    if (hostname.equals(node))
                        return c;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return null;
    }


    private String getNodePathInGroup(String hostname) {
        try {
            GetChildrenBuilder childrenBuilder = client.getChildren();
            String groupBasePath = getPath("/groups");
            List<String> children = childrenBuilder.forPath(groupBasePath);
            for (String c : children) {
                logger.debug("group : {}", c);
                String path = getPath("/groups", c);
                List<String> nodes = childrenBuilder.forPath(path);

                for (String node : nodes) {
                    if (hostname.equals(node))
                        return getPath("/groups", c, hostname);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return null;
    }

//    private void groupNodes(Message<String> message) {
//        String groupId = message.body();
//
//        Map<String, List<Node>> groupNodes = new HashMap<>();
//        String groupBasePath = getPath("/groups");
//
//        try {
//            if (!exists(groupBasePath)) {
//                message.reply(JsonObject.mapFrom(groupNodes));
//                return;
//            }
//
//            GetChildrenBuilder childrenBuilder = client.getChildren();
//            List<String> children = childrenBuilder.forPath(groupBasePath);
//            for (String c : children) {
//                logger.debug("group : {}", c);
//                if(StringUtils.isEmpty(groupId))
//                    continue;
//                String path = getPath("/groups", c);
//                List<String> hosts = childrenBuilder.forPath(path);
//                List<Node> nodes = new ArrayList<>();
//                groupNodes.put(c, nodes);
//                for (String host : hosts) {
//                    String wholePath = getPath("/groups", c, host);
//                    byte[] data = client.getData().forPath(wholePath);
//                    Node node = (Node) SerializeUtil.unserialize(data);
//                    nodes.add(node);
//                }
//            }
//
//            message.reply(JsonObject.mapFrom(groupNodes));
//        } catch (Exception e) {
//            message.fail(500, e.getMessage());
//            e.printStackTrace();
//        } finally {
//        }
//
//    }

}
