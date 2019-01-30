package com.kaitusoft.ratel.core.model;

import com.kaitusoft.ratel.core.handler.Proxy;
import com.kaitusoft.ratel.core.model.option.ApiExtendOption;
import com.kaitusoft.ratel.core.model.option.ProxyOption;
import com.kaitusoft.ratel.core.model.option.UpstreamOption;
import com.kaitusoft.ratel.core.model.po.ApiOption;
import com.kaitusoft.ratel.util.StringUtils;
import com.kaitusoft.ratel.util.URLUtil;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/21
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class Api {

    private App app;

    private Integer id;

    private String path;

    private String[] vhost;

    /**
     * 是否包含通配替换（通配符是 冒号+字符串）
     */
    private boolean wildcard;

    private String[] wildcards;

    /**
     * 是否是模糊，非正则
     */
    private boolean fuzzy;

    /**
     * 是否捕获参数（正则，且包含括号，每个括号是捕获的一组）
     */
    private boolean capture;

    /**
     * 路径规则是否正则表达式
     */
    private boolean isRegexp;

    private Preference preference;

    private Proxy proxy;

    private String root;

    private HttpClient httpClient;

    private ProxyOption proxyOption;

    private int running;

    public Api(App app, ApiOption option) throws Exception {
        this.app = app;
        this.id = option.getId();
        this.path = option.getPath();
        this.running = option.getRunning();
        if (!StringUtils.isEmpty(option.getVhost()) && !"*".equals(option.getVhost().trim())) {
            vhost = option.getVhost().split(" ");
        }

        analysis();

        JsonObject extendJson = new JsonObject(option.getParameter());
        ApiExtendOption extendOption = extendJson.mapTo(ApiExtendOption.class);

        if (extendOption.getPreferenceOption() != null) {
            preference = new Preference(extendOption.getPreferenceOption());
            root = extendOption.getPreferenceOption().getRoot();
            if (StringUtils.isEmpty(root) && extendOption.getPreferenceOption().isStaticServer())
                root = app.getRoot();

        } else if (app.getPreference() != null) //如果该路径本身没定义网关配置，使用app的网关设置。
            preference = app.getPreference();


        if (extendOption.getUpstreamOption() != null)
            proxyOption = extendOption.getUpstreamOption();
        else if (extendOption.getRedirectOption() != null)
            proxyOption = extendOption.getRedirectOption();
        else if (extendOption.getEchoOption() != null)
            proxyOption = extendOption.getEchoOption();


    }

    public boolean isPause(){
        return running == App.PAUSED;
    }

    /**
     * 分析路径，
     * 1.先判断是否使用了 通配规则 :param ，并提取出通配元素
     * 2.再判断是否使用了正则表达式，并判断是否设置为路径捕获，如果设置了路径捕获（即正则中用括号分组），提取出捕获规则
     * 3.判断是否模糊匹配
     */
    private void analysis() {
        Set<String> wildcards = URLUtil.getWildcard(path);
        setWildcard(wildcards.size() > 0);
        this.wildcards = new String[wildcards.size()];
        wildcards.toArray(this.wildcards);

        setCapture(URLUtil.isCapture(path));
        if (isCapture())
            setRegexp(true);
        else {
            String simple = path.replaceAll("\\*", "");
            setRegexp(URLUtil.isRegex2(simple));
        }

        setFuzzy(path.indexOf('*') > 0);
    }

    public void buildProxy(HttpClient httpClient) {
        this.httpClient = httpClient;
        proxy = Proxy.build(this, proxyOption);
    }

    public void buildProxy() {
        buildProxy(null);
    }


    public String assemble(HttpServerRequest request, Target target, String prefix, boolean passQueryString) {
        String requestPath = request.path();
        String requestUri = request.uri();

        String realUrl = assemble(target.getUrl(), prefix, requestPath);
//        String queryString = requestUri.substring(requestPath.length());

        if (!capture && !wildcard)
            return passQueryString ? appendQueryString(realUrl, requestUri.substring(requestPath.length())) : target.getUrl();

        String real = target.getUrl();
        if (target.isWildcard()) {
            String[] wildcards = target.getWildcards();
            for (int i = 0; i < wildcards.length; i++) {
                real = real.replaceAll(wildcards[i], request.getParam(wildcards[i].substring(1)));
            }
        }

        if (target.isCapture()) {
            String[] captures = target.getCaptures();
            for (int i = 0; i < captures.length; i++) {
                real = real.replaceAll(captures[i], request.getParam("param" + captures[i].substring(1)));
            }
        }

        return passQueryString ? appendQueryString(realUrl, requestUri.substring(requestPath.length())) : real;

    }

    /**
     * 装配路径，规则：
     * 1。如果 目标url以/结尾，表示全盘接受不了请求路径
     * 2。如果目标路径不以/结尾，表示从匹配路径后拼接
     *
     * @param url
     * @param prefix
     * @param requestUri
     * @return
     */
    private String assemble(String url, String prefix, String requestUri) {
        StringBuilder real = new StringBuilder(url);
        if (real.charAt(real.length() - 1) == '/')
            return real.deleteCharAt(real.length() - 1).append(requestUri).toString();

        String appendPath = requestUri.substring(prefix.length());
        if (!appendPath.startsWith("/"))
            return real.append("/").append(appendPath).toString();

        return real.append(appendPath).toString();
    }

    private String appendQueryString(String origin, String queryString) {
        if (StringUtils.isEmpty(queryString))
            return origin;

        StringBuilder sb = new StringBuilder(origin);
        int paramStart = origin.indexOf('?');
        if (paramStart == -1) {
            sb.append('?');
        } else if (paramStart == (origin.length() - 1)) {
            sb.append('&');
        }
        sb.append(queryString.substring(1));
        return sb.toString();
    }

    public HttpClientOptions buildHttpClientOption() {
        UpstreamOption upstreamOption = (UpstreamOption) proxyOption;
        return buildHttpClientOption(upstreamOption);
    }

    public HttpClientOptions buildHttpClientOption(UpstreamOption upstreamOption) {
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        if (upstreamOption == null)
            return httpClientOptions;
        httpClientOptions.setMaxPoolSize(upstreamOption.getMaxPoolSize());
        httpClientOptions.setMaxInitialLineLength(upstreamOption.getMaxInitialLineLength());
        httpClientOptions.setMaxHeaderSize(upstreamOption.getMaxHeaderSize());
        httpClientOptions.setKeepAliveTimeout(upstreamOption.getKeepAliveTimeout());
        httpClientOptions.setKeepAlive(upstreamOption.isKeepAlive());
        httpClientOptions.setConnectTimeout(upstreamOption.getTimeout());
        httpClientOptions.setIdleTimeout(upstreamOption.getMaxIdleTimeout());
        httpClientOptions.setMaxWaitQueueSize(upstreamOption.getMaxWaitQueueSize());
        httpClientOptions.setPipelining(true);
        httpClientOptions.setPipeliningLimit(5);
        httpClientOptions.setTcpFastOpen(true);
        httpClientOptions.setTcpNoDelay(true);
        httpClientOptions.setTcpQuickAck(true);

        return httpClientOptions;
    }

    public HttpClientOptions buildWsClientOption() {
        HttpClientOptions httpClientOptions = buildHttpClientOption();
        httpClientOptions.setMaxPoolSize(1);
        httpClientOptions.setKeepAlive(true);
        httpClientOptions.setKeepAliveTimeout(0);
        httpClientOptions.setIdleTimeout(30000);
//        httpClientOptions.setSendUnmaskedFrames(true);
        return httpClientOptions;
    }
}
