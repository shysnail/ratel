package com.kaitusoft.ratel.core.component;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/11
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class ZkConfig {

    private String zookeeperHosts;

    private String rootPath = "/ratel";

    private int sessionTimeout = 30000;

    private int connectTimeout = 3000;

}
