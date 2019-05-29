package com.kaitusoft.ratel.console.verticle;

import com.kaitusoft.ratel.ContextAttribute;
import com.kaitusoft.ratel.console.model.ExecuteResult;
import com.kaitusoft.ratel.console.model.User;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.util.StringUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author frog.w
 * @version 1.0.0, 2019/1/6
 *          <p>
 *          write description here
 */
public class UserAction extends BaseAction {

    private static Logger logger = LoggerFactory.getLogger(UserAction.class);


    protected void login(RoutingContext context) {
        HttpServerRequest request = context.request();
        String username = request.getParam("username");
        String password = request.getParam("password");

        JsonObject param = new JsonObject();
        param.put("username", username);
        HttpServerResponse response = context.response();
        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.USER_GET), username, reply -> {
            if (reply.succeeded()) {
                JsonObject user = reply.result().body();
                String rightPassword = (String) user.remove("password");
                if (user.getBoolean("lockedOut")) {
                    response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(Json.encode(new ExecuteResult(false, "您已被禁用，请联系管理员解除禁用")));
                } else if (rightPassword.equalsIgnoreCase(password)) {
                    //校验信息
                    logger.info("用户登录:{} -> ok", username);
                    User userSession = new User();
                    userSession.setPrincipal(user);
                    context.session().put(ContextAttribute.SESSION_USER, userSession);

                    response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(Json.encode(new ExecuteResult()));
                } else {
                    response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(Json.encode(new ExecuteResult(false, "用户名密码不匹配")));
                }

            } else {
                //可能查库失败或者用户不存在
                logger.error("用户登录:{} -> failed!", username, reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).putHeader("Content-Type", "application/json").end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
            }
        });

    }

    protected void logout(RoutingContext context) {
        context.session().remove(ContextAttribute.SESSION_USER);
        context.removeCookie(ContextAttribute.SESSION_USER);

        HttpServerResponse response = context.response();

        String xhr = context.request().getHeader("X-Requested-With");
        if (!StringUtils.isEmpty(xhr)) {
            response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(Json.encode(new ExecuteResult(true)));
        } else {
            response.setStatusCode(HttpResponseStatus.FOUND.code()).putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML).putHeader(HttpHeaders.LOCATION, "/login.html").end();
        }
    }

    protected void find(RoutingContext context) {
        HttpServerResponse response = context.response();

        context.vertx().eventBus().<JsonArray>send(Event.formatInternalAddress(Event.USER_FIND), null, reply -> {
            if (!reply.succeeded()) {
                logger.error("查找所有用户 -> failed!", reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult(false, reply.cause().getMessage())));
                return;
            }

            JsonArray usersJson = reply.result().body();
            usersJson.forEach(obj -> {
                JsonObject userJson = (JsonObject) obj;
                userJson.remove("password");
            });

            response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .end(Json.encode(usersJson));
        });
    }

    protected void get(RoutingContext context) {
        String account = context.request().getParam("account");
        logger.debug("got user : {}", account);
        HttpServerResponse response = context.response();

        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.USER_GET), account, reply -> {
            if (reply.succeeded()) {
                JsonObject userJson = reply.result().body();
                userJson.remove("password");
                logger.debug("get用户 -> ok");
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(userJson));
            } else {
                String error = reply.cause().getMessage();
                logger.error("get用户 -> failed", reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
            }
        });
    }

    protected void add(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        logger.debug("got user : {}", body);

        HttpServerResponse response = context.response();

        String event = Event.USER_ADD;
        if (body.getValue("id") != null && body.getValue("id").toString().length() > 0) {
            event = Event.USER_UPDATE;
        }

        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(event), body, reply -> {
            if (reply.succeeded()) {
                logger.debug("添加用户 -> ok");
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult("ok")));
            } else {
                String error = reply.cause().getMessage();
                logger.error("添加用户 -> failed", reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
            }
        });
    }

    protected void changePassword(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        User userSession = context.session().get(ContextAttribute.SESSION_USER);
        JsonObject user = userSession.principal();
        String account = user.getString("account");
        logger.debug("got user : {}", body);

        String originPassword = body.getString("originPassword");
        String newPassword = body.getString("newPassword");
        String confirmPassword = (String) body.remove("confirmPassword");

        HttpServerResponse response = context.response();

        if (StringUtils.isEmpty(newPassword)) {
            response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, "新密码不可为空")));
            return;
        }

        if (!newPassword.equalsIgnoreCase(confirmPassword)) {
            response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, "两次输入的密码不相同")));
            return;
        }

        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.USER_GET), account, res -> {
            if (res.succeeded()) {
                JsonObject userJson = res.result().body();
                String rightPassword = userJson.getString("password");

                if (StringUtils.isEmpty(originPassword) || !originPassword.equalsIgnoreCase(rightPassword)) {
                    response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON).end(Json.encode(new ExecuteResult(false, "旧密码不正确")));
                    return;
                }

                body.put("account", account);
                context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.USER_UPDATE_PASSWORD), body, reply -> {
                    if (reply.succeeded()) {
                        logger.debug("changePassword -> ok");
                        response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                                .end(Json.encode(new ExecuteResult("操作已完成")));
                    } else {
                        String error = reply.cause().getMessage();
                        logger.error("changePassword -> failed", reply.cause());
                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
                    }
                });
            } else {
                String error = res.cause().getMessage();
                logger.error("changePassword -> failed", res.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
            }
        });

    }


    protected void frozen(RoutingContext context) {
        String account = context.request().getParam("account");

        logger.debug("got user : {}", account);

        HttpServerResponse response = context.response();
        JsonObject body = new JsonObject();
        body.put("account", account);
        body.put("lock", 1);
        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.USER_FROZEN), body, reply -> {
            if (reply.succeeded()) {
                logger.debug("frozen -> ok");
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult("操作已完成")));
            } else {
                String error = reply.cause().getMessage();
                logger.error("frozen -> failed", reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
            }
        });
    }

    protected void unFrozen(RoutingContext context) {
        String account = context.request().getParam("account");

        logger.debug("got user : {}", account);

        HttpServerResponse response = context.response();
        JsonObject body = new JsonObject();
        body.put("account", account);
        body.put("lock", 0);
        context.vertx().eventBus().<JsonObject>send(Event.formatInternalAddress(Event.USER_FROZEN), body, reply -> {
            if (reply.succeeded()) {
                logger.debug("unFrozen -> ok");
                response.putHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encode(new ExecuteResult("操作已完成")));
            } else {
                String error = reply.cause().getMessage();
                logger.error("unFrozen -> failed", reply.cause());
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(error);
            }
        });
    }
}
