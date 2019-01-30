package com.kaitusoft.ratel.core.common;

import com.kaitusoft.ratel.core.component.*;
import com.kaitusoft.ratel.util.StringUtils;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author frog.w
 * @version 1.0.0, 2018/8/10
 *          <p>
 *          write description here
 */
@Data
@ToString
public class Configuration {

    public static final String CONFIG_FILE = "/ratel.yml";
    public static final String SESSION_NAME = "RATEL";
    public static final String MODEL_CODEC = "model.codec";
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    public static String hostname = "unknown";
    public static String OFFICIAL_NAME = "RATEL-1.0";
    public static String DOMAIN = null;
    private String name = "RATEL";
    private String version = "UNKNOWN";
    private int port = -1;
    private String domainName = null;
    private String mode = "product";

    private Database database;

    private Cluster cluster;

    private Monitor monitor;

    private String favicon = "favicon.ico";

    private Upload upload;

    private Console console = new Console();

    private Extend extend;

    private JsonObject systemOption = new JsonObject();

    private JsonObject consoleOption = new JsonObject();

    private JsonObject databaseOption = new JsonObject();

    private JsonObject clusterOption = new JsonObject();

    public Configuration() {

    }

//    private js class SingletonHolder{
//        private final js Configuration instance = load();
//    }
//
//    public js Configuration getInstance(){
//        return SingletonHolder.instance;
//    }

    public static Configuration load() {
        logger.info("read component from {}", CONFIG_FILE);
        Yaml yaml = new Yaml();
        InputStream in = Configuration.class.getResourceAsStream(CONFIG_FILE);
        Configuration config = yaml.loadAs(in, Configuration.class);

        config.getConsoleOption().put("port", config.getPort()).put("debug", config.getMode().equalsIgnoreCase("DEV"));

        config.setClusterOption(JsonObject.mapFrom(config.getCluster()));

        config.setDatabaseOption(JsonObject.mapFrom(config.getDatabase()));
        if (!StringUtils.isEmpty(config.getDatabase().getProviderClass())) {
            config.getDatabaseOption().put("provider_class", config.getDatabase().getProviderClass());
        } else {
            config.getDatabaseOption().put("driver_class", config.getDatabase().getDriverClass());
        }

        config.getSystemOption().put("version", config.getVersion());
        OFFICIAL_NAME = config.name + "-" + config.version;
        DOMAIN = config.domainName;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("canot get hostname ,use default");
        }

        return config;
    }


    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public JsonObject getSystemOption() {
        return systemOption;
    }

    public void setSystemOption(JsonObject systemOption) {
        this.systemOption = systemOption;
    }

    public JsonObject getConsoleOption() {
        return consoleOption;
    }

    public void setConsoleOption(JsonObject consoleOption) {
        this.consoleOption = consoleOption;
    }

    public JsonObject getDatabaseOption() {
        return databaseOption;
    }

    public void setDatabaseOption(JsonObject databaseOption) {
        this.databaseOption = databaseOption;
    }

    public JsonObject getClusterOption() {
        return clusterOption;
    }

    public void setClusterOption(JsonObject clusterOption) {
        this.clusterOption = clusterOption;
    }
}
