package com.itkevin.common.util;

import com.itkevin.common.enums.MDCConstantEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.http.MediaType;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

@Slf4j
public class HttpRequestUtil {

    /**
     * 获取请求参数
     * try-with-resource 执行完 try 块后会释放声明的资源（实现了 AutoCloseable 接口的类对象）
     * @param servletRequest
     * @return
     */
    public static String getParamString(HttpServletRequest servletRequest) {
        String result = "";
        String contentType = servletRequest.getContentType();

        if(contentType==null) {
            Map<String, String[]> parameterMap = servletRequest.getParameterMap();
            StringBuilder stringBuilder = new StringBuilder();
            for(String key : parameterMap.keySet()) {
                stringBuilder.append(convertStr(key,parameterMap.get(key)));
            }
            result = stringBuilder.toString();
        } else {
            if (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equalsIgnoreCase(contentType)) {
                Map<String, String[]> parameterMap = servletRequest.getParameterMap();
                StringBuilder stringBuilder = new StringBuilder();
                for(String key : parameterMap.keySet()) {
                    stringBuilder.append(convertStr(key,parameterMap.get(key)));
                }
                result = stringBuilder.toString();
            } else if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(contentType)
                    || MediaType.APPLICATION_JSON_UTF8_VALUE.equalsIgnoreCase(contentType)
                    || MediaType.TEXT_PLAIN_VALUE.equalsIgnoreCase(contentType)){
                try (ServletInputStream inputStream = servletRequest.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))) )
                {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line=reader.readLine())!=null) {
                        sb.append(line);
                    }
                    result = sb.toString();
                } catch (IOException e) {
                    log.warn("log skyeye >>> Failed to get the binary data stream contained in the request body! Url is '{}'.", servletRequest.getRequestURL().toString(), e);
                }
            }
        }

        return result;
    }


    /**
     * 使用 , 连接字符串，末尾是空格
     * @param values
     * @param key
     * @return key=value1,value2,value3
     */
    public static String convertStr(String key,String[] values) {
        if(values==null){
            return null;
        }
        int length = values.length;
        StringBuilder stringBuilder = new StringBuilder(length);
        stringBuilder.append(key).append("=");
        for(int i=0;i<length;i++){
            if(i!=length-1){
                stringBuilder.append(values[i]).append(",");
            }
            else{
                stringBuilder.append(values[i]);
            }
        }
        return stringBuilder.append(" ").toString();
    }

    /**
     * mdc上下文
     * @param request
     */
    public static void mdc(HttpServletRequest request) {
        try {
            MDCUtils.put(MDCConstantEnum.SERVER_NAME.getCode(), request.getServerName());
            MDCUtils.put(MDCConstantEnum.SOURCE_IP.getCode(), IPUtils.getIPAddress(request));
            MDCUtils.put(MDCConstantEnum.SERVER_IP.getCode(), IPUtils.getLocalIp());
            MDCUtils.put(MDCConstantEnum.SERVER_HOSTNAME.getCode(), IPUtils.getLocalHostName());
            MDCUtils.put(MDCConstantEnum.REQUEST_TYPE.getCode(), request.getScheme() + " " + request.getMethod());
            MDCUtils.put(MDCConstantEnum.TRACE_ID.getCode(), TraceContext.traceId());
            MDCUtils.put(MDCConstantEnum.REQUEST_URI.getCode(), request.getRequestURI());
            MDCUtils.put(MDCConstantEnum.REQUEST_PARAM.getCode(), request instanceof CustomRequestWrapper ? ((CustomRequestWrapper) request).getParamStr() : "");
        } catch (Exception e) {
            log.warn("log skyeye >>> LogWebFilter occur exception", e);
        }
    }

}
