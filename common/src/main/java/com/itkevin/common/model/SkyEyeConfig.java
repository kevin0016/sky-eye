package com.itkevin.common.model;

import lombok.Data;

/**
 * @ClassName SkyEyeConfig
 * @Date 2021/8/20 21:08
 * @Version 1.0
 */
@Data
public class SkyEyeConfig {

    /**
     * 是否启动报警
     */
    private Boolean logAlarmEnabled = false;

    /**
     * 报警钉钉严重错误机器人配置（支持多个机器人）
     */
    private String logAlarmSeriousMsgTalk;

    /**
     * 报警钉钉机器人配置（支持多个机器人）
     */
    private String logAlarmMsgTalk;

    /**
     * 堆栈行数配置
     */
    private Integer logAlarmStackNum;

    /**
     * 单条报警白名单
     */
    private String logAlarmWhiteList;

    /**
     * 聚合报警白名单
     */
    private String logAlarmAggreWhiteList;

    /**
     * 聚合报警间隔时间（单位分钟）
     */
    private Integer logAlarmNotifyTime;

    /**
     * 报警次数阀值
     */
    private Integer logAlarmNotifyCount;

    /**
     * 接口耗时报警间隔时间（单位分钟）
     */
    private Integer logAlarmUriElapsedTime;

    /**
     * 接口耗时超过阀值时间的次数阀值（阀值时间如果不指定则默认1000毫秒）
     */
    private Integer logAlarmUriElapsedCount;

    /**
     * 指定URI接口耗时时间阀值（单位毫秒，支持指定多个URI）
     */
    private String logAlarmUriElapsed;

    /**
     * 指定接口耗时时间阀值（单位毫秒，全局指定，不配置默认1000毫秒）
     */
    private String logAlarmUriElapsedGlobal;
}
