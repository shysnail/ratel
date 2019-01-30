package com.kaitusoft.ratel.cache.redis;


import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2018/1/26
 *          <p>
 *          write description here
 */
public class ShardedJedisClient extends RedisClient<ShardedJedis>{

    public ShardedJedisClient(RedisConfig config) {
        super(config);
    }

    @Override
    protected void initPool() {
        List<JedisShardInfo> shards = new ArrayList<>(config.getMasters().size());
        for(HostAndPort node : config.getMasters()){
            JedisShardInfo shardInfo = new JedisShardInfo(node.getHost(), node.getPort(), (int)config.getConnectTimeout());
            shards.add(shardInfo);
        }

        pool = new ShardedJedisPool(poolConfig, shards);

    }
}
