package com.kaitusoft.ratel.core.component;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.alibaba.druid.pool.DruidAbstractDataSource.*;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/10
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class Database {

    private String providerClass;

    private String driverClass;

    private String url;

    private String username;

    private String password;

    private int maxActive = DEFAULT_MAX_ACTIVE_SIZE;

    private int minIdle = DEFAULT_MIN_IDLE;

    private int initialSize = DEFAULT_INITIAL_SIZE;

    private int maxWait = DEFAULT_MAX_WAIT;

    private boolean testWhileIdle = DEFAULT_WHILE_IDLE;

}
