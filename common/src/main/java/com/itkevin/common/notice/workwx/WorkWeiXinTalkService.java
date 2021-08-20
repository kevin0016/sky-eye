package com.itkevin.common.notice.workwx;

import com.itkevin.common.notice.NoticeInterface;
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
 * 企业微信服务接口
 */
public interface WorkWeiXinTalkService {


    /**
     * 发送企业微信消息
     *
     * @param key
     * @param body
     * @return
     */
    @Headers({"Content-Type: application/json; charset=utf-8"})
    @POST("/cgi-bin/webhook/send")
    Call<ResponseBody> robotSendCall(@Query("key") String key, @Body RequestBody body);

    /**
     * 发送企业微信消息
     *
     * @param key
     * @param body
     * @return
     */
    @Headers({"Content-Type: application/json; charset=utf-8"})
    @POST("/cgi-bin/webhook/send")
    Mono<Response<String>> robotSendMonoStr(@Query("key") String key, @Body RequestBody body);

    /**
     * 发送企业微信消息
     *
     * @param key
     * @param body
     * @return
     */
    @Headers({"Content-Type: application/json; charset=utf-8"})
    @POST("/cgi-bin/webhook/send")
    Mono<Response<ResponseBody>> robotSendMono(@Query("key") String key, @Body RequestBody body);

}
