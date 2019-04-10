package com.kaitusoft.ratel.core.verticle;

import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.common.StatusCode;
import com.kaitusoft.ratel.core.model.App;
import com.kaitusoft.ratel.core.model.po.AppOption;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/16
 *          <p>
 *          write description here
 */
public class DeployVerticle extends AbstractVerticle {

    public static final Map<String, String> DEPLOY_APP = new HashMap<>();
    public static final Map<String, Set<String>> RUNNING_APIS = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DeployVerticle.class);

    @Override
    public void start() throws Exception {
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.RUN_ON_START), this::runOnStart);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.START_APP), this::startApp);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.RESTART_APP), this::restartApp);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.STOP_APP), this::stopApp);

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.START_API), this::startApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.STOP_API), this::stopApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.PAUSE_API), this::pauseApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.RESUME_API), this::resumeApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.RESTART_API), this::restartApi);

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.APP_STATUS), this::appStatus);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.API_STATUS), this::apiStatus);
    }


    @Override
    public void stop() throws Exception {
        logger.info("stop deploy verticle");
        undeployAllApps();
        super.stop();
    }

    private void undeployAllApps(){
        DEPLOY_APP.forEach((k, v) -> {
            vertx.undeploy(v);
        });
        DEPLOY_APP.clear();
        RUNNING_APIS.clear();
    }

    private void appStatus(Message<Void> message) {
        JsonArray runningApps = new JsonArray();
        DEPLOY_APP.forEach((k, v) -> {
            runningApps.add(k);
        });

        message.reply(runningApps);
    }

    private void apiStatus(Message<String> message) {
        String appId = message.body();
        JsonArray runningApis = new JsonArray();
        RUNNING_APIS.forEach((app, apis) -> {
            if(!appId.equalsIgnoreCase(app))
                return;

            apis.forEach(api -> {
                runningApis.add(api);
            });
        });
        message.reply(runningApis);
    }

    private void startApp(Message<String> message) {
        String appId = message.body();
        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), appId, reply -> {
            deploy(reply.result().body(), result -> {
                if (result.succeeded()) {
                    message.reply(result.result());
                } else {
                    message.fail(StatusCode.SYS_ERROR, result.cause().getMessage());
                }
            });
        });
    }

    private void restartApp(Message<String> message) {
        String appId = message.body();

        String deployId = DEPLOY_APP.get(appId);
        vertx.undeploy(deployId, stop -> {
            if (stop.succeeded()) {
                DEPLOY_APP.remove(appId);
                RUNNING_APIS.remove(appId);
                logger.info("停止 app: {} -> ok");

                vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.GET_APP), appId, reply -> {
                    deploy(reply.result().body(), result -> {
                        if (result.succeeded()) {
                            logger.info("重新启动 app: {} -> ok");
                            message.reply(1);
                        } else {
                            logger.error("重新启动 app: {} -> failed!", appId, result.cause());
                            message.fail(StatusCode.SYS_ERROR, result.cause().getMessage());
                        }
                    });
                });

            } else {
                logger.error("停止app: {} -> failed!", appId, stop.cause());
                message.fail(StatusCode.SYS_ERROR, stop.cause().getMessage());
            }
        });
    }


    private void runOnStart(Message<Void> message) {
        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_ALL_APP), null, reply -> {
            List<Future> allFuture = new ArrayList<>();
            JsonArray array = reply.result().body();
            final AtomicInteger successNum = new AtomicInteger(0);
            final AtomicInteger failNum = new AtomicInteger(0);
            array.forEach(obj -> {
                JsonObject appJson = (JsonObject) obj;
                int appId = appJson.getInteger("id");
                int originStat = appJson.getInteger("running");

                if(App.STOPPED == originStat){
                    logger.warn("app:{} 是停止状态，不启动", appId);
                    successNum.incrementAndGet();
                    return;
                }

                Future<Void> future = Future.future(start -> {
                    deploy(appJson, result -> {
                        if (result.succeeded()) {
                            JsonObject success = result.result();
                            logger.info("已启动app:{} - all apis, success:{}, fail:{}", appId, success.getInteger("success"), success.getInteger("fail"));
                            successNum.incrementAndGet();
                            start.complete();
                        } else {
                            logger.error("启动app:{} - app apis -> failed!", appId, result.cause());
                            failNum.incrementAndGet();
                            start.complete();
                        }
                    });
                });
                allFuture.add(future);
            });

            CompositeFuture.all(allFuture).setHandler(result -> {
                if (result.succeeded()) {
                    JsonObject num = new JsonObject();
                    num.put("success", successNum.get());
                    num.put("fail", failNum.get());
                    message.reply(num);
                } else {
                    message.fail(StatusCode.SYS_ERROR, result.cause().getMessage());
                }
            });
        });
    }

    protected void stopApp(Message<String> message) {
        String appId = message.body();

        String deployId = DEPLOY_APP.get(appId);
        vertx.undeploy(deployId, result -> {
            if (result.succeeded()) {
                DEPLOY_APP.remove(appId);
                RUNNING_APIS.remove(appId);

                //设置api状态为停止

                message.reply(1);
                logger.info("停止app: {} -> ok");
            } else {
                logger.error("停止app: {} -> failed!", appId, result.cause());
                message.fail(StatusCode.SYS_ERROR, result.cause().getMessage());
            }
        });
    }

    protected void stopApi(Message<JsonObject> message) {
        JsonObject param = message.body();
        String appId = param.getString("appId");
        String apiIds = param.getString("id");

        vertx.eventBus().send(Event.formatInternalAddress(Event.STOP_API, appId), apiIds, reply -> {
            if (reply.succeeded()) {
                Set apis = RUNNING_APIS.get(appId);
                String[] apiIdArray = apiIds.split(",");
                for (String api : apiIdArray) {
                    apis.remove(api);
                }

                logger.info("stop api:{} -> ok", apiIds);

                message.reply(1);

            } else {
                logger.error("stop api:{} -> failed", apiIds, reply.cause());
                message.reply(reply.cause());
            }

        });
    }

    protected void pauseApi(Message<JsonObject> message) {
        JsonObject param = message.body();
        String appId = param.getString("appId");
        String apiIds = param.getString("id");

        vertx.eventBus().send(Event.formatInternalAddress(Event.PAUSE_API, appId), apiIds, reply -> {
            if (reply.succeeded()) {
//                Set apis = RUNNING_APIS.get(appId);
//                String[] apiIdArray = apiIds.split(",");
//                for (String api : apiIdArray) {
//                    apis.remove(api);
//                }

                logger.info("pause api:{} -> ok", apiIds);

                message.reply(1);

            } else {
                logger.error("pause api:{} -> failed", apiIds, reply.cause());
                message.reply(reply.cause());
            }

        });
    }

    protected void resumeApi(Message<JsonObject> message) {
        JsonObject param = message.body();
        String appId = param.getString("appId");
        String apiIds = param.getString("id");

        vertx.eventBus().send(Event.formatInternalAddress(Event.RESUME_API, appId), apiIds, reply -> {
            if (reply.succeeded()) {
//                Set apis = RUNNING_APIS.get(appId);
//                String[] apiIdArray = apiIds.split(",");
//                for (String api : apiIdArray) {
//                    apis.remove(api);
//                }

                logger.info("reRun api:{} -> ok", apiIds);
                message.reply(1);
            } else {
                logger.error("reRun api:{} -> failed", apiIds, reply.cause());
                message.reply(reply.cause());
            }

        });
    }

    protected void restartApi(Message<JsonObject> message) {
        JsonObject param = message.body();
        String appId = param.getString("appId");
        String apiIds = param.getString("id");

        vertx.eventBus().send(Event.formatInternalAddress(Event.STOP_API, appId), apiIds, stop -> {
            if (stop.succeeded()) {
                Set apis = RUNNING_APIS.get(appId);
                String[] apiIdArray = apiIds.split(",");
                String stopApiIds = "";
                for (String api : apiIdArray) {
                    apis.remove(api);
                    stopApiIds += api + ",";
                }

                String[] ids = stopApiIds.split(",");
                JsonObject result = new JsonObject();
                int apiTotalNum = ids.length;
                final AtomicInteger success = new AtomicInteger(0);
                final AtomicInteger fail = new AtomicInteger(0);
                vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_API_COND), new JsonObject().put("appId", Integer.valueOf(appId)).put("ids", apiIds), reply -> {
                    if (!reply.succeeded()) {
                        logger.error("重新启动 app:{}-api:{}, 查询api基础配置出错", appId, apiIds, reply.cause());
                        message.fail(StatusCode.SYS_ERROR, reply.cause().getMessage());
                        return;
                    }

                    JsonArray array = reply.result().body();
                    array.forEach(obj -> {
                        JsonObject jsonObject = (JsonObject) obj;
                        startAppApi(jsonObject.getInteger("appId"), jsonObject, startResult -> {
                            if (startResult.succeeded()) {
                                success.getAndIncrement();
                                logger.info("重新启动 app:{}-api:{} -> ok", appId, jsonObject.getInteger("id"));
                            } else {
                                fail.getAndIncrement();
                                logger.error("重新启动 app:{}-api:{} -> failed", appId, jsonObject.getInteger("id"), startResult.cause());
                            }

                            if (success.get() + fail.get() == apiTotalNum) {
                                result.put("success", success.get());
                                result.put("fail", fail.get());
                                message.reply(result);
                            }
                        });
                    });

                });

                logger.info("stop api:{} -> ok", apiIds);

            } else {
                logger.error("stop api:{} -> failed", apiIds, stop.cause());
                message.fail(StatusCode.SYS_ERROR, stop.cause().getMessage());
            }

        });
    }

    protected void startApi(Message<JsonObject> message) {
        JsonObject param = message.body();
        String appId = param.getString("appId");
        String apiIds = param.getString("id");
        String[] ids = apiIds.split(",");
        JsonObject result = new JsonObject();
        int apiTotalNum = ids.length;
        final AtomicInteger success = new AtomicInteger(0);
        final AtomicInteger fail = new AtomicInteger(0);
        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_API_COND), new JsonObject().put("appId", Integer.valueOf(appId)).put("ids", apiIds), reply -> {
            if (!reply.succeeded()) {
                logger.error("启动 app:{}-api:{}, 查询api基础配置出错", appId, apiIds, reply.cause());
                message.fail(StatusCode.SYS_ERROR, reply.cause().getMessage());
                return;
            }

            JsonArray array = reply.result().body();
            array.forEach(obj -> {
                JsonObject jsonObject = (JsonObject) obj;
                startAppApi(jsonObject.getInteger("appId"), jsonObject, startResult -> {
                    if (startResult.succeeded()) {
                        success.getAndIncrement();
                        logger.info("启动 app:{}-api:{} -> ok", appId, jsonObject.getInteger("id"));
                    } else {
                        fail.getAndIncrement();
                        logger.error("启动 app:{}-api:{} -> failed", appId, jsonObject.getInteger("id"), startResult.cause());
                    }

                    if (success.get() + fail.get() == apiTotalNum) {
                        result.put("success", success.get());
                        result.put("fail", fail.get());
                        message.reply(result);
                    }
                });
            });

        });
    }

    private void deploy(JsonObject object, Handler<AsyncResult<JsonObject>> handler) {
        DeploymentOptions options = new DeploymentOptions();
        AppOption appOption = object.mapTo(AppOption.class);

        VertxOptions vertxOptions = new VertxOptions();
        DropwizardMetricsOptions metricsOptions = new DropwizardMetricsOptions();
        try {
            App app = new App(appOption);
            options.setWorkerPoolSize(app.getProxyOption().getMaxPoolSize());
            metricsOptions.setRegistryName("APP_" + app.getId());

        } catch (Exception e) {
            logger.error("can't deploy app {}, parse for pool error", appOption, e);
        }

        metricsOptions.setBaseName(metricsOptions.getRegistryName());
        metricsOptions.setEnabled(true);
        vertxOptions.setMetricsOptions(metricsOptions);

        vertx.deployVerticle(Application.class, new DeploymentOptions(options).setConfig(object), res -> {
            if (!res.succeeded()) {
                logger.error("启动应用网关:{} -> failed! :", object.getString("name"), res.cause().getMessage());
            } else {
                String deployId = res.result();
                DEPLOY_APP.put(object.getInteger("id").toString(), deployId);
                startAppAllApi(object.getInteger("id"), deployApiResult -> {
                    if (deployApiResult.succeeded()) {
                        JsonObject result = deployApiResult.result();
                        handler.handle(Future.succeededFuture(result));
                        logger.info("启动应用网关:{} -> ok, api success: {}, fail:{}", object.getString("name"), result.getInteger("success"), result.getInteger("fail"));
                    } else {
                        handler.handle(Future.failedFuture(deployApiResult.cause()));
                        logger.error("启动应用网关:{} -> failed! ", object.getString("name"), deployApiResult.cause());
                    }
                });
            }
        });
    }

    private void startAppAllApi(Integer id, Handler<AsyncResult<JsonObject>> handler) {
        vertx.eventBus().<JsonArray>send(Event.formatInternalAddress(Event.FIND_API), id.toString(), reply -> {
            if (reply.succeeded()) {
                JsonArray array = reply.result().body();
                JsonObject result = new JsonObject();
                final AtomicInteger success = new AtomicInteger(0);
                final AtomicInteger fail = new AtomicInteger(0);
                if (array == null || array.size() == 0) {
                    result.put("success", success.get());
                    result.put("fail", fail.get());
                    handler.handle(Future.succeededFuture(result));
                }
                int apiTotalNum = array.size();
                array.forEach(obj -> {
                    JsonObject apiJson = (JsonObject) obj;
                    startAppApi(id, apiJson, res -> {
                        if (res.succeeded()) {
                            success.incrementAndGet();
                        } else {
                            fail.incrementAndGet();
                        }

                        //所有的api都发布了
                        if ((success.get() + fail.get()) == apiTotalNum) {
                            result.put("success", success.get());
                            result.put("fail", fail.get());
                            handler.handle(Future.succeededFuture(result));
                        }
                    });
                });
            } else {
                logger.error("启动所有api时，获取 app:{} 所有api 出错", id, reply.cause());
                handler.handle(Future.failedFuture(reply.cause()));
            }
        });
    }

    /**
     * 启动某个api
     * 判断是否已启动
     * @param appId
     * @param obj
     * @param handler
     */
    private synchronized void startAppApi(Integer appId, JsonObject obj, Handler<AsyncResult<Boolean>> handler) {
        String apiId = obj.getInteger("id").toString();

        Set<String> runningApis = RUNNING_APIS.get(appId);
        if (runningApis != null && runningApis.contains(apiId)) {
            logger.debug("deploy app:{}-api:{} -> already running", appId, apiId);
            handler.handle(Future.succeededFuture());
            return;
        }
        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.START_API, appId), obj, apiReply -> {
            if (apiReply.succeeded()) {
                Set<String> apis = RUNNING_APIS.get(appId.toString());
                if (apis == null) {
                    apis = new HashSet<>();
                    RUNNING_APIS.put(appId.toString(), apis);
                }
                apis.add(apiId);

                logger.debug("deploy app:{}-api:{} -> ok", appId, apiId);

                handler.handle(Future.succeededFuture());
            } else {
                logger.error("deploy app:{}-api:{} -> failed!", appId, apiId, apiReply.cause());

                handler.handle(Future.failedFuture(apiReply.cause()));
            }

        });
    }

}
