//package com.itkevin.common.util;
//
//import com.xxl.job.core.handler.IJobHandler;
//import net.bytebuddy.asm.Advice;
//import net.bytebuddy.build.Plugin;
//import net.bytebuddy.description.annotation.AnnotationList;
//import net.bytebuddy.description.type.TypeDescription;
//import net.bytebuddy.dynamic.ClassFileLocator;
//import net.bytebuddy.dynamic.DynamicType;
//import net.bytebuddy.matcher.ElementMatchers;
//import org.springframework.util.CollectionUtils;
//
//import java.io.IOException;
//
//public class ByteBuddyPlugin implements Plugin {
//
//    @Override
//    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassFileLocator classFileLocator) {
//        if (typeDescription.isAssignableTo(ConsumerCallback.class)) {
//            return builder.visit(Advice.to(ConsumerCallbackAdvisor.class).on(ElementMatchers.isOverriddenFrom(ConsumerCallback.class).and(ElementMatchers.named("call"))));
//        } else if (typeDescription.isAssignableTo(BatchConsumerCallback.class)) {
//            return builder.visit(Advice.to(BatchConsumerCallbackAdvisor.class).on(ElementMatchers.isOverriddenFrom(BatchConsumerCallback.class).and(ElementMatchers.named("call"))));
//        } else if (typeDescription.isAssignableTo(IJobHandler.class)) {
//            return builder.visit(Advice.to(JobHandlerAdvisor.class).on(ElementMatchers.isOverriddenFrom(IJobHandler.class).and(ElementMatchers.named("execute"))));
//        } else {
//            return builder.visit(Advice.to(EventProcessorAdvisor.class).on(ElementMatchers.isAnnotatedWith(EventProcessor.class)));
//        }
//    }
//
//    @Override
//    public void close() throws IOException {
//
//    }
//
//    @Override
//    public boolean matches(TypeDescription typeDefinitions) {
//        boolean isConsumerCallback = typeDefinitions.isAssignableTo(ConsumerCallback.class);
//        boolean isBatchConsumerCallback = typeDefinitions.isAssignableTo(BatchConsumerCallback.class);
//        boolean isJobHander = typeDefinitions.isAssignableTo(IJobHandler.class);
//        AnnotationList annotationList = typeDefinitions.getDeclaredAnnotations();
//        return isConsumerCallback || isBatchConsumerCallback || isJobHander || (!CollectionUtils.isEmpty(annotationList) && annotationList.isAnnotationPresent(EventProcessor.class));
//    }
//}
