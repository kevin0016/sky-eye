package com.itkevin.web.common.web;

import com.itkevin.web.common.filter.LogWebFilter;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EnumSet;

/**
 * servlet初始化类
 */
public class WebInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        FilterRegistration filterRegistration = servletContext.getFilterRegistration("LogWebFilter");
        if (filterRegistration == null) {
            FilterRegistration.Dynamic logWebFilter = servletContext.addFilter("LogWebFilter", LogWebFilter.class);
            logWebFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
        }
    }
}
