package com.kaitusoft.ratel.core.model.option;

import com.kaitusoft.ratel.Result;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/28
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class DataDecoderOption implements Serializable {

    private static final long serialVersionUID = 1081736992837666846L;

    private String name;

    private String instance;

    private String usage;

    private Result failReturn;
}
