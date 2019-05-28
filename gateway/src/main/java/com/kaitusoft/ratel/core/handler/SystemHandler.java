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
import io.vertx.core.json.Json;
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

        //  如果需要记录日志，需先记录初始化信息
        context.put(ContextAttribute.CTX_REQ_START, System.currentTimeMillis());

        context.put(ContextAttribute.CTX_ATTR_PATH_ID, path.getId());
        context.put(ContextAttribute.CTX_ATTR_APP, app.getId());
        context.put(ContextAttribute.CTX_ATTR_PATH, path.getPath());
        String uri = request.uri();
        context.put(ContextAttribute.CTX_REQ_URI, uri);
        if (app.getProtocol() == ProtocolEnum.HTTP_HTTPS) {
            context.put(ContextAttribute.CTX_REQ_SCHEMA, context.request().isSSL() ? "https" : "http");
        }
        vertx.eventBus().send(Event.formatInternalAddress(Event.ACTION_REQUEST), Json.encode(context.data()));

        final String reqId = StringUtils.uniqueId();
        context.put(ContextAttribute.CTX_REQ_ID, reqId);
        context.put(ContextAttribute.CTX_REQ_METHOD, request.method().name());
        context.put(ContextAttribute.CTX_TIME_LOCAL, System.currentTimeMillis());
        context.put(ContextAttribute.CTX_REMOTE_ADDR, request.remoteAddress().host());

        logger.debug("new request:{} -> url: {}", reqId, uri);

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
            //如果需要记录处理日志，在此处记录
            logger.debug("app-{} request-{} end", app.getId(), uri);

            if (badGateway != null) {
                response.setStatusCode(HttpResponseStatus.BAD_GATEWAY.code()).end(badGateway);
            }else{
                response.end();
            }

            vertx.eventBus().send(Event.formatInternalAddress(Event.ACTION_REQUEST_ERROR), Json.encode(context.data()));
        });

        response.headersEndHandler(handle -> {
//            if(response.isChunked())
                context.put(ContextAttribute.CTX_RES_SENT_HEAD, response.bytesWritten());
        });

        response.endHandler(endHandler -> {
            int requestTime = (int) (System.currentTimeMillis() - (long) context.get(ContextAttribute.CTX_REQ_START));

            context.put(ContextAttribute.CTX_REQ_TIME, requestTime);
            context.put(ContextAttribute.STATUS, response.getStatusCode());

            context.put(ContextAttribute.CTX_RES_SENT, response.bytesWritten());
            //chunked 需计算实际写出数量去掉head长度
            long bodyLen = -1;
            String bodyLenStr = response.headers().get(HttpHeaders.CONTENT_LENGTH);
            bodyLen = bodyLenStr == null ? -1 : Long.parseLong(bodyLenStr);

            if(bodyLen < 0){
                Long headLen = context.get(ContextAttribute.CTX_RES_SENT_HEAD);
                if(headLen != null)
                    bodyLen = response.bytesWritten() - headLen;
                else {
                    bodyLen = response.bytesWritten();
                    logger.warn(" head length unknown");
                }
            }

            context.put(ContextAttribute.CTX_RES_SENT_BODY, bodyLen);

            if (context.get(ContextAttribute.CTX_ATTR_FAIL) != null) {
                logger.warn("请求失败:{}", response.getStatusCode());
                vertx.eventBus().send(Event.formatInternalAddress(Event.ACTION_REQUEST_FAIL), Json.encode(context.data()));
            } else {
                Long upstreamStart = context.get(ContextAttribute.CTX_UPSTREAM_START);
                if (upstreamStart != null)
                    context.put(ContextAttribute.CTX_UPSTREAM_TIME, System.currentTimeMillis() - upstreamStart.longValue());

                vertx.eventBus().send(Event.formatInternalAddress(Event.ACTION_REQUEST_DONE), Json.encode(context.data()));
            }

            //如果需要记录处理日志，在此处记录
            logger.debug("request:{} end, app:{}, code : {}", reqId, app.getId(), response.getStatusCode());

            app.getAccessLog().log(context);

        });

        response.putHeader(HttpHeaders.SERVER, Configuration.OFFICIAL_NAME);
        response.putHeader(HttpHeaders.DATE, StringUtils.getRfc822DateFormat(new Date()));

        context.next();
    }
}
