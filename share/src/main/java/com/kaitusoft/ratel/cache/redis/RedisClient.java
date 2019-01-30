package com.kaitusoft.ratel.cache.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.util.Pool;

import java.util.Properties;

/**
 * Created by frog.w on 2017/4/20.
 */
public abstract class RedisClient <T> {

    private static Logger logger = LoggerFactory.getLogger(RedisClient.class);

    protected Pool<T> pool;

    protected GenericObjectPoolConfig poolConfig = null;

    protected Properties properties = new Properties();

//    private String poolInfo = null;

    protected RedisConfig config;

    public RedisClient(RedisConfig config){
        logger.info("initialing redis client ...");

        configPool(config);
        logger.debug("use pool component : {}" , config);

        initPool();
        logger.info("redis client initialed...");
    }

    protected abstract void initPool();

    private void configPool(RedisConfig config) {
        this.config = config;

        poolConfig  = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(config.getMaxIdle());
        poolConfig.setMinIdle(config.getMinIdle());
        poolConfig.setMaxTotal(config.getMaxTotal());
        poolConfig.setMaxWaitMillis(config.getMaxWaitMillis());
        poolConfig.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
        poolConfig.setTestOnBorrow(config.isTestOnBorrow());
        poolConfig.setTestOnReturn(config.isTestOnReturn());
        poolConfig.setTestWhileIdle(config.isTestWhileIdle());


//        poolInfo = config.toString();
    }


    public T getResource(){
        return pool.getResource();
    }


    public void release(T t){
        release(t, false);
    }

    public void release(T t, boolean isBroken){
        if (t != null) {
            if (isBroken) {
                pool.returnBrokenResource(t);
            } else {
                pool.returnResource(t);
            }
        }
    }

    public void destroy(){
        this.pool.close();
        this.pool = null;
    }
}
