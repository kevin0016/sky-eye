package com.itkevin.common.notice.workwx;

import lombok.Data;

import java.io.Serializable;

@Data
public class WeWorkConfigData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * webHook
     */
    private String webHook;

    /**
     * key
     */
    private String key;


}
