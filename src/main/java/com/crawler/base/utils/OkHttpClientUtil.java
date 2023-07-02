package com.crawler.base.utils;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class OkHttpClientUtil {
    private static final int READ_TIMEOUT = 100;
    private static final int CONNECT_TIMEOUT = 60;
    private static final int WRITE_TIMEOUT = 60;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final byte[] LOCKER = new byte[0];
    private static OkHttpClientUtil mInstance;
    private OkHttpClient okHttpClient;
    private OkHttpClient.Builder okHttpClientBuilder;
    private static final ConnectionPool CONNECTION_POOL = new ConnectionPool(256, 5L, TimeUnit.MINUTES);

    private OkHttpClientUtil() {
//        okHttpClientBuilder = new okhttp3.OkHttpClient.Builder().connectionPool(CONNECTION_POOL);
        okHttpClientBuilder = new okhttp3.OkHttpClient.Builder().connectionPool(CONNECTION_POOL);
        // 读取超时
        okHttpClientBuilder.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
        // 连接超时
        okHttpClientBuilder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        //写入超时
        okHttpClientBuilder.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        okHttpClient = okHttpClientBuilder.build();
    }

    public OkHttpClientUtil(OkHttpClient.Builder okHttpClientBuilder) {
        this.okHttpClientBuilder = okHttpClientBuilder;
        okHttpClientBuilder.connectionPool(CONNECTION_POOL);
        this.okHttpClient = okHttpClientBuilder.build();
    }
    /**
     * 单例模式获取 NetUtils
     *
     * @return {@link OkHttpClientUtil}
     */
    public static OkHttpClientUtil getInstance() {
        if (mInstance == null) {
            synchronized (LOCKER) {
                if (mInstance == null) {
                    mInstance = new OkHttpClientUtil();
                }
            }
        }
        return mInstance;
    }
    public static OkHttpClient.Builder getBuilder(){
        return new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {

                    //自定义
                    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<HttpUrl, List<Cookie>>();

                    //复写
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url,cookies);
                    }

                    //复写
                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url);
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                });
    }
    /**
     * GET，同步方式，获取网络数据
     *
     * @param url 请求地址
     * @return {@link Response}
     */
    public Response getData(String url) {
        // 构造 Request
        Request request = getRequest(url);
        // 将 Request 封装为 Call
        Call call = getClient().newCall(request);
        // 执行 Call，得到 Response
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @NotNull
    public static Request getRequest(String url) {
        Request request = new Request.Builder().get().url(url).build();
        return request;
    }

    /**
     * POST 请求，同步方式，提交数据
     *
     * @param url        请求地址
     * @param bodyParams 请求参数
     * @return {@link Response}
     */
    public Response postData(String url, Map<String, String> bodyParams) {
        // 构造 RequestBody
        RequestBody body = setRequestBody(bodyParams);
        // 构造 Request
        Request.Builder requestBuilder = new Request.Builder();
        Request request = requestBuilder.post(body).url(url).build();
        // 将 Request 封装为 Call
        Call call = getClient().newCall(request);
        // 执行 Call，得到 Response
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * GET 请求，异步方式，获取网络数据
     *
     * @param url       请求地址
     * @param myNetCall 回调函数
     */
    public void getDataAsync(String url, final MyNetCall myNetCall) {
        // 构造 Request
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(url).build();
        // 将 Request 封装为 Call
        Call call = getClient().newCall(request);
        // 执行 Call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                myNetCall.failed(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                myNetCall.success(call, response);
            }
        });
    }

    /**
     * POST 请求，异步方式，提交数据
     *
     * @param url        请求地址
     * @param bodyParams 请求参数
     * @param myNetCall  回调函数
     */
    public void postDataAsync(String url, Map<String, String> bodyParams, final MyNetCall myNetCall) {
        // 构造 RequestBody
        RequestBody body = setRequestBody(bodyParams);
        // 构造 Request
        buildRequest(url, myNetCall, body);
    }

    /**
     * 同步 POST 请求，使用 JSON 格式作为参数
     *
     * @param url  请求地址
     * @param json JSON 格式参数
     * @return 响应结果
     * @throws IOException 异常
     */
    public String postJson(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = getClient().newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    /**
     * 异步 POST 请求，使用 JSON 格式作为参数
     *
     * @param url       请求地址
     * @param json      JSON 格式参数
     * @param myNetCall 回调函数
     * @throws IOException 异常
     */
    public void postJsonAsync(String url, String json, final MyNetCall myNetCall) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        // 构造 Request
        buildRequest(url, myNetCall, body);
    }

    /**
     * 构造 POST 请求参数
     *
     * @param bodyParams 请求参数
     * @return {@link RequestBody}
     */
    private RequestBody setRequestBody(Map<String, String> bodyParams) {
        RequestBody body = null;
        okhttp3.FormBody.Builder formEncodingBuilder = new okhttp3.FormBody.Builder();
        if (bodyParams != null) {
            Iterator<String> iterator = bodyParams.keySet().iterator();
            String key = "";
            while (iterator.hasNext()) {
                key = iterator.next().toString();
                formEncodingBuilder.add(key, bodyParams.get(key));
            }
        }
        body = formEncodingBuilder.build();
        return body;
    }

    /**
     * 构造 Request 发起异步请求
     *
     * @param url       请求地址
     * @param myNetCall 回调函数
     * @param body      {@link RequestBody}
     */
    private void buildRequest(String url, MyNetCall myNetCall, RequestBody body) {
        Request.Builder requestBuilder = new Request.Builder();
        Request request = requestBuilder.post(body).url(url).build();
        // 将 Request 封装为 Call
        Call call = getClient().newCall(request);
        // 执行 Call
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                myNetCall.failed(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                myNetCall.success(call, response);
            }
        });
    }

    /**
     * 自定义网络回调接口
     */
    public interface MyNetCall {
        /**
         * 请求成功的回调处理
         *
         * @param call     {@link Call}
         * @param response {@link Response}
         * @throws IOException 异常
         */
        void success(Call call, Response response) throws IOException;

        /**
         * 请求失败的回调处理
         *
         * @param call {@link Call}
         * @param e    异常
         */
        void failed(Call call, IOException e);
    }

//    public static void main(String[] args) throws IOException {
//        Response response = OkHttpClientUtil.getInstance().getData("https://www.baidu.com");
//        ResponseBody responseBody = response.body();
//        byte[] dataBytes;
//        if (responseBody != null && (dataBytes = responseBody.bytes()) != null ) {
//            String s = new String(dataBytes, 0, dataBytes.length);
//            System.out.println(s);
//        }
//    }
    public OkHttpClient getClient(){
        return okHttpClient;
//        return okHttpClientBuilder.connectionPool(CONNECTION_POOL).build();
    }

    /**
     * 获得不安全的okhttpClient，用来请求https接口
     * @return
     */
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
    public static void main(String[] args) throws IOException {
        Date beginTime = new Date();
        for(int i=0;i<100;i++){
            Response call = OkHttpClientUtil.getInstance().getData("https://www.baidu.com");
//            Response call = OkHttpClientUtil.getInstance().getData("https://www.baidu.com");
            System.out.println(call.body());
        }
        System.out.println(new Date().getTime() - beginTime.getTime());
        System.out.println("end");
    }
}
