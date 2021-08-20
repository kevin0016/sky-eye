package com.itkevin.common.notice;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public abstract class AbstractNotice implements NoticeInterface{

    /**
     * 初始化OkHttpClient实例
     * @return
     */
    public static OkHttpClient initOkHttpClient() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .writeTimeout(500, TimeUnit.MILLISECONDS)
                .build();
    }
}
