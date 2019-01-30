package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.Target;
import io.vertx.ext.web.RoutingContext;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/24
 *          <p>
 *          write description here
 */
public class IpHashProxyPolicy extends ProxyPolicy {

    @Override
    public Target choseNext(RoutingContext context) {
        String ip = context.request().remoteAddress().host();

        int index = ip.hashCode() % targets.length;
        index = index < 0 ? (0 - index) : index;

        return targets[index];
    }
}
