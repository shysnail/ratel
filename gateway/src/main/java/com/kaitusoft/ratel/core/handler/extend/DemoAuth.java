package com.kaitusoft.ratel.core.handler.extend;

import com.kaitusoft.ratel.handler.AbstractAuthProcessor;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/7
 *          <p>
 *          write description here
 */
public class DemoAuth extends AbstractAuthProcessor {

    @Override
    public String usage() {
        return "Auth2.0方式<br/>" +
                "使用方式：在请求头附带租户口令。header(accessToken, 租户口令)" +
                "<br/>" +
                "获取口令时方式:<br/>" +
                "认证api?clientId=租户id&cipher=md5(clientSecret+key)&key=随机code<br/>" +
                "本例仅做展示，不执行实际验证";
    }

    @Override
    protected boolean preCheck(RoutingContext context) {
        MultiMap headers = context.request().headers();
        String token = headers.get("accessToken");
        String token2 = context.request().getParam("accessToken");
        if (!StringUtils.isEmpty(token) || !StringUtils.isEmpty(token2))
            return true;

        return false;
    }
}
