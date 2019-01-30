package com.kaitusoft.ratel.core.model;

import com.kaitusoft.ratel.core.model.option.PassBodyOption;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.http.HttpMethod;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/24
 *          <p>
 *          write description here
 */
public abstract class PassData {

    protected PassBodyOption.PassBodyType passDataType;

    protected HttpMethod[] method;

    static HttpMethod[] build(String methodStr) {
        if (StringUtils.isEmpty(methodStr))
            return null;


        String[] methods = methodStr.split(",");
        HttpMethod[] method = new HttpMethod[methods.length];

        for (int i = 0; i < methods.length; i++) {
            method[i] = HttpMethod.valueOf(methods[i]);
        }

        return method;

    }

    protected boolean inMethods(HttpMethod method) {
        for (HttpMethod m : this.method) {
            if (m.equals(method))
                return true;
        }

        return false;
    }
}
