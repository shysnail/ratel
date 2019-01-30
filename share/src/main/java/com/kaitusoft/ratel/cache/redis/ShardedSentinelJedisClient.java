package com.kaitusoft.ratel.cache.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.ShardedJedis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by frog.w on 2017/5/10.
 */
public class ShardedSentinelJedisClient extends RedisClient<ShardedJedis> {

    public ShardedSentinelJedisClient(RedisConfig config) {
        super(config);
    }

    @Override
    protected void initPool() {
        Set<String> sentinels = new HashSet<String>();

        for(HostAndPort node : config.getSentinels() ){
            sentinels.add(node.toString());
        }

        List<String> masters = new ArrayList<>();
        for(HostAndPort node : config.getMasters()){
            masters.add(node.toString());
        }

        pool = new ShardedJedisSentinelPool(poolConfig, masters, sentinels, config.getPassword());
    }
}
