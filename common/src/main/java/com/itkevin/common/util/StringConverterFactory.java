package com.itkevin.common.util;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * retrofit2 String转换器
 */
public class StringConverterFactory extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return (Converter<ResponseBody, String>) ResponseBody::string;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return (Converter<String, RequestBody>) value -> RequestBody.create(MediaType.parse("text/plain"), value);
    }

}
