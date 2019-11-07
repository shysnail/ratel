package com.kaitusoft.ratel.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/18
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class Ssl {

    private int port = -1;

    private CertType certType;

    private String keyPath;

    private String certPath;

    private boolean publish;

    public enum CertType {
        PFX, PEM, JKS;
    }
}
