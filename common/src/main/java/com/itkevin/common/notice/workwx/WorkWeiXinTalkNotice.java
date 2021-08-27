package com.itkevin.common.notice.workwx;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itkevin.common.config.SysConfig;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.enums.LogLevelEnum;
import com.itkevin.common.notice.AbstractNotice;
import com.itkevin.common.notice.MarkDownBaseMessage;
import com.itkevin.common.util.LocalCacheUtils;
import com.itkevin.common.util.StringConverterFactory;
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class WorkWeiXinTalkNotice extends AbstractNotice {
    /**
     * 钉钉机器人标识
     */
    private static final String WEBHOOK_INDEX = "WEWORK_WEBHOOK_INDEX";

    /**
     * baseUrl
     */
    private static final String BASE_URL = "https://qyapi.weixin.qq.com";

    private static volatile WorkWeiXinTalkNotice workWeiXinTalkNotice;

    public static WorkWeiXinTalkNotice getInstance(){
        try {
            if(null == workWeiXinTalkNotice){
                synchronized (WorkWeiXinTalkNotice.class){
                    if(null == workWeiXinTalkNotice){
                        workWeiXinTalkNotice = new WorkWeiXinTalkNotice();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return workWeiXinTalkNotice;
    }

    /**
     * 钉钉服务接口（retrofit2-reactor-adapter：https://github.com/JakeWharton/retrofit2-reactor-adapter）
     */
    private static WorkWeiXinTalkService workWeiXinTalkService = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(initOkHttpClient())
            .addConverterFactory(new StringConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .build()
            .create(WorkWeiXinTalkService.class);

    /**
     * 发送钉钉消息
     * @param markDownBaseMessage
     */
    @Override
    public void sendMessage(MarkDownBaseMessage markDownBaseMessage) {
        try {
            String level = markDownBaseMessage.getLevel();
            String alarmDingTalk = StringUtils.isNotBlank(level) && level.equals(LogLevelEnum.SERIOUS.name())
                    ? SysConfig.instance.getAlarmSeriousTalkHook()
                    : SysConfig.instance.getAlarmTalkHook();
            if (StringUtils.isBlank(alarmDingTalk)) {
                log.warn("log skyeye >>> config 'skyeye.log.alarm.weWorktalk' or 'skyeye.log.alarm.serious.weWorktalk' is null");
                return;
            }
            List<WeWorkConfigData> weWorkConfigDataList = JSONUtil.toList(JSONUtil.parseArray(alarmDingTalk), WeWorkConfigData.class);
           String str = converMarkDownBaseMessage2Str(markDownBaseMessage);
            send(weWorkConfigDataList, str);
        } catch (Exception e) {
            log.warn("log skyeye >>> WeWorkTalkUtils.sendMessage occur exception", e);
        }
    }

    private String converMarkDownBaseMessage2Str(MarkDownBaseMessage markDownBaseMessage) {
        JSONObject markdownContent = new JSONObject();
        markdownContent.put("title", markDownBaseMessage.getTitle());
        markdownContent.put("content", markDownBaseMessage.getContent());
        markdownContent.put("mentioned_list", markDownBaseMessage.getAtMobiles());
        JSONObject json = new JSONObject();
        json.put("msgtype", markDownBaseMessage.getMsgType());
        json.put("markdown", markdownContent);
        return JSONUtil.toJsonStr(json);
    }


    /**
     * 发送
     * @param weWorkConfigDataList
     * @param jsonContent
     */
    private void send(List<WeWorkConfigData> weWorkConfigDataList, String jsonContent) {
        int currentIndex = getIndex(weWorkConfigDataList.size());
        WeWorkConfigData weWorkConfigData = weWorkConfigDataList.get(currentIndex);
        String key = weWorkConfigData.getKey();
        if (StringUtils.isBlank(key)) {
            Map<String, String> paramMap = HttpUtil.decodeParamMap(weWorkConfigData.getWebHook(), CharsetUtil.defaultCharset());
            key = paramMap.get("key");
        }
        send(key, jsonContent);
    }

    /**
     * 发送
     * @param key
     * @param jsonContent
     */
    private boolean send(String key, String jsonContent) {
        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonContent);
            log.info("log skyeye >>> send weWorktalk key: {}", key);
            Mono<Response<ResponseBody>> mono = workWeiXinTalkService.robotSendMono(key, requestBody);
            ResponseBody responseBody = mono.blockOptional().map(Response::body).orElse(null);
            String string = responseBody != null ? responseBody.string() : "";
            log.info("log skyeye >>> send weWorktalk result: {}", string);
            if (string.contains("<!DOCTYPE html>")) {
                return false;
            }
            JSONObject result = JSONUtil.parseObj(string);
            if (result.get("errcode") == null || !"0".equals(String.valueOf(result.get("errcode")))) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("log skyeye >>> weWorkTalkUtils.send occur exception", e);
            return false;
        }
    }

    /**
     * 获取第几个机器人
     * @param totalCount
     * @return
     */
    private synchronized int getIndex(int totalCount) {
        int currentIndex = LocalCacheUtils.getIntCache(WEBHOOK_INDEX);
        if (currentIndex > totalCount - 1) {
            currentIndex = 0;
            LocalCacheUtils.putIntCache(WEBHOOK_INDEX, currentIndex);
        }
        LocalCacheUtils.incr(WEBHOOK_INDEX);
        return currentIndex;
    }

    @Override
    public String filterFlag() {
        return "wework";
    }
}
