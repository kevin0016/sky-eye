package com.itkevin.log4j.api.filter;

import cn.hutool.core.date.DateUtil;
import com.itkevin.common.config.SysConfig;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.enums.LogTypeEnum;
import com.itkevin.common.model.FilterMessage;
import com.itkevin.common.model.LogData;
import com.itkevin.common.util.LocalCacheUtils;
import com.itkevin.common.util.LogUtils;
import com.itkevin.common.notice.NotifyMessageTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.Date;
import java.util.Optional;

/**
 * log4j Filter
 */
@Slf4j
public class Log4jFilter extends Filter {

    @Override
    public int decide(LoggingEvent event) {
        try {
            // error日志
            if (Level.ERROR.equals(event.getLevel())) {
                if (SysConfig.instance.getAlarmEnabled()) {
                    // error信息
                    String msg = event.getRenderedMessage() != null ? event.getRenderedMessage().trim() : "";
                    // 获取异常
                    Throwable ex = Optional.of(event).map(LoggingEvent::getThrowableInformation).map(ThrowableInformation::getThrowable).orElse(null);
                    // 异常message、异常堆栈
                    String message = ex != null ? ex.getMessage() : "";
                    String stackTrace = ex != null ? ExceptionUtils.getStackTrace(ex) : "";
                    // 过滤信息
                    FilterMessage filterMessage = new FilterMessage();
                    filterMessage.setLogType(LogTypeEnum.LOG4J.name());
                    filterMessage.setMsg(msg);
                    filterMessage.setMessage(message);
                    filterMessage.setStackTrace(stackTrace);
                    // 获取logData对象
                    LogData logData = LogUtils.obtainLogData(LogTypeEnum.LOG4J.name(), msg, message, stackTrace);
                    logData.setOccurrenceTime(DateUtil.formatDateTime(new Date(event.getTimeStamp())));
                    logData.setFilter(LogUtils.filter(filterMessage));
                    // 发送消息
                    NotifyMessageTools.getInstance().sendMessage(logData);
                    // 异常报警上报
                    if (!logData.getFilter() && !SysConstant.WELCOME.equals(logData.getErrorMessage())) {
                        LocalCacheUtils.incr(SysConstant.ALARM_METRIC_NAME);
                    }
                }
            }
        } catch (Throwable e) {
            log.warn("log skyeye >>> Log4jFilter occur exception", e);
        }

        return Filter.NEUTRAL;
    }

}
