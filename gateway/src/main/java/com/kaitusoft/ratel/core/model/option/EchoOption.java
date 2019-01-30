package com.kaitusoft.ratel.core.model.option;

import com.kaitusoft.ratel.Result;
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
public class EchoOption extends ProxyOption {

    private ProxyType proxyType = ProxyType.UPSTREAM;

    private Result echo;

    private EchoOption() {
        this.proxyType = ProxyType.ECHO;
    }

//    @Override
//    public String toJsonString() {
//        return Json.encode(this);
//    }
}
