package com.itkevin.common.config;

import cn.hutool.json.JSONUtil;
import com.itkevin.common.constants.SysConstant;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class SysConfig implements Serializable {

    public static SysConfig instance = new SysConfig();

    /**
     * log报警开启
     */
    @Getter
    private Boolean alarmEnabled = false;

    /**
     * 报警机器人配置（支持多个机器人）
     */
    @Getter
    private String alarmTalkHook = "{}";

    /**
     * 报警严重错误机器人配置（支持多个机器人）
     */
    @Getter
    private String alarmSeriousTalkHook = "{}";

    /**
     * 堆栈行数配置
     */
    @Getter
    private Integer alarmStacknum = 10;

    /**
     * 单条报警白名单
     */
    @Getter
    private String alarmWhiteList = "";

    /**
     * 聚合报警白名单
     */
    @Getter
    private String alarmAggreWhiteList = "";

    /**
     * 报警间隔时间（单位分钟）
     */
    @Getter
    private Integer alarmNotifyTime = 1;

    /**
     * 报警次数阀值
     */
    @Getter
    private Integer alarmNotifyCount = 5;

    /**
     * 接口耗时报警间隔时间（单位分钟）
     */
    @Getter
    private Integer alarmUriElapsedTime = 1;

    /**
     * 接口耗时超过阀值时间的次数阀值（阀值时间如果不指定则默认1000毫秒）
     */
    @Getter
    private Integer alarmUriElapsedCount = 1000;

    /**
     * 指定URI接口耗时时间阀值（单位毫秒，支持指定多个URI）
     */
    @Getter
    private String alarmUriElapsed = "{}";

    /**
     * 指定接口耗时时间阀值（单位毫秒，全局指定，不配置默认1000毫秒）
     */
    @Getter
    private Long alarmUriElapsedGlobal = 1000L;

    /**
     * skyeye异常报警途径
     */
    @Getter
    private String alarmTool = "wework";


    public static  SysConfig convertMap2SysConfig(Map<String,String> map){
        String enableStr = map.get(SysConstant.ALARM_ENABLED);
        if(!Objects.isNull(enableStr)){
            instance.alarmEnabled = Boolean.parseBoolean(enableStr);
        }
        String alarmTalkStr = map.get(SysConstant.ALARM_DINGTALK);
        if(JSONUtil.isJson(alarmTalkStr)){
            instance.alarmTalkHook = alarmTalkStr;
        }

        String alarmSeriousTalkStr = map.get(SysConstant.ALARM_SERIOUS_DINGTALK);
        if(JSONUtil.isJson(alarmSeriousTalkStr)){
            instance.alarmSeriousTalkHook = alarmSeriousTalkStr;
        }

        String alarmStackNum = map.get(SysConstant.ALARM_STACKNUM);
        if(StringUtils.isNumeric(alarmStackNum)){
            instance.alarmStacknum = Integer.parseInt(alarmStackNum);
        }

        String alarmWhiteList = map.get(SysConstant.ALARM_WHITE_LIST);
        if(StringUtils.isNotBlank(alarmWhiteList)){
            instance.alarmWhiteList = alarmWhiteList;
        }
        String alarmAggreWhiteList = map.get(SysConstant.ALARM_AGGRE_WHITE_LIST);
        if(StringUtils.isNotBlank(alarmAggreWhiteList)){
            instance.alarmAggreWhiteList = alarmAggreWhiteList;
        }
        String alarmNotifyTime = map.get(SysConstant.ALARM_NOTIFY_TIME);
        if(StringUtils.isNumeric(alarmNotifyTime)){
            instance.alarmNotifyTime = Integer.parseInt(alarmNotifyTime);
        }
        String alarmNotifyCount = map.get(SysConstant.ALARM_NOTIFY_COUNT);
        if(StringUtils.isNumeric(alarmNotifyCount)){
            instance.alarmNotifyCount = Integer.parseInt(alarmNotifyCount);
        }
        String alarmUriElapsedTime = map.get(SysConstant.ALARM_URI_ELAPSED_TIME);
        if(StringUtils.isNumeric(alarmUriElapsedTime)){
            instance.alarmUriElapsedTime = Integer.parseInt(alarmUriElapsedTime);
        }
        String alarmUriElapsedCount = map.get(SysConstant.ALARM_URI_ELAPSED_COUNT);
        if(StringUtils.isNumeric(alarmUriElapsedCount)){
            instance.alarmUriElapsedCount = Integer.parseInt(alarmUriElapsedCount);
        }
        String alarmUriElapsed = map.get(SysConstant.ALARM_URI_ELAPSED);
        if(JSONUtil.isJson(alarmUriElapsed)){
            instance.alarmUriElapsed = alarmUriElapsed;
        }
        String alarmUriElapsedGlobal = map.get(SysConstant.ALARM_URI_ELAPSED_GLOBAL);
        if(StringUtils.isNumeric(alarmUriElapsedGlobal)){
            instance.alarmUriElapsedGlobal = Long.parseLong(alarmUriElapsedGlobal);
        }

        String alarmTool = map.get(SysConstant.ALARM_TOOL);
        if(StringUtils.isNotBlank(alarmTool)){
            instance.alarmTool = alarmTool;
        }
        return instance;
    }
}
