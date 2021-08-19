package com.itkevin.common.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class LogData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否过滤日志数据
     */
    private Boolean filter;

    /**
     * 日志级别
     */
    private String level;

    /**
     * error信息
     */
    private String errorMessage;

    /**
     * 服务名称
     */
    private String serverName;

    /**
     * 来源IP
     */
    private String sourceIP;

    /**
     * 服务器IP
     */
    private String serverIP;

    /**
     * 服务器hostname
     */
    private String serverHostname;

    /**
     * 发生时间
     */
    private String occurrenceTime;

    /**
     * 请求类型
     */
    private String requestType;

    /**
     * 跟踪traceId
     */
    private String traceId;

    /**
     * 请求URI
     */
    private String requestURI;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 消息topic
     */
    private String messageTopic;

    /**
     * 消息msgId
     */
    private String messageId;

    /**
     * 消息keys
     */
    private String messageKeys;

    /**
     * 事件name
     */
    private String eventName;

    /**
     * 事件payload
     */
    private String eventPayload;

    /**
     * 异常信息
     */
    private String exceptionMessage;

    /**
     * 异常堆栈
     */
    private String exceptionStackTrace;

}
