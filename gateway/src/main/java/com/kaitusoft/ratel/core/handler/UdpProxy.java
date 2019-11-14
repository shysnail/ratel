package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.App;
import com.kaitusoft.ratel.core.model.Target;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import com.kaitusoft.ratel.handler.SocketProcessor;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/20
 *          <p>
 *          write description here
 */
public abstract class UdpProxy extends SocketProcessor<DatagramSocket> {

    protected ProxyPolicy proxyPolicy;

    protected App app;

    protected static final int HOST_CHECK_TASK_INTERVAL = 10000;
    protected static final Map<Target, Integer> RETRY_TARGETS = new ConcurrentHashMap<>();

    public UdpProxy(App app, ProxyOption option) {
        this.app = app;
    }

    public static UdpProxy build(App app, ProxyOption option) {
        if (option.getProxyType() == ProxyOption.ProxyType.UPSTREAM)
            return new UdpUpstreamProxy(app, option);
        else if (option.getProxyType() == ProxyOption.ProxyType.ECHO) {
            return new UdpEchoProxy(app, option);
        } else
            throw new UnsupportedOperationException("不支持的转发类型 + " + option.getProxyType());
    }

    public ProxyPolicy getProxyPolicy() {
        return proxyPolicy;
    }

    public Object getClientAddr(DatagramPacket packet){
        SocketAddress socketAddress = packet.sender();
        return socketAddress.host() + ":" + socketAddress.port();
    }
}
