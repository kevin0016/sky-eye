//package com.itkevin.common.util;
//
//import com.ctrip.framework.foundation.Foundation;
//import com.google.common.collect.Lists;
//import com.itkevin.common.enums.MDCConstantEnum;
//import com.itkevin.common.enums.RequestTypeEnum;
//import lombok.extern.slf4j.Slf4j;
//import net.bytebuddy.asm.Advice;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.apache.skywalking.apm.toolkit.trace.TraceContext;
//import org.springframework.util.CollectionUtils;
//
//import java.lang.reflect.Method;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class BatchConsumerCallbackAdvisor {
//
//    @Advice.OnMethodEnter
//    public static void onMethodEnter(@Advice.This Object object, @Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
//        try {
//            @SuppressWarnings("unchecked")
//            List<BatchConsumerCallback.MQMessage> batchMessage = arguments != null && arguments.length > 0 ? (List<BatchConsumerCallback.MQMessage>) arguments[0] : null;
//            if (!CollectionUtils.isEmpty(batchMessage)) {
//                List<String> topicList = Lists.newArrayList();
//                List<String> msgIdList = Lists.newArrayList();
//                List<String> keysList = Lists.newArrayList();
//                for (BatchConsumerCallback.MQMessage mqMessage : batchMessage) {
//                    MessageExt messageExt = (MessageExt) mqMessage.getMessageExt();
//                    topicList.add(messageExt.getTopic());
//                    msgIdList.add(messageExt.getMsgId());
//                    keysList.add(messageExt.getKeys());
//                }
//                MDCUtils.put(MDCConstantEnum.SERVER_NAME.getCode(), Foundation.app().getAppId());
//                MDCUtils.put(MDCConstantEnum.SERVER_HOSTNAME.getCode(), IPUtils.getLocalHostName());
//                MDCUtils.put(MDCConstantEnum.SERVER_IP.getCode(), IPUtils.getLocalIp());
//                MDCUtils.put(MDCConstantEnum.REQUEST_TYPE.getCode(), RequestTypeEnum.MQ.name().toLowerCase());
//                MDCUtils.put(MDCConstantEnum.TRACE_ID.getCode(), TraceContext.traceId());
//                MDCUtils.put(MDCConstantEnum.REQUEST_URI.getCode(), object.getClass().getName() + "#call");
//                MDCUtils.put(MDCConstantEnum.MESSAGE_TOPIC.getCode(), topicList.stream().distinct().collect(Collectors.joining(",")));
//                MDCUtils.put(MDCConstantEnum.MESSAGE_ID.getCode(), String.join(",", msgIdList));
//                MDCUtils.put(MDCConstantEnum.MESSAGE_KEYS.getCode(), String.join(",", keysList));
//            }
//        } catch (Exception e) {
//            log.warn("log skyeye >>> BatchConsumerCallbackAdvisor.onMethodEnter occur exception", e);
//        }
//    }
//
//    @Advice.OnMethodExit(onThrowable = Throwable.class)
//    public static void onMethodExit() {
//        MDCUtils.clear();
//    }
//
//}
