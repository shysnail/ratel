package com.kaitusoft.ratel.core.model.option;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/31
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class SessionOption {

    private int interval;

    private String name;

}
