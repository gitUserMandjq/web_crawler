package com.crawler.base.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

@Slf4j
public class PushUtils {
    public static final String accessKey = "AT_EvH89swF5QQLOFtoCdGdkEbCciEdzxay";
    public static final String uid = "UID_ldi3Bmuvx4a4kQoS6lPyDBGnbkeL";
    public static void pushMessageByWxPusher(String summary, String content) throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"appToken\":\""+accessKey+"\"" +
                ",\"content\":\"<p style=\\\"color:red;\\\">"+content+"</p>\"" +
                ",\"summary\":\""+summary+"\",\"contentType\":2" +
                ",\"uids\":[\""+uid+"\"]" +
                ",\"verifyPay\":false,\"verifyPayType\":0}");
        Request request = new Request.Builder()
                .url("https://wxpusher.zjiecode.com/api/send/message")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        log.info(response.body().string());
    }

    public static void main(String[] args) throws IOException {
        pushMessageByWxPusher("服务器:IONET-1失联", "服务器ip:xxx.xxx.xxx.xxx");
    }
}
