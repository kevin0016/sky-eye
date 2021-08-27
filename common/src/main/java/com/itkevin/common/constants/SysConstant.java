package com.itkevin.common.constants;

/**
 * 常量类
 */
public class SysConstant {

    /**
     * 欢迎语
     */
    public static final String WELCOME = "Welcome to the skyeye";

    /**
     * log报警开启
     */
    public static final String ALARM_ENABLED = "skyeye.log.alarm.enabled";

    /**
     * 报警机器人配置（支持多个机器人）
     */
    public static final String ALARM_DINGTALK = "skyeye.log.alarm.talk.hook";

    /**
     * 报警严重错误机器人配置（支持多个机器人）
     */
    public static final String ALARM_SERIOUS_DINGTALK = "skyeye.log.alarm.serious.talk.hook";

    /**
     * 异常message输出默认长度
     */
    public static final int ALARM_MESSAGE_STR_DEFAULT = 500;

    /**
     * 异常堆栈输出默认行数
     */
    public static final int ALARM_STACKNUM_DEFAULT = 10;

    /**
     * 堆栈行数配置
     */
    public static final String ALARM_STACKNUM = "skyeye.log.alarm.stackNum";

    /**
     * 单条报警白名单
     */
    public static final String ALARM_WHITE_LIST = "skyeye.log.alarm.white.list";

    /**
     * 聚合报警白名单
     */
    public static final String ALARM_AGGRE_WHITE_LIST = "skyeye.log.alarm.aggre.white.list";

    /**
     * 报警间隔时间（单位分钟）
     */
    public static final String ALARM_NOTIFY_TIME = "skyeye.log.alarm.notify.time";

    /**
     * 报警次数阀值
     */
    public static final String ALARM_NOTIFY_COUNT = "skyeye.log.alarm.notify.count";

    /**
     * 报警间隔时间默认值（单位分钟）
     */
    public static final Integer ALARM_NOTIFY_TIME_DEFAULT = 1;

    /**
     * 报警次数阀值默认值
     */
    public static final Integer ALARM_NOTIFY_COUNT_DEFAULT = 5;

    /**
     * 接口耗时报警间隔时间（单位分钟）
     */
    public static final String ALARM_URI_ELAPSED_TIME = "skyeye.log.alarm.uri.elapsed.time";

    /**
     * 接口耗时超过阀值时间的次数阀值（阀值时间如果不指定则默认1000毫秒）
     */
    public static final String ALARM_URI_ELAPSED_COUNT = "skyeye.log.alarm.uri.elapsed.count";

    /**
     * 指定URI接口耗时时间阀值（单位毫秒，支持指定多个URI）
     */
    public static final String ALARM_URI_ELAPSED = "skyeye.log.alarm.uri.elapsed";

    /**
     * 指定接口耗时时间阀值（单位毫秒，全局指定，不配置默认1000毫秒）
     */
    public static final String ALARM_URI_ELAPSED_GLOBAL = "skyeye.log.alarm.uri.elapsed.global";

    /**
     * 接口耗时阀值时间默认值（单位毫秒）
     */
    public static final long ALARM_URI_ELAPSED_DEFAULT = 1000;

    /**
     * 自定义线程池核心线程数
     */
    public static final Integer THREAD_NUM = 5;

    /**
     * 自定义线程池最大线程数
     */
    public static final Integer MAX_THREAD_NUM = 20;

    /**
     * 监控报警指标名称
     */
    public static final String ALARM_METRIC_NAME = "skyeye_alarm_num_gauge";

    /**
     * 监控报警指标名称help
     */
    public static final String ALARM_METRIC_NAME_HELP = "skyeye异常报警数";

    /**
     * skyeye异常报警途径
     */
    public static final String ALARM_TOOL = "skyeye.log.alarm.tool";

    /**
     * 默认报警途径为企业微信
     */
    public static final String ALARM_TOOL_DEFAULT = "wework";

}
