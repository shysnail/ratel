package com.kaitusoft.ratel.core.model.dao;

import com.kaitusoft.ratel.core.common.StatusCode;
import com.kaitusoft.ratel.core.model.po.AppOption;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/8
 *          <p>
 *          write description here
 */
public class AppDao extends BaseDao {

    private static final Logger logger = LoggerFactory.getLogger(AppDao.class);
    private static final String SQL_APP_ALL = "select * from app where 1=1 ";
    private static final String SQL_APP_GET = "select * from app where id=?";
    private static final String SQL_APP_ADD = "insert into app(name, protocol, port, deploy_group, description, parameter, vhost)" +
            " values(?,?,?,?,?,?,?)";
    private static final String SQL_APP_COUNT_EXISTS = "select count(1) from app where port=? and id!=? union all select count(1) from app where name=? and id!=?";
    private static final String SQL_APP_UPDATE = "update app set name=?, protocol=?, port=?, deploy_group=?, description=?, parameter=?, vhost=? where id=?";
    private static final String SQL_APP_DELETE = "delete from app where id=?";
    private static final String SQL_APP_COND = "select * from app where 1=1 ";
    private static final String SQL_APP_UPDATE_PROP = "update app set $CONDTIONS$ where id=?";


    public AppDao(JDBCClient jdbcClient) {
        super(jdbcClient);
    }

    public void findApps(Message<String> message) {
        String ids = message.body();
        JsonArray sqlParam = new JsonArray();
        String sql = SQL_APP_ALL;

        if (!StringUtils.isEmpty(ids)) {
            String[] idArray = ids.split(",");
            StringBuilder placeholder = new StringBuilder();
            for (String id : idArray) {
                placeholder.append("?,");
                sqlParam.add(Integer.parseInt(id));
            }

            if (sqlParam.size() > 0) {
                placeholder.deleteCharAt(placeholder.length() - 1);
                sql += " and id in (" + placeholder + ")";
            }
        }

        jdbcClient.queryWithParams(sql, sqlParam, res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                JsonArray jsonArray = new JsonArray(); //如果传递对象，需要另外实现编解码，所以懒省事用已有的支持的类型来交互
                if (rows != null && rows.size() > 0) {
                    rows.forEach(va -> {
                        va.put("createTime", va.remove("create_time"));
                        va.put("deployGroup", va.remove("deploy_group"));
                        jsonArray.add(va);
                    });
                }
                message.reply(jsonArray);
            } else {
                logger.error("findApps -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });

    }

    public void findAppsByCondition(Message<JsonObject> message) {
        JsonObject object = message.body();
        StringBuilder sql = new StringBuilder(SQL_APP_COND);
        JsonArray params = new JsonArray();
        Integer appId = object.getInteger("id", 0);
        if (appId != 0) {
            sql.append("and id=?");
            params.add(appId);
        }

        String ids = object.getString("ids");
        if (!StringUtils.isEmpty(ids)) {
            String[] idArray = ids.split(",");
            StringBuilder placeholder = new StringBuilder();
            for (String id : idArray) {
                placeholder.append("?,");
                params.add(Integer.parseInt(id));
            }
            placeholder.deleteCharAt(placeholder.length() - 1);

            sql.append(" and id in(" + placeholder.toString() + ")");
        }

        String groupId = object.getString("deploy_group");
        if (!StringUtils.isEmpty(groupId)) {
            sql.append(" and deploy_group = ? ");
            params.add(groupId);
        }


        jdbcClient.queryWithParams(sql.toString(), params, res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                JsonArray jsonArray = new JsonArray(); //如果传递对象，需要另外实现编解码，所以懒省事用已有的支持的类型来交互
                if (rows != null && rows.size() > 0) {
                    rows.forEach(va -> {
                        va.put("createTime", va.remove("create_time"));
                        va.put("deployGroup", va.remove("deploy_group"));
                        jsonArray.add(va);
                    });
                }
                message.reply(jsonArray);
            } else {
                logger.error("findAppsByCondition -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void getApp(Message<String> message) {
        Integer id = Integer.parseInt(message.body());
        jdbcClient.queryWithParams(SQL_APP_GET, new JsonArray().add(id), res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                if (rows == null || rows.size() == 0) {
                    logger.debug("getApp:{}, not exists", id);
                    return;
                }

                logger.debug("getApp:{} -> {}", id, rows.get(0));

                JsonObject va = rows.get(0);
                va.put("createTime", va.remove("create_time"));
                va.put("deployGroup", va.remove("deploy_group"));

//                String parameter = (String) va.remove("parameter");
//                JsonObject extendJson = new JsonObject(parameter);
//                AppExtendOption extendOption = extendJson.mapTo(AppExtendOption.class);

//                AppVo appVo = va.mapTo(AppVo.class);
//                appVo.setExtendOption(extendOption);

//                message.reply(appVo, new DeliveryOptions().setCodecName(Configuration.MODEL_CODEC));
                message.reply(va);
            } else {
                logger.error("getApp:{} -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void addApp(Message<AppOption> message) {

        AppOption app = message.body();

        int port = app.getPort();
        String name = app.getName();
        JsonArray countParam = new JsonArray().add(port);
        countParam.add("-1").add(name).add("-1");

        try {
            jdbcClient.queryWithParams(SQL_APP_COUNT_EXISTS, countParam, res -> {
                Object value = res.result().getResults().get(0).getValue(0);
                Object valueName = res.result().getResults().get(1).getValue(0);
                int count = ((Number) value).intValue();
                int countName = ((Number) valueName).intValue();
                if (count > 0) {
                    /*
                 * 启用端口合并，vhost和路径区分请求，此处调整为非阻塞
                 * 2019-05-27
                 */
                    logger.info("使用已存在端口:{}", port);
//                    message.fail(StatusCode.SYS_ERROR, "端口已被占用");
                } else if (countName > 0) {
                    logger.error("应用名字重复:{}", name);
                    message.fail(StatusCode.SYS_ERROR, "应用名字重复：" + name);
                    return;
                }

                JsonArray params = new JsonArray();
                params.add(name);
                params.add(app.getProtocol().toString());
                params.add(port);
                params.add(app.getDeployGroup() == null ? 0 : app.getDeployGroup());
                params.add(app.getDescription());
                params.add(app.getParameter());
                params.add(app.getVhost());
                jdbcClient.updateWithParams(SQL_APP_ADD, params, addRes -> {
                    if (addRes.succeeded()) {
                        logger.debug("addApp:{} -> ok", app);
                        message.reply(addRes.result().getUpdated());
                    } else {
                        logger.error("addApp:{} -> failed", app);
                        message.fail(StatusCode.SYS_ERROR, addRes.cause().toString());
                    }
                });

            });

        } catch (Exception e) {
            logger.error("addApp:{} -> failed", message.body(), e);
            message.fail(StatusCode.SYS_ERROR, e.getMessage());
        }
    }

    public void updateApp(Message<AppOption> message) {
        AppOption app = message.body();

        int port = app.getPort();
        String name = app.getName();
        JsonArray countParam = new JsonArray().add(port);
        countParam.add(app.getId()).add(name).add(app.getId());

        try {
            jdbcClient.queryWithParams(SQL_APP_COUNT_EXISTS, countParam, res -> {
                Object value = res.result().getResults().get(0).getValue(0);
                Object valueName = res.result().getResults().get(1).getValue(0);
                int count = ((Number) value).intValue();
                int countName = ((Number) valueName).intValue();
                /*
                 * 启用端口合并，vhost和路径区分请求，此处调整为非阻塞
                 * 2019-05-27
                 */
                if (count > 0) {
                    logger.info("使用已有端口:{}", port);
//                    message.fail(StatusCode.SYS_ERROR, "端口已被占用");
                } else if (countName > 0) {
                    logger.error("应用名字重复:{}", name);
                    message.fail(StatusCode.SYS_ERROR, "应用名字重复：" + name);
                    return;
                }

                JsonArray params = new JsonArray();
                params.add(name);
                params.add(app.getProtocol().toString());
                params.add(port);
                params.add(app.getDeployGroup() == null ? 0 : app.getDeployGroup());
                params.add(app.getDescription());
                params.add(app.getParameter());
                params.add(app.getVhost());
                params.add(app.getId());
                jdbcClient.updateWithParams(SQL_APP_UPDATE, params, addRes -> {
                    if (addRes.succeeded()) {
                        logger.debug("updateApp:{} -> ok", app);
                        message.reply(addRes.result().getUpdated());
                    } else {
                        logger.error("updateApp:{} -> failed", app);
                        message.fail(StatusCode.SYS_ERROR, addRes.cause().toString());
                    }
                });

            });
        } catch (Exception e) {
            logger.error("updateApp:{} -> failed", message.body(), e);
            message.fail(StatusCode.SYS_ERROR, e.getMessage());
        }

    }

    public void updateAppProp(Message<JsonObject> message) {
        JsonObject params = message.body();

        String appId = params.getString("appId");
        JsonObject prop = params.getJsonObject("prop");

        JsonArray sqlParams = new JsonArray();
        StringBuilder updates = new StringBuilder();
        for (Map.Entry<String, Object> entry : prop) {
            updates.append(entry.getKey() + "=?,");
            sqlParams.add(entry.getValue());
        }
        while (updates.charAt(updates.length() - 1) == ',') {
            updates.deleteCharAt(updates.length() - 1);
        }
        sqlParams.add(appId);

        try {
            jdbcClient.updateWithParams(SQL_APP_UPDATE_PROP.replace("$CONDTIONS$", updates), sqlParams, res -> {
                if (res.succeeded()) {
                    logger.debug("updateAppProp:{} -> ok", appId);
                    message.reply(res.result().getUpdated());
                } else {
                    logger.error("updateAppProp:{} -> failed", appId);
                    message.fail(StatusCode.SYS_ERROR, res.cause().toString());
                }
            });
        } catch (Exception e) {
            logger.error("updateAppProp:{} -> failed", message.body(), e);
            message.fail(StatusCode.SYS_ERROR, e.getMessage());
        }

    }

    public void deleteApp(Message<String> message) {
        Integer id = Integer.parseInt(message.body());
        jdbcClient.updateWithParams(SQL_APP_DELETE, new JsonArray().add(id), res -> {
            if (res.succeeded()) {
                logger.debug("deleteApp:{} -> ok", id);
                message.reply(res.result().getUpdated());
            } else {
                logger.error("deleteApp:{} -> failed", id);
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

}
