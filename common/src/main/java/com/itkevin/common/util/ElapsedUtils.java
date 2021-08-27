package com.itkevin.common.util;

import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.itkevin.common.config.SysConfig;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.enums.BusinessTypeEnum;
import com.itkevin.common.enums.MDCConstantEnum;
import com.itkevin.common.model.BusinessData;
import com.itkevin.common.model.HashedWheelData;
import com.itkevin.common.model.UriElapsedCollect;
import com.itkevin.common.model.UriElapsedData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ElapsedUtils {

    /**
     * 接口耗时统一前缀
     */
    public static final String URI_ELAPSED = "URI_ELAPSED_";

    /**
     * 接口耗时traceId集合统一后缀
     */
    public static final String URI_ELAPSED_TRACEID_LIST = "_URI_ELAPSED_TRACEID_LIST";

    /**
     * 接口最大耗时统一后缀
     */
    public static final String MAX_URI_ELAPSED = "_MAX_URI_ELAPSED";

    /**
     * 接口最大耗时traceId统一后缀
     */
    public static final String MAX_URI_ELAPSED_TRACEID = "_MAX_URI_ELAPSED_TRACEID";

    /**
     * 接口uri耗时聚合-指定线程池
     */
    private static ExecutorService executorService = new ThreadPoolExecutor(SysConstant.THREAD_NUM, SysConstant.MAX_THREAD_NUM, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1), new ThreadPoolExecutor.DiscardPolicy());

    /**
     * 接口uri耗时聚合
     * @param uriElapsedCollect
     */
    public static void uriElapsed(UriElapsedCollect uriElapsedCollect) {
        Mono.fromRunnable(() -> {
            try {
                long elapsed = uriElapsedCollect.getElapsed();
                String requestURI = uriElapsedCollect.getRequestURI();
                if (StringUtils.isNotBlank(requestURI)) {
                    requestURI = requestURI.replaceAll("/\\d+", "/{PathVariable}");
                    // 接口耗时报警间隔时间、接口耗时超过阀值时间次数
                    Integer alarmUriElapsedTime = SysConfig.instance.getAlarmUriElapsedTime();
                    Integer alarmUriElapsedCount = SysConfig.instance.getAlarmUriElapsedCount();
                    if (alarmUriElapsedTime == null || alarmUriElapsedCount == null || alarmUriElapsedTime == 0 || alarmUriElapsedCount == 0) {
                        return;
                    }
                    // 获取指定URI接口耗时时间阀值
                    long elapsedThreshold = getUriElapsedThreshold(requestURI);
                    // 耗时超过阀值
                    if (elapsed > elapsedThreshold) {
                        // 耗时超过阀值的次数
                        String uniqueKey = URI_ELAPSED + requestURI;
                        // 存储缓存数据
                        storageData(uniqueKey, uriElapsedCollect);
                        // 计算次数
                        Integer count = LocalCacheUtils.incr(uniqueKey);
                        if (count.compareTo(1) == 0) {
                            // 第一次耗时超过阀值时则给时间轮上添加任务
                            BusinessData businessData = new BusinessData();
                            businessData.setRequestURI(requestURI);
                            businessData.setServerName(uriElapsedCollect.getServerName());
                            businessData.setServerIP(uriElapsedCollect.getServerIP());
                            businessData.setServerHostname(uriElapsedCollect.getServerHostname());
                            HashedWheelUtils.putWheelQueue(new HashedWheelData(alarmUriElapsedTime, BusinessTypeEnum.URI_ELAPSED.name(), uniqueKey, JSONUtil.toJsonStr(businessData)));
                        }
                    }
                }
            } catch (Throwable e) {
                log.warn("log skyeye >>> ElapsedUtils.uriElapsed occur exception", e);
            }
        }).subscribeOn(Schedulers.fromExecutorService(executorService, "skyeye-uriElapsed-executor")).subscribe();
    }

    /**
     * 获取指定URI接口耗时时间阀值
     * @param requestURI
     * @return
     */
    public static long getUriElapsedThreshold(String requestURI) {
        if (StringUtils.isBlank(requestURI)) {
            return 0;
        }
        // 获取指定URI耗时时间阀值
        String alarmUriElapsed = SysConfig.instance.getAlarmUriElapsed();
        List<UriElapsedData> uriElapsedDataList = StringUtils.isNotBlank(alarmUriElapsed) ? JSONUtil.toList(JSONUtil.parseArray(alarmUriElapsed), UriElapsedData.class) : Lists.newArrayList();
        uriElapsedDataList = uriElapsedDataList.stream()
                .filter(uriElapsedData -> StringUtils.isNotBlank(uriElapsedData.getUri()))
                .peek(uriElapsedData -> uriElapsedData.setUri(uriElapsedData.getUri().replaceAll("/\\d+", "/{PathVariable}")))
                .collect(Collectors.toList());
        List<Long> elapsedList = uriElapsedDataList.stream().filter(uriElapsedData -> uriElapsedData.getUri().equalsIgnoreCase(requestURI)).map(UriElapsedData::getElapsed).collect(Collectors.toList());
        // 获取全局接口耗时时间阀值
        Long alarmUriElapsedGlobal = SysConfig.instance.getAlarmUriElapsedGlobal();

        return CollectionUtils.isEmpty(elapsedList) ? alarmUriElapsedGlobal : elapsedList.get(0);
    }

    /**
     * 存储缓存数据
     * @param uriElapsedCollect
     */
    private static void storageData(String uniqueKey, UriElapsedCollect uriElapsedCollect) {
        storageTraceidListData(uniqueKey, uriElapsedCollect.getTraceId());
        storageMaxElapsedData(uniqueKey, uriElapsedCollect.getElapsed(), uriElapsedCollect.getTraceId());
    }

    /**
     * 存储traceIdList
     * @param uniqueKey
     * @param traceId
     */
    private static synchronized void storageTraceidListData(String uniqueKey, String traceId) {
        String traceIdListKey = uniqueKey + URI_ELAPSED_TRACEID_LIST;
        List<String> traceIds = LocalCacheUtils.smember(traceIdListKey);
        if (CollectionUtils.isEmpty(traceIds)) {
            LocalCacheUtils.sadd(traceIdListKey, traceId);
        } else {
            if (traceIds.size() < 3) {
                LocalCacheUtils.sadd(traceIdListKey, traceId);
            }
        }
    }

    /**
     * 存储最大耗时和最大耗时traceId
     * @param uniqueKey
     * @param elapsed
     * @param traceId
     */
    private static synchronized void storageMaxElapsedData(String uniqueKey, long elapsed, String traceId) {
        String maxElapsedKey = uniqueKey + MAX_URI_ELAPSED;
        String maxElapsedTraceIdKey = uniqueKey + MAX_URI_ELAPSED + MAX_URI_ELAPSED_TRACEID;
        Long maxElapsed = LocalCacheUtils.getLongCache(maxElapsedKey);
        if (elapsed > maxElapsed) {
            LocalCacheUtils.putLongCache(maxElapsedKey, elapsed);
            LocalCacheUtils.putCache(maxElapsedTraceIdKey, traceId);
        }
    }

    /**
     * URI数据采集
     * @param elapsed
     * @return
     */
    public static UriElapsedCollect uriElapsedCollect(long elapsed) {
        UriElapsedCollect uriElapsedCollect = new UriElapsedCollect();
        uriElapsedCollect.setRequestURI(MDCUtils.get(MDCConstantEnum.REQUEST_URI.getCode()));
        uriElapsedCollect.setElapsed(elapsed);
        uriElapsedCollect.setTraceId(MDCUtils.get(MDCConstantEnum.TRACE_ID.getCode()));
        uriElapsedCollect.setServerName(MDCUtils.get(MDCConstantEnum.SERVER_NAME.getCode()));
        uriElapsedCollect.setServerIP(MDCUtils.get(MDCConstantEnum.SERVER_IP.getCode()));
        uriElapsedCollect.setServerHostname(MDCUtils.get(MDCConstantEnum.SERVER_HOSTNAME.getCode()));

        return uriElapsedCollect;
    }
}
