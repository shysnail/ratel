package com.kaitusoft.ratel.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author frog.w
 * @version 1.0.0, 2018/10/29
 *          <p>
 *          write description here
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CustomCode {

    private int code;

    private String contentType;

    private String content;
}
