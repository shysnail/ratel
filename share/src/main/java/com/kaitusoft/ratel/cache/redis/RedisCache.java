package com.kaitusoft.ratel.cache.redis;




import com.kaitusoft.ratel.cache.ICacheCommand;
import com.kaitusoft.ratel.util.SerializeUtil;
import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.JedisCommands;

import java.util.ArrayList;
import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2018/1/12
 *          <p>
 *          write description here
 */
public class RedisCache implements ICacheCommand<String, Object> {
    private RedisClient client;
    private StringSerializer keySerializer;

    public RedisCache(RedisConfig config){
        keySerializer = new StringSerializer();
        client = RedisClientFactory.build(config);
    }

    public void destroy(Object paramObject) {
        client.destroy();
    }

    public boolean put(String key, Object value) {
        return put(key, value, -1);
    }

    public boolean put(String key, Object value, int expires) {
        BinaryJedisCommands jedis = (BinaryJedisCommands) client.getResource();
        byte[] k = keySerializer.serialize(key);
        jedis.set(k, SerializeUtil.serialize(value));
        if(expires > 0)
            jedis.expire(k, expires);

        client.release(jedis);
        return true;
    }

    public Object get(String key) {
        BinaryJedisCommands jedis = (BinaryJedisCommands) client.getResource();
        byte[] data = jedis.get(keySerializer.serialize(key));
        client.release(jedis);
        if (data == null) {
            return null;
        }
        Object obj = SerializeUtil.unserialize(data);
        return obj;
    }

    public boolean remove(String key) {
        BinaryJedisCommands jedis = (BinaryJedisCommands) client.getResource();
        jedis.del(keySerializer.serialize(key));
        client.release(jedis);
        return true;
    }

    public Object getAndRemove(String key) {
        BinaryJedisCommands jedis = (BinaryJedisCommands) client.getResource();
        byte[] bkey = keySerializer.serialize(key);
        byte[] data = jedis.get(bkey);
        jedis.del(bkey);
        client.release(jedis);

        Object obj = SerializeUtil.unserialize(data);
        return obj;
    }

    @Override
    public boolean putList(String key, List<Object> data) {
        BinaryJedisCommands jedis = (BinaryJedisCommands) client.getResource();
        byte[] k = keySerializer.serialize(key);
        byte[][] values = new byte[data.size()][];
        for(int i = 0; i < data.size(); i ++){
            values[i] = SerializeUtil.serialize(data.get(i));
        }
        jedis.lpush(k, values);
        client.release(jedis);
        return true;
    }

    @Override
    public boolean setListElem(String key, int index, Object data) {
        BinaryJedisCommands jedis = (BinaryJedisCommands) client.getResource();
        byte[] k = keySerializer.serialize(key);
        jedis.lset(k, index, SerializeUtil.serialize(data));
        client.release(jedis);
        return true;
    }

    public List getListAll(String key) {
        BinaryJedisCommands jedis = (BinaryJedisCommands) client.getResource();
        byte[] bkey = keySerializer.serialize(key);
        List<byte[]> data = jedis.lrange(bkey, 0, -1);
        client.release(jedis);

        if(data == null || data.size() == 0) {
            return new ArrayList(0);
        }

        List result = new ArrayList(data.size());
        for(byte[] d : data){
            result.add(SerializeUtil.unserialize(d));
        }

        return result;
    }

    public List getList(String key, int startIndex, int endIndex) {
        if(startIndex < endIndex)
            throw new IllegalStateException("start index must small than end index!");
        BinaryJedisCommands jedis = (BinaryJedisCommands) client.getResource();
        byte[] bkey = keySerializer.serialize(key);
        List<byte[]> data = jedis.lrange(bkey, startIndex, endIndex);
        client.release(jedis);

        if(data == null || data.size() == 0) {
            return new ArrayList(0);
        }

        List result = new ArrayList(data.size());
        for(byte[] d : data){
            result.add(SerializeUtil.unserialize(d));
        }

        return result;
    }

    @Override
    public Long getLong(String key) {
        JedisCommands jedis = (JedisCommands) client.getResource();
        String s = jedis.get(key);
        return Long.valueOf(s);
    }

    @Override
    public Long addAndGetLong(String key){
        JedisCommands jedis = (JedisCommands) client.getResource();
        Long v = jedis.incr(key);
        return v;
    }

    @Override
    public Long addAndGetLong(String key, Long delta) {
        JedisCommands jedis = (JedisCommands) client.getResource();
        Long v = jedis.incrBy(key, delta);
        return v;
    }

    @Override
    public Long getAndAddLong(String key) {
        JedisCommands jedis = (JedisCommands) client.getResource();
        String s = jedis.get(key);
        jedis.incr(key);
        return Long.valueOf(s);
    }

    @Override
    public Long getAndAddLong(String key, Long delta) {
        JedisCommands jedis = (JedisCommands) client.getResource();
        String s = jedis.get(key);
        jedis.incrBy(key, delta);
        return Long.valueOf(s);
    }

}
