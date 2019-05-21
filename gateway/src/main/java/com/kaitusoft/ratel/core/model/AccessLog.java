package com.kaitusoft.ratel.core.model;

import com.kaitusoft.ratel.ContextAttribute;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.async.AsyncLogger;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.filter.ThreadContextMapFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.KeyValuePair;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author frog.w
 * @version 1.0.0, 2019/5/17
 *          <p>
 *          write description here
 */
public class AccessLog {

    private static final String LOGNAME = AccessLog.class.getName();

    private String format;

    private String savePath;

    private Logger logger;

    private String appLogName;

    private String template;

    private List<String> keys = new ArrayList<>();

    //为false时，返回多个LoggerContext对象，   true：返回唯一的单例LoggerContext
    private static final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    private static final Configuration config = ctx.getConfiguration();
    private static LoggerConfig loggerConfig;
    static{
        loggerConfig = config.getLoggerConfig(LOGNAME);
        if(loggerConfig == null){
//            config.get
            loggerConfig = AsyncLoggerConfig.createLogger(true, Level.ALL, LOGNAME, "true", new AppenderRef[]{}, new Property[]{}, config, null);
        }
    }

    public AccessLog(App app, String format, String savePath) throws RuntimeException{
        this.format = format;
        this.savePath = savePath;
        if(!valid(format))
            throw new IllegalArgumentException("日志格式不正确");
        this.template = format;

        appLogName = "app_" + app.getId();
        createLog();
    }

    private void createLog(){
        parse();
        PatternLayout layout = PatternLayout.newBuilder()
                .withCharset(Charset.forName("UTF-8"))
                .withConfiguration(config)
                .withPattern("%m%n")
                .build();
        final TriggeringPolicy policy = TimeBasedTriggeringPolicy.newBuilder()
                .withModulate(true)
                .withInterval(1)
                .build();
        DefaultRolloverStrategy strategy = DefaultRolloverStrategy.createStrategy(
                "7", "1", null, null, null, false, config);
        Appender appender = RollingFileAppender.newBuilder()
                .withName(appLogName)
                .withFileName(savePath)
                .withImmediateFlush(false)
                .withFilePattern(savePath + ".%d{yyyy-MM-dd-a}.txt")
                .withLayout(layout)
                .withPolicy(policy)
                .withStrategy(strategy)
                .build();
        appender.start();
        loggerConfig.addAppender(appender, Level.ALL, null);

        ctx.updateLoggers(config);

        logger = ctx.getLogger(LOGNAME);
    }

    public void destroy() {
        synchronized (config){
            if(loggerConfig != null){
                Appender appender = loggerConfig.getAppenders().get(appLogName);
                if(appender != null) {
                    appender.stop();
                    loggerConfig.removeAppender(appLogName);
                }
            }

            ctx.updateLoggers();
        }
    }

    public static boolean valid(String format){
        return true;
    }

    /**
     * 先预处理format，提取出要取得信息。
     * 定义格式字典部分兼容nginx格式，
     * 字典内容如下：
     * remote_addr
     * uri  -- full request，include args
     * path -- request path，no args
     * method -- request method
     * args -- 请求中的参数，querystring段
     * args_insist --请求中的参数，get/post均有效
     * scheme -- http|https
     * time_local|time_gmt|time_utc
     * http_{header_name}
     * cookie_{cookie_name}
     * request_time    收到客户端第一个字节，到最后一个字节发送回客户端，中间的时长
     * request_length  请求头和请求体大小
     * status -- response code
     * bytes_sent
     * body_bytes_sent
     * upstream_addr
     */
    private void prepare(){

    }

    /**
     * 日志可记录请求头，
     * 这个方法后续会全异步
     * @param context
     */
    public void log(RoutingContext context){
        Object[] data = new Object[keys.size()];
        for(int i = 0; i < keys.size(); i ++){
            String key = keys.get(i);
            Object v = null;
            if(key.startsWith("http_")){
                String head = key.substring(5);
                v = context.request().getHeader(head);
            }else if(key.startsWith("cookie_")){
                String cookieName = key.substring(7);
                Cookie cookie = context.getCookie(cookieName);
                if(cookie != null)
                    v = cookie.getValue();
            }else{
                v = context.get(key);
                if(key.equals(ContextAttribute.CTX_TIME_LOCAL)){
//                    Instant instant = Instant.now();
//                    Date date = new Date();
                    Instant instant = Instant.ofEpochMilli((Long) v);
                    v = instant.atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                }else{

                }
            }

            v = v == null ? "-" : v;

            data[i] = v;
        }

        logger.info(template, data);
    }

    private final static String regexp = "((\\$[a-zA-Z])[a-zA-Z0-9_-]{0,})|((\\$_)[a-zA-Z0-9_-]{1,})";
    private final static Pattern pattern = Pattern.compile(regexp);
    private String parse(){
        StringBuilder msg = new StringBuilder();
        Matcher matcher = pattern.matcher(format);
        while(matcher.find()){
            String s = matcher.group();
            template = template.replace(s, "{}");
            String key = s.substring(1);
//            if(!key.startsWith("cookie"))
//                key = key.toLowerCase();
            keys.add(key);
        }

        return msg.toString();
    }
}
