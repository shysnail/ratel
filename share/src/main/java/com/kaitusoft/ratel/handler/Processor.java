package com.kaitusoft.ratel.handler;

import com.kaitusoft.ratel.ContextAttribute;
import com.kaitusoft.ratel.Result;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author frog.w
 * @version 1.0.0, 2018/9/6
 *          <p>
 *          write description here
 */
public abstract class Processor implements Handler<RoutingContext> {

    protected static final Logger logger = LoggerFactory.getLogger(Processor.class);

    protected Result failResult;

    @Override
    public void handle(RoutingContext context) {
        logger.debug("process:{}", context.get(ContextAttribute.CTX_REQ_ID).toString());
        if (!preCheck(context)) {
            context.put(ContextAttribute.CTX_ATTR_FAIL, true);
            sendAndEnd(context.response(), failResult);
        } else
            context.next();
    }

    protected boolean preCheck(RoutingContext context) {
        return true;
    }

    protected void send(HttpServerResponse response, Result result) {
        logger.debug("send result:{}", result);
        if (result == null) {
            return;
        }

        response.setStatusCode(result.getCode()).putHeader("Content-Type", result.getContentType()).write(result.getContent().toString());

    }

    protected void sendAndEnd(HttpServerResponse response, Result result) {
        logger.debug("send result and end:{}", result);
        if (result == null) {
            response.end();
            return;
        }

        response.setStatusCode(result.getCode()).putHeader("Content-Type", result.getContentType()).end(result.getContent().toString());
    }
}
