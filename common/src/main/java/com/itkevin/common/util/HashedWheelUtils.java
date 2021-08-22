package com.itkevin.common.util;

import cn.hutool.json.JSONUtil;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.enums.BusinessTypeEnum;
import com.itkevin.common.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HashedWheelUtils {

    /**
     * 时间轮大小
     */
    public static final int WHEEL_SIZE = 60;

    /**
     * 时间轮当前位置
     */
    public static final String WHEEL_CURRENT_INDEX = "WHEEL_CURRENT_INDEX";

    /**
     * 时间轮延迟位置
     */
    public static final String WHEEL_WAIT_INDEX = "WHEEL_WAIT_INDEX";

    /**
     * 发送消息时间轮数据-指定线程池
     */
    private static ExecutorService executorService = new ThreadPoolExecutor(SysConstant.THREAD_NUM, SysConstant.MAX_THREAD_NUM, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1), new ThreadPoolExecutor.DiscardPolicy());

    /**
     * intGaugeNumber
     */
    private static AtomicInteger gaugeNumber = SimpleMetricsUtils.getIntGaugeNumber(SysConstant.ALARM_METRIC_NAME, Collections.emptyList(), Collections.emptyList());

    /**
     * 将任务加入到时间轮
     * @param hashedWheelData
     */
    protected static void putWheelQueue(HashedWheelData hashedWheelData) {
        // 任务模型
        TaskModel taskModel = new TaskModel();
        taskModel.setBusinessId(hashedWheelData.getBusinessId());
        int cycleNum = 0;
        if (hashedWheelData.getDelayTime() > WHEEL_SIZE) {
            cycleNum = hashedWheelData.getDelayTime() / WHEEL_SIZE;
        }
        taskModel.setCycleNum(cycleNum);
        // 任务数据放入缓存
        LocalCacheUtils.putCache(hashedWheelData.getBusinessId(), JSONUtil.toJsonStr(hashedWheelData));
        // 获取时间轮当前位置
        Integer currentIndex = LocalCacheUtils.getIntCache(WHEEL_CURRENT_INDEX);
        // 计算任务存放位置
        int offset = hashedWheelData.getDelayTime() - WHEEL_SIZE * cycleNum;
        if (currentIndex + offset > WHEEL_SIZE) {
            currentIndex = currentIndex + offset - WHEEL_SIZE;
        } else {
            currentIndex += offset;
        }
        // 任务加入时间轮
        LocalCacheUtils.sadd(WHEEL_WAIT_INDEX + currentIndex, JSONUtil.toJsonStr(taskModel));
    }

    /**
     * 时间轮任务
     */
    protected synchronized static void task() {
        Integer currentIndex = LocalCacheUtils.incr(WHEEL_CURRENT_INDEX);
        if (currentIndex > WHEEL_SIZE) {
            currentIndex = currentIndex - WHEEL_SIZE;
            LocalCacheUtils.putIntCache(WHEEL_CURRENT_INDEX, currentIndex);
        }
        // 获取时间轮当前位置的任务列表
        List<String> list = LocalCacheUtils.smember(WHEEL_WAIT_INDEX + currentIndex);
        if (!CollectionUtils.isEmpty(list)) {
            // 循环处理任务
            for (String task : list) {
                String businessId = "";
                try {
                    TaskModel taskModel = JSONUtil.toBean(task, TaskModel.class);
                    businessId = taskModel.getBusinessId();
                    if (taskModel.getCycleNum() == 0) {
                        String data = LocalCacheUtils.getCache(businessId);
                        if (StringUtils.isNotBlank(data)) {
                            HashedWheelData hashedWheelData = JSONUtil.toBean(data, HashedWheelData.class);
                            handleHashedWheelData(hashedWheelData);
                            LocalCacheUtils.srem(WHEEL_WAIT_INDEX + currentIndex, task);
                            LocalCacheUtils.delCache(businessId);
                            LocalCacheUtils.delIntCache(hashedWheelData.getBusinessId());
                            LocalCacheUtils.delLongCache(businessId + ElapsedUtils.MAX_URI_ELAPSED);
                            LocalCacheUtils.delCache(businessId + ElapsedUtils.MAX_URI_ELAPSED + ElapsedUtils.MAX_URI_ELAPSED_TRACEID);
                            LocalCacheUtils.delCache(businessId + ElapsedUtils.URI_ELAPSED_TRACEID_LIST);
                        }
                    } else {
                        taskModel.setCycleNum(taskModel.getCycleNum() - 1);
                        LocalCacheUtils.srem(WHEEL_WAIT_INDEX + currentIndex, task);
                        LocalCacheUtils.sadd(WHEEL_WAIT_INDEX + currentIndex, JSONUtil.toJsonStr(taskModel));
                    }
                } catch (Exception e) {
                    log.warn("log skyeye >>> HashedWheelTask occur exception, businessId: {}", businessId, e);
                    LocalCacheUtils.srem(WHEEL_WAIT_INDEX + currentIndex, task);
                    LocalCacheUtils.delCache(businessId);
                    LocalCacheUtils.delIntCache(businessId);
                    LocalCacheUtils.delLongCache(businessId + ElapsedUtils.MAX_URI_ELAPSED);
                    LocalCacheUtils.delCache(businessId + ElapsedUtils.MAX_URI_ELAPSED + ElapsedUtils.MAX_URI_ELAPSED_TRACEID);
                    LocalCacheUtils.delCache(businessId + ElapsedUtils.URI_ELAPSED_TRACEID_LIST);
                }
            }
        }
        // 异常报警上报
        Integer skyAlarmNum = LocalCacheUtils.getIntCache(SysConstant.ALARM_METRIC_NAME);
        SimpleMetricsUtils.setIntGaugeNumber(gaugeNumber, skyAlarmNum);
        LocalCacheUtils.putIntCache(SysConstant.ALARM_METRIC_NAME, 0);
    }

    /**
     * 处理时间轮任务数据
     * @param hashedWheelData
     */
    private static void handleHashedWheelData(HashedWheelData hashedWheelData) {
        String businessType = hashedWheelData.getBusinessType();
        if (BusinessTypeEnum.NOTIFY.name().equalsIgnoreCase(businessType)) {
            Integer alarmNotifyTime = ConfigUtils.getIntProperty(SysConstant.ALARM_NOTIFY_TIME, SysConstant.ALARM_NOTIFY_TIME_DEFAULT);
            Integer alarmNotifyCount = ConfigUtils.getIntProperty(SysConstant.ALARM_NOTIFY_COUNT, SysConstant.ALARM_NOTIFY_COUNT_DEFAULT);
            Integer count = LocalCacheUtils.getIntCache(hashedWheelData.getBusinessId());
            // 实际报警次数超过阀值则发送聚合报警消息
            if (count > alarmNotifyCount) {
                BusinessData businessData = JSONUtil.toBean(hashedWheelData.getBusinessData(), BusinessData.class);
                LogCompressData logCompressData = CommonConverter.getConverter().map(businessData, LogCompressData.class);
                logCompressData.setAlarmTime(alarmNotifyTime);
                logCompressData.setAlarmCount(count);
                Mono.fromRunnable(() -> {
                    try {
                        NotifyMessageUtils.getInstance().sendAlarmTalk(logCompressData);
                    } catch (Throwable e) {
                        log.warn("log skyeye >>> HashedWheelTask.handleHashedWheelData[{}] occur exception", businessType, e);
                    }
                }).subscribeOn(Schedulers.fromExecutorService(executorService, "skyeye-sendMessage-hashedWheelData-executor")).subscribe();
            }
        }
        if (BusinessTypeEnum.URI_ELAPSED.name().equalsIgnoreCase(businessType)) {
            Integer alarmUriElapsedTime = ConfigUtils.getIntProperty(SysConstant.ALARM_URI_ELAPSED_TIME, 0);
            Integer alarmUriElapsedCount = ConfigUtils.getIntProperty(SysConstant.ALARM_URI_ELAPSED_COUNT, 0);
            Integer count = LocalCacheUtils.getIntCache(hashedWheelData.getBusinessId());
            // 耗时大于指定时间的次数超过阀值则发送聚合消息
            if (count > alarmUriElapsedCount) {
                BusinessData businessData = JSONUtil.toBean(hashedWheelData.getBusinessData(), BusinessData.class);
                LogUriElapsedData logUriElapsedData = CommonConverter.getConverter().map(businessData, LogUriElapsedData.class);
                logUriElapsedData.setAlarmTime(alarmUriElapsedTime);
                logUriElapsedData.setAlarmCount(count);
                logUriElapsedData.setUriElapsedThreshold(ElapsedUtils.getUriElapsedThreshold(businessData.getRequestURI()));
                logUriElapsedData.setTraceIdList(LocalCacheUtils.smember(hashedWheelData.getBusinessId() + ElapsedUtils.URI_ELAPSED_TRACEID_LIST));
                logUriElapsedData.setMaxUriElapsed(LocalCacheUtils.getLongCache(hashedWheelData.getBusinessId()  + ElapsedUtils.MAX_URI_ELAPSED));
                logUriElapsedData.setMaxUriElapsedTraceId(LocalCacheUtils.getCache(hashedWheelData.getBusinessId()  + ElapsedUtils.MAX_URI_ELAPSED + ElapsedUtils.MAX_URI_ELAPSED_TRACEID));
                Mono.fromRunnable(() -> {
                    try {
                        NotifyMessageUtils.getInstance().sendAlarmTalk(logUriElapsedData);
                    } catch (Throwable e) {
                        log.warn("log skyeye >>> HashedWheelTask.handleHashedWheelData[{}] occur exception", businessType, e);
                    }
                }).subscribeOn(Schedulers.fromExecutorService(executorService, "skyeye-sendMessage-hashedWheelData-executor")).subscribe();
            }
        }
    }
}
