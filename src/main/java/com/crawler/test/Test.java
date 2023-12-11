package com.crawler.test;

import com.crawler.base.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.TimeUnit;
@Slf4j
public class Test {
    public static void main(String[] args) {

        try {
//            String url = "18.210.58.21";
//            OkHttpClient client = getUnsafeOkHttpClient();
////            String accessToken = login(client, url);
////            String s = startNode(client, url, accessToken);
////            String status = getStatus(client, url, accessToken);
////            System.out.println(status);
//            String index = "4639,6263,6492,6493,6494,6495,11162,11163";
//            List<Map> opsideStatus = getOpsideStatus(client, index);
            String s = "\"result\":\"0x46463d254f4ae4344d346edb14ac4eb96c90a0076a31743e3751958ef534813431987c7026d7c3b95905fb3f6a2b9468f3c946aadbfe88935d231e15a89f0a4e285c71c9f569acab730c9102bc9b762c1205ab3d75d01e22eb08589ff0315c49481b8e7de1c45da0bd76c91ed8495c7aaf23d1abd13cb0982fdcdce19c4ccf4c\",\"id\"";
            String sessionKey = s.split("result\":\"")[1].split("\",\"id\"")[0];
            System.out.println(sessionKey);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    static List<Map> getOpsideStatus(OkHttpClient client, String index) throws IOException {
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://pre-alpha-beacon.opside.info/dashboard/data/validators?validators="+index)
                .get()
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("Sec-Fetch-Mode", "navigate")
                .addHeader("Sec-Fetch-User", "?1")
                .addHeader("Sec-Fetch-Dest", "document")
                .addHeader("host", "pre-alpha-beacon.opside.info")
                .build();
        Response response = client.newCall(request).execute();
        String result = response.body().string();
        System.out.println(result);
        Map map = JsonUtil.string2Obj(result);
        List<List> data = (List<List>) map.get("data");
        List<Map> list = new ArrayList<>();
        for(List m:data){
            Map mm = new HashMap();
            mm.put("index", m.get(1));
            mm.put("status", m.get(3));
            list.add(mm);
            System.out.println(JsonUtil.object2String(mm));
        }
        return list;
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
