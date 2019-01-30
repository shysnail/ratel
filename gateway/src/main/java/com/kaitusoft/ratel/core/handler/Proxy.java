package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.Api;
import com.kaitusoft.ratel.core.model.Preference;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import com.kaitusoft.ratel.handler.Processor;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/20
 *          <p>
 *          write description here
 */
//public abstract class Proxy implements Handler<RoutingContext>{
public abstract class Proxy extends Processor {

    protected boolean hasPostProcessor;

    protected Api api;

    protected Preference preference;

    protected ProxyPolicy proxyPolicy;


    public Proxy(Api api, ProxyOption option) {
        this.preference = api.getPreference();
        this.api = api;
        hasPostProcessor = api.getPreference().getPostProcessors() != null && api.getPreference().getPostProcessors().length > 0;
    }

    public static Proxy build(Api api, ProxyOption option) {
        if (option.getProxyType() == ProxyOption.ProxyType.REDIRECT)
            return new RedirectProxy(api, option);
        else if (option.getProxyType() == ProxyOption.ProxyType.UPSTREAM)
            return new UpstreamProxy(api, option);
        else if (option.getProxyType() == ProxyOption.ProxyType.ECHO) {
            return new EchoProxy(api, option);
        } else
            throw new UnsupportedOperationException("不支持的转发类型 + " + option.getProxyType());
    }

    public ProxyPolicy getProxyPolicy() {
        return proxyPolicy;
    }

}
