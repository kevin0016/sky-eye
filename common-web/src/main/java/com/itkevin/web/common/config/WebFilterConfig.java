package com.itkevin.web.common.config;

import com.itkevin.web.common.filter.LogWebFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
@ConditionalOnClass(
        value = {ch.qos.logback.classic.Logger.class, ch.qos.logback.core.filter.Filter.class}
)
public class WebFilterConfig {

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
}
