package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.option.AccessLimitOption;
import com.kaitusoft.ratel.handler.HttpProcessor;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/25
 *          <p>
 *          write description here
 */
public class DefaultAccessLimit extends HttpProcessor {

    private AccessLimitOption option;

    /**
     *
     */
    private Map<String, AtomicInteger> ipCountMap = null;

    private Map<String, AtomicInteger> clientCountMap = null;

    private AtomicLong totalCount = new AtomicLong(0);
    private AtomicLong intervalCount = new AtomicLong(0);

    /**
     * 不绝对精确
     */
    private volatile long lastCheckTime = System.currentTimeMillis();

    private long INTERVAL;

    /**
     * 根据app预估体量初始化ip计数map，计数map的大小跟计数周期也有关
     * 1024初始大小的map，可以满足一般中型网站的需求了
     *
     * @param option
     */
    public DefaultAccessLimit(AccessLimitOption option) {
        this.option = option;
        this.failResult = option.getOverloadedReturn();
        this.INTERVAL = option.getTimeUnit().getMillSeconds() * option.getInterval();
        if (option.getLimitPerIp() > 0)
            ipCountMap = new ConcurrentHashMap<>(1024);
        else if (option.getLimitPerClient() > 0)
            clientCountMap = new ConcurrentHashMap<>(1024);
    }

    @Override
    protected boolean preCheck(RoutingContext context) {
        totalCount.incrementAndGet(); //总次数 + 1;
        intervalCount.incrementAndGet();
        if (overload(context)) {
            return false;
        }

        return true;
    }

    private boolean overload(RoutingContext context) {
        long checkTime = System.currentTimeMillis();
        long interval = checkTime - lastCheckTime;
        logger.debug("限速 频次 :{}/{}，当前次数:{}， 间隔:{}", option.getLimit() + "|" + option.getLimitPerClient() + "|" + option.getLimitPerIp(), option.getInterval() + option.getTimeUnit().toString(), totalCount.get(), interval);

        //按照请求方身份
        if (option.getLimitPerClient() > 0) {
            StringBuilder idsb = new StringBuilder();
            for (String key : option.getKeys()) {
                idsb.append(context.request().getHeader(key));
            }
            String id = idsb.toString();
            AtomicInteger clientCount = clientCountMap.get(id);
            //注意，此处并非线程安全
            if (clientCount == null) {
                clientCount = new AtomicInteger(0);
                clientCountMap.put(id, clientCount);
            }

            if (clientCount.incrementAndGet() > option.getLimitPerClient()) {
                if (interval <= INTERVAL)
                    return true;

                clientCountMap.get(id).set(0);
                lastCheckTime = checkTime;
                return false;

            }

        }

        //按照ip计数
        if (option.getLimitPerIp() > 0) {
            String ip = null;
            if (!StringUtils.isEmpty(option.getIpHeaderKey()))
                ip = context.request().getHeader(option.getIpHeaderKey());
            else
                ip = context.request().remoteAddress().host();
            AtomicInteger ipCount = ipCountMap.get(ip);
            //注意，此处并非线程安全
            if (ipCount == null) {
                ipCount = new AtomicInteger(0);
                ipCountMap.put(ip, ipCount);
            }

            if (ipCount.incrementAndGet() > option.getLimitPerIp()) {
                if (interval <= INTERVAL)
                    return true;

                ipCountMap.get(ip).set(0);
                lastCheckTime = checkTime;
                return false;
            }

        }

        /*
         * 对本接口总访问数做限制
         * 一般不推荐，一般推荐：用户
         */
        if (option.getLimit() > 0) {
            if (intervalCount.get() > option.getLimit()) {
                if (interval <= INTERVAL)
                    return true;

                intervalCount.set(0);
                lastCheckTime = checkTime;
                return false;

            }
        }

        return false;
    }


//    private boolean overload(){
//        //超出一个周期，说明未达到限制标准，重置计数器，并返回true
//        long checkTime = System.currentTimeMillis();
//        long interval = checkTime - lastCheckTime;
//        if(interval > INTERVAL){
//            lastCheckTime = checkTime;
//            return false;
//        }
//
//        //不到一个周期就达到限制了，返回false
//        return true;
//
//    }

}
