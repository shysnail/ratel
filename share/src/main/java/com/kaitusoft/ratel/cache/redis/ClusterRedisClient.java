package com.kaitusoft.ratel.cache.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;

/**
 * @author frog.w
 * @version 1.0.0, 2018/12/19
 *          <p>
 *          write description here
 */
public class ClusterRedisClient extends RedisClient<JedisCluster> {
    public ClusterRedisClient(RedisConfig config) {
        super(config);
    }

    @Override
    protected void initPool() {
        JedisCluster jedisCluster = new JedisCluster(new HashSet<HostAndPort>(config.getMasters()), poolConfig);
    }
}
