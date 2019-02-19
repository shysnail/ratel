package com.kaitusoft.ratel.cluster;

import com.kaitusoft.ratel.core.common.Configuration;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.model.vo.Node;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/11
 *          <p>
 *          write description here
 */
public class ClusterVerticle extends AbstractVerticle {
    private static Logger logger = LoggerFactory.getLogger(ClusterVerticle.class);

    public static volatile Integer myGroup = 0;
    public static volatile String myNodeId = "";
    private boolean isCommander;

    public static ClusterMessage clusterMessage;

    private static ClusterManager clusterManager;
    private static GroupNodeManager groupNodeManager;

    private AppApiAction appApiAction;

    public static ClusterManager getClusterManager(JsonObject configCluster){
        if(clusterManager == null){
            clusterManager = new ZookeeperClusterManager(configCluster);
            clusterManager.nodeListener(new NodeListener() {
                @Override
                public void nodeAdded(String s) {
                    logger.debug("node online:{}", s);
                }

                @Override
                public void nodeLeft(String s) {
                    logger.debug("node offline:{}", s);
                    try {
                        groupNodeManager.removeNode(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.debug("node offline:{}, but remove data error!", s, e);
                    }
                }
            });
        }
        return clusterManager;
    }

    public void start(Future future) throws Exception {
        clusterMessage = new ClusterMessage(vertx);
        String deployId = this.deploymentID();
        JsonObject clusterConfig = config();
        groupNodeManager = new GroupNodeManager(vertx, clusterConfig);
        appApiAction = new AppApiAction(vertx);

        isCommander = config().getBoolean("console");

        boolean configCluster = clusterConfig != null && clusterConfig.getBoolean("enabled", false);

        vertx.executeBlocking(cluster -> {
            if(configCluster && vertx.isClustered()) {
                try {
                    groupNodeManager.init();
                } catch (Exception e) {
                    logger.error("cluster verticle init error", e);
                }
            }
            cluster.complete();
        }, other -> {
            registerConsumer();

            if(configCluster && !vertx.isClustered()){
                logger.debug("cluster verticle deploy id:{}, configured cluster mode, but fail join into cluster :{}!", deployId, clusterConfig);
                //配置集群并且未能连接到集群
                future.complete(false);
            }else{
                if(configCluster && vertx.isClustered()){
                    logger.debug("cluster verticle deploy id:{}, in cluster mode", deployId);
                }else{
                    logger.debug("cluster verticle deploy id:{}, not cluster mode", deployId);
                }
                future.complete(true);
            }
        });
    }

    public void stop() throws Exception {
        logger.info("stop cluster verticle");
        if(vertx.isClustered())
            groupNodeManager.destroy();

        clusterManager = null;
    }

    private void registerConsumer() {
        //控制台具备节点管理功能，此处有单点风险，一旦控制台挂了，集群运转不来了

            vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.CLUSTER_NODE_ADD), groupNodeManager::nodeAdd);
            vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.CLUSTER_NODE_OFFLINE), groupNodeManager::nodeLeft);
            vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.CLUSTER_EXPEL_NODE), groupNodeManager::expelNode);

            vertx.eventBus().consumer(Event.formatAddress(Event.CLUSTER_GROUPING), groupNodeManager::grouping);

            if(!vertx.isClustered()) {
                myNodeId = Configuration.hostname;
                Node newNode = new Node();
                newNode.setNodeId(myNodeId);
                newNode.setHostname(myNodeId);
                newNode.setGroupId(myGroup.toString());
                vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.CLUSTER_GET_NODES), message -> {
                    message.reply(new JsonArray().add(JsonObject.mapFrom(newNode)));
                });
                vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.CLUSTER_GET_NODE), message -> {
                    message.reply(JsonObject.mapFrom(newNode));
                });
            }else{
                //本地即可获取
                vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.CLUSTER_GET_NODES), groupNodeManager::nodes);
                vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.CLUSTER_GET_NODE), groupNodeManager::node);

                /*
                 * 接受集群消息
                 */
                vertx.eventBus().consumer(Event.formatAddress(Event.CLUSTER_HALT_NODE, myNodeId), groupNodeManager::haltNode);
                vertx.eventBus().consumer(Event.formatAddress(Event.CLUSTER_RESTART_NODE, myNodeId), groupNodeManager::restartNode);
            }
//            vertx.eventBus().consumer(Event.CLUSTER_GROUP_NODES, this::groupNodes);


//        vertx.eventBus().consumer(Event.CLUSTER_START_APP_ALL, this::startAllApp);
        vertx.eventBus().consumer(Event.formatAddress(Event.START_APP), appApiAction::startApp);
        vertx.eventBus().consumer(Event.formatAddress(Event.STOP_APP), appApiAction::stopApp);
        vertx.eventBus().consumer(Event.formatAddress(Event.RESTART_APP), appApiAction::restartApp);

        vertx.eventBus().consumer(Event.formatAddress(Event.START_API), appApiAction::startApi);
        vertx.eventBus().consumer(Event.formatAddress(Event.STOP_API), appApiAction::stopApi);
        vertx.eventBus().consumer(Event.formatAddress(Event.PAUSE_API), appApiAction::pauseApi);
        vertx.eventBus().consumer(Event.formatAddress(Event.RESUME_API), appApiAction::resumeApi);
        vertx.eventBus().consumer(Event.formatAddress(Event.RESTART_API), appApiAction::restartApi);


        vertx.eventBus().consumer(Event.formatAddress(Event.CLUSTER_NODE_APP_STATUS), appApiAction::appStatus);
        vertx.eventBus().consumer(Event.formatAddress(Event.CLUSTER_NODE_API_STATUS), appApiAction::apiStatus);

        vertx.eventBus().consumer("cluster.test", this::test);

    }




    @Deprecated
    private void consumer(Vertx vertx, Handler<AsyncResult> handler) {

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3, 3000);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1", 60000, 3000, retryPolicy);

        try {
            client.start();
            ExistsBuilder existsBuilder = client.checkExists();
            Stat stat = existsBuilder.forPath("/ratel/clients/" + 111);

            CuratorZookeeperClient c = client.getZookeeperClient();
            ZooKeeper zk = c.getZooKeeper();
            String conn = c.getCurrentConnectionString();
            CuratorFrameworkState ss = client.getState();
            if (stat == null) {
                client.create().forPath("/ratel/clients/" + 111);
            }
            stat = existsBuilder.forPath("/ratel/groups");
            if (stat == null) {
                client.create().forPath("/ratel/groups");
            }

            client.setData().forPath("/ratel/clients", "sdsfsdfsfs".getBytes());
            GetChildrenBuilder getChildrenBuilder = client.getChildren();
            List<String> clients = getChildrenBuilder.forPath("/ratel/clients");
            clients.forEach(ip -> {
                logger.debug("client - ip:{}", ip);
            });
            byte[] data = client.getData().forPath("/ratel/clients");
            String jsons = new String(data);
            logger.debug("get data from zk:{}", jsons);
            handler.handle(Future.<Boolean>succeededFuture());
        } catch (KeeperException.NoNodeException e) {
            logger.error("data failed:", e);
            handler.handle(Future.failedFuture(e));
        } catch (Exception e) {
            handler.handle(Future.failedFuture(e));
        } finally {
            client.close();
        }

    }

    private void test(Message<JsonObject> message) {
        logger.debug("receive message:{}", message.body());
    }


}
