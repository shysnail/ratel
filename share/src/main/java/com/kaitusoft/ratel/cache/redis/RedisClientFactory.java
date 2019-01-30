package com.kaitusoft.ratel.cache.redis;


/**
 * @author frog.w
 * @version 1.0.0, 2018/12/19
 *          <p>
 *          write description here
 */
public class RedisClientFactory {


    public static RedisClient build(RedisConfig config){
        RedisClient client = null;
        if(config.getMode() == RedisConfig.RedisMode.CLUSTER)
            throw new IllegalStateException("not support node:cluster");

        if(config.getMode() == RedisConfig.RedisMode.SINGLE)
            client = new SingleRedisClient(config);
        else if(config.getMode() == RedisConfig.RedisMode.CLIENT_SHARD)
            client = new ShardedJedisClient(config);
        else  if(config.getMode() == RedisConfig.RedisMode.SENTINEL)
            client = new SentinelJedisClient(config);
        else if(config.getMode() == RedisConfig.RedisMode.CLIENT_SHARD_SENTINEL)
            client = new ShardedSentinelJedisClient(config);
        else
            throw new IllegalStateException("unsupported redis mode :" + config.getMode() + ", must one of {" + RedisConfig.RedisMode.SINGLE + "|" + RedisConfig.RedisMode.SENTINEL+ "|" + RedisConfig.RedisMode.CLIENT_SHARD_SENTINEL + "}");


        return client;
    }

}
