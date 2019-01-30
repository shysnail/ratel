package com.kaitusoft.ratel.cache.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

/**
 * @author frog.w
 * @version 1.0.0, 2018/12/19
 *          <p>
 *          write description here
 */
public class SentinelJedisClient extends RedisClient<Jedis>{

    public SentinelJedisClient(RedisConfig config) {
        super(config);
    }

    @Override
    protected void initPool() {
        Set<String> sentinels = new HashSet<String>();

        for(HostAndPort node : config.getSentinels() ){
            sentinels.add(node.toString());
        }
        pool = new JedisSentinelPool("", sentinels, poolConfig);
    }
}
