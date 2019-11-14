package com.kaitusoft.ratel.core.handler.extend;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.kaitusoft.ratel.handler.AbstractAuthHttpProcessor;
import io.vertx.ext.web.RoutingContext;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/11
 *          <p>
 *          write description here
 */
public class JWTAuth extends AbstractAuthHttpProcessor {

    private static final String TOKEN_NAME = "token";

    private static final String TOKEN_USER_NAME = "uuid";

    private Map<String, String> userSecrets;

    @Override
    public String usage() {
        return "JWT方式（相对不安全，无法判定用户获取口令时是不是合法）<br/>" +
                "使用方式：在请求头附带租户口令。header(token, 租户口令)" +
                "<br/>" +
                "获取口令时方式:<br/>" +
                "认证api";
    }


    @Override
    protected boolean preCheck(RoutingContext context) {

        String token = context.request().getHeader(TOKEN_NAME);
        String user = context.request().getHeader(TOKEN_USER_NAME);

        String secret = userSecrets.get(user);

        JWTVerifier verifier = null;
        try {
            verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            DecodedJWT jwt = verifier.verify(token);
            Map<String, Claim> map = jwt.getClaims();
            long expiredAt = map.get("expiredAt").asLong();
            if (expiredAt < System.currentTimeMillis()) {
                //过期
                return false;
            }
        } catch (UnsupportedEncodingException e) {

            return false;
        }

        return true;
    }

    /**
     * 这是新生成 jwt token的逻辑
     *
     * @param user
     * @return
     * @throws Exception
     */
    protected String createToken(String user) throws Exception {
        Map<String, Object> map = Collections.emptyMap();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        String token = JWT.create()
                .withHeader(map)//header
                .withClaim("name", user)//payload
                .withClaim("expiredAt", System.currentTimeMillis() + 600000 + 300000)
                .sign(Algorithm.HMAC256(userSecrets.get(user)));//加密
        return token;
    }

}
