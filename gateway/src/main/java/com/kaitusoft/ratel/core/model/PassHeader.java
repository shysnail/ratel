package com.kaitusoft.ratel.core.model;

import com.kaitusoft.ratel.core.model.option.PassBodyOption;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/24
 *          <p>
 *          write description here
 */
public class PassHeader extends PassData {

    private static final Logger logger = LoggerFactory.getLogger(PassHeader.class);

    private Map<String, String> headers = new HashMap<>();

    public static PassHeader fromModel(PassBodyOption passDataOption) {
        PassHeader passHeader = new PassHeader();
        passHeader.passDataType = passDataOption.getPassBodyType();

        JsonObject object = new JsonObject(passDataOption.getOption());

        if (passHeader.passDataType == PassBodyOption.PassBodyType.PASS_BY_METHODS) {
            String methodStr = object.getString("method");
            passHeader.method = PassData.build(methodStr);

            JsonObject headers = object.getJsonObject("headers");
            if (headers != null)
                passHeader.headers = headers.mapTo(HashMap.class);
        }

        return passHeader;
    }

    public PassBodyOption toModel() {
        PassBodyOption passDataOption = new PassBodyOption();
        passDataOption.setPassBodyType(this.passDataType);
        JsonObject option = new JsonObject();
        if (method != null)
            option.put("method", method);
        if (headers != null && headers.size() > 0)
            option.put("headers", headers);

        passDataOption.setOption(option.toString());

        return passDataOption;
    }

    public void pass(HttpServerRequest clientRequest, HttpClientRequest upstreamRequest) {
        pass(clientRequest, upstreamRequest, null);
    }

    public void pass(HttpServerRequest clientRequest, HttpClientRequest upstreamRequest, Handler<Void> result) {
        if (passDataType == PassBodyOption.PassBodyType.ALL_HOLD) {
            if (result != null)
                result.handle(null);
            return;
        }

        if (method != null && !inMethods(clientRequest.method())) {
            if (result != null)
                result.handle(null);
            return;
        }

        /**
         * 没指定透传哪些header，透传所有header
         */
        if (headers == null || headers.size() == 0) {

        }

    }

}