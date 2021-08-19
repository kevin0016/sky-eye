package com.itkevin.common.util;

import cn.hutool.json.JSONUtil;
import com.itkevin.common.model.TaskModel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class LocalCacheUtils {

    /**
     * int前缀
     */
    private static final String KEY_INT = "LOG_SKYEYE_INT_";

    /**
     * alarm前缀
     */
    private static final String KEY_ALARM = "LOG_SKYEYE_ALARM_";

    /**
     * 对key的value值做加1操作
     * @param key
     * @return
     */
    public static Integer incr(String key) {
        if (StringUtils.isBlank(key)) {
            return 0;
        }
        try {
            key = KEY_INT + key;
            AtomicInteger atomicInteger = (AtomicInteger) GuavaCacheUtils.get(key, AtomicInteger::new);
            return atomicInteger.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 存储int缓存
     */
    protected static void putIntCache(String key, Integer value) {
        try {
            if (StringUtils.isNotBlank(key) && value != null) {
                key = KEY_INT + key;
                GuavaCacheUtils.put(key, new AtomicInteger(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取int缓存
     */
    protected static Integer getIntCache(String key) {
        try {
            if (StringUtils.isNotBlank(key)) {
                key = KEY_INT + key;
                AtomicInteger atomicInteger = (AtomicInteger) GuavaCacheUtils.get(key, AtomicInteger::new);
                return atomicInteger.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 删除int缓存
     * @param key
     */
    protected static void delIntCache(String key) {
        try {
            if (StringUtils.isNotBlank(key)) {
                key = KEY_INT + key;
                GuavaCacheUtils.remove(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 存储long缓存
     */
    protected static void putLongCache(String key, Long value) {
        try {
            if (StringUtils.isNotBlank(key) && value != null) {
                key = KEY_INT + key;
                GuavaCacheUtils.put(key, new AtomicLong(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取long缓存
     */
    protected static Long getLongCache(String key) {
        try {
            if (StringUtils.isNotBlank(key)) {
                key = KEY_INT + key;
                AtomicLong atomicLong = (AtomicLong) GuavaCacheUtils.get(key, AtomicLong::new);
                return atomicLong.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * 删除long缓存
     * @param key
     */
    protected static void delLongCache(String key) {
        try {
            if (StringUtils.isNotBlank(key)) {
                key = KEY_INT + key;
                GuavaCacheUtils.remove(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 存储缓存
     * @param key
     * @param value
     */
    protected static void putCache(String key, String value) {
        try {
            if (StringUtils.isNotBlank(key) && value != null) {
                key = KEY_ALARM + key;
                GuavaCacheUtils.put(key, new AtomicReference<>(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取缓存
     * @param key
     * @return
     */
    protected static String getCache(String key) {
        try {
            if (StringUtils.isNotBlank(key)) {
                key = KEY_ALARM + key;
                AtomicReference<?> atomicReference = (AtomicReference<?>) GuavaCacheUtils.get(key, AtomicReference::new);
                Object object = atomicReference.get();
                if (object instanceof String) {
                    return atomicReference.get().toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除缓存
     * @param key
     */
    protected static void delCache(String key) {
        try {
            if (StringUtils.isNotBlank(key)) {
                key = KEY_ALARM + key;
                GuavaCacheUtils.remove(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 存储集合缓存
     * @param key
     * @param value
     */
    protected static void sadd(String key, String value) {
        try {
            if (StringUtils.isNotBlank(key) && value != null) {
                key = KEY_ALARM + key;
                CopyOnWriteArrayList<String> list = (CopyOnWriteArrayList<String>) GuavaCacheUtils.get(key, CopyOnWriteArrayList::new);
                list.add(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取集合缓存
     * @param key
     * @return
     */
    protected static List<String> smember(String key) {
        try {
            if (StringUtils.isNotBlank(key)) {
                key = KEY_ALARM + key;
                return (CopyOnWriteArrayList<String>) GuavaCacheUtils.get(key, CopyOnWriteArrayList::new);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除集合缓存元素
     * @param key
     * @param value
     */
    protected static void srem(String key, String value) {
        try {
            if (StringUtils.isNotBlank(key) && value != null) {
                key = KEY_ALARM + key;
                CopyOnWriteArrayList<String> list = (CopyOnWriteArrayList<String>) GuavaCacheUtils.get(key, CopyOnWriteArrayList::new);
                list.removeIf(s -> s.equals(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TaskModel taskModel = new TaskModel();
        taskModel.setBusinessId("hashcode");
        taskModel.setCycleNum(1);
        sadd("key", JSONUtil.toJsonStr(taskModel));
        sadd("key", JSONUtil.toJsonStr(taskModel));
        System.out.println(smember("key"));
        srem("key", JSONUtil.toJsonStr(taskModel));
        System.out.println(smember("key"));
    }
}
