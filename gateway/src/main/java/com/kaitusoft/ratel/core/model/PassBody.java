package com.kaitusoft.ratel.core.model;

import com.kaitusoft.ratel.codex.DataDecoder;
import com.kaitusoft.ratel.core.model.option.DataDecoderOption;
import com.kaitusoft.ratel.core.model.option.PassBodyOption;
import com.kaitusoft.ratel.util.ResourceUtil;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.WriteStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/24
 *          <p>
 *          write description here
 */
public class PassBody {

    private static final Logger logger = LoggerFactory.getLogger(PassBody.class);

    protected PassBodyOption.PassBodyType passBodyType;

    protected HttpMethod[] method;

    protected boolean decoder;

    protected DataDecoder dataDecoder;

    public PassBody(PassBodyOption passDataOption) {
        passBodyType = passDataOption.getPassBodyType();

        if (PassBodyOption.PassBodyType.PASS_BY_METHODS.equals(passBodyType)) {
            method = PassBody.build(passDataOption.getOption());
        }

        this.decoder = passDataOption.isDecode();
        if (this.decoder) {
            DataDecoderOption decoderOption = new JsonObject(passDataOption.getDecodeOption()).mapTo(DataDecoderOption.class);
            try {
                dataDecoder = ResourceUtil.instanceClass(decoderOption.getInstance(), DataDecoder.class);
            } catch (Exception e) {
                logger.error("初始化解码器出错！", e);
                this.decoder = false;
            }
        }

    }

    private static HttpMethod[] build(String methodStr) {
        if (StringUtils.isEmpty(methodStr))
            return null;

        String[] methods = methodStr.split(",");
        HttpMethod[] method = new HttpMethod[methods.length];

        for (int i = 0; i < methods.length; i++) {
            method[i] = HttpMethod.valueOf(methods[i]);
        }

        return method;
    }

    public void pass(Object reqId, HttpMethod requestMethod, Buffer buffer, WriteStream upstreamRequest) {
        if (PassBodyOption.PassBodyType.ALL_HOLD.equals(passBodyType)) {
            upstreamRequest.end();
            logger.debug("api delivery none body ");
            return;
        }

        if (method != null && !inMethods(requestMethod)) {
            upstreamRequest.end();
            logger.debug("api don't delivery body : method not support {}", method);
            return;
        }

        if (decoder) { //如果需要解码
            buffer = Buffer.buffer(dataDecoder.decode(buffer));
        }

        upstreamRequest.write(buffer).end();
    }

//    public void pass(HttpServerRequest clientRequest, HttpClientRequest upstreamRequest, Handler<Object> result) {
//        if (PassBodyOption.PassBodyType.ALL_HOLD.equals(passBodyType)) {
//            result.handle(null);
//            return;
//        }
//
//        if (method != null && !inMethods(clientRequest.method())) {
//            result.handle(null);
//            return;
//        }
//
//        if (decoder) { //如果需要解码
//            clientRequest.bodyHandler(buffer -> {
//                //在这里进行解码
//                if(dataDecoder != null) {
//                    Buffer decoded = Buffer.buffer(dataDecoder.decode(buffer));
//                    buffer = decoded;
//                }
//                pass(clientRequest.method(), buffer, upstreamRequest, result);
//            });
//
//            return;
//        }
//
//        upstreamRequest.setChunked(true);
//
//        Handler transfer = new Handler() {
//            @Override
//            public void handle(Object data) {
//                upstreamRequest.write((Buffer) data);
//                if (upstreamRequest.writeQueueFull()) {
//                    clientRequest.pause();
//                    clientRequest.resume();
//                    upstreamRequest.handler(this);
//                }
//            }
//        };
//        clientRequest.handler(transfer);
//
//        clientRequest.exceptionHandler(e -> {
//            clientRequest.handler(null);
//            upstreamRequest.end();
//            result.handle(e);
//        });
//
//        clientRequest.endHandler(end -> {
//            result.handle(null);
//        });
//
//    }

    public void pass(Object reqId, HttpServerRequest clientRequest, HttpClientRequest upstreamRequest) {
        if (PassBodyOption.PassBodyType.ALL_HOLD.equals(passBodyType)) {
            upstreamRequest.end();
            logger.debug("api delivery none body ");
            return;
        }

        if (method != null && !inMethods(clientRequest.method())) {
            upstreamRequest.end();
            logger.debug("api don't delivery body : method not support {}", method);
            return;
        }

        if (decoder) { //如果需要解码
            clientRequest.bodyHandler(buffer -> {
                //在这里进行解码
                if(dataDecoder != null) {
                    Buffer decoded = Buffer.buffer(dataDecoder.decode(buffer));
                    buffer = decoded;
                }
                pass(reqId, clientRequest.method(), buffer, upstreamRequest);
            });

            return;
        }

        upstreamRequest.setChunked(true);
        Pump upstreamPump = Pump.pump(clientRequest, upstreamRequest);

        clientRequest.exceptionHandler(e -> {
            upstreamPump.stop();
            upstreamRequest.end();
            logger.debug("request:{} passbody error:", reqId, e);
        });

        /*
        客户端请求完毕，upstreamRequest也会完毕
         */
        clientRequest.endHandler(end -> {
            upstreamRequest.end();
            logger.debug("request:{} 透传 body 完成", reqId);
        });

        upstreamPump.start();

    }

    protected boolean inMethods(HttpMethod method) {
        for (HttpMethod m : this.method) {
            if (m.equals(method))
                return true;
        }

        return false;
    }
}
