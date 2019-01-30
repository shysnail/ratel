package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.Target;
import io.vertx.ext.web.RoutingContext;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/20
 *          <p>
 *          write description here
 */
public class WeightPollingProxyPolicy extends ProxyPolicy {

    private static final AtomicInteger currentIndex = new AtomicInteger(0);
    private static final AtomicInteger currentWeight = new AtomicInteger(0);
    /**
     * 最大权重
     */
    private int maxWeight;
    /**
     * 权重的最大公约数
     */
    private int gcdWeight;

    public int greaterCommonDivisor(int a, int b) {
        BigInteger aBig = new BigInteger(String.valueOf(a));
        BigInteger bBig = new BigInteger(String.valueOf(b));
        return aBig.gcd(bBig).intValue();
    }

    public int greatestCommonDivisor(Target[] servers) {
        int divisor = 0;
        for (int index = 0, len = servers.length; index < len - 1; index++) {
            if (index == 0) {
                divisor = greaterCommonDivisor(
                        servers[index].getWeight(), servers[index + 1].getWeight());
            } else {
                divisor = greaterCommonDivisor(divisor, servers[index].getWeight());
            }
        }
        return divisor;
    }

    public int greatestWeight(Target[] servers) {
        int weight = 0;
        for (Target server : servers) {
            if (weight < server.getWeight()) {
                weight = server.getWeight();
            }
        }
        return weight;
    }

    @Override
    public Target choseNext(RoutingContext context) {
//        String url = targets[currentIndex.getAndIncrement() % targets.length].getUrl();
        return getNext1();
    }

    protected void init() {
        maxWeight = greatestWeight(targets);
        gcdWeight = greatestCommonDivisor(targets);
    }

    private String getNext() {
        while (true) {
            currentIndex.set(currentIndex.incrementAndGet() % targets.length);
            if (currentIndex.get() == 0) {
                currentWeight.addAndGet(0 - gcdWeight);
                if (currentWeight.get() <= 0) {
                    currentWeight.set(maxWeight);
                    if (currentWeight.get() == 0) {
                        return null;
                    }
                }
            }

            int index = currentIndex.get();
            Target target = targets[index];
            if (target.getWeight() >= currentWeight.get()) {
                return target.getUrl();
            }
        }
    }

    /**
     * 借鉴nginx的负载均衡
     *
     * @return
     */
    private Target getNext1() {
        int index = -1;
        int total = 0;

        for (int i = 0; i < targets.length; i++) {
            targets[i].setCurWeight(targets[i].getWeight() + targets[i].getCurWeight());
            total += targets[i].getWeight();

            if (index == -1 || targets[index].getCurWeight() < targets[i].getCurWeight()) {
                index = i;
            }
        }

        targets[index].setCurWeight(targets[index].getCurWeight() - total);

        return targets[index];
    }


}
