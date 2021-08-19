package com.itkevin.common.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UriElapsedCollect implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求URI
     */
    private String requestURI;

    /**
     * URI耗时
     */
    private long elapsed;

    /**
     * 跟踪traceId
     */
    private String traceId;

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
