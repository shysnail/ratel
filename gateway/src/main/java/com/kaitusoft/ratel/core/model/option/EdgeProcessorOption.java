package com.kaitusoft.ratel.core.model.option;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/14
 *          <p>
 *          write description here
 */
@Data
@ToString
@NoArgsConstructor
public class EdgeProcessorOption implements Serializable {

    private static final long serialVersionUID = -4321020529567356296L;

    private String name;

    private String instance;

    private String usage;
}
