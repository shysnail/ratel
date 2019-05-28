package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.handler.Processor;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.VirtualHostHandler;

import java.util.regex.Pattern;

/**
 * @author frog.w
 * @version 1.0.0, 2019/5/27
 *          <p>
 *          write description here
 */
public class VHostHandler extends Processor {
    private Pattern[] regexs;

    public VHostHandler(String[] vhosts){
        if(vhosts != null && vhosts.length > 0){
            regexs = new Pattern[vhosts.length];

            for(int i = 0; i < vhosts.length; i ++){
                String vhost = vhosts[i];
                regexs[i] = Pattern.compile("^" + vhost.replaceAll("\\.", "\\\\.").replaceAll("[*]", "(.*?)") + "$", 2);
            }
        }
    }


    @Override
    public void handle(RoutingContext context) {
        if(regexs == null) {
            context.next();
        }else{
            String host = context.request().host();
            if(host == null) {
                context.next();
            }else{
                boolean match = false;
                String[] var4 = host.split(":");
                int var5 = var4.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    String h = var4[var6];
                    if(matchAny(h)){
                        match = true;
                        break;
                    }
                }

                context.next();
                if(match) {
                    context.next();
                } else {     //host不匹配，不处理，交由后续的route处理
//                    context.fail(HttpResponseStatus.NOT_FOUND.code());
                    return;
                }
            }
        }

    }

    private boolean matchAny(String host){
        for(Pattern regex : regexs){
            if(regex.matcher(host).matches()) {
                return true;
            }
        }

        return false;
    }

}
