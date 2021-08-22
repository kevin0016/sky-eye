package com.itkevin.common.enums;

import com.itkevin.common.notice.NoticeInterface;
import com.itkevin.common.notice.dingding.DingTalkNotice;
import com.itkevin.common.notice.workwx.WorkWeiXinTalkNotice;

import java.util.Arrays;

public enum AlarmToolEnum {

    WEWORK("wework", WorkWeiXinTalkNotice.class),
    DINGDING("dingding", DingTalkNotice.class);

    private String code;
    private Class<NoticeInterface> name;

    public String getCode() {
        return code;
    }

    public Class getName() {
        return name;
    }

    AlarmToolEnum(String code, Class name) {
        this.code = code;
        this.name = name;
    }

    public static AlarmToolEnum getByValue(String code) {
        return Arrays.stream(AlarmToolEnum.values())
                .filter(resultCodeEnum -> resultCodeEnum.code.equals(code))
                .findFirst().orElse(null);
    }
}
