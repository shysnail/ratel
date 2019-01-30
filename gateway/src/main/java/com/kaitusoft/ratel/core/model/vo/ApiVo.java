package com.kaitusoft.ratel.core.model.vo;

import com.kaitusoft.ratel.core.model.option.ApiExtendOption;
import com.kaitusoft.ratel.core.model.po.ApiOption;
import com.kaitusoft.ratel.core.model.po.AppOption;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/3
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class ApiVo extends ApiOption {

    private static final long serialVersionUID = -5943738617632781379L;

    private AppOption app;

    private ApiExtendOption extendOption;

    public JsonObject toModleJson() {
//        JsonObject parameter = new JsonObject();
//        parameter.put("preferenceOption", JsonObject.mapFrom(preferenceOption));
//        parameter.put("proxyOption", JsonObject.mapFrom(proxyOption));
//
//        setParameter(parameter.toString());
        JsonObject jsonObject = JsonObject.mapFrom(this);
        jsonObject.remove("preferenceOption");
        jsonObject.remove("proxyOption");

        return jsonObject;
    }
}
