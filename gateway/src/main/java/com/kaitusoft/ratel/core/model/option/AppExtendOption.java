package com.kaitusoft.ratel.core.model.option;

import com.kaitusoft.ratel.core.model.CrossDomain;
import com.kaitusoft.ratel.core.model.Ssl;
import io.vertx.core.json.Json;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/26
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class AppExtendOption {

    private PreferenceOption preferenceOption;

    private Ssl ssl;

    private CrossDomain crossDomain;

    private SessionOption sessionOption;

    private UpstreamOption upstreamOption;

    private String blowSetting;

    public String toJsonString() {
        return Json.encode(this);
    }
}
