package com.itkevin.logback.api.listener;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import com.ctrip.framework.apollo.Config;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.listener.ConfigListener;
import com.itkevin.common.util.ConfigUtils;
import com.itkevin.common.util.HashedWheelTask;
import com.itkevin.logback.api.filter.LogbackFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * logback初始化
 */
@Slf4j
public class LogbackApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
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
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender<ILoggingEvent> fileAppenderERROR = logger.getAppender("FILE_ERROR");
        Appender<ILoggingEvent> fileAppender = fileAppenderERROR == null ? logger.getAppender("FILE_INFO") : fileAppenderERROR;
        if (fileAppender != null) {
            List<Filter<ILoggingEvent>> filters = fileAppender.getCopyOfAttachedFiltersList();
            for (Filter<ILoggingEvent> filter : filters) {
                if (filter instanceof LogbackFilter) {
                    return;
                }
            }
            fileAppender.addFilter(new LogbackFilter());
            // 初始化任务调度器
            HashedWheelTask.init();
            // 项目启动后发送欢迎语
            log.error(SysConstant.WELCOME);
        }
    }

}

