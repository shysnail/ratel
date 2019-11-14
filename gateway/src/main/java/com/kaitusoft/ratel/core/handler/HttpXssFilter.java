package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.handler.HttpProcessor;
import io.vertx.ext.web.RoutingContext;

public class HttpXssFilter extends HttpProcessor {

    @Override
    protected boolean preCheck(RoutingContext context) {

        return true;
    }
}
