package com.itkevin.common.config;

import java.util.Map;

/**
 * 开放接口配置
 */
public interface ConfigTool {

    /**
     * 获取配置
     * @return
     */
    Map<String,String> getConfig();

    /**
     * 排序过滤器，选取数据最大的作为获取配置的途径
     * @return
     */
    Integer sortFlag();
}
