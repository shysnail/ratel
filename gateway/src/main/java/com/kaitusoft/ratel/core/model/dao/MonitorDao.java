package com.kaitusoft.ratel.core.model.dao;

import com.kaitusoft.ratel.core.common.StatusCode;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/8
 *          <p>
 *          write description here
 */
public class MonitorDao extends BaseDao {
    private static final Logger logger = LoggerFactory.getLogger(MonitorDao.class);
    private static final String SQL_STATUS_ADD = "insert into sys_status(node, `type`, status) values (?,0,?)";
    private static final String SQL_APP_STATUS_ADD = "insert into sys_app_status(node, app_id, `type`, status) values (?,?,0,?) ";
    private static final String SQL_STATUS_APP_FIND = "select * from sys_app_status where 1=1 ${where} order by create_time desc limit ?";
    private static final String SQL_APP_STATUS_DELETE = "delete from sys_app_status where 1=1 ${where}";
    private static final String SQL_STATUS_FIND = "select * from sys_status where 1=1 ${where} order by create_time desc limit ?";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String SQL_STATUS_DELETE = "delete from sys_status where 1=1 ${where}";
    public MonitorDao(JDBCClient jdbcClient) {
        super(jdbcClient);
    }

    public void addStatus(Message<JsonObject> message) {
        JsonObject body = message.body();
        JsonArray params = new JsonArray();
        params.add(body.remove("node"));
        params.add(body.toString());
        jdbcClient.updateWithParams(SQL_STATUS_ADD, params, res -> {
            if (res.succeeded()) {
                logger.debug("addStatus: -> ok");
                message.reply(res.result().getUpdated());
            } else {
                logger.error("addStatus: -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void addAppStatus(Message<JsonArray> message) {
        JsonArray apps = message.body();

        StringBuilder sb = new StringBuilder("insert into sys_app_status(node, app_id, `type`, status) values ");
        JsonArray params = new JsonArray();

        if (apps == null || apps.size() == 0) {
            message.reply(0);
            return;
        }

        apps.forEach(obj -> {
            sb.append(" (?,?,0,?),");
            JsonObject app = (JsonObject) obj;
            params.add(app.remove("node"));
            params.add(app.remove("app_id"));
            params.add(app.toString());
        });

        while (sb.charAt(sb.length() - 1) == ',')
            sb.deleteCharAt(sb.length() - 1);

        jdbcClient.updateWithParams(sb.toString(), params, res -> {
            if (res.succeeded()) {
                logger.debug("addAppStatus -> ok");
                message.reply(res.result().getUpdated());
            } else {
                logger.error("addAppStatus: -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void findAppStatus(Message<JsonObject> message) {
        String where = "";
        JsonObject body = message.body();
        JsonArray params = new JsonArray();

        int limit = body.getInteger("size", 20);
        String timeline = body.getString("timestamp", null);
        if (!StringUtils.isEmpty(timeline)) {
            where = " and unix_timestamp(create_time) > ?";
            //string date to long
            long fromTime = System.currentTimeMillis() - 60000;
            try {
                Date date = dateFormat.parse(timeline);
                params.add(date.getTime());
            } catch (ParseException e) {
            }
            params.add(fromTime);
        }
        String statusFindSql = SQL_STATUS_APP_FIND.replace("${where}", where);
        params.add(limit);

        jdbcClient.queryWithParams(statusFindSql, params, res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                JsonArray jsonArray = new JsonArray();
                if (rows != null && rows.size() > 0) {
                    rows.forEach(va -> {
                        va.put("createTime", va.remove("create_time"));
                        jsonArray.add(va);
                    });
                }
                message.reply(jsonArray);
            } else {
                logger.error("findAppStatus -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void deleteAppStatus(Message<JsonObject> message) {
        JsonObject body = message.body();
        StringBuilder where = new StringBuilder();
        JsonArray params = new JsonArray();
        if (!StringUtils.isEmpty(body.getString("ids", null))) {
            String ids = body.getString("ids");
            String[] idArray = ids.split(",");
            StringBuilder placeholder = new StringBuilder();
            JsonArray inParam = new JsonArray();
            for (String id : idArray) {
                placeholder.append("?,");
                inParam.add(Integer.parseInt(id));
            }
            placeholder.deleteCharAt(placeholder.length() - 1);
            where.append(" and id in (").append(placeholder).append(")");
            params.add(inParam);
        }

        if (!StringUtils.isEmpty(body.getString("nodeId", null))) {
            where.append(" and node_id = ?");
            params.add(body.getString("nodeId"));
        }

        if (!StringUtils.isEmpty(body.getString("appIds", null))) {
            String ids = body.getString("appIds");
            String[] idArray = ids.split(",");
            StringBuilder placeholder = new StringBuilder();
            JsonArray inParam = new JsonArray();
            for (String id : idArray) {
                placeholder.append("?,");
                inParam.add(Integer.parseInt(id));
            }
            placeholder.deleteCharAt(placeholder.length() - 1);
            where.append(" and app_id in (").append(placeholder).append(")");
            params.add(inParam);
        }

        if (body.getLong("timestamp", 0L) > 0) {
            where.append(" and unix_timestamp(create_time) <= ?");
            params.add(body.getLong("timestamp"));
        }

        String sqlDelete = SQL_STATUS_DELETE.replace("${where}", where);

        jdbcClient.updateWithParams(sqlDelete, params, res -> {
            if (res.succeeded()) {
                logger.debug("deleteAppStatus -> ok + {}", res.result().getUpdated());
                message.reply(res.result().getUpdated());
            } else {
                logger.error("deleteAppStatus -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void findStatus(Message<JsonObject> message) {
        String where = "";
        JsonObject body = message.body();
        JsonArray params = new JsonArray();

        int limit = body.getInteger("size", 20);
        String timeline = body.getString("timestamp", null);
        if (!StringUtils.isEmpty(timeline)) {
            where = " and unix_timestamp(create_time) > ?";
            //string date to long
            long fromTime = System.currentTimeMillis() - 60000;
            try {
                Date date = dateFormat.parse(timeline);
                params.add(date.getTime());
            } catch (ParseException e) {
            }
            params.add(fromTime);
        }
        String statusFindSql = SQL_STATUS_FIND.replace("${where}", where);
        params.add(limit);

        jdbcClient.queryWithParams(statusFindSql, params, res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                JsonArray jsonArray = new JsonArray();
                if (rows != null && rows.size() > 0) {
                    rows.forEach(va -> {
                        va.put("createTime", va.remove("create_time"));
                        jsonArray.add(va);
                    });
                }
                message.reply(jsonArray);
            } else {
                logger.error("findStatus -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void deleteStatus(Message<JsonObject> message) {
        JsonObject body = message.body();
        StringBuilder where = new StringBuilder();
        JsonArray params = new JsonArray();
        if (!StringUtils.isEmpty(body.getString("ids", null))) {
            String ids = body.getString("ids");
            String[] idArray = ids.split(",");
            StringBuilder placeholder = new StringBuilder();
            JsonArray inParam = new JsonArray();
            for (String id : idArray) {
                placeholder.append("?,");
                inParam.add(Integer.parseInt(id));
            }
            placeholder.deleteCharAt(placeholder.length() - 1);
            where.append(" and id in (").append(placeholder).append(")");
            params.add(inParam);
        }

        if (!StringUtils.isEmpty(body.getString("nodeId", null))) {
            where.append(" and node = ?");
            params.add(body.getString("nodeId"));
        }

//        if(StringUtils.isEmpty(body.getString("appIds", null))){
//            String ids = body.getString("appIds");
//            String[] idArray = ids.split(",");
//            StringBuilder placeholder = new StringBuilder();
//            JsonArray inParam = new JsonArray();
//            for(String id : idArray){
//                placeholder.append("?,");
//                inParam.add(Integer.parseInt(id));
//            }
//            placeholder.deleteCharAt(placeholder.length() - 1);
//            where.append(" and app_id in (").append(placeholder).append(")");
//            params.add(inParam);
//        }

        if (body.getLong("timestamp", 0L) > 0) {
            where.append(" and unix_timestamp(create_time) <= ?");
            params.add(body.getLong("timestamp"));
        }

        String sqlDelete = SQL_STATUS_DELETE.replace("${where}", where);

        jdbcClient.updateWithParams(sqlDelete, params, res -> {
            if (res.succeeded()) {
                logger.debug("deleteStatus -> ok + {}", res.result().getUpdated());
                message.reply(res.result().getUpdated());
            } else {
                logger.error("deleteStatus -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }
}