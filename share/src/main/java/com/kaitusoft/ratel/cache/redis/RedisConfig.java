package com.kaitusoft.ratel.cache.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import redis.clients.jedis.HostAndPort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2018/1/25
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public final class RedisConfig {

    private RedisMode mode = RedisMode.SINGLE;

    private List<HostAndPort> masters = new ArrayList<>();

    private String[] masterAddresses;

    private List<HostAndPort> sentinels = new ArrayList<>();

    private String[] sentinelAddresses;

    private String password;

    private int maxIdle = 3;

    private int minIdle = 1;

    private int maxTotal = 5;

    private int connectTimeout = 3000;

    private int maxWaitMillis = 30000;

    private int minEvictableIdleTimeMillis = 30000;

    private int timeBetweenEvictionRunsMillis = 1000;

    private boolean testOnBorrow = false;

    private boolean testOnReturn = false;

    private boolean testWhileIdle = true;



    public enum RedisMode{
        SINGLE, CLUSTER, CLIENT_SHARD, SENTINEL, CLIENT_SHARD_SENTINEL;
    }


    public void setMasterAddresses(String[] masterAddresses){
        this.masterAddresses = masterAddresses;
        transfer(masterAddresses, this.masters);
    }

    public void setSentinelAddresses(String[] sentinelAddresses){
        this.sentinelAddresses = sentinelAddresses;
        transfer(sentinelAddresses, this.sentinels);
    }

    private void transfer(String[] src, Collection target){
        if(src == null || src.length == 0)
            return;

        for(String master : src){
            String[] hp = master.split(":");
            if(master.trim().length() == 0 || hp.length > 2){
                throw new IllegalStateException("invalid master address: " + master);
            }

            int p = 6379;
            String h = hp[0];
            if(hp.length == 2){
                p = Integer.parseInt(hp[1]);
            }
            HostAndPort host = new HostAndPort(h, p);
            target.add(host);
        }
    }

}
