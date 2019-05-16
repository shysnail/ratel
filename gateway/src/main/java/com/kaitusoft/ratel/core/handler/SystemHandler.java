package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.ContextAttribute;
import com.kaitusoft.ratel.Result;
import com.kaitusoft.ratel.cluster.ClusterVerticle;
import com.kaitusoft.ratel.core.common.Configuration;
import com.kaitusoft.ratel.core.common.Event;
import com.kaitusoft.ratel.core.common.ProtocolEnum;
import com.kaitusoft.ratel.core.model.Api;
import com.kaitusoft.ratel.core.model.App;
import com.kaitusoft.ratel.handler.Processor;
import com.kaitusoft.ratel.util.StringUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Date;

/**
 * @author frog.w
 * @version 1.0.0, 2018/10/9
 *          <p>
 *          write description here
 */
public class SystemHandler extends Processor {

    private Vertx vertx;

    private App app;

    private Api path;

    private String badGateway;

    public SystemHandler(Vertx vertx, App app, Api path) {
        this.vertx = vertx;
        this.app = app;
        this.path = path;
        if (app.getCustomResult() != null && app.getCustomResult().get(HttpResponseStatus.BAD_GATEWAY.code()) != null)
            badGateway = app.getCustomResult().get(HttpResponseStatus.BAD_GATEWAY.code()).getContent().toString();
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response();
        HttpServerRequest request = context.request();

        JsonObject action = new JsonObject();
        action.put("app", app.getId());
        action.put("path", path.getId());
        String uri = context.request().uri();
        action.put("uri", uri);

        if (app.getProtocol() == ProtocolEnum.HTTP_HTTPS) {
            action.put("https", context.request().isSSL());
        }
        vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.ACTION_REQUEST), action);

        //  如果需要记录日志，需先记录初始化信息
        context.put(ContextAttribute.CTX_ATTR_START, System.currentTimeMillis());

        context.put(ContextAttribute.CTX_ATTR_PATH_ID, path.getId());
        context.put(ContextAttribute.CTX_ATTR_APP, app.getId());
        context.put(ContextAttribute.CTX_ATTR_PATH, path.getPath());
        final String reqId = StringUtils.uniqueId();
        context.put(ContextAttribute.CTX_REQ_ID, reqId);

        logger.debug("new request:{} -> url: {}....", reqId, context.request().absoluteURI());

        if(path.isPause()){
            logger.debug("接口:{}-{}-{} 暂停服务", path.getId(), path.getPath(), uri);
            //暂停，返回应用暂不可达的信息
            Result result = app.getBlowSetting().getResult();
            if(result != null)
                response.setStatusCode(result.getCode()).putHeader(HttpHeaders.CONTENT_TYPE, result.getContentType()).end(result.getContent().toString());
            else
                response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end("暂停提供服务");

            return;
        }

        request.exceptionHandler(exceptionHandler -> {
            vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.ACTION_REQUEST_ERROR), action);

//                appStatus.requestFail(path.getId(), context.request().uri(), true);
            //如果需要记录处理日志，在此处记录
            logger.debug("app-{} request-{} end", app.getId(), uri);

            if (badGateway != null) {
                response.setStatusCode(HttpResponseStatus.BAD_GATEWAY.code()).end(badGateway);
            }
        });

        response.endHandler(endHandler -> {
            action.put("duration", System.currentTimeMillis() - (long) context.get(ContextAttribute.CTX_ATTR_START));
            if (context.get(ContextAttribute.CTX_ATTR_FAIL) != null) {
                logger.warn("请求未通过检验");
                vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.ACTION_REQUEST_FAIL), action);
//                    appStatus.requestFail(path.getId(), context.request().uri());
            } else {
                Long upstreamStart = context.get(ContextAttribute.CTX_ATTR_UPSTREAM_START);
                if (upstreamStart != null)
                    action.put("requestTime", System.currentTimeMillis() - upstreamStart.longValue());
                vertx.eventBus().<JsonObject>send(Event.formatInternalAddress(Event.ACTION_REQUEST_DONE), action);
//                    appStatus.requestDone(path.getId(), context.request().uri());
            }

            //如果需要记录处理日志，在此处记录
            logger.debug("request:{} end, app:{}, code : {}", reqId, app.getId(), response.getStatusCode());

        });

        response.putHeader(HttpHeaders.SERVER, Configuration.OFFICIAL_NAME);
        response.putHeader(HttpHeaders.DATE, StringUtils.getRfc822DateFormat(new Date()));


        context.next();
    }
}
