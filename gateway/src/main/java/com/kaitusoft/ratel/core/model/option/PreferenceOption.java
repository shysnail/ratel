package com.kaitusoft.ratel.core.model.option;

import com.kaitusoft.ratel.Result;
import io.vertx.core.json.Json;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/13
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class PreferenceOption {

    private boolean staticServer;

    private String root;

    private String method;

    private String[] ipBlacklist;

    private AccessLimitOption accessLimitOption;

    private AuthOption authOption;

    private List<EdgeProcessorOption> preProcessors;

    private List<EdgeProcessorOption> postProcessors;

    private List<Result> customCodes;

    private Result stopServiceReturn;

    public String toJsonString() {
        return Json.encode(this);
    }

}