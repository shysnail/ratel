package com.kaitusoft.ratel.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author frog.w
 * @version 1.0.0, 2018/1/11
 *          <p>
 *          这个类需实现的功能：
 *          1。初始化一个本地容器用做缓存
 *          2。原则上这个类的所有方法为protected，只提供对本包内的可见
 */
public class LocalCache implements ICacheCommand<String ,Object>{

    private static Logger logger = LoggerFactory.getLogger(LocalCache.class);
    // 具体内容存放的地方
    private List<ConcurrentHashMap<String, Object>> caches;
    //超期信息存储
    private ConcurrentHashMap<String, Long> expiryCache;
    //清理超期内容的服务
    private ScheduledExecutorService scheduleService;
    //清理超期信息的时间间隔，默认10分钟
    private int expiryInterval = 10;
    //内部cache的个数，根据key的hash对module取模来定位到具体的某一个内部的Map， 减小阻塞情况发生。
    private int moduleSize = 10;

    public LocalCache() {
        init();
    }

    public LocalCache(int expiryInterval, int moduleSize) {
        this.expiryInterval = expiryInterval;
        this.moduleSize = moduleSize;
        init();
    }

    /**
     * 初始化容器
     */
    private void init() {
        caches = new ArrayList<ConcurrentHashMap<String, Object>>();
        for (int i = 0; i < moduleSize; i++) {
            caches.add(new ConcurrentHashMap<String, Object>());
        }
        expiryCache = new ConcurrentHashMap<String, Long>();
        scheduleService = Executors.newScheduledThreadPool(1);
        scheduleService.scheduleAtFixedRate(new CheckOutOfDateSchedule(caches,
                expiryCache), 0, expiryInterval * 60, TimeUnit.SECONDS);

        logger.info("DefaultCache CheckService is start!");

    }

    /**
     * 清楚全部缓存
     * @return
     */
    public boolean clear() {
        if (caches != null) {
            for (ConcurrentHashMap<String, Object> cache : caches) {
                cache.clear();
            }
        }
        if (expiryCache != null) {
            expiryCache.clear();
        }
        return true;
    }

    public boolean containsKey(String key) {
        checkValidate(key);
        return getCache(key).containsKey(key);
    }

    @Override
    public boolean put(String key, Object value) {
        getCache(key).put(key, value);
        return true;
    }

    @Override
    public boolean put(String key, Object value, int expires) {
        getCache(key).put(key, value);
        expiryCache.put(key, System.currentTimeMillis() + (expires * 1000));
        return true;
    }

    public Object get(String key) {
        checkValidate(key);
        return getCache(key).get(key);
    }

    @Override
    public boolean remove(String key) {
        getCache(key).remove(key);
        expiryCache.remove(key);
        return true;
    }

    @Override
    public Object getAndRemove(String key) {
        Object obj = null;
        if(isValid(key)) {
            obj = getCache(key).get(key);
        }else{
            invalid(key);
        }

        return obj;
    }

    @Override
    public boolean putList(String key, List<Object> data) {
        getCache(key).put(key, data);
        return true;
    }

    @Override
    public boolean setListElem(String key, int index, Object data) {
        checkValidate(key);
        List list = (List) get(key);
        list.set(index, data);
        return true;
    }

    @Override
    public List getListAll(String key) {
        checkValidate(key);
        List list = (List) get(key);
        return list;
    }

    @Override
    public List getList(String key, int startIndex, int endIndex) {
        if(startIndex < endIndex)
            throw new IllegalStateException("start index must small than end index!");
        checkValidate(key);
        List list = (List) get(key);
        if(list == null)
            return null;
        if(list.size() < endIndex)
            throw new IllegalStateException("exceeded list size , endindex must less than " + list.size());
        List subList = list.subList(startIndex, endIndex);
        return subList;
    }

    @Override
    public Long getLong(String key) {
        return null;
    }

    @Override
    public Long addAndGetLong(String key) {
        return null;
    }

    @Override
    public Long addAndGetLong(String key, Long delta) {
        return null;
    }

    @Override
    public Long getAndAddLong(String key) {
        return null;
    }

    @Override
    public Long getAndAddLong(String key, Long delta) {
        return null;
    }


    private ConcurrentHashMap<String, Object> getCache(String key) {
        long hashCode = key.hashCode();

        if (hashCode < 0) {
            hashCode = -hashCode;
        }
        int moudleNum = (int) hashCode % moduleSize;
        return caches.get(moudleNum);
    }

    private void checkValidate(String key) {
        if (key != null && expiryCache.get(key) != null
                && expiryCache.get(key) != -1
                && new Date(expiryCache.get(key)).before(new Date())) {
            getCache(key).remove(key);
            expiryCache.remove(key);
        }
    }

    private boolean isValid(String key) {
        boolean valid = true;
        if (key != null && expiryCache.get(key) != null
                && expiryCache.get(key) != -1
                && new Date(expiryCache.get(key)).before(new Date())) {
            valid = false;
        }
        return valid;
    }

    private void invalid(String key){
        getCache(key).remove(key);
        expiryCache.remove(key);
    }

    private void checkAll() {
        Iterator<String> iterator = expiryCache.keySet().iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();
            checkValidate(key);
        }
    }

    class CheckOutOfDateSchedule implements Runnable {
        /**
         * 具体内容存放的地方
         */
        List<ConcurrentHashMap<String, Object>> caches;
        /**
         * 超期信息存储
         */
        ConcurrentHashMap<String, Long> expiryCache;

        public CheckOutOfDateSchedule(
                List<ConcurrentHashMap<String, Object>> caches,
                ConcurrentHashMap<String, Long> expiryCache) {
            this.caches = caches;
            this.expiryCache = expiryCache;
        }

        @Override
        public void run() {
            check();
        }

        public void check() {
            try {
                for (ConcurrentHashMap<String, Object> cache : caches) {
                    Iterator<String> keys = cache.keySet().iterator();

                    while (keys.hasNext()) {
                        String key = keys.next();

                        if (expiryCache.get(key) == null) {
                            continue;
                        }

                        long date = expiryCache.get(key);

                        if (date > 0 && new Date(date).before(new Date())) {
                            expiryCache.remove(key);
                            cache.remove(key);
                        }

                    }

                }
            } catch (Exception ex) {
                logger.info("DefaultCache CheckService is start!");
            }
        }

    }

    public void destroy() {
        try {
            clear();

            if (scheduleService != null) {
                scheduleService.shutdown();
            }

            scheduleService = null;
        } catch (Exception e) {
            logger.error("shutdown local cache error", e);
        }
    }
}
