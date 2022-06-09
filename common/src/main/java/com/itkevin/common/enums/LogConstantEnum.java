package com.itkevin.common.enums;

import java.util.Arrays;

/**
 * 日志常量枚举
 */
public enum LogConstantEnum {

    REQUEST_URI("requestURI", "请求URI"),

    EXCEPTION_MESSAGE("exceptionMessage", "异常信息"),

    ERROR_MESSAGE("errorMessage", "error信息"),

    ALARM_TIME("alarmTime", "时间"),

    ALARM_COUNT("alarmCount", "次数"),

    URI_ELAPSED_THRESHOLD("uriElapsedThreshold", "阀值"),

    URI_ELAPSED_TRACEID_LIST("uriElapsedTraceidList", "抽样traceId"),

    MAX_URI_ELAPSED("maxUriElapsed", "最大耗时"),

    MAX_URI_ELAPSED_TRACEID("maxUriElapsedTraceId", "最大耗时traceId"),

    SERVER_NAME("serverName", "服务名称"),

    SERVER_IP("serverIP", "服务器IP"),

    SERVER_HOSTNAME("serverHostname", "服务器hostname");

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    LogConstantEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LogConstantEnum getByValue(String code) {
        return Arrays.stream(LogConstantEnum.values())
                .filter(resultCodeEnum -> resultCodeEnum.code.equals(code))
                .findFirst().orElse(null);
    }
}
