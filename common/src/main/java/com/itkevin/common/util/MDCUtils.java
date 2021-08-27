package com.itkevin.common.util;

import cn.hutool.core.collection.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.Map;

public class MDCUtils {

    /**
     * MDC 获取数据
     * @param key
     * @return
     */
    public static String get(String key) {
        String value = null;
        try {
            if (StringUtils.isNotBlank(key)) {
                value = MDC.get(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * MDC 获取数据
     * @param key
     * @param defaultValue
     * @return
     */
    public static String get(String key,String defaultValue) {
        String value = null;
        try {
            if (StringUtils.isNotBlank(key)) {
                value = MDC.get(key);
                value = value != null ? value : defaultValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * MDC put数据
     * @param key
     * @param value
     */
    public static void put(String key,String value) {
        try {
            if (StringUtils.isNotEmpty(key) && value != null) {
                MDC.put(key,value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制一份MDC
     * @return
     */
    public static Map<String, String> getCopyMDC() {
        try {
            return MDC.getCopyOfContextMap();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置MDC
     * @param mdc
     */
    public static void setMDC(Map<String, String> mdc) {
        try {
            if (!CollectionUtil.isEmpty(mdc)) {
                MDC.setContextMap(mdc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * MDC删除
     * @param key
     */
    public static void remove(String key) {
        try {
            if (StringUtils.isNotBlank(key)) {
                MDC.remove(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * MDC清理
     */
    public static void clear() {
        try {
            MDC.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
