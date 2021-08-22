package com.itkevin.common.notice.workwx;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itkevin.common.notice.MarkDownBaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class WeWorkMarkDownMessage extends MarkDownBaseMessage implements Serializable {
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
        markdownContent.put("mentioned_list", this.getAtMobiles());
        JSONObject json = new JSONObject();
        json.put("msgtype", this.getMsgType());
        json.put("markdown", markdownContent);
        return JSONUtil.toJsonStr(json);
    }
}
