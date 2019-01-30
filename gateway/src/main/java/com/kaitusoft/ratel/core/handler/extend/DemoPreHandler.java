package com.kaitusoft.ratel.core.handler.extend;


import com.kaitusoft.ratel.handler.AbstractPreHandler;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/29
 *          <p>
 *          write description here
 */
public class DemoPreHandler extends AbstractPreHandler {
    @Override
    public String usage() {
        return "我先来";
    }

//    @Override
//    public void handle(RoutingContext context) {
//        context.next();
//    }

}
