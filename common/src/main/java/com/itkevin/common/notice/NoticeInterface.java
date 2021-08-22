package com.itkevin.common.notice;

import com.itkevin.common.notice.model.BaseMessage;
import okhttp3.OkHttpClient;

/**
 * 开放通知接口
 */
public interface NoticeInterface {

    /**
     * 发送消息
     * @param baseMessage
     */
    public void sendMessage(MarkDownBaseMessage baseMessage);
}
