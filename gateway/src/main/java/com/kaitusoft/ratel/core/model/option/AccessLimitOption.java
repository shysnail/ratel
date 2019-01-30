package com.kaitusoft.ratel.core.model.option;

import com.kaitusoft.ratel.Result;
import com.kaitusoft.ratel.core.common.TimeUnitEnum;
import io.vertx.core.json.Json;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/14
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class AccessLimitOption implements Serializable {

    private static final long serialVersionUID = 6099545312845334341L;

    /**
     * 接口请求限制
     */
    private int limit = -1;

    private int limitPerIp = -1;

    private int limitPerClient = -1;

    private String ipHeaderKey;

    /**
     * 配合时间单位使用，才是完整的周期
     */
    private int interval = 1;

    private TimeUnitEnum timeUnit = TimeUnitEnum.MINUTE;

    private String[] keys;

    private Result overloadedReturn;


    private String toJson() {
        return Json.encode(this);
    }
}
