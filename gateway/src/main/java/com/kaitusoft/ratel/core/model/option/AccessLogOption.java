package com.kaitusoft.ratel.core.model.option;

import com.kaitusoft.ratel.core.common.Env;
import lombok.Data;
import lombok.ToString;

import java.io.File;

/**
 * @author frog.w
 * @version 1.0.0, 2019/5/17
 *          <p>
 *          write description here
 */
@Data
@ToString
public class AccessLogOption {
    public static final String DEFAULT_FORMAT = "[$time_local] $method:$uri $request_time $status $http_referer $http_User-Agent";

    private String format = DEFAULT_FORMAT;

    private String savePath = Env.HOME + "logs" + File.separator + "access.log";
}
