package com.kaitusoft.ratel.cache.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by frog.w on 2017/5/10.
 */
public class SingleRedisClient extends RedisClient<Jedis>{


    public SingleRedisClient(RedisConfig config) {
        super(config);
    }

    @Override
    protected void initPool(){
        HostAndPort host = config.getMasters().size() > 0 ? config.getMasters().get(0) : null;

        pool = new JedisPool(poolConfig, host.getHost(), host.getPort(), config.getConnectTimeout(), config.getPassword());

    }
}
