package com.itkevin.common.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UriElapsedData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求uri
     */
    private String uri;

    /**
     * 耗时时间（单位毫秒）
     */
    private long elapsed;

}
