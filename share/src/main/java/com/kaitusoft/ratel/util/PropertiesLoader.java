package com.kaitusoft.ratel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * @author frog.w
 * @version 1.0.0, 2018/1/11
 *          <p>
 *          write description here
 */
public class PropertiesLoader {

    private static Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

    public static Properties load(String... files) throws IOException {
        Properties prop = new Properties();

        if (files == null || files.length == 0)
            return prop;

        for (String file : files) {
            prop.putAll(load(file));
        }

        return prop;

    }

    public static Properties load(String file) throws IOException {
        Properties prop = new Properties();

        InputStream is = null;
        try{
            String target = ResourceUtil.findFileInClasspath(file);
            File f = new File(target);

            if (f != null && f.exists()) {
                is = new FileInputStream(f);
            }
        }catch (Exception e){
            logger.warn("no file found,{}", e.getMessage());
            try {
                is = PropertiesLoader.class.getResourceAsStream("/" + file);
            } catch (Exception ex) {
                try {
                    is = PropertiesLoader.class.getResourceAsStream("/component/" + file);
                } catch (Exception e1) {
                    throw new IOException(e1);
                }
            }
        }

        if (is == null)
            throw new IllegalStateException("没有找到有效的配置文件，确保classpath下存在" + file);

        BufferedReader bfd = new BufferedReader(new InputStreamReader(is));

        prop.load(bfd);

        if(is != null)
            is.close();

        return prop;
    }
}
