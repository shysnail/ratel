package com.kaitusoft.ratel.console.auth;

import com.kaitusoft.ratel.ContextAttribute;
import com.kaitusoft.ratel.console.model.ErrorInfo;
import com.kaitusoft.ratel.console.model.ExecuteResult;
import com.kaitusoft.ratel.console.model.User;
import com.kaitusoft.ratel.util.StringUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author frog.w
 * @version 1.0.0, 2018/12/21
 *          <p>
 *          write description here
 */
public class LoginCheck implements Handler<RoutingContext> {

    /**
     * 排除某些路径，这种方法并不好
     * 因为所有的路径都要验证。
     * 后续调整成加载在相关路径上，排除路径不加载
     */
    private Set<String> excludes = new HashSet<>();

    public LoginCheck() {

    }

    public static LoginCheck create() {
        return new LoginCheck();
    }

    public static LoginCheck create(Collection<String> excludes) {
        LoginCheck check = new LoginCheck();
        check.excludes.addAll(excludes);
        return check;
    }

    public static LoginCheck create(String[] excludes) {
        LoginCheck check = new LoginCheck();
        if (excludes != null) {
            for (String e : excludes) {
                check.excludes.add(e);
            }
        }
        return check;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();
        if (isExclude(request.path())) {
            context.next();
        } else {
            User user = context.session().get(ContextAttribute.SESSION_USER);
            if (user != null) {
                context.setUser(user);
                context.next();
            } else {
                String xhr = request.getHeader("X-Requested-With");
                HttpServerResponse response = context.response();
                if (!StringUtils.isEmpty(xhr)) {
                    response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(Json.encode(new ExecuteResult(false, new ErrorInfo("" + HttpResponseStatus.UNAUTHORIZED.code(), "请先登录再操作"))));
                } else {
                    response.setStatusCode(HttpResponseStatus.FOUND.code()).putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML).putHeader(HttpHeaders.LOCATION, "/login.html").end();
                }
            }
        }
    }


    protected boolean isExclude(String requestPath) {
        if (requestPath.equalsIgnoreCase("/favicon.ico"))
            return true;
        for (String path : excludes) {
            if (StringUtils.isMatch(requestPath, path)) {
                return true;
            }
        }
        return false;
    }
}
