package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.Target;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/20
 *          <p>
 *          write description here
 */
public abstract class ProxyPolicy {

    protected Target[] targets;
    protected Set<Target> deadTargets = new HashSet<>();

    protected boolean keepSession = false;

    private Map<Object, Target> sessionMap;


    public static ProxyPolicy create(LoadBalance loadBalance, Target[] targets) {
        return create(loadBalance, false, targets);
    }

    public static ProxyPolicy create(LoadBalance loadBalance, boolean keepSession, Target[] targets) {
        ProxyPolicy proxyPolicy = null;
        if (loadBalance == LoadBalance.POLLING_AVAILABLE) {
            proxyPolicy = new WeightPollingProxyPolicy();
        } else if (loadBalance == LoadBalance.RANDOM) {
            proxyPolicy = new RandomProxyPolicy();
        } else if (loadBalance == LoadBalance.IP_HASH) {
            proxyPolicy = new IpHashProxyPolicy();
        } else {
            throw new IllegalStateException("not support yet");
        }

        proxyPolicy.targets = new Target[targets.length];
        System.arraycopy(targets, 0, proxyPolicy.targets, 0, targets.length);

        proxyPolicy.keepSession = keepSession;
        if(keepSession)
            proxyPolicy.sessionMap = new ConcurrentHashMap<Object, Target>(65535, 0.75f, 16);

        proxyPolicy.init();
        return proxyPolicy;
    }

//    protected String[] status;

    protected void init() {
    }

    protected synchronized void dead(String target) {
        if (StringUtils.isEmpty(target) || targets.length == 0)
            return;

        Target[] newTargets = new Target[targets.length - 1];
        for (int i = 0, j = 0; i < targets.length; i++) {
            if (targets[i].getUrl().equals(target)) {
                deadTargets.add(targets[i]);
                i++;
                continue;
            }

            newTargets[j] = targets[i];
            j++;
        }

        targets = newTargets;

        // 上报给 t 重试任务
    }

    protected void dead(Target target) {
        dead(target.getUrl());
    }

    public Set<Target> dead() {
        return deadTargets;
    }

    public void rebirth(Target target) {
        rebirth(target.getUrl());
    }

    public synchronized void rebirth(String host) {
        if (StringUtils.isEmpty(host))
            return;

        Target target = null;
        for (Target t : deadTargets) {
            if (t.getUrl().equals(host)) {
                target = t;
                break;
            }
        }

        /**
         * 移除
         */
        if (target == null || !deadTargets.remove(target))
            return;

        Target[] newTargets = new Target[targets.length + 1];

        System.arraycopy(targets, 0, newTargets, 0, targets.length);
        newTargets[targets.length] = target;
        targets = newTargets;

    }

    public Target next(Object client) {
        if (targets == null || targets.length == 0)
            return null;

        Target t = null;
        if(keepSession && client != null) {
            t = sessionMap.get(client);

            if(t == null) {
                t = choseNext(client);
                sessionMap.put(client, t);
            }
        }else{
            t = choseNext(client);
        }

        return t;
    }

    public abstract Target choseNext(Object client);


    public enum LoadBalance {

        /**
         * 随机
         */
        RANDOM,

        /**
         * 轮询可用
         */
        POLLING_AVAILABLE,

        /**
         * ip哈希化
         */
        IP_HASH,

        /**
         * 最小活跃数
         */
        LEAST_ACTIVE,
    }

}
