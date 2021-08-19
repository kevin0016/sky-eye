package com.itkevin.common.util;

import com.ctrip.framework.foundation.Foundation;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.enums.LogLevelEnum;
import com.itkevin.common.enums.MDCConstantEnum;
import com.itkevin.common.model.FilterMessage;
import com.itkevin.common.model.LogData;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogUtils {

    /**
     * 异常过滤
     *
     * @param filterMessage
     * @return
     */
    public static boolean filter(FilterMessage filterMessage) {
        String logType = filterMessage.getLogType();
        String msg = filterMessage.getMsg();
        String message = filterMessage.getMessage();
        String stackTrace = filterMessage.getStackTrace();
        // 特殊error信息过滤
        if (StringUtils.isNotBlank(msg)) {
            // rocketmq打印的error过滤掉
            if (msg.contains("consume topic:") && msg.contains("consumer:") && msg.contains("msg:") && msg.contains("msgId:") && msg.contains("bornTimestamp:")
                    || msg.contains("topic:") && msg.contains("consumer:") && msg.contains("msgSize:"))
                return true;
        }
        // 获取配置
        String alarmWhiteList = ConfigUtils.getProperty(SysConstant.ALARM_WHITE_LIST, null);

        return filter(msg, alarmWhiteList) || filter(message, alarmWhiteList) || filter(stackTrace, alarmWhiteList);
    }

    /**
     * 获取LogData数据对象
     *
     * @param logType
     * @param msg
     * @param message
     * @param stackTrace
     * @return
     */
    public static LogData obtainLogData(String logType, String msg, String message, String stackTrace) {
        // 获取配置
        String stackNumStr = ConfigUtils.getProperty(SysConstant.ALARM_STACKNUM, null);
        int stackNum = StringUtils.isNotBlank(stackNumStr) ? Integer.parseInt(stackNumStr.trim()) : SysConstant.ALARM_STACKNUM_DEFAULT;
        // logData
        LogData logData = new LogData();
        logData.setLevel(getLogLevel(msg));
        msg = StringUtils.isNotBlank(msg) ? msg.replaceAll("header(.*)", "").trim() : "";
        msg = StringUtils.isNotBlank(msg) ? msg.replaceAll("headers(.*)", "").trim() : "";
        logData.setErrorMessage(msg);
        logData.setServerName(Foundation.app().getAppId());
        logData.setServerIP(IPUtils.getLocalIp());
        logData.setServerHostname(IPUtils.getLocalHostName());
        logData.setSourceIP(MDCUtils.get(MDCConstantEnum.SOURCE_IP.getCode()));
        logData.setRequestType(MDCUtils.get(MDCConstantEnum.REQUEST_TYPE.getCode()));
        logData.setTraceId(MDCUtils.get(MDCConstantEnum.TRACE_ID.getCode()));
        logData.setRequestURI(MDCUtils.get(MDCConstantEnum.REQUEST_URI.getCode()));
        logData.setRequestParam(MDCUtils.get(MDCConstantEnum.REQUEST_PARAM.getCode()));
        logData.setMessageTopic(MDCUtils.get(MDCConstantEnum.MESSAGE_TOPIC.getCode()));
        logData.setMessageId(MDCUtils.get(MDCConstantEnum.MESSAGE_ID.getCode()));
        logData.setMessageKeys(MDCUtils.get(MDCConstantEnum.MESSAGE_KEYS.getCode()));
        logData.setEventName(MDCUtils.get(MDCConstantEnum.EVENT_NAME.getCode()));
        logData.setEventPayload(MDCUtils.get(MDCConstantEnum.EVENT_PAYLOAD.getCode()));
        logData.setExceptionMessage(message);
        String exceptionStackTrace = getRegexContent(stackTrace, stackNum);
        if (!exceptionStackTrace.contains("Caused by")) {
            String causedByContent = getCausedByContentOfStackTrace(stackTrace, 1);
            exceptionStackTrace += causedByContent;
        }
        String regex = "(dubbo)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        exceptionStackTrace = exceptionStackTrace.replaceAll(regex,"XXX");
        logData.setExceptionStackTrace(exceptionStackTrace);

        return logData;
    }

    /**
     * 信息过滤
     *
     * @param msg
     * @param filter
     * @return
     */
    public static boolean filter(String msg, String filter) {
        if (StringUtils.isNotBlank(msg) && StringUtils.isNotBlank(filter)) {
            String[] filters = filter.split(",");
            for (String filt : filters) {
                if (msg.contains(filt)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 日志级别
     *
     * @param msg
     * @return
     */
    private static String getLogLevel(String msg) {
        String level = LogLevelEnum.NORMAL.name();
        if (StringUtils.isNotBlank(msg)) {
            return msg.toLowerCase().contains("level") && msg.toLowerCase().contains("serious") ? LogLevelEnum.SERIOUS.name() : LogLevelEnum.NORMAL.name();
        }

        return level;
    }

    /**
     * 字符串正则截取
     *
     * @param content
     * @param seq
     * @return
     */
    private static String getRegexContent(String content, int seq) {
        Matcher slashMatcher = Pattern.compile(System.getProperty("line.separator")).matcher(content);
        int mIdx = 0;
        do {
            if (!slashMatcher.find()) {
                return content;
            }
            ++mIdx;
        } while(mIdx != seq);

        return content.substring(0, slashMatcher.start());
    }

    /**
     * 截取异常堆栈的Caused by内容
     *
     * @param stackTrace
     * @param stackNum
     * @return
     */
    private static String getCausedByContentOfStackTrace(String stackTrace, int stackNum) {
        String content = "";
        if (stackTrace.contains("Caused by")) {
            content = System.getProperty("line.separator") + getRegexContent(stackTrace.substring(stackTrace.indexOf("Caused by")), stackNum);
        }

        return content;
    }

}
