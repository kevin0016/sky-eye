package com.itkevin.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LogUriElapsedData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求URI
     */
    private String requestURI;

    /**
     * 报警间隔
     */
    private Integer alarmTime;

    /**
     * 报警次数
     */
    private Integer alarmCount;

    /**
     * 阀值
     */
    private Long uriElapsedThreshold;

    /**
     * 抽样traceId
     */
    private List<String> traceIdList;

    /**
     * 最大耗时
     */
    private Long maxUriElapsed;

    /**
     * 最大耗时traceId
     */
    private String maxUriElapsedTraceId;

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