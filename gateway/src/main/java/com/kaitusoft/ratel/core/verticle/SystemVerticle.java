package com.kaitusoft.ratel.core.verticle;

import com.kaitusoft.ratel.cluster.ClusterVerticle;
import com.kaitusoft.ratel.core.common.Env;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.model.*;
import com.kaitusoft.ratel.core.model.po.Group;
import com.kaitusoft.ratel.util.StringUtils;
import com.kaitusoft.ratel.util.URLUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/16
 *          <p>
 *          write description here
 */
public class SystemVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(SystemVerticle.class);
    private static int COLLECT_PERIOD = 120000;
    private static int CLEAN_STATUS_AGE = 24 * 3600 * 7;

//    /** JVM最大内存量 */
//    private long maxMemory = 0;
//
//    /** JVM总的内存量 */
//    private long totalMemory = 0;
//
//    /** JVM的空闲内存量 */
//    private long freeMemory = 0;
//
//    /** 线程数量 */
//    private int threads = 0;
//
//    /**
//     * 异常次数
//     */
//    private AtomicInteger errorCountPeriod = new AtomicInteger();
//
//    /**
//     * 请求到达网关的总次数
//     */
//    private AtomicLong requestCountPeriod = new AtomicLong();
    /**
     * 启动时间
     */
    private LocalDateTime startTime = LocalDateTime.now();
//
//    private Map<Integer, AtomicLong> requestFailedCountMap = new ConcurrentHashMap<>();
    //    private SysStatus status = new SysStatus();
//
    private SysStatus lastStatus = null;
    private Map<Object, AppStatus> appStatusMap = new ConcurrentHashMap<>(16, 0.75f, 4);
    private Map<Object, AppStatus> appStatusLastPeriod = new ConcurrentHashMap<>(16, 0.75f, 4);

    private MetricsService metricsService;

    @Override
    public void start() throws Exception {
        metricsService = MetricsService.create(vertx);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.SYSTEM_INFO_GET), this::getSysInfo);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.INIT_ENV), this::initEnv);

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.ACTION_APP_DEPLOYED), deploy -> {
            JsonObject action = (JsonObject) deploy.body();
            int appId = action.getInteger("app");
            AppStatus appStatus = appStatusMap.get(appId);
            if (appStatus == null) {
                appStatus = new AppStatus();
                appStatus.setId(appId);
                appStatusMap.put(appId, appStatus);
            }
            appStatus.deployed(action.getString("deployId"));
        });

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.ACTION_APP_UNDEPLOYED), deploy -> {
            Integer id = (Integer) deploy.body();
            AppStatus appStatus = appStatusMap.get(id);
            if (appStatus == null) {
                return;
            }

            appStatus.unDeploy();
        });

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.ACTION_REQUEST), newRequest -> {
            JsonObject action = (JsonObject) newRequest.body();
            int appId = action.getInteger("app");
            AppStatus appStatus = appStatusMap.get(appId);
            if (appStatus == null)
                return;

            appStatus.request(action.getBoolean("https", false), appId, action.getString("uri"));
        });
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.ACTION_REQUEST_DONE), requestDone -> {
            JsonObject action = (JsonObject) requestDone.body();
            int appId = action.getInteger("app");
            AppStatus appStatus = appStatusMap.get(appId);
            if (appStatus == null)
                return;

            appStatus.requestDone(appId, action.getString("uri"));
        });
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.ACTION_REQUEST_FAIL), requestFail -> {
            JsonObject action = (JsonObject) requestFail.body();
            int appId = action.getInteger("app");
            AppStatus appStatus = appStatusMap.get(appId);
            if (appStatus == null)
                return;

            appStatus.requestFail(appId, action.getString("uri"));
        });
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.ACTION_REQUEST_ERROR), requestError -> {
            JsonObject action = (JsonObject) requestError.body();
            int appId = action.getInteger("app");
            AppStatus appStatus = appStatusMap.get(appId);
            if (appStatus == null)
                return;

            appStatus.requestFail(appId, action.getString("uri"), true);
        });

        initMonitor();

//        initRetryTask();

        Future.<Void>future(initEnv -> {
            try {
                Env.loadCustomInstance();
            } catch (Exception e) {
                logger.warn("load extend instance error: {}", e.getMessage(), e);
            }
            initEnv.complete();
        });
    }

    @Override
    public void stop() throws Exception {
        logger.info("停止 任务");
        if(retryTaskId >= 0){
            vertx.cancelTimer(retryTaskId);
        }

        if(monitorTaskId >= 0){
            vertx.cancelTimer(monitorTaskId);
        }
        super.stop();
    }

    /**
     * 暂未启用此方法
     */
    private static final Map<Api, Set<Target>> API_DEAD_TARGETS = new ConcurrentHashMap<>();
    private long retryTaskId = -1;
    private void initRetryTask() {
        retryTaskId = vertx.setPeriodic(5000, retry -> {
            if(API_DEAD_TARGETS.size() == 0)
                return;

            logger.debug("retry test urls available");
            API_DEAD_TARGETS.forEach((k, v) -> {
                if (v == null || v.size() == 0)
                    return;

                v.forEach(target -> {
                    logger.debug("retry test url available:{}", target);
                    try {
                        if (URLUtil.useful(target.getUrl())) {
                            k.getProxy().getProxyPolicy().rebirth(target);
                        }
                    } catch (IOException e) {
                        logger.error("重试连接 {} 出错", target.getUrl());
                    }
                });
            });
        });

    }

    private long monitorTaskId = -1;
    private void initMonitor() {
        monitorTaskId = vertx.setPeriodic(COLLECT_PERIOD, collect -> {

            JsonObject metricsEventBus = metricsService.getMetricsSnapshot("vertx.eventbus.message");
            logger.error("event bus:{}", metricsEventBus);

            JsonObject metricsRequest = metricsService.getMetricsSnapshot("vertx.http.servers");
            logger.error("event bus:{}", metricsRequest);

            //每个周期收集信息
            JsonObject period = new JsonObject();

            SysStatus status = new SysStatus();
            status.collectBasic();

            JsonObject system = new JsonObject();
            system.put("total", status.getUsedMemory());
            system.put("free", status.getFreeMemorySize());
            system.put("max", status.getTotalMemorySize());

            system.put("s1", status.getS1());
            system.put("s2", status.getS2());

            system.put("cpu", status.getCpuRatio());
            system.put("threads", status.getTotalThread());
            Map<String, JsonObject> networks = new HashMap<>();
            if (lastStatus != null) {
                Map<String, Network> nowNetwork = status.getNetworks();
                nowNetwork.forEach((k, v) -> {
                    Network last = lastStatus.getNetworks().get(k);
                    if (last == null)
                        return;

                    Network p = new Network();
                    p.setName(k);
                    p.setInBytes(v.getInBytes() - last.getInBytes());
                    p.setOutBytes(v.getOutBytes() - last.getOutBytes());
                    p.setInPackets(v.getInPackets() - last.getInPackets());
                    p.setOutPackets(v.getOutPackets() - last.getOutPackets());
                    networks.put(k, JsonObject.mapFrom(p));
                });
            }

            system.put("netIo", JsonObject.mapFrom(networks));

            period.put("system", system);

            lastStatus = status;

            JsonObject gate = new JsonObject();

            long inBytes = 0L;
            long outBytes = 0;
            long totalRequest = 0;
            long totalError = 0;
            long totalFail = 0;
            long totalHttpsRequest = 0;
            long totalProcessing = 0;

            Set<Map.Entry<Object, AppStatus>> sets = appStatusMap.entrySet();
            Iterator<Map.Entry<Object, AppStatus>> iSets = sets.iterator();

            JsonArray apps = new JsonArray();

            while (iSets.hasNext()) {
                Map.Entry<Object, AppStatus> entry = iSets.next();
                Object k = entry.getKey();
                AppStatus v = entry.getValue();
                AppStatus last = null;
                last = appStatusLastPeriod.get(k);
                if (last == null) {
                    last = new AppStatus(v);
                    appStatusLastPeriod.put(k, last);
                    continue;
                }

                JsonObject app = new JsonObject();

                long pInBytes = v.getInBytes().longValue() - last.getInBytes().longValue();
                inBytes += pInBytes;
                long pOutBytes = v.getOutBytes().longValue() - last.getOutBytes().longValue();
                outBytes += pOutBytes;

                long pRequest = v.getRequestCount().longValue() - last.getRequestCount().longValue();
                totalRequest += pRequest;

                long pError = v.getErrorCount().longValue() - last.getErrorCount().longValue();
                totalError += pError;
                long pFail = v.getFailCount().longValue() - last.getFailCount().longValue();
                totalFail += pFail;

                long pHttpsRequest = v.getHttpsRequestCount().longValue() - last.getHttpsRequestCount().longValue();
                totalHttpsRequest += pHttpsRequest;

                long pProcessing = v.getProcessingCount().longValue();// - last.getProcessingCount().longValue();
                totalProcessing += pProcessing;

                app.put("app_id", k);
                app.put("inBytes", pInBytes);
                app.put("outBytes", pOutBytes);
                app.put("request", pRequest);
                app.put("error", pError);
                app.put("fail", pFail);
                app.put("httpsRequest", pHttpsRequest);
                app.put("processing", pProcessing);
                app.put("node", ClusterVerticle.myNodeId);
                apps.add(app);

                AppStatus lastPeriod = new AppStatus(v);
                appStatusLastPeriod.put(k, lastPeriod);
            }

            gate.put("inBytes", inBytes);
            gate.put("outBytes", outBytes);
            gate.put("request", totalRequest);
            gate.put("error", totalError);
            gate.put("fail", totalFail);
            gate.put("httpsRequest", totalHttpsRequest);
            gate.put("processing", totalProcessing);

            period.put("gate", gate);

            period.put("node", ClusterVerticle.myNodeId);
            vertx.eventBus().<Object>send(Event.formatInternalAddress(Event.STATUS_ADD), period, reply -> {
                logger.debug("save status -> {}", reply.succeeded());
                if (!reply.succeeded()) {
                    logger.error("save status failed", reply.cause());
                }
            });

            if(apps.size() > 0)
            vertx.eventBus().<Object>send(Event.formatInternalAddress(Event.APP_STATUS_ADD), apps, reply -> {
                logger.debug("save apps status -> {}", reply.succeeded());
                if (!reply.succeeded()) {
                    logger.error("save apps status failed", reply.cause());
                }
            });

            long cleanTimestamp = System.currentTimeMillis() / 1000 - CLEAN_STATUS_AGE;
            JsonObject cleanParam = new JsonObject();
            cleanParam.put("timestamp", cleanTimestamp);
            vertx.eventBus().<Object>send(Event.formatInternalAddress(Event.STATUS_DELETE), cleanParam, reply -> {
                logger.debug("clean status -> {}", reply.succeeded());
                if (reply.failed()) {
                    logger.error("clean status failed", reply.cause());
                }
            });
            vertx.eventBus().<Object>send(Event.formatInternalAddress(Event.APP_STATUS_DELETE), cleanParam, reply -> {
                logger.debug("clean app status -> {}", reply.succeeded());
                if (reply.failed()) {
                    logger.error("clean app status failed", reply.cause());
                }
            });

        });
    }

    /**
     * 新请求
     * @param message
     */
//    private void newRequest(Message<JsonObject> message){
//        JsonObject action = (JsonObject) message.body();
//        int appId = action.getInteger("app");
//        AppStatus appStatus = appStatusMap.get(appId);
//        if(appStatus == null)
//            return;
//
//        appStatus.request(appId, action.getString("uri"));
//    }

    /**
     * 处理完成请求
     *
     * @param message
     */
    private void requestDone(Message<Enum> message) {

    }


    private void getSysInfo(Message<JsonObject> message) {
//        // 获取在线网关应用与API的数量
//        Future<JsonObject> countResult = Future.future();

        JsonObject body = message.body();
        String node = body.getString("node", ClusterVerticle.myNodeId);
        body.put("node", node);
        String app = body.getString("app", null);
        String event = Event.formatInternalAddress(Event.STATUS_FIND);
        if (!StringUtils.isEmpty(app))
            event = Event.formatInternalAddress(Event.APP_STATUS_FIND);
        Duration duration = Duration.between(startTime, LocalDateTime.now());
        JsonObject data = new JsonObject();
        data.put("runtimeSimple", StringUtils.toReadableTime(duration.toMillis()));
        data.put("runtime", duration.toMillis() / 1000);
        vertx.eventBus().<JsonArray>send(event, body, reply -> {
            if (reply.succeeded()) {
                logger.debug("get status ok:");
                data.put("nodeInfo", reply.result().body());
            } else {
                logger.debug("get status failed:");
            }

            message.reply(data);

        });

    }

    private void initEnv(Message<String> message) {
        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_ALL_GROUP), null, reply -> {
            if (reply.succeeded()) {
                JsonArray body = reply.result().body();
                Env.groups = toGroup(body);
                message.reply(body.size());
            } else {
                logger.error("获取所有组失败");
                message.fail(500, reply.cause().getMessage());
            }
        });
    }

    private Set<Group> toGroup(JsonArray array) {
        Set<Group> groups = new HashSet<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.getJsonObject(i);
            groups.add(obj.mapTo(Group.class));
        }

        return groups;
    }
}
