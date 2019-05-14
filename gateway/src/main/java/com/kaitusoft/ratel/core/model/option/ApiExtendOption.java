package com.kaitusoft.ratel.core.model.option;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/5
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class ApiExtendOption {

    private PreferenceOption preferenceOption;

    private UpstreamOption upstreamOption;

    private RedirectOption redirectOption;

    private EchoOption echoOption;

    private SecurityOption securityOption;
}
