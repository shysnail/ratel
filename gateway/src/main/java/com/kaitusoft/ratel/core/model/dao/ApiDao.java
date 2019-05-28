package com.kaitusoft.ratel.core.model.dao;

import com.kaitusoft.ratel.core.common.StatusCode;
import com.kaitusoft.ratel.core.model.po.ApiOption;
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
public class ApiDao extends BaseDao{
    private static final Logger logger = LoggerFactory.getLogger(ApiDao.class);

    public ApiDao(JDBCClient jdbcClient) {
        super(jdbcClient);
    }

    private static final String SQL_API_ALL = "select * from api where app_id=?";
    private static final String SQL_API_COND = "select * from api where 1=1 ";
    private static final String SQL_API_GET = "select * from api where id=?";
    private static final String SQL_API_ADD = "insert into api(app_id, name, path, parameter, running)" +
            " values(?,?,?,?,?)";
    private static final String SQL_API_COUNT_EXISTS = "select count(1) from api where app_id=? and name=? and id!=? union all select count(1) from api where app_id=? and path=? and id!=?";
    private static final String SQL_API_UPDATE = "update api set name=?, path=?, parameter=?, running=? where id=? and app_id=?";
    private static final String SQL_API_DELETE = "delete from api where id in (?)";

    public void findApis(Message<String> message) {
        Integer appId = Integer.parseInt(message.body());
        jdbcClient.queryWithParams(SQL_API_ALL, new JsonArray().add(appId), res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                JsonArray jsonArray = new JsonArray(); //如果传递对象，需要另外实现编解码，所以懒省事用已有的支持的类型来交互
                if (rows != null && rows.size() > 0) {
                    rows.forEach(va -> {
                        va.put("createTime", va.remove("create_time"));
                        va.put("appId", va.remove("app_id"));
                        jsonArray.add(va);
                    });
                }
                message.reply(jsonArray);
            } else {
                logger.error("findApis -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void findApisByCondition(Message<JsonObject> message) {
        JsonObject object = message.body();
        StringBuilder sql = new StringBuilder(SQL_API_COND);
        JsonArray params = new JsonArray();
        Integer appId = object.getInteger("appId", 0);
        if (appId != 0) {
            sql.append("and app_id=?");
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


        jdbcClient.queryWithParams(sql.toString(), params, res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                JsonArray jsonArray = new JsonArray(); //如果传递对象，需要另外实现编解码，所以懒省事用已有的支持的类型来交互
                if (rows != null && rows.size() > 0) {
                    rows.forEach(va -> {
                        va.put("createTime", va.remove("create_time"));
                        va.put("appId", va.remove("app_id"));
                        jsonArray.add(va);
                    });
                }
                message.reply(jsonArray);
            } else {
                logger.error("findApisByCondition -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void getApi(Message<String> message) {
        Integer id = Integer.parseInt(message.body());
        jdbcClient.queryWithParams(SQL_API_GET, new JsonArray().add(id), res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                if (rows == null || rows.size() == 0) {
                    logger.debug("getApi:{}, not exists", id);
                    return;
                }

                logger.debug("getApi:{} -> {}", id, rows.get(0));

                JsonObject va = rows.get(0);
                va.put("createTime", va.remove("create_time"));
                va.put("appId", va.remove("app_id"));
                message.reply(va);
            } else {
                logger.error("getApi:{} -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }


    private static final String SQL_APP_API_GET = "select app.port, app.protocol, app.create_time as app_create_time, app.deploy_group, app.name as app_name, app.description ,\n" +
            " app.parameter as app_parameter, app.vhost as app_vhost , app.running as app_running, api.* " +
            " from app as app, api as api where api.id=? and api.app_id=app.id";
    public void getAppApi(Message<String> message) {
        Integer id = Integer.parseInt(message.body());
        jdbcClient.queryWithParams(SQL_APP_API_GET, new JsonArray().add(id), res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                if (rows == null || rows.size() == 0) {
                    logger.debug("getAppApi:{}, not exists", id);
                    return;
                }

                logger.debug("getAppApi:{} -> {}", id, rows.get(0));

                JsonObject va = rows.get(0);
                va.put("appCreateTime", va.remove("app_create_time"));
                va.put("deployGroup", va.remove("deploy_group"));
                va.put("appName", va.remove("app_name"));
                va.put("appParameter", va.remove("app_parameter"));
                va.put("appVhost", va.remove("app_vhost"));
                va.put("appRunning", va.remove("app_running"));
                va.put("createTime", va.remove("create_time"));
                va.put("appId", va.remove("app_id"));
                message.reply(va);
            } else {
                logger.error("getAppApi:{} -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void addApi(Message<ApiOption> message) {
        ApiOption api = message.body();
        String name = api.getName();
        String path = api.getPath();
//        String vhost = api.getVhost();
        int running = api.getRunning();
        int appId = api.getAppId();
        jdbcClient.queryWithParams(SQL_API_COUNT_EXISTS, new JsonArray().add(appId).add(name).add(-1).add(appId).add(path).add(-1), res -> {
            Object value = res.result().getResults().get(0).getValue(0);
            Object valueName = res.result().getResults().get(1).getValue(0);
            int countName = ((Number) value).intValue();
            int countPath = ((Number) valueName).intValue();
            if (countName > 0) {
                logger.error("名称不能重复:{}", name);
                message.fail(StatusCode.SYS_ERROR, "名称不能重复:" + name);
            } else if (countPath > 0) {
                logger.error("路径重复:{}", path);
                message.fail(StatusCode.SYS_ERROR, "路径重复：" + path);
            } else {
                JsonArray params = new JsonArray();
                params.add(appId);
                params.add(name);
                params.add(path);
                params.add(api.getParameter());
                params.add(running);
                jdbcClient.updateWithParams(SQL_API_ADD, params, addRes -> {
                    if (addRes.succeeded()) {
                        logger.debug("app:{} - addApi:{} -> ok", appId, path);
                        message.reply(addRes.result().getUpdated());
                    } else {
                        logger.error("app:{} - addApi:{} -> failed", appId, path);
                        message.fail(StatusCode.SYS_ERROR, addRes.cause().toString());
                    }
                });
            }
        });

    }

    public void updateApi(Message<ApiOption> message) {
        ApiOption api = message.body();
        String name = api.getName();
        String path = api.getPath();
//        String vhost = api.getVhost();
        int running = api.getRunning();
        int appId = api.getAppId();
        int id = api.getId();
        jdbcClient.queryWithParams(SQL_API_COUNT_EXISTS, new JsonArray().add(appId).add(name).add(id).add(appId).add(path).add(id), res -> {
            Object value = res.result().getResults().get(0).getValue(0);
            Object valueName = res.result().getResults().get(1).getValue(0);
            int countName = ((Number) value).intValue();
            int countPath = ((Number) valueName).intValue();
            if (countName > 0) {
                logger.error("名称不能重复:{}", name);
                message.fail(StatusCode.SYS_ERROR, "名称不能重复:" + name);
            } else if (countPath > 0) {
                logger.error("路径重复:{}", path);
                message.fail(StatusCode.SYS_ERROR, "路径重复：" + path);
            } else {
                JsonArray params = new JsonArray();
                params.add(name);
                params.add(path);
                params.add(api.getParameter());
                params.add(running);
                params.add(id);
                params.add(appId);
                jdbcClient.updateWithParams(SQL_API_UPDATE, params, addRes -> {
                    if (addRes.succeeded()) {
                        logger.debug("app:{} - updateApi:{} -> ok", appId, path);
                        message.reply(addRes.result().getUpdated());
                    } else {
                        logger.error("app:{} - updateApi:{} -> failed", appId, path);
                        message.fail(StatusCode.SYS_ERROR, addRes.cause().toString());
                    }
                });
            }

        });
    }

    private static final String SQL_API_UPDATE_PROP = "update api set $CONDTIONS$ where id=? and app_id=?";
    public void updateApiProp(Message<JsonObject> message) {
        JsonObject params = message.body();

        String appId = params.getString("appId");
        String apiId = params.getString("apiId");
        JsonObject prop = params.getJsonObject("prop");

        JsonArray sqlParams = new JsonArray();
        StringBuilder updates = new StringBuilder();
        for (Map.Entry<String, Object> entry : prop) {
            updates.append(entry.getKey() + "=?,");
            sqlParams.add(entry.getValue());

        }
        while (updates.charAt(updates.length() - 1) == ','){
            updates.deleteCharAt(updates.length() - 1);
        }
        sqlParams.add(apiId);
        sqlParams.add(appId);

        try {
            jdbcClient.updateWithParams(SQL_API_UPDATE_PROP.replace("$CONDTIONS$", updates), sqlParams, res -> {
                if (res.succeeded()) {
                    logger.debug("updateApiProp:{}-{} -> ok", appId, apiId);
                    message.reply(res.result().getUpdated());
                } else {
                    logger.error("updateApiProp:{}-{} -> failed", appId, apiId, res.cause());
                    message.fail(StatusCode.SYS_ERROR, res.cause().toString());
                }
            });
        } catch (Exception e) {
            logger.error("updateApiProp:{}-{} -> failed", appId, apiId, e);
            message.fail(StatusCode.SYS_ERROR, e.getMessage());
        }

    }

    public void deleteApi(Message<String> message) {
        String ids = message.body();
        String[] idArray = ids.split(",");
        StringBuilder placeholder = new StringBuilder();
        JsonArray sqlParam = new JsonArray();
        for (String id : idArray) {
            placeholder.append("?,");
            sqlParam.add(Integer.parseInt(id));
        }
        placeholder.deleteCharAt(placeholder.length() - 1);
        jdbcClient.updateWithParams(SQL_API_DELETE.replace("?", placeholder), sqlParam, res -> {
            if (res.succeeded()) {
                logger.debug("deleteApi:{} -> ok", ids);
                message.reply(res.result().getUpdated());
            } else {
                logger.error("deleteApi:{} -> failed", ids);
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }
}
