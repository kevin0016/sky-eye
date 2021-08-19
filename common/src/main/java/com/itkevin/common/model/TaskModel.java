package com.itkevin.common.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务id
     */
    private String businessId;

    /**
     * 循环次数
     */
    private Integer cycleNum;
}
