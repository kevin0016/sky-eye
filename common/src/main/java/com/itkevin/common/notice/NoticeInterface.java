package com.itkevin.common.notice;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * 开放通知接口
 */
public interface NoticeInterface {

    /**
     * 发送消息
     * @param baseMessage
     */
    void sendMessage(MarkDownBaseMessage baseMessage);

    /**
     * 配置过滤器
     * @return
     */
    String filterFlag();

    /**
     * 初始化OkHttpClient实例
     * @return
     */
    static OkHttpClient initOkHttpClient() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .writeTimeout(500, TimeUnit.MILLISECONDS)
                .build();
    }

}
