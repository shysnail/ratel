package com.kaitusoft.ratel.core.model.vo;

import com.kaitusoft.ratel.core.model.option.AppExtendOption;
import com.kaitusoft.ratel.core.model.po.ApiOption;
import com.kaitusoft.ratel.core.model.po.AppOption;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Set;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/3
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class AppVo extends AppOption implements Serializable {

    private static final long serialVersionUID = -270650786187744024L;
    /**
     * parameter 和  AppExtendOption互相转化
     */

    private Set<ApiOption> apis;

    private boolean running;

    private int runningApiNum;

    private AppExtendOption extendOption;

    public static AppVo fromJson(JsonObject jsonObject) {
        return jsonObject.mapTo(AppVo.class);
    }

    public JsonObject toModleJson() {
//        setParameter(extendOption.toJsonString());
        JsonObject jsonObject = JsonObject.mapFrom(this);
        jsonObject.remove("extendOption");
        jsonObject.remove("pathOptions");
        return jsonObject;
    }


}
