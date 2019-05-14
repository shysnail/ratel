package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.handler.Processor;
import io.vertx.ext.web.RoutingContext;

public class XssFilter extends Processor {

    @Override
    protected boolean preCheck(RoutingContext context) {

        return true;
    }
}
