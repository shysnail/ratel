package com.kaitusoft.ratel.core.component;

import lombok.Data;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/19
 *          <p>
 *          write description here
 */
@Data
@ToString
public class Console {

    private int port;

    private String favicon;

    private String uploadTempDir;
}
