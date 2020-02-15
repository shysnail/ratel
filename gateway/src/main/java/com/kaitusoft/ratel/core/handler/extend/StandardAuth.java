package com.kaitusoft.ratel.core.handler.extend;

import com.kaitusoft.ratel.cache.ICacheCommand;
import com.kaitusoft.ratel.cache.redis.RedisCache;
import com.kaitusoft.ratel.cache.redis.RedisConfig;
import com.kaitusoft.ratel.handler.AbstractAuthHttpProcessor;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/7
 *          <p>
 *          write description here
 */
public class StandardAuth extends AbstractAuthHttpProcessor {

    ICacheCommand<String, Object> cache;

    public StandardAuth() {
        initCache();
    }

    private void initCache() {
        InputStream is = null;
        try {
            is = StandardAuth.class.getResourceAsStream("/redis.yml");
        } catch (Exception e) {
            throw new RuntimeException("用户认证无法加载 redis配置，检查classpath下是否有redis.yml文件，且配置正确");
        }

        if (is == null)
            return;

        RedisConfig config = null;
        Yaml yaml = new Yaml();
        config = yaml.loadAs(is, RedisConfig.class);
        cache = new RedisCache(config);
    }

    @Override
    public String usage() {
        return "oAuth2.0方式<br/>" +
                "使用方式：在请求头附带租户口令。header(Authorization, bearer 租户口令)" +
                "<br/>" +
                "获取口令方式:<br/>" +
                "认证api?clientId=租户id&cipher=md5(clientSecret+timestamp)&timestamp=请求时的时间戳，long型，精确到毫秒<br/>";
    }

    @Override
    protected boolean preCheck(RoutingContext context) {

        MultiMap headers = context.request().headers();

        String ticket = headers.get("Authorization");
        if (StringUtils.isEmpty(ticket)) {
            ticket = context.request().getParam("Authorization");
        }
        if (StringUtils.isEmpty(ticket))
            return false;

        if(ticket.startsWith("bearer ")){
            ticket = ticket.substring(7);
        }

        String clientId = headers.get("clientId");
        String appId = headers.get("appId");

        String tokenJson = (String) cache.get("TK_" + appId + "." + clientId);
        logger.debug("got token{} for {}-{}", tokenJson, appId, clientId);

        if (StringUtils.isEmpty(tokenJson)) {
            return false;
        }

//        return true;

        JsonObject json = new JsonObject(tokenJson);
        String token = json.getString("token");
        long expiresAt = json.getLong("expires");

        //已过期
//        if (expiresAt < System.currentTimeMillis()) {
//            return false;
//        }

        return token.equalsIgnoreCase(ticket);
    }
}
