package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.Api;
import com.kaitusoft.ratel.core.model.Preference;
import com.kaitusoft.ratel.core.model.Target;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import com.kaitusoft.ratel.handler.HttpProcessor;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/20
 *          <p>
 *          write description here
 */
//public abstract class Proxy implements Handler<RoutingContext>{
public abstract class HttpProxy extends HttpProcessor {

    protected boolean hasPostProcessor;

    protected Api api;

//    protected Preference preference;

    protected ProxyPolicy proxyPolicy;

    protected static final int HOST_CHECK_TASK_INTERVAL = 10000;
    protected static final Map<Target, Object> RETRY_TARGETS = new ConcurrentHashMap<>();

    public HttpProxy(Api api, ProxyOption option) {
//        this.preference = api.getPreference();
        this.api = api;
        hasPostProcessor = api.getPreference().getPostProcessors() != null && api.getPreference().getPostProcessors().length > 0;
    }

    public static HttpProxy build(Api api, ProxyOption option) {
        if (option.getProxyType() == ProxyOption.ProxyType.REDIRECT)
            return new HttpRedirectProxy(api, option);
        else if (option.getProxyType() == ProxyOption.ProxyType.UPSTREAM)
            return new HttpUpstreamProxy(api, option);
        else if (option.getProxyType() == ProxyOption.ProxyType.ECHO) {
            return new HttpEchoProxy(api, option);
        } else
            throw new UnsupportedOperationException("不支持的转发类型 + " + option.getProxyType());
    }

    public ProxyPolicy getProxyPolicy() {
        return proxyPolicy;
    }

    public Object getClientAddr(RoutingContext context){
        String client = context.request().getHeader("X-Forwarded-For");
        if(!StringUtils.isEmpty(client)){
            client = client.split(",")[0];
        }else{
            client = context.request().remoteAddress().host() + ":" + context.request().remoteAddress().port();
        }
        return client;
    }

}
