package com.itkevin.log4j.api.config;

import com.itkevin.log4j.api.listener.Log4jApplicationListener;
import com.itkevin.web.common.filter.LogWebFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * log4j config
 */
@Configuration
@ConditionalOnClass(
        value = {org.apache.log4j.Logger.class, org.apache.log4j.spi.Filter .class}
)
public class Log4jConfig {

    @Bean
    @ConditionalOnMissingBean
    public Log4jApplicationListener logbackApplicationListener() {
        return new Log4jApplicationListener();
    }

}
