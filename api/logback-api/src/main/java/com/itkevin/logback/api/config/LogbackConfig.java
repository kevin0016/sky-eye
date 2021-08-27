package com.itkevin.logback.api.config;

import com.itkevin.logback.api.listener.LogbackApplicationListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * log config
 */
@Configuration
@ConditionalOnClass(
        value = {ch.qos.logback.classic.Logger.class, ch.qos.logback.core.filter.Filter.class}
)
public class LogbackConfig {

    @Bean
    @ConditionalOnMissingBean
    public LogbackApplicationListener logbackApplicationListener() {
        return new LogbackApplicationListener();
    }

}
