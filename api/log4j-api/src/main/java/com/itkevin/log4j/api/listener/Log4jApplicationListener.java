package com.itkevin.log4j.api.listener;

import com.ctrip.framework.apollo.Config;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.listener.ConfigListener;
import com.itkevin.common.util.ConfigUtils;
import com.itkevin.common.util.HashedWheelTask;
import com.itkevin.log4j.api.filter.Log4jFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.Filter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * log4j初始化
 */
@Slf4j
public class Log4jApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        // 添加apollo配置监听器，获取apollo配置放入缓存
        Config config = ConfigUtils.getConfig();
        config.addChangeListener(new ConfigListener());
        Set<String> propertyNames = config.getPropertyNames();
        if (!CollectionUtils.isEmpty(propertyNames)) {
            propertyNames.forEach(propertyName -> {
                String propertyValue = config.getProperty(propertyName, null);
                ConfigUtils.saveProperty(propertyName, propertyValue);
            });
        }
        // 设置log filter，初始化动作
        Logger logger = Logger.getRootLogger();
        Appender fileAppender = logger.getAppender("file");
        if (fileAppender != null) {
            Filter filter = fileAppender.getFilter();
            if (!(filter instanceof Log4jFilter)) {
                fileAppender.addFilter(new Log4jFilter());
                // 初始化任务调度器
                HashedWheelTask.init();
                // 项目启动后发送欢迎语
                log.error(SysConstant.WELCOME);
            }
        }
    }

}

