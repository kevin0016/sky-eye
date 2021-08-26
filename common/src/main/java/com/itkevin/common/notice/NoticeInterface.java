package com.itkevin.common.notice;

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

}
