package com.itkevin.logback.api.listener;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import com.google.common.collect.Lists;
import com.itkevin.common.config.ConfigTool;
import com.itkevin.common.config.SysConfig;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.util.HashedWheelTask;
import com.itkevin.logback.api.filter.LogbackFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * logback初始化
 */
@Slf4j
public class LogbackApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 配置放入缓存
        ServiceLoader<ConfigTool> serviceLoader = ServiceLoader.load(ConfigTool.class);
        List<ConfigTool> configTools = Lists.newArrayList();
        for (ConfigTool configTool : serviceLoader) {
            configTools.add(configTool);
        }
        configTools.stream().max(Comparator.comparing(ConfigTool::sortFlag)).ifPresent(configTool -> {
            Map<String, String> map = configTool.getConfig();
            SysConfig.convertMap2SysConfig(map);
        });

        // 设置log filter，初始化动作
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender<ILoggingEvent> fileAppenderERROR = logger.getAppender("console");
        Appender<ILoggingEvent> fileAppender = fileAppenderERROR == null ? logger.getAppender(SysConfig.instance.getSkyeyeLogAppender()) : fileAppenderERROR;
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

