package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.Result;
import com.kaitusoft.ratel.core.model.Api;
import com.kaitusoft.ratel.core.model.option.EchoOption;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/8
 *          <p>
 *          write description here
 */
public class EchoProxy extends Proxy {

    private Result echo;

    public EchoProxy(Api api, ProxyOption proxyOption) {
        super(api, proxyOption);
        this.echo = ((EchoOption) proxyOption).getEcho();
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.setStatusCode(echo.getCode());
        response.putHeader("Content-Type", echo.getContentType());
        response.setChunked(true);
        response.write(echo.toString());

        if (!hasPostProcessor)
            response.end();
        else context.next();
    }

    @Override
    protected boolean preCheck(RoutingContext context) {
        return true;
    }
}
