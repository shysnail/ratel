package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.Result;
import com.kaitusoft.ratel.core.model.App;
import com.kaitusoft.ratel.core.model.option.EchoOption;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.net.SocketAddress;


/**
 * @author frog.w
 * @version 1.0.0, 2018/9/8
 *          <p>
 *          write description here
 */
public class UdpEchoProxy extends UdpProxy {

    private Result echo;

    public UdpEchoProxy(App app, ProxyOption proxyOption) {
        super(app, proxyOption);
        this.echo = ((EchoOption) proxyOption).getEcho();
    }

    public void handle(DatagramSocket socket) {
        socket.handler(packet -> {
            SocketAddress client = packet.sender();
            socket.send(echo.toString(), client.port(), client.host(), res -> {
                if(res.failed()){
                    logger.error("UDP: echo message error！");
                }
                socket.close();
            });
        });
    }

}
