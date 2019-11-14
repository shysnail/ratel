package com.kaitusoft.ratel.handler;

import com.kaitusoft.ratel.Result;
import io.vertx.core.Handler;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author frog.w
 * @version 1.0.0, 2018/9/6
 *          <p>
 *          write description here
 */
public abstract class SocketProcessor<E> implements Handler<E> {

    protected static final Logger logger = LoggerFactory.getLogger(SocketProcessor.class);

    protected Result failResult;

//    @Override
//    public abstract void handle(E socket);
}
