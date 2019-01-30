package com.kaitusoft.ratel.core.model.option;

import com.kaitusoft.ratel.core.handler.ProxyPolicy;
import com.kaitusoft.ratel.core.model.Target;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/26
 *          <p>
 *          write description here
 */
@Data
@ToString
public class UpstreamOption extends ProxyOption {

    private UpstreamThreadType threadType = UpstreamThreadType.APP;
    private int timeout;
    private short retry = 1;
    private int maxContentLength = -1;
    private int maxInitialLineLength = HttpClientOptions.DEFAULT_MAX_INITIAL_LINE_LENGTH;
    private int maxHeaderSize = HttpClientOptions.DEFAULT_MAX_HEADER_SIZE;
    private int maxPoolSize = HttpClientOptions.DEFAULT_MAX_POOL_SIZE;
    private boolean keepAlive = HttpClientOptions.DEFAULT_KEEP_ALIVE;
    private int keepAliveTimeout = HttpClientOptions.DEFAULT_KEEP_ALIVE_TIMEOUT;
    private int maxIdleTimeout = HttpClientOptions.DEFAULT_IDLE_TIMEOUT;
    private int maxWaitQueueSize = HttpClientOptions.DEFAULT_MAX_WAIT_QUEUE_SIZE;
    private boolean passQueryString = true;
    private PassBodyOption passBody;
    private Map<String, String> appendHeaders;
    private String[] removeHeaders;
    private HttpMethod methodForward;
    private ProxyPolicy.LoadBalance loadBalance;
    private Target[] targets;

    public UpstreamOption() {
        this.proxyType = ProxyType.UPSTREAM;
    }

    public enum UpstreamThreadType {
        APP, API;
    }

//    @Override
//    public String toJsonString() {
////        JsonObject object = new JsonObject();
////        object.put("threadType", threadType);
////        object.put("maxContentLength", maxContentLength);
////        object.put("maxInitialLineLength", maxInitialLineLength);
////        object.put("maxHeaderSize", maxHeaderSize);
////        object.put("maxPoolSize", maxPoolSize);
////        object.put("keepAlive", keepAlive);
////        object.put("keepAliveTimeout", keepAliveTimeout);
////        object.put("loadBalance", loadBalance);
////        object.put("targets", targets);
////        object.put("passQueryString", passQueryString);
////        object.put("passBody", passBody.toJson());
////        object.put("appendHeaders", appendHeaders);
////        object.put("removeHeaders", removeHeaders);
////        object.put("methodForward", methodForward);
////        return object;
//        return Json.encode(this);
//    }

}
