package com.kaitusoft.ratel.core.verticle;

import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.model.dao.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/16
 *          <p>
 *          write description here
 */
public class PersistVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(PersistVerticle.class);


    private JDBCClient jdbcClient;

    private AppDao appDao;
    private ApiDao apiDao;
    private GroupDao groupDao;
    private MonitorDao monitorDao;
    private UserDao userDao;

    @Override
    public void start(Future<Void> future) throws Exception {
        JsonObject dbConfig = config();

        try {
            jdbcClient = JDBCClient.createShared(vertx, dbConfig);
            logger.debug("datasource inited ,use component: {}", dbConfig);
        } catch (Exception e) {
            logger.debug("create datasource failed", e);
            future.fail(e);
        }
        appDao = new AppDao(jdbcClient);
        apiDao = new ApiDao(jdbcClient);
        groupDao = new GroupDao(jdbcClient);
        monitorDao = new MonitorDao(jdbcClient);
        userDao = new UserDao(jdbcClient);

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.USER_GET), userDao::get);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.USER_FIND), userDao::find);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.USER_ADD), userDao::add);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.USER_UPDATE), userDao::update);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.USER_UPDATE_PASSWORD), userDao::updatePassword);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.USER_FROZEN), userDao::frozen);

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.FIND_ALL_APP), appDao::findApps);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.FIND_APP_COND), appDao::findAppsByCondition);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.GET_APP), appDao::getApp);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.ADD_APP), appDao::addApp);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.UPDATE_APP), appDao::updateApp);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.UPDATE_APP_PROP), appDao::updateAppProp);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.DELETE_APP), appDao::deleteApp);

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.FIND_ALL_GROUP), groupDao::findGroups);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.FIND_GROUP_IDS), groupDao::findGroupByIds);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.GET_GROUP), groupDao::getGroup);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.ADD_GROUP), groupDao::addGroup);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.UPDATE_GROUP), groupDao::updateGroup);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.DELETE_GROUP), groupDao::deleteGroup);

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.FIND_API), apiDao::findApis);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.FIND_API_COND), apiDao::findApisByCondition);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.GET_API), apiDao::getApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.GET_APP_API), apiDao::getAppApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.ADD_API), apiDao::addApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.UPDATE_API), apiDao::updateApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.UPDATE_API_PROP), apiDao::updateApiProp);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.DELETE_API), apiDao::deleteApi);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.GET_APP_API), apiDao::getAppApi);

        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.STATUS_ADD), monitorDao::addStatus);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.STATUS_FIND), monitorDao::findStatus);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.STATUS_DELETE), monitorDao::deleteStatus);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.APP_STATUS_ADD), monitorDao::addAppStatus);
        vertx.eventBus().localConsumer(Event.formatInternalAddress(Event.APP_STATUS_DELETE), monitorDao::deleteAppStatus);

        future.complete();
    }

    @Override
    public void stop() throws Exception {
        logger.info("stopping persist verticle");
        jdbcClient.close();
        super.stop();
    }
}
