package com.kaitusoft.ratel.core.model.option;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/20
 *          <p>
 *          store as json format, option determined by proxyType
 *          proxyOption:{
 *          proxyType:UPSTREAM,
 *          option:{
 *          loadBalance: RANDOM/POLLING_AVAILABLE...,
 *          targets:[{url:sss, weight:1},{...}]
 *          }
 *          }
 *          <p>
 *          proxyOption:{
 *          proxyType:REDIRECT,
 *          option: sdfsf
 *          }
 *          <p>
 *          proxyOption:{
 *          proxyType:ECHO,
 *          option:{
 *          Result.json
 *          }
 *          }
 */
@Data
@ToString
@NoArgsConstructor
public abstract class ProxyOption implements Cloneable {

    protected ProxyType proxyType = ProxyType.UPSTREAM;

    public static ProxyOption fromJson(JsonObject option) {
        String proxyType = option.getString("proxyType");
        if ("UPSTREAM".equalsIgnoreCase(proxyType)) {
            return option.mapTo(UpstreamOption.class);
        } else if ("REDIRECT".equalsIgnoreCase(proxyType)) {
            return option.mapTo(RedirectOption.class);
        } else if ("ECHO".equalsIgnoreCase(proxyType)) {
            return option.mapTo(EchoOption.class);
        } else if (proxyType == null) {
            return option.mapTo(UpstreamOption.class);
        } else {
            throw new IllegalStateException("wrong proxy option!");
        }
    }

    public String toJsonString() {
        return Json.encode(this);
    }

    public enum ProxyType {
        UPSTREAM, REDIRECT, ECHO;
    }
}
