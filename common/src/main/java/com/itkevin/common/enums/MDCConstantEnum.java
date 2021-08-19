package com.itkevin.common.enums;

import java.util.Arrays;

/**
 * MDC常量枚举
 */
public enum MDCConstantEnum {

    ERROR_MESSAGE("errorMessage", "error信息"),
    SERVER_NAME("serverName", "服务名称"),
    SOURCE_IP("sourceIP", "来源IP"),
    SERVER_IP("serverIP", "服务器IP"),
    SERVER_HOSTNAME("serverHostname", "服务器hostname"),
    OCCURRENCE_TIME("occurrenceTime", "发生时间"),
    REQUEST_TYPE("requestType", "请求类型"),
    TRACE_ID("traceId", "跟踪traceId"),
    REQUEST_URI("requestURI", "请求URI"),
    REQUEST_PARAM("requestParam", "请求参数"),
    MESSAGE_TOPIC("messageTopic", "消息topic"),
    MESSAGE_ID("messageId", "消息msgId"),
    MESSAGE_KEYS("messageKeys", "消息keys"),
    EVENT_NAME("eventName", "事件name"),
    EVENT_PAYLOAD("eventPayload", "事件payload"),
    EXCEPTION_MESSAGE("exceptionMessage", "异常信息"),
    EXCEPTION_STACKTRACE("exceptionStackTrace", "异常堆栈"),
    DEPLOY_TAG("deployTag", "部署tag号");

    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    MDCConstantEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MDCConstantEnum getByValue(String code) {
        return Arrays.stream(MDCConstantEnum.values())
                .filter(resultCodeEnum -> resultCodeEnum.code.equals(code))
                .findFirst().orElse(null);
    }
}
