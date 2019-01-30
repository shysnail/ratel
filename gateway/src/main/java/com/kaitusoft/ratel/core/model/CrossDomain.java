package com.kaitusoft.ratel.core.model;

import io.vertx.core.http.HttpMethod;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/18
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class CrossDomain {

    private String allowedOrigin;
    private boolean allowCredentials;
    private int maxAgeSeconds;

    private Set<HttpMethod> allowedMethods;
    private Set<String> allowedHeaders;
    private Set<String> exposedHeaders;

}
