package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.Api;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import com.kaitusoft.ratel.core.model.option.RedirectOption;
import com.kaitusoft.ratel.util.StringUtils;
import com.kaitusoft.ratel.util.URLUtil;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/20
 *          <p>
 *          write description here
 */
public class RedirectProxy extends Proxy {

    private int code = 302;
    private String url;
    private boolean isPassQueryString;

    public RedirectProxy(Api path, ProxyOption option) {
        super(path, option);
        if (option.getProxyType() != ProxyOption.ProxyType.REDIRECT)
            throw new IllegalStateException("proxy type must upstream!");

        RedirectOption redirectOption = (RedirectOption) option;

        code = redirectOption.getCode();
        url = redirectOption.getUrl();
        isPassQueryString = redirectOption.isPassQueryString();
    }


    public void handle(RoutingContext context) {
        HttpServerRequest clientRequest = context.request();
        String clientRequestUri = clientRequest.path();
        String prefix = context.currentRoute().getPath();

        String target = url;
        if (isPassQueryString) {
            String queryString = clientRequestUri.substring(prefix.length());
            if (!StringUtils.isEmpty(queryString))
                target = URLUtil.graft(target, queryString);
        }

        HttpServerResponse response = context.request().response();
        response.setStatusCode(code);
        response.putHeader("location", target);
        response.setChunked(true);
        if (!hasPostProcessor)
            response.end();
        else
            context.next();
    }

}
