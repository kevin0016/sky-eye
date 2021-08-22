package com.itkevin.common.notice;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itkevin.common.notice.model.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class MarkDownBaseMessage extends BaseMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    private String msgType = "markdown";

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    @Override
    public String toString() {
        JSONObject markdownContent = new JSONObject();
        markdownContent.put("title", this.getTitle());
        markdownContent.put("content", this.getContent());
        JSONObject json = new JSONObject();
        json.put("msgtype", this.getMsgType());
        json.put("at", this.setAtAllAndMobile(this.getAtMobiles()));
        json.put("markdown", markdownContent);
        return JSONUtil.toJsonStr(json);
    }
}
