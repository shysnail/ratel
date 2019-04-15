package com.kaitusoft.ratel.core.handler;

import com.kaitusoft.ratel.ContextAttribute;
import com.kaitusoft.ratel.core.model.Api;
import com.kaitusoft.ratel.core.model.PassBody;
import com.kaitusoft.ratel.core.model.Target;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import com.kaitusoft.ratel.core.model.option.UpstreamOption;
import com.kaitusoft.ratel.util.StringUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/20
 *          <p>
 *          write description here
 */
public class UpstreamProxy extends Proxy {

    private static final Logger logger = LoggerFactory.getLogger(UpstreamProxy.class);

    private static final int HOST_CHECK_TASK_INTERVAL = 10000;

    private HttpClient httpClient;
    private UpstreamOption upstreamOption;
    private PassBody passBody;

    public UpstreamProxy(Api api, ProxyOption option) {
        super(api, option);
        if (option.getProxyType() != ProxyOption.ProxyType.UPSTREAM)
            throw new IllegalStateException("proxy type must upstream!");

        upstreamOption = (UpstreamOption) option;
        if (upstreamOption.getPassBody() != null)
            passBody = new PassBody(upstreamOption.getPassBody());

        this.httpClient = api.getHttpClient();

        ProxyPolicy.LoadBalance loadBalance = upstreamOption.getLoadBalance();

        this.proxyPolicy = ProxyPolicy.create(loadBalance, upstreamOption.getTargets());

    }

    @Override
    public void handle(RoutingContext context) {

        logger.debug("prepare upstream request:{}...", context.get(ContextAttribute.CTX_REQ_ID).toString());

        context.put(ContextAttribute.CTX_ATTR_UPSTREAM_START, System.currentTimeMillis());

        // 用户发起的请求，真正到达网关的请求
        HttpServerRequest clientRequest = context.request();
        MultiMap clientHeaders = clientRequest.headers();

//        clientHeaders.remove("Cookie").remove("cookie");
//        clientHeaders.remove("Host").remove("host");

        if (upstreamOption.getAppendHeaders() != null)
            upstreamOption.getAppendHeaders().forEach((k, v) -> {
                if (v.startsWith("${") && v.endsWith("}"))
                    v = v.substring(2, v.length() - 1);
                clientHeaders.add(k, v);
            });

        if (upstreamOption.getRemoveHeaders() != null) {
            for (String head : upstreamOption.getRemoveHeaders()) {
                clientHeaders.remove(head);
            }
        }

        context.vertx().runOnContext( upstream -> {
            doUpstream(context, clientRequest, clientHeaders);
        });
    }

    private void doUpstream(RoutingContext context, HttpServerRequest clientRequest, MultiMap clientHeaders) {

        Target target = null;
        try{
            target = proxyPolicy.next(context);
        }catch(Exception e){
            logger.error("no available upstream target", e);
        }
        if(target == null)
            return;

        String prefix = context.currentRoute().getPath();

        String host = target.getHost();
        clientHeaders.set(HttpHeaders.HOST, host);
        String refer = clientRequest.scheme() + "://" + host + "/";
        if (!StringUtils.isEmpty(clientHeaders.get(HttpHeaders.REFERER))) {
            clientHeaders.set(HttpHeaders.REFERER, refer);
        }
        if (!StringUtils.isEmpty(clientHeaders.get(HttpHeaders.ORIGIN))) {
            clientHeaders.set(HttpHeaders.ORIGIN, refer);
        }

        String url = api.assemble(clientRequest, target, prefix, upstreamOption.isPassQueryString());

        HttpMethod clientMethod = upstreamOption.getMethodForward();
        if (clientMethod == null)
            clientMethod = clientRequest.method();

        if (clientHeaders.contains(HttpHeaders.UPGRADE, HttpHeaders.WEBSOCKET, true)) {

            ServerWebSocket ws = clientRequest.upgrade();
            final String reqId = context.get(ContextAttribute.CTX_REQ_ID).toString();
            logger.debug("request:{}, ws request", reqId);
            if (logger.isDebugEnabled()) {
                ws.headers().forEach((entry) -> {
                    logger.debug("request:{}, ws header:{} -> {}", reqId.toString(), entry.getKey(), entry.getValue());
                });
            }

            String version = ws.headers().get("Sec-WebSocket-Version");
            WebsocketVersion wsVersion = WebsocketVersion.V13;
            if (!StringUtils.isEmpty(version)) {
                wsVersion = WebsocketVersion.valueOf("V" + version);
            }

            HttpClient wsClient = context.vertx().createHttpClient(api.buildWsClientOption());
            String wsUrl = "ws://" + target.getHost() + url.substring(target.getUrl().length());
            ReadStream<WebSocket> readStream = wsClient.websocketStreamAbs(wsUrl, clientHeaders.remove("Sec-WebSocket-Extensions"), wsVersion, ws.subProtocol());

            doWsUpstream(readStream, ws, context);
        } else {
            doUpstream(target, clientMethod, url, clientHeaders, clientRequest, context);
        }
    }

    private void doWsUpstream(ReadStream<WebSocket> wsUpstream, ServerWebSocket wsClient, RoutingContext context) {
        final String reqId = context.get(ContextAttribute.CTX_REQ_ID).toString();

        wsClient.closeHandler(close -> {
            logger.error("ws request:{},  close!", reqId);
            wsClient.frameHandler(null);
            WebSocket ws = context.remove(ContextAttribute.CTX_REQ_WS);
            if(ws != null)
                ws.frameHandler(null);
        });

        wsClient.exceptionHandler(error -> {
            logger.error("ws request:{}, exception:", reqId, error);
            wsClient.frameHandler(null);
            WebSocket ws = context.remove(ContextAttribute.CTX_REQ_WS);
            if(ws != null)
                ws.frameHandler(null);
        });

        wsClient.frameHandler(frame -> {
            logger.debug("ws request-{}: {}", reqId, frame.textData());

            WebSocket upstreamWs = context.get(ContextAttribute.CTX_REQ_WS);
            if (upstreamWs != null) {
                upstreamWs.writeFrame(frame);
            }

            wsUpstream.handler(ws -> {
                context.put(ContextAttribute.CTX_REQ_WS, ws);
                ws.exceptionHandler(error -> {
                    context.remove(ContextAttribute.CTX_REQ_WS);
                });

                ws.frameHandler(response -> {
//                    logger.debug("ws response-{}: {}", reqId, response.textData());
                    wsClient.writeFrame(response);
                    if (wsClient.writeQueueFull()) {
                        ws.pause();
                        ws.resume();
                    }
                });

                ws.writeFrame(frame);
            });
        });

//        wsClient.handler(read -> {
//            logger.error("ws request-{}: {}", reqId, read.length());
//
//            WebSocket upstreamWs = context.get(ContextAttribute.CTX_REQ_WS);
//            if (upstreamWs != null) {
//                upstreamWs.write(read);
//            }
//
//            wsUpstream.handler(ws -> {
//                context.put(ContextAttribute.CTX_REQ_WS, ws);
//                ws.exceptionHandler(error -> {
//                    context.remove(ContextAttribute.CTX_REQ_WS);
//                });
//
//                ws.handler(data -> {
//                    logger.error("ws response-{}: {}", reqId, data.length());
//                    wsClient.write(data);
//                    if (wsClient.writeQueueFull()) {
//                        ws.pause();
//                        ws.resume();
//                    }
//                });
//
//                ws.write(read);
//            });
//        });
    }


    private void doUpstream(Target currentTarget, HttpMethod clientMethod, String target, MultiMap clientHeaders, HttpServerRequest clientRequest, RoutingContext context) {
        final String reqId = context.get(ContextAttribute.CTX_REQ_ID).toString();
        logger.debug("request:{} -> {}:{}", reqId, clientMethod, target);
        HttpClientRequest upstream = httpClient.requestAbs(clientMethod, target).setTimeout(upstreamOption.getTimeout());
        upstream.setFollowRedirects(true);

        //如果不去host，会出现意想不到的情况，因为host到了目标服务器后会根据host分发
        upstream.headers().addAll(clientHeaders);

        HttpServerResponse clientResponse = context.response();
        upstream.exceptionHandler(e -> {
            Integer errCount = context.get(ContextAttribute.CTX_FAIL_COUNT);
            if (errCount == null)
                errCount = 0;

            logger.error("upstream error {} times :", errCount + 1, e);

            if (clientResponse.ended() || clientResponse.closed()) {
                return;
            }

            /**
             * 当前转发目标 重试次数
             */
            if (errCount < upstreamOption.getRetry()) {
                context.put(ContextAttribute.CTX_FAIL_COUNT, errCount + 1);
                doUpstream(currentTarget, clientMethod, target, clientHeaders, clientRequest, context);
                return;
            } else {
                //接口需标记为异常，重试其它目标
                proxyPolicy.dead(currentTarget);
                doUpstream(context, clientRequest, clientHeaders);
                tryReconnectTask(currentTarget, context.vertx(), clientMethod, target);
            }

            if (hasPostProcessor) {
                context.put(ContextAttribute.CTX_ATTR_UPSTREAM, Future.<Boolean>failedFuture(e));
                context.next();
                return;
            }

            clientResponse.setStatusCode(HttpResponseStatus.BAD_GATEWAY.code()).end(e.toString());
        });


//        upstream.handler(upstreamResponse -> {
//            if (clientResponse.ended()) {
//                context.put(ContextAttribute.CTX_ATTR_UPSTREAM, Future.succeededFuture(true));
//                return;
//            }
//
//            clientResponse.setStatusCode(upstreamResponse.statusCode());
//            upstreamResponse.headers().forEach(header -> {
//                clientResponse.putHeader(header.getKey(), header.getValue());
//            });
//
//            if (clientResponse.isChunked() || needChunked(upstreamResponse))
//                clientResponse.setChunked(true);
//
//            upstreamResponse.exceptionHandler(e -> {
//                context.put(ContextAttribute.CTX_ATTR_UPSTREAM, Future.<Boolean>failedFuture(e));
//                upstreamResponse.handler(null);
//                try {
//                    upstream.end();
//                } catch (Exception ue) {
//                    logger.debug("exception occur when upstream :", e);
//                }
//
//            });
//
//            upstreamResponse.endHandler(end -> {
//                if (hasPostProcessor) {
//                    context.put(ContextAttribute.CTX_ATTR_UPSTREAM, Future.succeededFuture(true));
//                    context.next();
//                    return;
//                }
//
//                clientResponse.end();
//            });
//
//            Handler transfer = new Handler() {
//                @Override
//                public void handle(Object data) {
//                    clientResponse.write((Buffer) data);
//                    if (clientResponse.writeQueueFull()) {
//                        upstreamResponse.pause();
//                        upstreamResponse.resume();
//                        upstreamResponse.handler(this);
//                    }
//                }
//            };
//            upstreamResponse.handler(transfer);
//
//        });


        upstream.handler(upstreamResponse -> {
            if (clientResponse.ended()) {
                context.put(ContextAttribute.CTX_ATTR_UPSTREAM, Future.succeededFuture(true));
                return;
            }

            clientResponse.setStatusCode(upstreamResponse.statusCode());
            upstreamResponse.headers().forEach(header -> {
                clientResponse.putHeader(header.getKey(), header.getValue());
            });

            if (clientResponse.isChunked() || needChunked(upstreamResponse))
                clientResponse.setChunked(true);

            Pump pump = Pump.pump(upstreamResponse, clientResponse);

            upstreamResponse.exceptionHandler(e -> {
                context.put(ContextAttribute.CTX_ATTR_UPSTREAM, Future.<Boolean>failedFuture(e));
                pump.stop();
                try {
                    upstream.end();
                } catch (Exception ue) {
                    logger.debug("exception occur when upstream :", e);
                }

            });

            upstreamResponse.endHandler(end -> {
                if (hasPostProcessor) {
                    context.put(ContextAttribute.CTX_ATTR_UPSTREAM, Future.succeededFuture(true));
                    context.next();
                    return;
                }

                clientResponse.end();
            });

            pump.start();

        });

//            upstream.endHandler(end -> {
//                if(logger.isDebugEnabled()){
//                    upstream.headers().forEach((entry) -> {
//                        logger.debug("request:{}, upstream header:{} -> {}", reqId.toString(), entry.getKey(), entry.getValue());
//                    });
//                }
//            });

        if (passBody != null) {
            if (hasBody(clientRequest)) {
                passBody.pass(reqId, clientRequest, upstream);
            } else{
                Buffer buffer = context.remove(ContextAttribute.CTX_REQ_BODY);
                if (buffer != null && buffer.length() > 0) {
                    passBody.pass(reqId, clientMethod, buffer, upstream);
                } else {
                    upstream.end();
                }
            }
        } else {
            upstream.end();
        }


    }


    /**
     * request有可能由于某些操作提早end，
     *
     * @param request
     * @return
     */
    private boolean hasBody(HttpServerRequest request) {
        if (request.isEnded())
            return false;
        return request.method().equals(HttpMethod.POST) || request.method().equals(HttpMethod.PUT);
    }

    private boolean needChunked(HttpClientResponse upstreamResponse) {
        if (HttpHeaders.CHUNKED.toString().equalsIgnoreCase(upstreamResponse.getHeader("Transfer-Encoding"))) {
            return true;
        }

        if (StringUtils.isEmpty(upstreamResponse.getHeader("Content-Length"))) {
            return upstreamResponse.version() != HttpVersion.HTTP_1_0;
        }

        return false;
    }

    /**
     * 此方法缺点明显，重试一次后，如果仍旧失败，仍需重试
     * @param currentTarget
     * @param vertx
     * @param clientMethod
     * @param target
     */
    private void tryReconnectTask(Target currentTarget, Vertx vertx, HttpMethod clientMethod, String target) {
        logger.warn("retry connect target {}:{}", clientMethod, target);
        Handler retryTask = new Handler() {
            @Override
            public void handle(Object event) {
                doReconnect(currentTarget, vertx, clientMethod, target, this);
            }
        };

        vertx.setTimer(HOST_CHECK_TASK_INTERVAL, retryTask);
    }

    private void doReconnect(Target currentTarget, Vertx vertx, HttpMethod clientMethod, String target, Handler task){
        httpClient.requestAbs(clientMethod, target).setTimeout(upstreamOption.getTimeout()).handler(response -> {
            int statusCode = response.statusCode();
            if (statusCode != 200) {
                logger.warn("app:{}, api:{} , 失败接口已连接，返回码非 200，{}", api.getApp().getName(), api.getPath(), statusCode);
            } else {
                logger.warn("app:{}, api:{} , 失败接口已连接", api.getApp().getName(), api.getPath());
            }
            proxyPolicy.rebirth(currentTarget);
        }).exceptionHandler(e -> {
            logger.error("app:{}, api:{} , 失败接口已连接", api.getApp().getName(), api.getPath(), e);
            if(task != null)
                vertx.setTimer(HOST_CHECK_TASK_INTERVAL, task);
        }).end();
    }
}
