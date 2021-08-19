//package com.itkevin.common.util;
//
//import com.xxl.job.core.handler.IJobHandler;
//import com.xxl.job.core.handler.annotation.JobHandler;
//import lombok.extern.slf4j.Slf4j;
//import net.bytebuddy.ByteBuddy;
//import net.bytebuddy.agent.ByteBuddyAgent;
//import net.bytebuddy.asm.Advice;
//import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
//import net.bytebuddy.matcher.ElementMatchers;
//import org.springframework.aop.support.AopUtils;
//import org.springframework.context.ApplicationContext;
//import org.springframework.util.CollectionUtils;
//
//import java.util.Map;
//
//@Slf4j
//public class ByteBuddyUtils {
//
//    /**
//     * 字节码增强
//     * @param applicationContext
//     */
//    public static void byteBuddy(ApplicationContext applicationContext) {
//        // RocketMq消息者回调实现类增强
//        byteBuddyMq(applicationContext);
//        // job任务类增强
//        byteBuddyJob(applicationContext);
//    }
//
//    /**
//     * RocketMq消息者回调实现类增强
//     * @param applicationContext
//     */
//    private static void byteBuddyMq(ApplicationContext applicationContext) {
//        Map<String, ConsumerCallback> consumerCallbackMap = applicationContext.getBeansOfType(ConsumerCallback.class);
//        if (!CollectionUtils.isEmpty(consumerCallbackMap)) {
//            consumerCallbackMap.forEach((name, consumerCallback) -> {
//                try{
//                    log.info("byteBuddyMq consumerCallback : {}, class {}",name,AopUtils.getTargetClass(consumerCallback));
//                    ByteBuddyAgent.install();
//                    new ByteBuddy().redefine(AopUtils.getTargetClass(consumerCallback))
//                            .visit(Advice.to(ConsumerCallbackAdvisor.class).on(ElementMatchers.isOverriddenFrom(ConsumerCallback.class).and(ElementMatchers.named("call"))))
//                            .make()
//                            .load(consumerCallback.getClass().getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
//                }catch (Exception e){
//                    log.warn("byteBuddyMq error, e: {}",e.getMessage());
//                }
//
//            });
//        }
//        Map<String, BatchConsumerCallback> batchConsumerCallbackMap = applicationContext.getBeansOfType(BatchConsumerCallback.class);
//        if (!CollectionUtils.isEmpty(batchConsumerCallbackMap)) {
//            batchConsumerCallbackMap.forEach((name, batchConsumerCallback) -> {
//               try{
//                   log.info("byteBuddyMq batchConsumerCallback : {}, class {}: ",name,AopUtils.getTargetClass(batchConsumerCallback));
//                   ByteBuddyAgent.install();
//                   new ByteBuddy().redefine(AopUtils.getTargetClass(batchConsumerCallback))
//                           .visit(Advice.to(BatchConsumerCallbackAdvisor.class).on(ElementMatchers.isOverriddenFrom(BatchConsumerCallback.class).and(ElementMatchers.named("call"))))
//                           .make()
//                           .load(batchConsumerCallback.getClass().getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
//               }catch (Exception e){
//                   log.warn("byteBuddyMq error, e: {}",e.getMessage());
//               }
//            });
//        }
//    }
//
//    /**
//     * job任务类增强
//     * @param applicationContext
//     */
//    private static void byteBuddyJob(ApplicationContext applicationContext) {
//        Map<String, Object> jobHandlerMap = applicationContext.getBeansWithAnnotation(JobHandler.class);
//        if (!CollectionUtils.isEmpty(jobHandlerMap)) {
//            try{
//                jobHandlerMap.forEach((name, jobHandler) -> {
//                    log.info("byteBuddyJob : {},class : {}",name,AopUtils.getTargetClass(jobHandler));
//                    ByteBuddyAgent.install();
//                    new ByteBuddy().redefine(AopUtils.getTargetClass(jobHandler))
//                            .visit(Advice.to(JobHandlerAdvisor.class).on(ElementMatchers.isOverriddenFrom(IJobHandler.class).and(ElementMatchers.named("execute"))))
//                            .make()
//                            .load(jobHandler.getClass().getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
//                });
//            }catch (Exception e){
//                log.warn("byteBuddyJob error, e: {}",e.getMessage());
//            }
//
//        }
//    }
//}
