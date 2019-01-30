package com.kaitusoft.ratel.core.model.option;

import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/24
 *          <p>
 *          write description here
 */
@Data
@ToString
public class PassBodyOption {

    private PassBodyType passBodyType;

    private String option;

    private boolean decode;

    private String decodeOption;

    public JsonObject toJsonObject() {
        return JsonObject.mapFrom(this);
    }

    public enum PassBodyType {
        ALL_PASS, ALL_HOLD, PASS_BY_METHODS;
    }
}
