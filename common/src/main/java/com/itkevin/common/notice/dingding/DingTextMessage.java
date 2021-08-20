package com.itkevin.common.notice.dingding;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itkevin.common.notice.model.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class DingTextMessage extends BaseMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    private String msgType = "text";

    /**
     * 消息内容
     */
    private String content;

    @Override
    public String toString() {
        JSONObject content = new JSONObject();
        content.put("content", this.getContent());
        JSONObject json = new JSONObject();
        json.put("msgtype", this.getMsgType());
        json.put("at", this.setAtAllAndMobile(this.getAtMobiles()));
        json.put("text", content);
        return JSONUtil.toJsonStr(json);
    }
}
