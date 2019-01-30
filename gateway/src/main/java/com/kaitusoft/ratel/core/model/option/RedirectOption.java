package com.kaitusoft.ratel.core.model.option;

import lombok.Data;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/2
 *          <p>
 *          write description here
 */
@Data
@ToString
public class RedirectOption extends ProxyOption {

    private int code = 302;
    private boolean passQueryString = false;
    private String url;

    public RedirectOption() {
        this.proxyType = ProxyType.REDIRECT;
    }

//    @Override
//    public String toJsonString() {
//        return Json.encode(this);
//    }
}
