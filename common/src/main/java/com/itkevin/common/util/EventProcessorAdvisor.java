//package com.itkevin.common.util;
//
//import com.ctrip.framework.foundation.Foundation;
//import com.itkevin.common.enums.MDCConstantEnum;
//import com.itkevin.common.enums.RequestTypeEnum;
//import lombok.extern.slf4j.Slf4j;
//import net.bytebuddy.asm.Advice;
//import org.apache.skywalking.apm.toolkit.trace.TraceContext;
//import sun.plugin2.message.EventMessage;
//
//import java.lang.reflect.Method;
//
//@Slf4j
//public class EventProcessorAdvisor {
//
//    @Advice.OnMethodEnter
//    public static void onMethodEnter(@Advice.This Object object, @Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
//        try {
//            Object argument = arguments != null && arguments.length > 0 ? arguments[0] : null;
//            EventMessage eventMessage = argument instanceof EventMessage ? (EventMessage) argument : null;
//            MDCUtils.put(MDCConstantEnum.SERVER_NAME.getCode(), Foundation.app().getAppId());
//            MDCUtils.put(MDCConstantEnum.SERVER_IP.getCode(), IPUtils.getLocalIp());
//            MDCUtils.put(MDCConstantEnum.SERVER_HOSTNAME.getCode(), IPUtils.getLocalHostName());
//            MDCUtils.put(MDCConstantEnum.REQUEST_TYPE.getCode(), RequestTypeEnum.EVENT.name().toLowerCase());
//            MDCUtils.put(MDCConstantEnum.TRACE_ID.getCode(), TraceContext.traceId());
//            MDCUtils.put(MDCConstantEnum.REQUEST_URI.getCode(), object.getClass().getName() + "#" + method.getName());
//            MDCUtils.put(MDCConstantEnum.EVENT_NAME.getCode(), eventMessage != null ? eventMessage.getEventName() : "");
//            MDCUtils.put(MDCConstantEnum.EVENT_PAYLOAD.getCode(), eventMessage != null ? eventMessage.getPayload() : "");
//        } catch (Exception e) {
//            log.warn("log skyeye >>> EventProcessorAdvisor.onMethodEnter occur exception", e);
//        }
//    }
//
//    @Advice.OnMethodExit(onThrowable = Throwable.class)
//    public static void onMethodExit() {
//        MDCUtils.clear();
//    }
//
//}
