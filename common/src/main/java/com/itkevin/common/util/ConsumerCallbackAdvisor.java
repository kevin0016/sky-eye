//package com.itkevin.common.util;
//
//import com.ctrip.framework.foundation.Foundation;
//import com.skyeye.k12.teacher.common.enums.MDCConstantEnum;
//import com.skyeye.k12.teacher.common.enums.RequestTypeEnum;
//import lombok.extern.slf4j.Slf4j;
//import net.bytebuddy.asm.Advice;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.apache.skywalking.apm.toolkit.trace.TraceContext;
//
//import java.lang.reflect.Method;
//
//@Slf4j
//public class ConsumerCallbackAdvisor {
//
//    @Advice.OnMethodEnter
//    public static void onMethodEnter(@Advice.This Object object, @Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
//        try {
//            MessageExt messageExt = arguments != null && arguments.length > 0 ? (MessageExt) arguments[1] : null;
//            MDCUtils.put(MDCConstantEnum.SERVER_NAME.getCode(), Foundation.app().getAppId());
//            MDCUtils.put(MDCConstantEnum.SERVER_IP.getCode(), IPUtils.getLocalIp());
//            MDCUtils.put(MDCConstantEnum.SERVER_HOSTNAME.getCode(), IPUtils.getLocalHostName());
//            MDCUtils.put(MDCConstantEnum.REQUEST_TYPE.getCode(), RequestTypeEnum.MQ.name().toLowerCase());
//            MDCUtils.put(MDCConstantEnum.TRACE_ID.getCode(), TraceContext.traceId());
//            MDCUtils.put(MDCConstantEnum.REQUEST_URI.getCode(), object.getClass().getName() + "#call");
//            MDCUtils.put(MDCConstantEnum.MESSAGE_TOPIC.getCode(), messageExt != null ? messageExt.getTopic() : "");
//            MDCUtils.put(MDCConstantEnum.MESSAGE_ID.getCode(), messageExt != null ? messageExt.getMsgId() : "");
//            MDCUtils.put(MDCConstantEnum.MESSAGE_KEYS.getCode(), messageExt != null ? messageExt.getKeys() : "");
//        } catch (Exception e) {
//            log.warn("log skyeye >>> ConsumerCallbackAdvisor.onMethodEnter occur exception", e);
//        }
//    }
//
//    @Advice.OnMethodExit(onThrowable = Throwable.class)
//    public static void onMethodExit() {
//        MDCUtils.clear();
//    }
//
//}
