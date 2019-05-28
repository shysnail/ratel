package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.Result;
import com.kaitusoft.ratel.handler.Processor;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/6
 *          <p>
 *          write description here
 */
public class IpFilterHandler extends Processor {

    private String[] blacklist;

    private Result failReturn;

    public IpFilterHandler(String[] blacklist) {
        this.blacklist = blacklist;
    }

    public IpFilterHandler(List<String> ips){
        blacklist = new String[ips.size()];
        ips.toArray(blacklist);
    }

    @Override
    protected boolean preCheck(RoutingContext context) {
        if (blacklist == null || blacklist.length == 0) {
            return true;
        }

        String host = context.request().remoteAddress().host();
        if (!inBlacklist(host)) {
            return true;

        }

        return false;
    }

    private boolean inBlacklist(String host) {
        for (String ip : blacklist) {
            if (ip.equals(host))
                return true;
        }

        return false;
    }
}
