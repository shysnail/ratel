package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.App;
import com.kaitusoft.ratel.core.model.Target;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import com.kaitusoft.ratel.core.model.option.UpstreamOption;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author frog.w
 * @version 1.0.0, 2019/11/12
 *          <p>
 *          write description here
 */
public class UdpUpstreamProxy extends UdpProxy {

    private static final Logger logger = LoggerFactory.getLogger(UdpUpstreamProxy.class);

    public UdpUpstreamProxy(App app, ProxyOption option) {
        super(app, option);
        if (option.getProxyType() != ProxyOption.ProxyType.UPSTREAM)
            throw new IllegalStateException("proxy type must upstream!");

        UpstreamOption upstreamOption = (UpstreamOption) option;

        ProxyPolicy.LoadBalance loadBalance = upstreamOption.getLoadBalance();

        this.proxyPolicy = ProxyPolicy.create(loadBalance, upstreamOption.getTargets());
    }

    @Override
    public void handle(DatagramSocket socket) {
        socket.endHandler(end -> {
            logger.debug("socket end");
        });

//        final UdpProxy udpProxy = app.getUdpProxy();
//        DatagramSocket datagramSocket = Vertx.vertx().createDatagramSocket();
//        datagramSocket.listen(app.getPort(), "0.0.0.0", result -> {
//            if(result.succeeded()){
//                udpProxy.handle(result.result());
//
//            }else{
//            }
//        }).exceptionHandler(ex -> {
//            ex.printStackTrace();
//        });

        socket.handler(packet -> {
            Object from = getClientAddr(packet);
            Buffer data = packet.data();
            logger.debug("receive data:{} packet from :{}, now delivering", data.length(), from);
            Target next = proxyPolicy.next(from);
            socket.send(data, next.getPort(), next.getHost(), res -> {
                if(res.failed()){
                    logger.error("UDP: echo message error！");
                }else{
                    logger.debug("data deliver succeed!");
                }
            });
        });
    }
}
