package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.Target;
import io.vertx.ext.web.RoutingContext;

import java.util.Random;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/20
 *          <p>
 *          write description here
 */
public class RandomProxyPolicy extends ProxyPolicy {

    private Random random = new Random();

    @Override
    public Target choseNext(Object client) {
        int nextInt = random.nextInt(this.targets.length);
        return targets[nextInt];
    }
}
