package com.itkevin.common.notice;

import com.itkevin.common.notice.model.BaseMessage;
import okhttp3.OkHttpClient;

/**
 * 开放通知接口
 */
public interface NoticeInterface {

    public void sendMessage(BaseMessage baseMessage);
}
