package com.itkevin.logback.api.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import cn.hutool.core.date.DateUtil;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.enums.LogTypeEnum;
import com.itkevin.common.model.FilterMessage;
import com.itkevin.common.model.LogData;
import com.itkevin.common.util.ConfigUtils;
import com.itkevin.common.util.LocalCacheUtils;
import com.itkevin.common.util.LogUtils;
import com.itkevin.common.util.NotifyMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Date;

/**
 * logback filter
 */
@Slf4j
public class LogbackFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        try {
            // error日志
            if (Level.ERROR.equals(event.getLevel())) {
                if (Boolean.TRUE.toString().equalsIgnoreCase(ConfigUtils.getProperty(SysConstant.ALARM_ENABLED, Boolean.FALSE.toString()))) {
                    // error信息
                    String msg = event.getFormattedMessage() != null ? event.getFormattedMessage().trim() : "";
                    // 异常message、异常堆栈
                    String message = "";
                    String stackTrace = "";
                    // 获取异常
                    IThrowableProxy iThrowableProxy = event.getThrowableProxy();
                    if (iThrowableProxy instanceof ThrowableProxy) {
                        ThrowableProxy throwableProxy = (ThrowableProxy) iThrowableProxy;
                        Throwable ex = throwableProxy.getThrowable();
                        message = ex.getMessage();
                        stackTrace = ExceptionUtils.getStackTrace(ex);
                    }
                    // 过滤信息
                    FilterMessage filterMessage = new FilterMessage();
                    filterMessage.setLogType(LogTypeEnum.LOGBACK.name());
                    filterMessage.setMsg(msg);
                    filterMessage.setMessage(message);
                    filterMessage.setStackTrace(stackTrace);
                    // 获取logData对象
                    LogData logData = LogUtils.obtainLogData(LogTypeEnum.LOGBACK.name(), msg, message, stackTrace);
                    logData.setOccurrenceTime(DateUtil.formatDateTime(new Date(event.getTimeStamp())));
                    logData.setFilter(LogUtils.filter(filterMessage));
                    // 发送消息
                    NotifyMessageUtils.getInstance().sendMessage(logData);
                    // 异常报警上报
                    if (!logData.getFilter() && !SysConstant.WELCOME.equals(logData.getErrorMessage())) {
                        LocalCacheUtils.incr(SysConstant.ALARM_METRIC_NAME);
                    }
                }
            }
        } catch (Throwable e) {
            log.warn("log skyeye >>> LogbackFilter occur exception", e);
        }

        return FilterReply.NEUTRAL;
    }

}
