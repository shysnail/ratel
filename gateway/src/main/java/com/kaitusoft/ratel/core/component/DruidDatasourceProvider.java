package com.kaitusoft.ratel.core.component;

import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.pool.DruidDataSource;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.spi.DataSourceProvider;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/20
 *          <p>
 *          write description here
 */
public class DruidDatasourceProvider extends DruidDataSource implements DataSourceProvider {

    private static final long serialVersionUID = -114714925303957692L;

    @Override
    public int maximumPoolSize(DataSource dataSource, JsonObject config) throws SQLException {
        return config.getInteger("maxPoolSize", 5).intValue();
    }

    public DataSource getDataSource(JsonObject config) throws SQLException {
        Log4j2Filter log4j2 = new Log4j2Filter();
        DruidDataSource ds = new DruidDataSource();
        ds.getProxyFilters().add(log4j2);

        ds.setDriverClassName(config.getString("driverClass"));
        ds.setUrl(config.getString("url"));
        ds.setUsername(config.getString("username"));
        ds.setPassword(config.getString("password"));
        ds.setMaxActive(config.getInteger("maxActive", 5));
        ds.setMinIdle(config.getInteger("minIdle", 1));
        ds.setInitialSize(config.getInteger("initialSize", 1));
        ds.setMaxWait(config.getInteger("maxWait", 20000));

        ds.setTestWhileIdle(true);
        ds.setRemoveAbandoned(true);
        ds.setRemoveAbandonedTimeout(60);
        ds.setLogAbandoned(true);
        ds.setFilters("stat,wall,config");
        ds.setValidationQuery(config.getValue("validationQuery", "select 1").toString());

        return ds;
    }

    public void close(DataSource dataSource) throws SQLException {
        if ((dataSource instanceof DruidDataSource))
            ((DruidDataSource) dataSource).close();
    }
}
