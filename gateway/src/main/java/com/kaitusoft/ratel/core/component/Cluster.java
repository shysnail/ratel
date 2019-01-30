package com.kaitusoft.ratel.core.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/10
 *          <p>
 *          write description here
 */
@Data
@ToString
@AllArgsConstructor
public class Cluster {

    private static final int DEFAULT_SESSION_TIMEOUT = 600000;

    private static final int DEFAULT_CONNECTION_TIMEOUT = 3000;

    private boolean enabled;

    private String host;

    private int port = 5677;

    private int pingInterval = 30000;

    private int pingRetryInterval = 30000;

    private String zookeeperHosts;

    private String rootPath = "com.kaitusoft.ratel";

    private int sessionTimeoutMs = DEFAULT_SESSION_TIMEOUT;

    private int connectionTimeoutMs = DEFAULT_CONNECTION_TIMEOUT;

    private Retry retry = new Retry();

    public Cluster(boolean enabled) {
        this.enabled = enabled;
    }

    public Cluster() {
        this(false);
    }


    @Data
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Retry{

        private static final int DEFAULT_RETRY_INITIAL_SLEEPTIME = 1000;

        private static final int DEFAULT_RETRY_MAX_TIMES = 3;

        private static final int DEFAULT_RETRY_MAX_SLEEP = 3000;


        private int initialSleepTimeMs = DEFAULT_RETRY_INITIAL_SLEEPTIME;

        private int maxTimes = DEFAULT_RETRY_MAX_TIMES;

        private int maxSleepMs = DEFAULT_RETRY_MAX_SLEEP;
    }

}
