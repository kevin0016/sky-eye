package com.itkevin.common.util;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.foundation.Foundation;
import com.google.common.collect.ImmutableList;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
public class SimpleMetricsUtils {

    /**
     * 累计计数
     * @param name
     * @param tags
     * @param values
     * @param amount
     */
    public static void counter(String name, List<String> tags, List<String> values, Double amount) {
        try {
            if (filter()) return;
            String[] tagValues = getTagValues(tags, values);
//            Monitors.count(name, amount, tagValues);
        } catch (Exception e) {
            log.warn("log skyeye >>> SimpleMetricsUtils.counter occur exception", e);
        }
    }

    /**
     * 获取intGaugeNumber
     * @param name
     * @param tags
     * @param values
     * @return
     */
    public static AtomicInteger getIntGaugeNumber(String name, List<String> tags, List<String> values) {
        AtomicInteger number = new AtomicInteger(0);
        try {
            if (filter()) return number;
            String[] tagValues = getTagValues(tags, values);
            number = Metrics.gauge(name, Tags.of(tagValues), new AtomicInteger(0));
        } catch (Exception e) {
            log.warn("log skyeye >>> SimpleMetricsUtils.getIntGaugeNumber occur exception", e);
        }
        return number;
    }

    /**
     * 设置intGaugeNumber
     * @param number
     * @param amount
     */
    public static void setIntGaugeNumber(AtomicInteger number, Integer amount) {
        try {
            if (filter()) return;
            number.set(amount);
        } catch (Exception e) {
            log.warn("log skyeye >>> SimpleMetricsUtils.setIntGaugeNumber occur exception", e);
        }
    }

    /**
     * 过滤上报
     * @return
     */
    private static boolean filter() {
        boolean filter = false;
        String envName = Foundation.server().getEnvType();
//        if (Env.YUFA.name().equalsIgnoreCase(envName)) {
//            filter = true;
//        }
        return filter;
    }

    /**
     * 获取标签数组
     * @param tags
     * @param values
     * @return
     */
    private static String[] getTagValues(List<String> tags, List<String> values) {
        return ArrayUtils.toStringArray(
                IntStream.range(0, Math.min(tags.size(), values.size())).mapToObj(index -> ImmutableList.of(tags.get(index), values.get(index)))
                        .flatMap(Collection::stream).toArray()
        );
    }

}
