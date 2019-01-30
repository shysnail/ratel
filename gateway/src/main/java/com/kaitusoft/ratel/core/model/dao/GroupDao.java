package com.kaitusoft.ratel.core.model.dao;

import com.kaitusoft.ratel.core.common.StatusCode;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/8
 *          <p>
 *          write description here
 */
public class GroupDao extends BaseDao{

    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

    public GroupDao(JDBCClient jdbcClient) {
        super(jdbcClient);
    }

    private static final String SQL_GROUP_ALL = "select * from groups";
    private static final String SQL_GROUP_FIND_IDS = "select * from groups where id in (?)";
    private static final String SQL_GROUP_DELETE = "delete from groups where id in (?)";
    private static final String SQL_GROUP_COUNT_EXISTS = "select count(1) from groups where name=? and id!=?";
    private static final String SQL_GROUP_UPDATE = "update groups set name=?, description=? where id=?";
    private static final String SQL_GROUP_ADD = "insert into groups(name, description) values(?,?)";
    private static final String SQL_GROUP_GET = "select * from groups where id=?";

    public void findGroups(Message<String> message) {
        jdbcClient.query(SQL_GROUP_ALL, res -> {
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
                logger.error("findGroups -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });

    }

    public void findGroupByIds(Message<String> message) {
        String ids = message.body();
        String[] idArray = ids.split(",");
        StringBuilder placeholder = new StringBuilder();
        JsonArray sqlParam = new JsonArray();
        for (String id : idArray) {
            placeholder.append("?,");
            sqlParam.add(Integer.parseInt(id));
        }
        placeholder.deleteCharAt(placeholder.length() - 1);
        jdbcClient.queryWithParams(SQL_GROUP_FIND_IDS.replace("?", placeholder), sqlParam, res -> {
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
                logger.error("findGroups -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });

    }

    public void deleteGroup(Message<String> message) {
        String ids = message.body();
        String[] idArray = ids.split(",");
        StringBuilder placeholder = new StringBuilder();
        JsonArray sqlParam = new JsonArray();
        for (String id : idArray) {
            placeholder.append("?,");
            sqlParam.add(Integer.parseInt(id));
        }
        placeholder.deleteCharAt(placeholder.length() - 1);
        jdbcClient.updateWithParams(SQL_GROUP_DELETE.replace("?", placeholder), sqlParam, res -> {
            if (res.succeeded()) {
                logger.debug("deleteGroup:{} -> ok", ids);
                message.reply(res.result().getUpdated());
            } else {
                logger.error("deleteGroup:{} -> failed", ids);
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void updateGroup(Message<JsonObject> message) {
        int id = message.body().getInteger("id");
        String name = message.body().getString("name", id + "");
        String description = message.body().getString("description", name);
        jdbcClient.queryWithParams(SQL_GROUP_COUNT_EXISTS, new JsonArray().add(name).add(id), res -> {
            Object value = res.result().getResults().get(0).getValue(0);
            int count = ((Number) value).intValue();
            if (count > 0) {
                logger.error("名称不能重复:{}", name);
                message.fail(StatusCode.SYS_ERROR, "名称不能重复:" + name);
                return;
            }

            JsonArray params = new JsonArray();
            params.add(name);
            params.add(description);
            params.add(id);
            jdbcClient.updateWithParams(SQL_GROUP_UPDATE, params, addRes -> {
                if (addRes.succeeded()) {
                    logger.debug("updateGroup:{} -> ok", id);
                    message.reply(addRes.result().getUpdated());
                } else {
                    logger.error("updateGroup:{} -> failed", id);
                    message.fail(StatusCode.SYS_ERROR, addRes.cause().toString());
                }
            });

        });

    }

    public void addGroup(Message<JsonObject> message) {
        String name = message.body().getString("name");
        String description = message.body().getString("description", name);

        jdbcClient.queryWithParams(SQL_GROUP_COUNT_EXISTS, new JsonArray().add(name).add(-1), res -> {
            Object value = res.result().getResults().get(0).getValue(0);
            int count = ((Number) value).intValue();
            if (count > 0) {
                logger.error("名称不能重复:{}", name);
                message.fail(StatusCode.SYS_ERROR, "名称不能重复:" + name);
                return;
            }

            JsonArray params = new JsonArray();
            params.add(name);
            params.add(description);
            jdbcClient.updateWithParams(SQL_GROUP_ADD, params, addRes -> {
                if (addRes.succeeded()) {
                    logger.debug("addGroup:{} -> ok", name);
                    message.reply(addRes.result().getUpdated());
                } else {
                    logger.error("addGroup:{} -> failed", name);
                    message.fail(StatusCode.SYS_ERROR, addRes.cause().toString());
                }
            });

        });

    }

    public void getGroup(Message<String> message) {
        Integer id = Integer.parseInt(message.body());

        jdbcClient.queryWithParams(SQL_GROUP_GET, new JsonArray().add(id), res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                if (rows == null || rows.size() == 0) {
                    logger.debug("getGroup:{}, not exists", id);
                    return;
                }

                logger.debug("getGroup:{} -> {}", id, rows.get(0));

                JsonObject va = rows.get(0);
                va.put("createTime", va.remove("create_time"));
                message.reply(va);
            } else {
                logger.error("getGroup: -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }
}
