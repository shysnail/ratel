package com.kaitusoft.ratel.core.component;

import lombok.Data;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/29
 *          <p>
 *          write description here
 */
@Data
@ToString
public class Extend {


    private Long blockedThreadCheckInterval = 1000L;

    private Integer eventLoopPoolSize;

    private Integer workerPoolSize;


}
