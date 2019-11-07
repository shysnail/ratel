package com.kaitusoft.ratel.core.model;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/17
 *          <p>
 *          write description here
 */
public class BlowCondition {

    private Condition condition;

    private Operator operator;

    private String value;

    enum Condition {
        ERROR, TIMEOUT;
    }

    enum Operator {
        GT, LT;
    }

    enum Relation {
        AND, OR;
    }
}
