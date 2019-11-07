package com.kaitusoft.ratel.console.auth;

import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.HashSet;
import java.util.Set;

/**
 * @author frog.w
 * @version 1.0.0, 2018/12/21
 *          <p>
 *          write description here
 */
public abstract class HandlerInterceptor implements Handler<RoutingContext> {

    private Set<String> excludes = new HashSet<>();

    public HandlerInterceptor() {

    }

    abstract boolean preHandle(RoutingContext context, HttpServerRequest request, HttpServerResponse response);

//    abstract void postHandle(RoutingContext context, HttpServerRequest request, HttpServerResponse response, Object var3);
//
//    abstract void afterCompletion(RoutingContext context, HttpServerRequest request, HttpServerResponse response, Object var3, Exception e);


    @Override
    public void handle(RoutingContext context) {
        String path = context.request().path();
        if (isExclude(path))
            context.next();

        if (preHandle(context, context.request(), context.response())) {
            context.next();
        }
    }

    protected boolean isExclude(String requestPath) {
        for (String path : excludes) {
            if (StringUtils.isMatch(requestPath, path)) {
                return true;
            }
        }
        return false;
    }

}
