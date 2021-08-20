package com.itkevin.common.notice.model;

import cn.hutool.json.JSONObject;
import com.itkevin.common.enums.LogLevelEnum;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

@Data
public class BaseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * {@link LogLevelEnum}
     * 消息级别
     */
    private String level;

    /**
     * at所有人
     */
    private Boolean isAtAll;

    /**
     * at成员
     */
    private List<String> atMobiles;

    /**
     * at成员
     * @param atMobiles 手机号
     * @return
     */
    protected JSONObject setAtAllAndMobile(List<String> atMobiles) {
        JSONObject atMobile = new JSONObject();
        if (!CollectionUtils.isEmpty(atMobiles)) {
            atMobile.put("atMobiles", atMobiles);
            atMobile.put("isAtAll", false);
        }
        return atMobile;
    }
}