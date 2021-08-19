package com.itkevin.common.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.enums.LogLevelEnum;
import com.itkevin.common.enums.MDCConstantEnum;
import com.itkevin.common.model.DingConfigData;
import com.itkevin.common.model.DingMarkDownMessage;
import com.itkevin.common.model.DingMessage;
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import java.util.concurrent.TimeUnit;

@Slf4j
public class DingTalkUtils {

    /**
     * 钉钉服务接口（retrofit2-reactor-adapter：https://github.com/JakeWharton/retrofit2-reactor-adapter）
     */
    private static DingTalkService dingTalkService = new Retrofit.Builder()
            .baseUrl("https://oapi.dingtalk.com")
            .client(initOkHttpClient())
            .addConverterFactory(new StringConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .build()
            .create(DingTalkService.class);

    /**
     * 钉钉机器人标识
     */
    private static final String WEBHOOK_INDEX = "WEBHOOK_INDEX";

    /**
     * 发送钉钉消息
     * @param dingMessage
     */
    public static void sendMessage(DingMessage dingMessage) {
        try {
            String level = dingMessage.getLevel();
            String alarmDingTalk = StringUtils.isNotBlank(level) && level.equals(LogLevelEnum.SERIOUS.name())
                    ? ConfigUtils.getProperty(SysConstant.ALARM_SERIOUS_DINGTALK, "")
                    : ConfigUtils.getProperty(SysConstant.ALARM_DINGTALK, "");
            if (StringUtils.isBlank(alarmDingTalk)) {
                log.warn("log skyeye >>> config 'skyeye.log.alarm.dingtalk' or 'skyeye.log.alarm.serious.dingtalk' is null");
                return;
            }
            List<DingConfigData> dingConfigDataList = JSONUtil.toList(JSONUtil.parseArray(alarmDingTalk), DingConfigData.class);
            send(dingConfigDataList, dingMessage.toString());
        } catch (Exception e) {
            log.warn("log skyeye >>> DingTalkUtils.sendMessage occur exception", e);
        }
    }

    /**
     * 发送
     * @param dingConfigDataList
     * @param jsonContent
     */
    private static void send(List<DingConfigData> dingConfigDataList, String jsonContent) {
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
     * @param accessToken
     * @param secret
     * @param jsonContent
     */
    private static boolean send(String accessToken, String secret, String jsonContent) {
        try {
            Long timestamp = System.currentTimeMillis();
            String sign = StringUtils.isNotBlank(secret) ? signData(timestamp, secret) : "";
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonContent);
            log.info("log skyeye >>> send dingding accessToken: {}, timestamp: {}, sign: {}", accessToken, timestamp, sign);
            /*Call<ResponseBody> call = dingTalkService.robotSendCall(accessToken, timestamp, sign, requestBody);
            Response<ResponseBody> response = call.execute();
            ResponseBody responseBody = response.body();*/
            /*Mono<Response<String>> mono = dingTalkService.robotSendMonoStr(accessToken, timestamp, sign, requestBody);
            String string = mono.blockOptional().map(Response::body).orElse(null);*/
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
     * 发送
     * @param webHook
     * @param accessToken
     * @param secret
     * @param jsonContent
     * @return
     */
    /*private static boolean send(String webHook, String accessToken, String secret, String jsonContent) {
        try {
            String type = "application/json; charset=utf-8";
            RequestBody body = RequestBody.create(MediaType.parse(type), jsonContent);
            String apiUrl = webHook;
            if (null == apiUrl || "".equals(apiUrl)) {
                apiUrl = "https://oapi.dingtalk.com/robot/send?access_token=" + accessToken;
            }
            if (!StringUtils.isEmpty(secret)) {
                Long time = System.currentTimeMillis();
                apiUrl = apiUrl + "&timestamp=" + time + "&sign=" + signData(time, secret);
            }
            Request.Builder builder = (new Request.Builder()).url(apiUrl);
            builder.addHeader("Content-Type", type).post(body);
            log.info("log skyeye >>> apiUrl: {}", apiUrl);
            Request request = builder.build();
            Response response = client.newCall(request).execute();
            String string = response.body().string();
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
    }*/

    /**
     * 验签
     * @param timestamp
     * @param secret
     * @return
     */
    private static String signData(Long timestamp, String secret) {
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
     * 初始化OkHttpClient实例
     * @return
     */
    private static OkHttpClient initOkHttpClient() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .writeTimeout(500, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 获取第几个机器人
     * @param totalCount
     * @return
     */
    private static synchronized int getIndex(int totalCount) {
        int currentIndex = LocalCacheUtils.getIntCache(WEBHOOK_INDEX);
        if (currentIndex > totalCount - 1) {
            currentIndex = 0;
            LocalCacheUtils.putIntCache(WEBHOOK_INDEX, currentIndex);
        }
        LocalCacheUtils.incr(WEBHOOK_INDEX);
        return currentIndex;
    }

    public static void main(String[] args) {
        /*List<DingConfigData> dingConfigDataList = new ArrayList<>();
        DingConfigData dingConfigData1= new DingConfigData();
        dingConfigData1.setWebHook("https://oapi.dingtalk.com/robot/send?access_token=ec2ee44dedaa1207e1fa4541e97f7a4489f6bbad8e17a8e98d85a0fd97fc69aa");
        dingConfigData1.setSecret("SEC2e6249a1bf419db8a89643ebf2625dbc0c3e47af12ce130a6582415d3f38da05");
        DingConfigData dingConfigData2= new DingConfigData();
        dingConfigData2.setWebHook("https://oapi.dingtalk.com/robot/send?access_token=389678a82224d8a53bcb592ac00a406ce14654c045f88d2026e7ef32a49febd2");
        dingConfigData2.setSecret("SECc193b8c3b7e47ad0fd4821bf26f08dc777010ecef372689604922b1c78e8184c");
        dingConfigDataList.add(dingConfigData1);
        dingConfigDataList.add(dingConfigData2);*/

        DingMarkDownMessage message = new DingMarkDownMessage();
        message.setTitle("出错啦！");
        String exceptionMessage;
        String exceptionStackTrace;
        try {
            throw new RuntimeException("异常message");
        } catch (Exception e) {
            exceptionMessage = e.getMessage();
            exceptionStackTrace = ExceptionUtils.getStackTrace(e);
        }
        String content = "## **" + MDCConstantEnum.ERROR_MESSAGE.getName() + "：" + "出错啦！" + "**" + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.SERVER_NAME.getName() + "：" + "localhost" + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.SOURCE_IP.getName() + "：" + "10.155.8.91" + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.SERVER_IP.getName() + "：" + "10.155.8.91" + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.SERVER_HOSTNAME.getName() + "：" + "itkevin.it.com" + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.OCCURRENCE_TIME.getName() + "：" + "2020-06-23 14:20:05" + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.REQUEST_TYPE.getName() + "：" + "http GET" + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.TRACE_ID.getName() + "：" + "4696.167.15928932046440001" + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.REQUEST_URI.getName() + "：" + "/api/health" + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.REQUEST_PARAM.getName() + "：" + "{a=a, b=b}" + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.EXCEPTION_MESSAGE.getName() + "：" + exceptionMessage + System.getProperty("line.separator") +
                "+ " + MDCConstantEnum.EXCEPTION_STACKTRACE.getName() + "：" + System.getProperty("line.separator") + System.getProperty("line.separator") +
                "`" + exceptionStackTrace + "`";
        message.setContent(content);
        DingTalkUtils.sendMessage(message);
    }
}
