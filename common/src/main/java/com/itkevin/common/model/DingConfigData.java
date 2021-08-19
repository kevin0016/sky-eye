package com.itkevin.common.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class DingConfigData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * webHook
     */
    private String webHook;

    /**
     * token
     */
    private String accessToken;

    /**
     * secret
     */
    private String secret;

}
