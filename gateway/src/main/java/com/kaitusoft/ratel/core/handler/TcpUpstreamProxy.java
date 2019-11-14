package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.core.model.App;
import com.kaitusoft.ratel.core.model.Target;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import com.kaitusoft.ratel.core.model.option.UpstreamOption;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.core.streams.Pump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author frog.w
 * @version 1.0.0, 2019/11/7
 *          <p>
 *          write description here
 */
public class TcpUpstreamProxy extends TcpProxy {

    private static final Logger logger = LoggerFactory.getLogger(TcpUpstreamProxy.class);

    private UpstreamOption upstreamOption;

    public TcpUpstreamProxy(App app, ProxyOption option) {
        super(app, option);
        if (option.getProxyType() != ProxyOption.ProxyType.UPSTREAM)
            throw new IllegalStateException("proxy type must upstream!");

        upstreamOption = (UpstreamOption) option;

        ProxyPolicy.LoadBalance loadBalance = upstreamOption.getLoadBalance();

        this.proxyPolicy = ProxyPolicy.create(loadBalance, true, upstreamOption.getTargets());
    }


    @Override
    public void handle(NetSocket socket) {
        logger.debug("TCP: new client connect:{}->{}", socket.localAddress().toString(), socket.remoteAddress().toString());

        NetClient client = Vertx.vertx().createNetClient();
        Target next = proxyPolicy.next(getClientAddr(socket));

        socket.pause();

        client.connect(next.getPort(), next.getHost(), res -> {
            if(res.succeeded()){
                NetSocket upstreamSocket = res.result();
                logger.debug("TCP: upstream connected:{}", upstreamSocket.writeHandlerID());

                Pump upstream = Pump.pump(socket, upstreamSocket);
                Pump echo = Pump.pump(upstreamSocket, socket);

                upstreamSocket.closeHandler(close -> {
                    logger.debug("TCP: upstream socket closed! : {}", upstreamSocket.writeHandlerID());
                    socket.close();
                });

                socket.closeHandler(close -> {
                    logger.debug("TCP: socket closed! : {}", socket.writeHandlerID());
                    upstreamSocket.close();
                    client.close();
                });
                upstreamSocket.endHandler(end -> {
                    logger.debug("TCP: upstream socket ended : {}", upstreamSocket.writeHandlerID());
                });
                socket.endHandler(end -> {
                    logger.debug("TCP: socket ended :{}", socket.writeHandlerID());
                    upstreamSocket.end();
                });
                upstreamSocket.exceptionHandler(ex -> {
                    logger.error("TCP: upstrea socket exception", upstreamSocket.writeHandlerID());
                    upstream.stop();
                    upstreamSocket.close();
                });
                socket.exceptionHandler(ex -> {
                    logger.error("TCP: client exception", socket.writeHandlerID());
                    echo.stop();
                    socket.close();
                });
//
                echo.start();
                upstream.start();
                socket.resume();

            }else{
                logger.error("cannot connect remote host");
                tryReconnectTask(next, Vertx.vertx());
            }
        });

    }


    /**
     * 此方法缺点明显，重试一次后，如果仍旧失败，仍需重试
     *
     * @param currentTarget
     * @param vertx
     */
    private synchronized void tryReconnectTask(Target currentTarget, Vertx vertx) {
        logger.warn("retry connect target {}", currentTarget);
        if (RETRY_TARGETS.get(currentTarget) != null) {
            logger.debug("target {} retring", currentTarget);
            return;
        }

        RETRY_TARGETS.put(currentTarget, 1);

        Handler retryTask = new Handler() {
            @Override
            public void handle(Object event) {
                doReconnect(currentTarget, vertx, this);
            }
        };

        vertx.setTimer(HOST_CHECK_TASK_INTERVAL, retryTask);
    }

    private void doReconnect(Target currentTarget, Vertx vertx, Handler task) {
        NetClient client = Vertx.vertx().createNetClient();
        client.connect(currentTarget.getPort(), currentTarget.getHost(), res -> {
            if(res.succeeded()){
                RETRY_TARGETS.remove(currentTarget);
                proxyPolicy.rebirth(currentTarget);
                logger.warn("app:{}, 失败主机:{} 已重新连接", app.getName(), currentTarget.getHostAndPort());
            }else{
                int errCount = RETRY_TARGETS.get(currentTarget);
                logger.warn("app:{}, 失败主机:{} 无法连接，已重试 {} 次", app.getName(), currentTarget.getHostAndPort(), errCount);
                if(errCount < 3){
                    RETRY_TARGETS.put(currentTarget, errCount + 1);
                    vertx.setTimer(HOST_CHECK_TASK_INTERVAL, task);
                }
            }
        }).close();
    }

}
