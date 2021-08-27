package com.itkevin.common.util;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GuavaCacheUtils {

    /**
     * 缓存操作对象
     */
    private static Cache<String, Object> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                //.removalListener(notification -> log.info("log skyeye >>> {} was removed from guava cache, cause is {}", notification.getKey(), notification.getCause()))
                .build();
    }

    /**
     * 当天剩余时间
     * @return
     */
    private static long getMilliSeconds() {
        Calendar curDate = Calendar.getInstance();
        Calendar tomorrowDate = new GregorianCalendar(curDate.get(Calendar.YEAR),
                curDate.get(Calendar.MONTH),
                curDate.get(Calendar.DATE) + 1, 0, 0, 0);
        return tomorrowDate.getTimeInMillis() - System.currentTimeMillis();
    }

    /**
     * 获取缓存值
     *
     * @param key
     * @return
     */
    protected static Object get(String key) {
        String value = null;
        try {
            if (StringUtils.isNotBlank(key)) {
                return cache.getIfPresent(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * 获取缓存值，如果值不存在返回默认值
     *
     * @param key
     * @return
     */
    protected static Object get(String key, String defaultValue) {
        Object value = null;
        try {
            if (StringUtils.isNotBlank(key)) {
                value = cache.getIfPresent(key);
                value = value != null ? value : defaultValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * 获取缓存值，如果值不存在执行回调方法
     *
     * @param key
     * @param loader
     * @return
     */
    protected static Object get(String key, Callable<Object> loader) {
        Object value = null;
        try {
            if (StringUtils.isNotBlank(key) && loader != null) {
                return cache.get(key, loader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * 放入缓存
     *
     * @param key
     * @param value
     */
    protected static void put(String key, Object value) {
        try {
            if (StringUtils.isNotEmpty(key) && value != null) {
                cache.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 移除缓存
     *
     * @param key
     */
    protected static void remove(String key) {
        try {
            if (StringUtils.isNotEmpty(key)) {
                cache.invalidate(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量移除缓存
     *
     * @param keys
     */
    protected static void remove(List<String> keys) {
        try {
            if (!CollectionUtil.isEmpty(keys)) {
                cache.invalidateAll(keys);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空缓存
     */
    protected static void removeAll() {
        try {
            cache.invalidateAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
