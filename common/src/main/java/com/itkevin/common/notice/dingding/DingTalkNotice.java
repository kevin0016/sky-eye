package com.itkevin.common.notice.dingding;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itkevin.common.config.SysConfig;
import com.itkevin.common.enums.LogLevelEnum;
import com.itkevin.common.notice.MarkDownBaseMessage;
import com.itkevin.common.notice.NoticeInterface;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
public class DingTalkNotice implements NoticeInterface {

    private static volatile DingTalkNotice dingTalkNotice;

    public static DingTalkNotice getInstance() {
        try {
            if (null == dingTalkNotice) {
                synchronized (DingTalkNotice.class) {
                    if (null == dingTalkNotice) {
                        dingTalkNotice = new DingTalkNotice();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dingTalkNotice;
    }

    /**
     * 钉钉服务接口（retrofit2-reactor-adapter：https://github.com/JakeWharton/retrofit2-reactor-adapter）
     */
    private static DingTalkService dingTalkService = new Retrofit.Builder()
            .baseUrl("https://oapi.dingtalk.com")
            .client(NoticeInterface.initOkHttpClient())
            .addConverterFactory(new StringConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .build()
            .create(DingTalkService.class);

    /**
     * 钉钉机器人标识
     */
    private static final String WEBHOOK_INDEX = "DINGDING_WEBHOOK_INDEX";

    /**
     * 发送钉钉消息
     *
     * @param dingMarkDownMessage
     */
    @Override
    public void sendMessage(MarkDownBaseMessage dingMarkDownMessage) {
        try {
            String level = dingMarkDownMessage.getLevel();
            String alarmDingTalk = StringUtils.isNotBlank(level) && level.equals(LogLevelEnum.SERIOUS.name())
                    ? SysConfig.instance.getAlarmSeriousTalkHook()
                    : SysConfig.instance.getAlarmTalkHook();
            if (StringUtils.isBlank(alarmDingTalk)) {
                log.warn("log skyeye >>> config 'skyeye.log.alarm.dingtalk' or 'skyeye.log.alarm.serious.dingtalk' is null");
                return;
            }
            List<DingConfigData> dingConfigDataList = JSONUtil.toList(JSONUtil.parseArray(alarmDingTalk), DingConfigData.class);
            String str = converMarkDownDingMessage2Str(dingMarkDownMessage);
            send(dingConfigDataList, str);
        } catch (Exception e) {
            log.warn("log skyeye >>> DingTalkUtils.sendMessage occur exception", e);
        }
    }

    private String converMarkDownDingMessage2Str(MarkDownBaseMessage markDownBaseMessage) {
        JSONObject markdownContent = new JSONObject();
        markdownContent.put("title", markDownBaseMessage.getTitle());
        markdownContent.put("text", markDownBaseMessage.getContent());
        JSONObject json = new JSONObject();
        json.put("msgtype", markDownBaseMessage.getMsgType());
        json.put("at", markDownBaseMessage.setAtAllAndMobile(markDownBaseMessage.getAtMobiles()));
        json.put("markdown", markdownContent);
        return JSONUtil.toJsonStr(json);
    }

    /**
     * 发送
     *
     * @param dingConfigDataList
     * @param jsonContent
     */
    private void send(List<DingConfigData> dingConfigDataList, String jsonContent) {
        int currentIndex = getIndex(dingConfigDataList.size());
        DingConfigData dingConfigData = dingConfigDataList.get(currentIndex);
        String accessToken = dingConfigData.getAccessToken();
        if (StringUtils.isBlank(accessToken)) {
            Map<String, String> paramMap = HttpUtil.decodeParamMap(dingConfigData.getWebHook(), CharsetUtil.defaultCharset());
            accessToken = paramMap.get("access_token");
            accessToken = StringUtils.isBlank(accessToken) ? paramMap.get("accessToken") : accessToken;
        }
        send(accessToken, dingConfigData.getSecret(), jsonContent);
    }

    /**
     * 发送
     *
     * @param accessToken
     * @param secret
     * @param jsonContent
     */
    private boolean send(String accessToken, String secret, String jsonContent) {
        try {
            Long timestamp = System.currentTimeMillis();
            String sign = StringUtils.isNotBlank(secret) ? signData(timestamp, secret) : "";
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonContent);
            log.info("log skyeye >>> send dingding accessToken: {}, timestamp: {}, sign: {}", accessToken, timestamp, sign);
            Mono<Response<ResponseBody>> mono = dingTalkService.robotSendMono(accessToken, timestamp, sign, requestBody);
            ResponseBody responseBody = mono.blockOptional().map(Response::body).orElse(null);
            String string = responseBody != null ? responseBody.string() : "";
            log.info("log skyeye >>> send dingding result: {}", string);
            if (string.contains("<!DOCTYPE html>")) {
                return false;
            }
            JSONObject result = JSONUtil.parseObj(string);
            if (result.get("errcode") == null || !"0".equals(String.valueOf(result.get("errcode")))) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("log skyeye >>> DingTalkUtils.send occur exception", e);
            return false;
        }
    }

    /**
     * 验签
     *
     * @param timestamp
     * @param secret
     * @return
     */
    private String signData(Long timestamp, String secret) {
        try {
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            return URLEncoder.encode(new String(Base64.getEncoder().encode(signData)), "UTF-8");
        } catch (Exception e) {
            log.warn("log skyeye >>> DingTalkUtils.signData occur exception", e);
            return null;
        }
    }


    /**
     * 获取第几个机器人
     *
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

    public static void main(String[] args) {
//        List<DingConfigData> dingConfigDataList = new ArrayList<>();
//        DingConfigData dingConfigData1= new DingConfigData();
//        dingConfigData1.setWebHook("https://oapi.dingtalk.com/robot/send?access_token=xxxx");
//        dingConfigData1.setSecret("SEC2e6249a1bf419db8a89643ebf2625dbc0c3e47af12ce130a6582415d3f38da05");
//        DingConfigData dingConfigData2= new DingConfigData();
//        dingConfigData2.setWebHook("https://oapi.dingtalk.com/robot/send?access_token=xxxx");
//        dingConfigData2.setSecret("SECc193b8c3b7e47ad0fd4821bf26f08dc777010ecef372689604922b1c78e8184c");
//        dingConfigDataList.add(dingConfigData1);
//        dingConfigDataList.add(dingConfigData2);

//        DingMarkDownMessage message = new DingMarkDownMessage();
//        message.setTitle("出错啦！");
//        String exceptionMessage;
//        String exceptionStackTrace;
//        try {
//            throw new RuntimeException("异常message");
//        } catch (Exception e) {
//            exceptionMessage = e.getMessage();
//            exceptionStackTrace = ExceptionUtils.getStackTrace(e);
//        }
//        String content = "## **" + MDCConstantEnum.ERROR_MESSAGE.getName() + "：" + "出错啦！" + "**" + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.SERVER_NAME.getName() + "：" + "localhost" + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.SOURCE_IP.getName() + "：" + "10.155.8.91" + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.SERVER_IP.getName() + "：" + "10.155.8.91" + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.SERVER_HOSTNAME.getName() + "：" + "itkevin.it.com" + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.OCCURRENCE_TIME.getName() + "：" + "2020-06-23 14:20:05" + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.REQUEST_TYPE.getName() + "：" + "http GET" + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.TRACE_ID.getName() + "：" + "4696.167.15928932046440001" + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.REQUEST_URI.getName() + "：" + "/api/health" + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.REQUEST_PARAM.getName() + "：" + "{a=a, b=b}" + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.EXCEPTION_MESSAGE.getName() + "：" + exceptionMessage + System.getProperty("line.separator") +
//                "+ " + MDCConstantEnum.EXCEPTION_STACKTRACE.getName() + "：" + System.getProperty("line.separator") + System.getProperty("line.separator") +
//                "`" + exceptionStackTrace + "`";
//        message.setContent(content);
//        DingTalkNotice.sendMessage(message);
    }

    @Override
    public String filterFlag() {
        return "dingding";
    }
}
