package com.kaitusoft.ratel.core.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/9/4
 *          <p>
 *          write description here
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Upload {
    private String tmpdir = "file-uploads";

    private String size;
}
