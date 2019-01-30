package com.kaitusoft.ratel.core;

import com.kaitusoft.ratel.cluster.ClusterVerticle;
import com.kaitusoft.ratel.console.verticle.ConsoleVerticle;
import com.kaitusoft.ratel.core.common.Configuration;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.component.ObjectMessageCodec;
import com.kaitusoft.ratel.core.verticle.DeployVerticle;
import com.kaitusoft.ratel.core.verticle.PersistVerticle;
import com.kaitusoft.ratel.core.verticle.SystemVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/10
 *          <p>
 *          write description here
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    private List<String> deplyed;

    public MainVerticle() {
        super();
    }

    @Override
    public void start(Future<Void> startFuture) {
        deplyed = new ArrayList<>();
        Configuration config;

        JsonObject configJson = config();
        JsonObject consoleConfig;
        JsonObject clusterConfig;
        JsonObject databaseConfig;
        JsonObject systemConfig;
        if(configJson.isEmpty()){
            config = Configuration.load();
            clusterConfig = config.getClusterOption();
            consoleConfig = config.getConsoleOption();
            databaseConfig = config.getDatabaseOption();
            systemConfig = config.getSystemOption();
        }else{
            consoleConfig = configJson.getJsonObject("console");
            clusterConfig = configJson.getJsonObject("cluster");
            databaseConfig = configJson.getJsonObject("database");
            systemConfig = configJson.getJsonObject("system");
        }
        final int consolePort = consoleConfig.getInteger("port", 5678);

        vertx.eventBus().registerDefaultCodec(Serializable.class, new ObjectMessageCodec());

        vertx.executeBlocking(clusterFuture -> {
            clusterConfig.put("console", consolePort > 0);
            vertx.deployVerticle(ClusterVerticle.class, new DeploymentOptions().setConfig(clusterConfig), res -> {
                if (res.succeeded()) {
                    deplyed.add(res.result());
                    logger.debug("cluster verticle deployed!");
                } else {
                    logger.error("cluster verticle deployed failed!");
                }

                clusterFuture.complete();
            });
        }, mainFuture -> {
            List<Future> futureList = new ArrayList<>();

            futureList.add(Future.<Void>future(sysInfo -> {
                logger.debug("deploy system verticle ...");
                vertx.deployVerticle(SystemVerticle.class, new DeploymentOptions().setConfig(systemConfig), res -> {
                    if (res.succeeded()) {
                        deplyed.add(res.result());
                        logger.debug("system verticle deployed!");
                    } else {
                        logger.error("system verticle deployed failed!", res.cause());
                    }

                    sysInfo.complete();
                });
            }));

            futureList.add(Future.<Void>future(persist -> {
                logger.debug("deploy persist verticle ...");
                vertx.deployVerticle(PersistVerticle.class, new DeploymentOptions().setConfig(databaseConfig), res -> {
                    if (res.succeeded()) {
                        deplyed.add(res.result());
                        logger.debug("persist verticle deployed!");
                    } else {
                        logger.error("persist verticle deployed failed!");
                    }

                    persist.complete();
                });
            }));

            futureList.add(Future.<Void>future(deploy -> {
                logger.debug("deploy deploy verticle ...");
                vertx.deployVerticle(DeployVerticle.class, new DeploymentOptions(), res -> {
                    if (res.succeeded()) {
                        deplyed.add(res.result());
                        logger.debug("deploy verticle deployed!");
                    } else {
                        logger.error("deploy verticle deployed failed!");
                    }
                    deploy.complete();
                });
            }));

            CompositeFuture.all(futureList).setHandler(res -> {
                if (res.succeeded()) {
                    init();
                    if (consolePort > 0) {
                        vertx.deployVerticle(ConsoleVerticle.class, new DeploymentOptions().setConfig(consoleConfig), console -> {
                            if(console.succeeded()){
                                deplyed.add(console.result());
                                logger.info("已启动控制台:{}", consolePort);
                            }else{
                                logger.error("未能启动控制台", console.cause());
                            }

                            startFuture.complete();
                        });
                    }
                } else {
                    logger.error("启动网关出错！", res.cause());
                    startFuture.fail(res.cause());
                }
            });
        });


    }
    private void init() {
        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.RUN_ON_START), null, reply -> {
            if (reply.succeeded()) {
                logger.debug("发布所有app -> ok");
            } else {
                logger.debug("发布所有app -> failed", reply.cause());
            }
        });

        if(vertx.isClustered()){
            vertx.eventBus().publish(Event.formatInternalAddress(Event.CLUSTER_NODE_ADD), new JsonObject().put("nodeId", ClusterVerticle.myNodeId).put("hostname", Configuration.hostname));
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("stop main verticle");
        deplyed.forEach(id -> {
            vertx.undeploy(id);
        });

        vertx.eventBus().unregisterDefaultCodec(ObjectMessageCodec.class);
    }
}
