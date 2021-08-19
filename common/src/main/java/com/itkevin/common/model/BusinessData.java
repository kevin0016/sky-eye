package com.itkevin.common.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class BusinessData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求URI
     */
    private String requestURI;

    /**
     * 异常信息
     */
    private String exceptionMessage;

    /**
     * error信息
     */
    private String errorMessage;

    /**
     * 服务名称
     */
    private String serverName;

    /**
     * 服务器IP
     */
    private String serverIP;

    /**
     * 服务器hostname
     */
    private String serverHostname;

}
