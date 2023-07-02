package com.crawler.test;

import com.crawler.base.utils.JsonUtil;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) {

        try {
            String url = "18.210.58.21";
            OkHttpClient client = getUnsafeOkHttpClient();
            String accessToken = login(client, url);
            String s = startNode(client, url, accessToken);
            String status = getStatus(client, url, accessToken);
            System.out.println(status);
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
    static String login(OkHttpClient client, String url) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"password\":\"e96617c582d231ffc093f9f711fe070f162d552ae5ce2649f8d2eac7bd659491\"}");
        Request request = new Request.Builder()
                .url("https://"+url+":8080/auth/login")
                .method("POST", body)
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "*/*")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("host", url)
                .build();
        Response response = client.newCall(request).execute();
        Map map = JsonUtil.string2Obj(response.body().string());
        return (String) map.get("accessToken");
    }
    static String getStatus(OkHttpClient client, String url, String accessToken) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://"+url+":8080/api/node/status")
                .get()
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("Content-Type", "application/json")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                .addHeader("X-Api-Token", accessToken)
                .addHeader("Accept", "*/*")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("host", url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    static String stopNode(OkHttpClient client, String url, String accessToken) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://"+url+":8080/api/node/stop")
                .method("POST", body)
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("Content-Type", "application/json")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                .addHeader("X-Api-Token", accessToken)
                .addHeader("Accept", "*/*")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("host", url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    static String startNode(OkHttpClient client, String url, String accessToken) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://"+url+":8080/api/node/start")
                .method("POST", body)
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("Content-Type", "application/json")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                .addHeader("X-Api-Token", accessToken)
                .addHeader("Accept", "*/*")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("host", url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            // 过时方法，单构造参数方法过时，多传一个X509TrustManager就可以了
            //builder.sslSocketFactory(sslSocketFactory);
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            builder.connectTimeout(30, TimeUnit.SECONDS);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
