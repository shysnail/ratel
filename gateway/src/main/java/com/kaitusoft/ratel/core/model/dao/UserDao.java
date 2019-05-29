package com.kaitusoft.ratel.core.model.dao;

import com.kaitusoft.ratel.core.common.StatusCode;
import com.kaitusoft.ratel.util.StringUtils;
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
public class UserDao extends BaseDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    private static final String SQL_SYS_USER_GET = "select * from sys_user where account=?";
    private static final String SQL_USER_FIND = "select * from sys_user where 1=1 ";
    private static final String SQL_USER_COUNT_EXISTS = "select count(1) from sys_user where account=? and id!=?";
    private static final String SQL_USER_ADD = "insert into sys_user(account, password, name, email, role, department)" +
            " values(?,?,?,?,?,?)";
    private static final String SQL_USER_UPDATE = "update sys_user set name=?, role=?, department=? where account=?";
    private static final String SQL_USER_UPDATE_PASSWORD = "update sys_user set password=? where account=?";
    private static final String SQL_USER_FROZEN = "update sys_user set is_locked_out=? where account=?";

    public UserDao(JDBCClient jdbcClient) {
        super(jdbcClient);
    }

    public void get(Message<String> message) {
        String username = message.body();

        jdbcClient.queryWithParams(SQL_SYS_USER_GET, new JsonArray().add(username), res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                if (rows == null || rows.size() == 0) {
                    logger.debug("getSysUser:{}, not exists", username);
                    message.fail(StatusCode.NOT_FOUND, "用户 " + username + " 不存在");
                    return;
                }

                logger.debug("getSysUser:{} -> {}", username, rows.get(0));

                JsonObject va = rows.get(0);
                va.put("createTime", va.remove("create_time"));
                Integer lock = va.getInteger("is_locked_out");
                va.put("lockedOut", lock != null && lock == 1);
                va.remove("is_locked_out");
                va.put("lastLoginTime", va.remove("last_login_time"));
//                va.remove("password");
                message.reply(va);
            } else {
                logger.error("getSysUser: -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void find(Message<JsonObject> message) {
        jdbcClient.queryWithParams(SQL_USER_FIND, new JsonArray(), res -> {
            if (res.succeeded()) {
                List<JsonObject> rows = res.result().getRows();
                JsonArray jsonArray = new JsonArray(); //如果传递对象，需要另外实现编解码，所以懒省事用已有的支持的类型来交互
                if (rows != null && rows.size() > 0) {
                    rows.forEach(va -> {
                        va.put("createTime", va.remove("create_time"));
                        va.put("lastLoginTime", va.remove("last_login_time"));
                        Integer lock = va.getInteger("is_locked_out");
                        va.put("lockedOut", lock != null && lock == 1);
                        va.remove("is_locked_out");
//                        va.remove("password");
                        jsonArray.add(va);
                    });
                }
                message.reply(jsonArray);
            } else {
                logger.error("findUser: -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void add(Message<JsonObject> message) {
        JsonObject data = message.body();
        String account = data.getString("account");
        String id = data.getString("id");
        if (StringUtils.isEmpty(id))
            id = "-1";

        jdbcClient.queryWithParams(SQL_USER_COUNT_EXISTS, new JsonArray().add(account).add(Integer.parseInt(id)), res -> {
            if (res.succeeded()) {
                Object value = res.result().getResults().get(0).getValue(0);
                int count = ((Number) value).intValue();
                if (count > 0) {
                    logger.error("账号重复:{}", account);
                    message.fail(StatusCode.SYS_ERROR, "账号重复：" + account);
                } else {
                    JsonArray params = new JsonArray();
                    params.add(account);
                    params.add(data.getString("password"));
                    params.add(data.getString("name"));
                    params.add(data.getString("email"));
                    params.add(data.getString("role"));
                    params.add(data.getString("department"));
                    jdbcClient.updateWithParams(SQL_USER_ADD, params, addRes -> {
                        if (addRes.succeeded()) {
                            logger.debug("addUser:{} -> ok", account);
                            message.reply(addRes.result().getUpdated());
                        } else {
                            logger.error("addUser:{} -> failed", account);
                            message.fail(StatusCode.SYS_ERROR, addRes.cause().toString());
                        }
                    });
                }

            } else {
                logger.error("addUser: -> failed", res.cause());
                message.fail(StatusCode.SYS_ERROR, res.cause().toString());
            }
        });
    }

    public void update(Message<JsonObject> message) {
        JsonObject data = message.body();
        String name = data.getString("name");
        String account = data.getString("account");
        if (StringUtils.isEmpty(account)) {
            logger.error("缺少数据标识");
            message.fail(StatusCode.SYS_ERROR, "缺少数据标识");
            return;
        }

        JsonArray params = new JsonArray();
        params.add(name);
        params.add(data.getString("role"));
        params.add(data.getString("department"));
        params.add(account);
        jdbcClient.updateWithParams(SQL_USER_UPDATE, params, addRes -> {
            if (addRes.succeeded()) {
                logger.debug("updateUser:{} -> ok", account);
                message.reply(addRes.result().getUpdated());
            } else {
                logger.error("updateUser:{} -> failed", account);
                message.fail(StatusCode.SYS_ERROR, addRes.cause().toString());
            }
        });
    }

    public void updatePassword(Message<JsonObject> message) {
        JsonObject data = message.body();
        String password = data.getString("newPassword");
        String account = data.getString("account");
        if (StringUtils.isEmpty(account)) {
            logger.error("缺少数据标识");
            message.fail(StatusCode.SYS_ERROR, "缺少数据标识");
            return;
        }

        jdbcClient.updateWithParams(SQL_USER_UPDATE_PASSWORD, new JsonArray().add(password).add(account), addRes -> {
            if (addRes.succeeded()) {
                logger.debug("updatePassword:{} -> ok", account);
                message.reply(addRes.result().getUpdated());
            } else {
                logger.error("updatePassword:{} -> failed", account);
                message.fail(StatusCode.SYS_ERROR, addRes.cause().toString());
            }
        });
    }

    public void frozen(Message<JsonObject> message) {
        JsonObject data = message.body();
        String account = data.getString("account");
        int lock = data.getInteger("lock");
        if (StringUtils.isEmpty(account)) {
            logger.error("缺少数据标识");
            message.fail(StatusCode.SYS_ERROR, "缺少数据标识");
            return;
        }

        JsonArray params = new JsonArray();
        params.add(lock);
        params.add(account);
        jdbcClient.updateWithParams(SQL_USER_FROZEN, params, addRes -> {
            if (addRes.succeeded()) {
                logger.debug("frozen:{} -> ok", account);
                message.reply(addRes.result().getUpdated());
            } else {
                logger.error("frozen:{} -> failed", account);
                message.fail(StatusCode.SYS_ERROR, addRes.cause().toString());
            }
        });
    }

}