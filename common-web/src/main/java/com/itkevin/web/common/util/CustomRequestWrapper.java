package com.itkevin.web.common.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;

public class CustomRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] paramArray;

    private final String paramStr;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request
     * @throws IllegalArgumentException if the request is null
     */
    public CustomRequestWrapper(HttpServletRequest request) {
        super(request);
        paramStr = HttpRequestUtil.getParamString(request);
        paramArray = paramStr.getBytes(Charset.forName("UTF-8"));
    }

    public String getParamStr() {
        return paramStr;
    }


    @Override
    public BufferedReader getReader() throws IOException {
        if (StringUtils.isBlank(paramStr)) {
            return super.getReader();
        }
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (StringUtils.isBlank(paramStr)) {
            return super.getInputStream();
        }
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(paramArray);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public String getHeader(String name) {
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return super.getHeaderNames();
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return super.getHeaders(name);
    }
}
