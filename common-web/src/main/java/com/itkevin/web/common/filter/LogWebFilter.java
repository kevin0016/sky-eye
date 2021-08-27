package com.itkevin.web.common.filter;

import com.itkevin.common.model.UriElapsedCollect;
import com.itkevin.web.common.util.CustomRequestWrapper;
import com.itkevin.common.util.ElapsedUtils;
import com.itkevin.web.common.util.HttpRequestUtil;
import com.itkevin.common.util.MDCUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * log servlet filter
 */
public class LogWebFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long start = 0;
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String contentType = request.getContentType();
            if (StringUtils.isNotBlank(contentType) && contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
                HttpRequestUtil.mdc(request);
                start = System.currentTimeMillis();
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                CustomRequestWrapper requestWrapper = new CustomRequestWrapper(request);
                HttpRequestUtil.mdc(requestWrapper);
                start = System.currentTimeMillis();
                filterChain.doFilter(requestWrapper, servletResponse);
            }
        } finally {
            UriElapsedCollect uriElapsedCollect = ElapsedUtils.uriElapsedCollect(System.currentTimeMillis() - start);
            ElapsedUtils.uriElapsed(uriElapsedCollect);
            MDCUtils.clear();
        }
    }

    @Override
    public void destroy() {

    }
}
