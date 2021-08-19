package com.itkevin.common.util;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import reactor.core.publisher.Mono;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 钉钉服务接口
 */
public interface DingTalkService {

    /**
     * 发送钉钉消息
     * @param accessToken
     * @param timestamp
     * @param sign
     * @param body
     * @return
     */
    @Headers({"Content-Type: application/json; charset=utf-8"})
    @POST("/robot/send")
    Call<ResponseBody> robotSendCall(@Query("access_token") String accessToken, @Query("timestamp") Long timestamp, @Query("sign") String sign, @Body RequestBody body);

    /**
     * 发送钉钉消息
     * @param accessToken
     * @param timestamp
     * @param sign
     * @param body
     * @return
     */
    @Headers({"Content-Type: application/json; charset=utf-8"})
    @POST("/robot/send")
    Mono<Response<String>> robotSendMonoStr(@Query("access_token") String accessToken, @Query("timestamp") Long timestamp, @Query("sign") String sign, @Body RequestBody body);

    /**
     * 发送钉钉消息
     * @param accessToken
     * @param timestamp
     * @param sign
     * @param body
     * @return
     */
    @Headers({"Content-Type: application/json; charset=utf-8"})
    @POST("/robot/send")
    Mono<Response<ResponseBody>> robotSendMono(@Query("access_token") String accessToken, @Query("timestamp") Long timestamp, @Query("sign") String sign, @Body RequestBody body);

}
