package com.itkevin.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

@Slf4j
public class PropertiesUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtils.class);

    private PropertiesUtils() {
    }

    private static volatile Properties PROPERTIES;

    public static final String SYSTEM_GLOBAL = "SystemGlobals.properties";

    public static Properties getProperties() {
        if (PROPERTIES == null) {
            synchronized (PropertiesUtils.class) {
                if (PROPERTIES == null) {
                    String path = System.getProperty(SYSTEM_GLOBAL);
                    if (path == null || path.length() == 0) {
                        path = System.getenv(SYSTEM_GLOBAL);
                        if (path == null || path.length() == 0) {
                            path = SYSTEM_GLOBAL;
                        }
                    }
                    PROPERTIES = loadProperties(path, false, true);
                }
            }
        }
        return PROPERTIES;
    }

    public static void addProperties(Properties properties) {
        if (properties != null) {
            getProperties().putAll(properties);
        }
    }

    public static void setProperties(Properties properties) {
        if (properties != null) {
            PROPERTIES = properties;
        }
    }

    public static String getProperty(String key) {
        return getProperty(key, null);
    }

    public static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null && value.trim().length() == 0) {
            return null;
        }
        if (value != null) {
            return value;
        }
        Properties properties = getProperties();
        value = properties.getProperty(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public static Properties loadProperties(String fileName) {
        return loadProperties(fileName, false, false);
    }

    public static Properties loadProperties(String fileName, boolean allowMultiFile) {
        return loadProperties(fileName, allowMultiFile, false);
    }

    public static Properties loadProperties(String fileName, boolean allowMultiFile, boolean optional) {
        Properties properties = new Properties();
        if (fileName.startsWith("/")) {
            try {
                FileInputStream input = new FileInputStream(fileName);
                try {
                    properties.load(input);
                }
                finally {
                    input.close();
                }
            }
            catch (Throwable e) {
                logger.warn(
                        "Failed to load " + fileName + " file from " + fileName + "(ingore this file): "
                                + e.getMessage(), e);
            }
            return properties;
        }

        List<URL> list = new ArrayList<URL>();
        try {
            Enumeration<URL> urls = PropertiesUtils.class.getClassLoader().getResources(fileName);
            list = new ArrayList<URL>();
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        }
        catch (Throwable t) {
            logger.warn("Fail to load " + fileName + " file: " + t.getMessage(), t);
        }

        if (list.size() == 0) {
            if (!optional) {
                logger.warn("No " + fileName + " found on the class path.");
            }
            return properties;
        }

        if (!allowMultiFile) {
            if (list.size() > 1) {
                String errMsg = String.format(
                        "only 1 %s file is expected, but %d dubbo.properties files found on class path: %s", fileName,
                        list.size(), list.toString());
                logger.warn(errMsg);
                // throw new IllegalStateException(errMsg); // see
                // http://code.alibabatech.com/jira/browse/DUBBO-133
            }

            // fall back to use method getResourceAsStream
            try {
                properties.load(PropertiesUtils.class.getClassLoader().getResourceAsStream(fileName));
            }
            catch (Throwable e) {
                logger.warn(
                        "Failed to load " + fileName + " file from " + fileName + "(ingore this file): "
                                + e.getMessage(), e);
            }
            return properties;
        }

        logger.info("load " + fileName + " properties file from " + list);

        for (URL url : list) {
            try {
                Properties p = new Properties();
                InputStream input = url.openStream();
                if (input != null) {
                    try {
                        p.load(input);
                        properties.putAll(p);
                    }
                    finally {
                        try {
                            input.close();
                        }
                        catch (Throwable t) {}
                    }
                }
            }
            catch (Throwable e) {
                logger.warn("Fail to load " + fileName + " file from " + url + "(ingore this file): " + e.getMessage(),
                        e);
            }
        }

        return properties;
    }

    public static void setProperty(String key, String value) {

        Properties properties = getProperties();
        properties.setProperty(key, value);
    }
}
