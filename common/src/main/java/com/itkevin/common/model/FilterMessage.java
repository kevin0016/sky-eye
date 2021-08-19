package com.itkevin.common.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class FilterMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 日志类型
     */
    private String logType;

    /**
     * error信息
     */
    private String msg;

    /**
     * 异常message
     */
    private String message;

    /**
     * 异常堆栈
     */
    private String stackTrace;

}
