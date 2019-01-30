package com.kaitusoft.ratel.core.model;

import com.kaitusoft.ratel.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/17
 *          <p>
 *          write description here
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Blow {

    private boolean open;

    private BlowCondition[] blowConditions;

    private BlowCondition[] recoverConditions;

    private Result result;
}
