package com.kaitusoft.ratel.core.model.dao;

import io.vertx.ext.jdbc.JDBCClient;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author frog.w
 * @version 1.0.0, 2019/1/8
 *          <p>
 *          write description here
 */
@Data
@AllArgsConstructor
public class BaseDao {
    protected JDBCClient jdbcClient;

}
