package com.itkevin.common.util;

import cn.hutool.core.util.NumberUtil;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.itkevin.common.model.SkyEyeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

@Slf4j
public class ConfigUtils {

    /**
     * 配置属性
     */
    private static final Properties properties = new Properties();

    public static final SkyEyeConfig skyEyeConfig = new SkyEyeConfig();

    /**
     * config instance
     * @return
     */
    public static Config getConfig() {
        return ConfigService.getConfig("skyeye");
    }

    /**
     * 获取String类型配置
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getProperty(String key, String defaultValue) {
        if (StringUtils.isBlank(key)) {
            return defaultValue;
        }
        try {
            return properties.getProperty(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 获取Integer类型配置
     * @param key
     * @param defaultValue
     * @return
     */
    public static Integer getIntProperty(String key, Integer defaultValue) {
        if (StringUtils.isBlank(key)) {
            return defaultValue;
        }
        try {
            Object o = properties.getOrDefault(key, defaultValue);
            return NumberUtil.isInteger(o.toString()) ? Integer.parseInt(o.toString()) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 获取Long类型配置
     * @param key
     * @param defaultValue
     * @return
     */
    public static Long getLongProperty(String key, Long defaultValue) {
        if (StringUtils.isBlank(key)) {
            return defaultValue;
        }
        try {
            Object o = properties.getOrDefault(key, defaultValue);
            return NumberUtil.isLong(o.toString()) ? Long.parseLong(o.toString()) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 存储配置
     * @param key
     * @param value
     */
    public static void saveProperty(String key, String value) {
        if (StringUtils.isBlank(key) || value == null) {
            return;
        }
        try {
            properties.put(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除配置
     * @param key
     */
    public static void removeProperty(String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        try {
            properties.remove(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
