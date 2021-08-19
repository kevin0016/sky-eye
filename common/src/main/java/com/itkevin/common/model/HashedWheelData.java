package com.itkevin.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HashedWheelData {
    private static final long serialVersionUID = 1L;

    /**
     * 延迟时间（单位分钟）
     */
    private Integer delayTime;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务id
     */
    private String businessId;

    /**
     * 业务数据
     */
    private String businessData;

}
