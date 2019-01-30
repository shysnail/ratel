package com.kaitusoft.ratel.core.common;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/15
 *          <p>
 *          write description here
 */
public enum TimeUnitEnum {

    SECOND(1), MINUTE(60), HOUR(3600), DAY(86400), WEEK(7 * 86400);

    private long seconds;

    private TimeUnitEnum(long seconds) {
        this.seconds = seconds;
    }

    public long getSeconds() {
        return seconds;
    }

    public long getMillSeconds() {
        return seconds * 1000;
    }
}