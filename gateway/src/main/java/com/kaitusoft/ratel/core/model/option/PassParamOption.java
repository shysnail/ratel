package com.kaitusoft.ratel.core.model.option;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/24
 *          <p>
 *          write description here
 */
public class PassParamOption {

    private PassParamType passDataType;

    private String option;

    public enum PassParamType {
        ALL_PASS, ALL_HOLD, PASS_BY_METHODS;
    }
}
