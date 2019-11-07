package com.kaitusoft.ratel.console.auth;

import com.kaitusoft.ratel.ContextAttribute;
import com.kaitusoft.ratel.console.model.User;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @author frog.w
 * @version 1.0.0, 2018/12/21
 *          <p>
 *          write description here
 */
public class Authtication implements Handler<RoutingContext> {
    private static final String UNAUTHORIZED = "您不具备操作权限";

    @Override
    public void handle(RoutingContext context) {
        User user = context.session().get(ContextAttribute.SESSION_USER);
        if (user != null) {
            context.setUser(user);
            context.next();
            return;
        }

        String requestPath = context.request().path();
        user.isAuthorized(requestPath, result -> {
            if (result.succeeded()) {
                context.next();
            } else {
                HttpServerResponse response = context.response();
                response.end(UNAUTHORIZED);
            }
        });
    }
}
