package com.itkevin.logback.api.config;

import com.itkevin.common.filter.LogWebFilter;
import com.itkevin.logback.api.listener.LogbackApplicationListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * log config
 */
@Configuration
@ConditionalOnClass(
        value = {ch.qos.logback.classic.Logger.class, ch.qos.logback.core.filter.Filter.class}
)
public class LogbackConfig {

    @Bean
    public Filter logWebFilter() {
        return new LogWebFilter();
    }

    @Bean
    public FilterRegistrationBean logWebFilterBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(logWebFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public LogbackApplicationListener logbackApplicationListener() {
        return new LogbackApplicationListener();
    }

}
