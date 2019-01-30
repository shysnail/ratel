package com.kaitusoft.ratel.core.model.po;

import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/31
 *          <p>
 *          write description here
 */
@Data
@ToString
public class Group {

    private Short id;

    private String name;

    private String description;

    private Timestamp createTime;

    public boolean equals(Object obj) {
        try {
            Group other = (Group) obj;
            return name.equals(other.getName()) || id.equals(other.getId());
        } catch (Exception e) {
            return false;
        }

    }

}
