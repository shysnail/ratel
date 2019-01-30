package com.kaitusoft.ratel;

import com.kaitusoft.ratel.cluster.ClusterVerticle;
import com.kaitusoft.ratel.core.MainVerticle;
import com.kaitusoft.ratel.core.common.Configuration;
import com.kaitusoft.ratel.core.component.Cluster;
import com.kaitusoft.ratel.core.component.Extend;
import com.kaitusoft.ratel.core.component.Monitor;
import com.kaitusoft.ratel.util.InetAddressUtil;
import com.kaitusoft.ratel.util.StringUtils;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/11
 *          <p>
 *          write description here
 */
public class Ratel {
    private static Logger logger;
    private Vertx vertx;
    private long startTime;

    private static Ratel ratel;
    private String deployId;
    private Configuration config;
    private volatile boolean restarting = false;
    private volatile boolean running = false;
    private static Thread shutdownHook;


    /**
     * 没什么并发加载类问题，懒汉即可
     * @return
     */
    public synchronized static Ratel getInstance() {
        if (ratel == null) {
            ratel = new Ratel();
        }
        return ratel;
    }

    private Ratel(){
        configLogger();
    }

    public void start(String extraLogMessage){
        startTime = System.currentTimeMillis();
        if(extraLogMessage != null)
            logger.info(extraLogMessage);

        Future start = Future.future();
        start.setHandler(started -> {
            if(start.failed()){
                logger.error("{} start failed!", config.getName(), start.cause());
                try {
                    Thread.sleep(1500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(-2);
            }

            logger.info("{} start up in {} s, mode:{}, version:{}, cluster:{}", config.getName(), String.format("%.2f", (System.currentTimeMillis()-startTime)/1000.0), config.getMode(), config.getVersion(), vertx.isClustered());
            shutdownHook = new ShutdownHook();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        });

        startMainVertx(res -> {
            if(res.succeeded()){
                running = true;
                start.complete();
            }else{
                start.fail(res.cause());
            }
        });
    }

    public void restart(){
        if ((this.restarting) || (!this.running)) {
            return;
        }

        logger.info("Restart gateway inited...");

        restarting = true;
        running = false;

        Thread restarter = new Thread(new Runnable() {
            @Override
            public void run() {
                Ratel.this.halt();
                Ratel.this.logger.info("Restart gateway complete!");
            }
        }, "--== Restarter ==--");

        restarter.start();
    }

    public void halt(){
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        vertx.undeploy(deployId, result-> {
            logger.info("undeploy main verticle: {}", result.succeeded());
//            vertx.close();
            vertx.close(res -> {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
                if(res.succeeded()){
                    logger.debug("main vertx stopped");
                }else{
                    logger.error("cannot stop main vertx", res.cause());
                }
                boolean needRestart = restarting;
                this.restarting = false;
                this.running = false;

                try {
                    if (needRestart) {
                        if (!res.succeeded()) {
                            logger.error("cannot restart server, when main vertx stop failed");
                            return;
                        }

                        Runtime.getRuntime().removeShutdownHook(shutdownHook);
                        shutdownHook = null;

                        System.gc();
                        Thread.sleep(4000L);

                        start("Restarting Server Ratel...");
                    } else {
                        System.exit(0);
                    }
                }catch (Exception e){
                    logger.warn("Error while shutting down the server: " + e.getMessage());;
                }

            });
        });

    }


    private void startMainVertx(Handler<AsyncResult> handler) {
        config = Configuration.load();
        VertxOptions options = new VertxOptions();

        configVertx(config, options);
        Cluster cluster = config.getCluster();
        Future future = Future.future();
        future.setHandler(deply -> {
            JsonObject mainConfig = new JsonObject();
            mainConfig.put("console", config.getConsoleOption());
            mainConfig.put("cluster", config.getClusterOption());
            mainConfig.put("database", config.getDatabaseOption());
            mainConfig.put("system", config.getSystemOption());
            mainConfig.put("name", config.getName());
            mainConfig.put("version", config.getVersion());
            mainConfig.put("mode", config.getMode());
            deployMainVerticle(mainConfig, res -> {
                if(res.succeeded()){
                    handler.handle(Future.succeededFuture());
                }else{
                    handler.handle(Future.failedFuture(res.cause()));
                }
            });
        });
        if(cluster != null && cluster.isEnabled()){
            configCluster(cluster, options);
            Monitor monitor = config.getMonitor();
            if(monitor != null && monitor.isEnabled())
                configMetrics(options);
            Vertx.clusteredVertx(options, result -> {
                if(result.succeeded()) {
                    ClusterVerticle.myNodeId = options.getClusterManager().getNodeID();
                    logger.info("join cluster, my nodeId:{}", ClusterVerticle.myNodeId);
                    System.setProperty("com.kaitusoft.ratel.nodeId", ClusterVerticle.myNodeId);
                }else {
                    logger.warn("config cluster mode, but join cluster failed, cluster ? : ", result.cause());
                }
                if(result.result() == null || !result.result().isClustered()){
                    logger.warn("config cluster mode, but join cluster failed, create single vertx");
                    options.setClustered(false);
                    vertx = Vertx.vertx(options);
                }else
                    vertx = result.result();

                future.complete();
            });
        }else{
            vertx = Vertx.vertx(options);
            future.complete();
        }

    }

    private void configMetrics(VertxOptions options) {
        options.setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
    }

    private void configCluster(Cluster cluster, VertxOptions options) {
        JsonObject configCluster = JsonObject.mapFrom(cluster);
        ClusterManager clusterManager = ClusterVerticle.getClusterManager(configCluster);
        options.setClustered(true);
        options.setClusterManager(clusterManager);
        options.setClusterPort(cluster.getPort());
        if(!StringUtils.isEmpty(cluster.getHost()))
            options.setClusterHost(cluster.getHost());
        else{
            String host = null;
            try {
                host = InetAddressUtil.getDefaultAddressNotLoopback();

            } catch (Exception e) {
                logger.error("无法获取网卡信息", e);
            }
            if(host != null)
                options.setClusterHost(host);
            else{
                options.setClusterHost(InetAddressUtil.getLoopbackAddress());
            }
        }
    }

    private void deployMainVerticle(JsonObject config, Handler<AsyncResult> handler){
        vertx.deployVerticle(MainVerticle.class, new DeploymentOptions().setConfig(config), result -> {
            if(result.succeeded()){
                deployId = result.result();
                handler.handle(Future.succeededFuture());
            }else{
                logger.error("启动 MainVerticle verticle 出错", result.cause());
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    private void configVertx(Configuration config, VertxOptions options) {
        Extend extend = config.getExtend();
        if(extend == null){
            options.setPreferNativeTransport(true);
            return;
        }

        if(extend.getBlockedThreadCheckInterval() > 0)
            options.setBlockedThreadCheckInterval(extend.getBlockedThreadCheckInterval());

        if(extend.getEventLoopPoolSize() != null)
            options.setEventLoopPoolSize(extend.getEventLoopPoolSize());

        if(extend.getWorkerPoolSize() != null)
            options.setWorkerPoolSize(extend.getWorkerPoolSize());

    }

    private void configLogger() {
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");
        System.setProperty("vertx.disableDnsResolver", "true");
        logger = LoggerFactory.getLogger(Startup.class);
    }


    protected class ShutdownHook extends Thread {
        private final Logger log;

        public ShutdownHook() {
            super("ShutdownHook");
            this.log = LoggerFactory.getLogger(getClass());
        }

        @Override
        public void run() {
            this.log
                    .warn("{} is shutting down. The process may take a few seconds...", config.getName());
            log.info("bye");

        }
    }
}
