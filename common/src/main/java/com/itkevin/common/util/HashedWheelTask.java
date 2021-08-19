package com.itkevin.common.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HashedWheelTask {

    /**
     * 调度线程池
     */
    private static ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    /**
     * 初始化
     */
    public static void init() {
        service.scheduleAtFixedRate(HashedWheelUtils::task, 1, 1, TimeUnit.MINUTES);
    }

}
