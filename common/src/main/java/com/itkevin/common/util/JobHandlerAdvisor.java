package com.itkevin.common.util;

import com.ctrip.framework.foundation.Foundation;
import com.itkevin.common.enums.MDCConstantEnum;
import com.itkevin.common.enums.RequestTypeEnum;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

import java.lang.reflect.Method;

@Slf4j
public class JobHandlerAdvisor {

    @Advice.OnMethodEnter
    public static void onMethodEnter(@Advice.This Object object, @Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
        try {
            String requestParam = arguments != null && arguments.length > 0 ? (String) arguments[0] : "";
            MDCUtils.put(MDCConstantEnum.SERVER_NAME.getCode(), Foundation.app().getAppId());
            MDCUtils.put(MDCConstantEnum.SERVER_IP.getCode(), IPUtils.getLocalIp());
            MDCUtils.put(MDCConstantEnum.SERVER_HOSTNAME.getCode(), IPUtils.getLocalHostName());
            MDCUtils.put(MDCConstantEnum.REQUEST_TYPE.getCode(), RequestTypeEnum.JOB.name().toLowerCase());
            MDCUtils.put(MDCConstantEnum.TRACE_ID.getCode(), TraceContext.traceId());
            MDCUtils.put(MDCConstantEnum.REQUEST_URI.getCode(), object.getClass().getName() + "#execute");
            MDCUtils.put(MDCConstantEnum.REQUEST_PARAM.getCode(), requestParam);
        } catch (Exception e) {
            log.warn("log skyeye >>> JobHandlerAdvisor.onMethodEnter occur exception", e);
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit() {
        MDCUtils.clear();
    }

}
