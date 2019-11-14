package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.Result;
import com.kaitusoft.ratel.core.model.App;
import com.kaitusoft.ratel.core.model.option.EchoOption;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import io.vertx.core.net.NetSocket;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/8
 *          <p>
 *          write description here
 */
public class TcpEchoProxy extends TcpProxy {

    private Result echo;

    public TcpEchoProxy(App app, ProxyOption proxyOption) {
        super(app, proxyOption);
        this.echo = ((EchoOption) proxyOption).getEcho();
    }

    public void handle(NetSocket socket) {
        socket.write(echo.getContent().toString(), res ->{
            if(res.failed()){
                logger.error("TCP: echo message error！");
            }
            socket.close();
        });
    }

}
