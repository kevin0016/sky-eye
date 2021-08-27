package com.itkevin.common.config;

import cn.hutool.core.collection.CollectionUtil;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.google.common.collect.Maps;
import com.itkevin.common.listener.ConfigListener;

import java.util.Map;
import java.util.Set;

public class ApolloConfigTool implements ConfigTool{

    @Override
    public Map<String, String> getConfig() {
        // 添加apollo配置监听器，获取apollo配置放入缓存
        Map<String, String> map = Maps.newHashMap();
        Config config = ConfigService.getConfig("skyeye");
        config.addChangeListener(new ConfigListener());
        Set<String> propertyNames = config.getPropertyNames();
        if (!CollectionUtil.isEmpty(propertyNames)) {
            propertyNames.forEach(propertyName -> {
                String propertyValue = config.getProperty(propertyName, null);
                map.put(propertyName,propertyValue);
            });
        }
        return map;
    }

    @Override
    public Integer sortFlag() {
        return 0;
    }
}
