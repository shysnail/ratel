package com.kaitusoft.ratel.core.model;

import lombok.Data;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/6
 *          <p>
 *          write description here
 */
@Data
@ToString
public class AppStatus {

    private Integer id;

    private String deployId;

    private long lastDeployTime;

    private long runningTime = 0;

    private int deployCount;

    private AtomicLong processTime = new AtomicLong();

    private AtomicLong upstreamTime = new AtomicLong();

    private AtomicLong inBytes = new AtomicLong();

    private AtomicLong outBytes = new AtomicLong();

    /**
     * 异常次数
     */
    private AtomicInteger errorCount = new AtomicInteger();

    /**
     * 请求到达网关的总次数
     */
    private AtomicLong requestCount = new AtomicLong();

    /**
     * 如果网关协议为http/https，并且开启了https，此数量为https请求数量
     * 仅在此情况下有效
     */
    private AtomicLong httpsRequestCount = new AtomicLong();

    /**
     * 当前正在处理的数量
     */
    private AtomicLong processingCount = new AtomicLong();

    /**
     * 失败次数-未通过检验|异常
     */
    private AtomicInteger failCount = new AtomicInteger();


    private AtomicLong processedCount = new AtomicLong();


    /**
     * 请求API失败的次数，包含异常及黑名单、限流等等校验不通过
     */
    private Map<Integer, AtomicInteger> requestFailedCountMap = new ConcurrentHashMap<>();

    /**
     * API请求的数量
     */
    private Map<Integer, AtomicLong> requestCountMap = new ConcurrentHashMap<>();

//    /**
//     * 存储API的监控记录信息
//     */
//    private Map<Integer, Deque<JsonObject>> trackSucceededMap = new HashMap<>();

    /**
     * 某个path的请求失败次数，包含异常及黑名单、限流等等校验不通过
     */
    private Map<String, AtomicInteger> uriFailedCountMap = new ConcurrentHashMap<>();

    /**
     * 某个path的请求次数
     */
    private Map<String, AtomicLong> uriCountMap = new ConcurrentHashMap<>();

    public AppStatus() {

    }

    public AppStatus(AppStatus old) {
        this.id = old.getId();
        this.deployId = old.getDeployId();
        this.lastDeployTime = old.getLastDeployTime();
        this.runningTime = old.getRunningTime();
        this.deployCount = old.getDeployCount();

        this.inBytes = new AtomicLong(old.getInBytes().longValue());
        this.outBytes = new AtomicLong(old.getOutBytes().longValue());

        this.errorCount = new AtomicInteger(old.getErrorCount().intValue());
        this.failCount = new AtomicInteger(old.getFailCount().intValue());
        this.requestCount = new AtomicLong(old.getRequestCount().longValue());
        this.httpsRequestCount = new AtomicLong(old.getHttpsRequestCount().longValue());
        this.processingCount = new AtomicLong(old.getProcessingCount().longValue());

        old.getRequestFailedCountMap().forEach((k, v) -> {
            this.requestFailedCountMap.put(k, new AtomicInteger(v.intValue()));
        });

        old.getRequestCountMap().forEach((k, v) -> {
            this.requestCountMap.put(k, new AtomicLong(v.longValue()));
        });

        old.getUriFailedCountMap().forEach((k, v) -> {
            this.uriFailedCountMap.put(k, new AtomicInteger(v.intValue()));
        });

        old.getUriCountMap().forEach((k, v) -> {
            this.uriCountMap.put(k, new AtomicLong(v.longValue()));
        });
    }

    public void deployed(String deployId) {
        this.deployId = deployId;
        deployCount += 1;
        lastDeployTime = System.currentTimeMillis();
    }

    public void unDeploy() {
        runningTime = getDuration();
        deployId = null;
    }

    public void request(int api, String uri) {
        request(false, api, uri);
    }

    public void request(boolean https, int api, String uri) {
        requestCount.incrementAndGet();
        processingCount.incrementAndGet();
        if (https)
            httpsRequestCount.incrementAndGet();

        AtomicLong apiRequestCount = requestCountMap.get(api);
        if (apiRequestCount == null) {
            apiRequestCount = new AtomicLong(0);
            requestCountMap.put(api, apiRequestCount);
        }
        apiRequestCount.incrementAndGet();

        AtomicLong uriRequestCount = uriCountMap.get(uri);
        if (uriRequestCount == null) {
            uriRequestCount = new AtomicLong(0);
            uriCountMap.put(uri, uriRequestCount);
        }
        uriRequestCount.incrementAndGet();

    }

    public void requestDone(int api, String uri) {
        processingCount.decrementAndGet();
    }

    public void requestFail(int api, String uri) {
        requestFail(api, uri, false);
    }

    public void requestFail(int api, String uri, boolean exception) {
        processingCount.decrementAndGet();

        failCount.incrementAndGet();

        if (exception)
            errorCount.incrementAndGet();

        AtomicInteger apiRequestCount = requestFailedCountMap.get(api);
        if (apiRequestCount == null) {
            apiRequestCount = new AtomicInteger(0);
            requestFailedCountMap.put(api, apiRequestCount);
        }
        apiRequestCount.incrementAndGet();

        AtomicInteger uriRequestCount = uriFailedCountMap.get(uri);
        if (uriRequestCount == null) {
            uriRequestCount = new AtomicInteger(0);
            uriFailedCountMap.put(uri, uriRequestCount);
        }
        uriRequestCount.incrementAndGet();

    }

    public long getDuration() {
        if (deployId == null)
            return runningTime;

        long duration = System.currentTimeMillis() - lastDeployTime;
        return runningTime + duration;
    }
}
