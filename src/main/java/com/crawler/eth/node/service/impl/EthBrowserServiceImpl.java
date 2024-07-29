package com.crawler.eth.node.service.impl;

import com.crawler.base.utils.JsonUtil;
import com.crawler.base.utils.OkHttpClientUtil;
import com.crawler.eth.node.dao.EthBrowserDao;
import com.crawler.eth.node.model.EthBrowserModel;
import com.crawler.eth.node.model.EthNodeDetailModel;
import com.crawler.eth.node.model.EthNodeModel;
import com.crawler.eth.node.service.IEthBrowserService;
import com.crawler.eth.node.service.IEthNodeService;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.*;

@Service
@Slf4j
public class EthBrowserServiceImpl implements IEthBrowserService {
    @Resource
    EthBrowserDao ethBrowserDao;
    @Resource
    IEthNodeService ethNodeService;
    @Value("${http.proxy:}")
    String proxy;

    /**
     * 刷新ionet的token
     * @param id
     * @throws IOException
     */
    @Override
    @Deprecated
    public void ionetRefreshToken(Long id) throws IOException {
        EthBrowserModel browser = ethBrowserDao.getById(id);
        Map<String, Object> session = JsonUtil.string2Obj(browser.getSession());
        String refresh_token = (String) session.get("refresh_token");
        Map<String, Object> o = JsonUtil.string2Obj(browser.getCookies());
        OkHttpClient client = OkHttpClientUtil.getUnsafeOkHttpClient(proxy).cookieJar(OkHttpClientUtil.getCookieJar("id.io.net", o)).build();
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, "{\"refresh_token\":\""+refresh_token+"\"}");
        Request request = new Request.Builder()
                .url("https://id.io.net/auth/v1/token?grant_type=refresh_token")
                .method("POST", body)
                .addHeader("accept", "*/*")
                .addHeader("accept-language", "zh-CN,zh;q=0.9")
                .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im14dGZka3BweHlmbG1tZ2x1bGxmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDgwNDI1ODEsImV4cCI6MjAyMzYxODU4MX0.mNkDiJaCBB5twRNypzThEKl-s8d5VjasNyJj1l9BK9o")
                .addHeader("authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im14dGZka3BweHlmbG1tZ2x1bGxmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDgwNDI1ODEsImV4cCI6MjAyMzYxODU4MX0.mNkDiJaCBB5twRNypzThEKl-s8d5VjasNyJj1l9BK9o")
                .addHeader("content-type", "application/json;charset=UTF-8")
                .addHeader("origin", "https://cloud.io.net")
                .addHeader("priority", "u=1, i")
                .addHeader("referer", "https://cloud.io.net/")
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"125\", \"Chromium\";v=\"125\", \"Not.A/Brand\";v=\"24\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-site")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .addHeader("x-client-info", "supabase-js-web/2.39.2")
                .build();
        Response response = client.newCall(request).execute();
        List<Cookie> cookies = Cookie.parseAll(request.url(), response.headers());
        Map<String, Object> cookieMap = new HashMap<>();
        for (Cookie cookie : cookies) {
            cookieMap.put(cookie.name(), cookie.value());
        }
        browser.setCookies(JsonUtil.object2String(cookieMap));
        String result = response.body().string();
        log.info("请求结果:{}", result);
        if(!StringUtils.isEmpty(result)){
            browser.setSession(result);
            browser.setLastUpdateTime(new Date());
            ethBrowserDao.save(browser);
        }
    }

    /**
     * 获取ionet设备状态
     * @param node
     * @throws IOException
     */
    @Override
    public void getionetDeviceStatus(EthNodeDetailModel node) {
        EthBrowserModel browser = ethBrowserDao.getById(node.getBrowserId());
        Map<String, Object> session = null;
        try {
            session = JsonUtil.string2Obj(browser.getSession());
            String access_token = (String) session.get("access_token");
            OkHttpClient client = OkHttpClientUtil.getUnsafeOkHttpClient(proxy).build();
//        MediaType mediaType = MediaType.parse("text/plain");
//        RequestBody body = RequestBody.create(mediaType, "");
            String url = "https://api.io.solutions/v1/io-worker/devices/" + node.getDeviceId() + "/details";
            log.info("url:{}, token:{}", url, access_token);
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("authority", "api.io.solutions")
                    .addHeader("accept", "application/json, text/plain, */*")
                    .addHeader("accept-language", "zh-CN,zh;q=0.9")
                    .addHeader("origin", "https://worker.io.net")
                    .addHeader("referer", "https://worker.io.net/")
                    .addHeader("sec-ch-ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .addHeader("sec-fetch-dest", "empty")
                    .addHeader("sec-fetch-mode", "cors")
                    .addHeader("sec-fetch-site", "cross-site")
                    .addHeader("token", access_token)
                    .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .build();
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            //{
            //    "status": "succeeded",
            //    "data": {
            //        "device_id": "e7626461-3260-4c1f-a2e2-f9189eed5d0e",
            //        "hardware_quantity": 1,
            //        "heartbeat_id": "e7626461-3260-4c1f-a2e2-f9189eed5d0e",
            //        "start_date": "2024-04-02 02:56:55",
            //        "status": "up",
            //        "device_type": "gpu",
            //        "operating_system": "Linux",
            //        "security_soc2": false,
            //        "location_name": "United States",
            //        "location_icon": null,
            //        "iso2": "US",
            //        "brand_icon": "https://mxtfdkppxyflmmglullf.supabase.co/storage/v1/object/public/icons/nvidia.svg?t=2023-09-05T21%3A52%3A29.736Z",
            //        "brand_name": "NVIDIA",
            //        "hardware_name": "GeForce RTX 4060",
            //        "download_speed_mbps": 617.0,
            //        "upload_speed_mbps": 71.0,
            //        "base_tier_name": "High Speed",
            //        "jobs": [],
            //        "down_percentage": 112,
            //        "downtime_by_date": {
            //            "2024-04-03": {
            //                "downtime": 3663.8226130000003,
            //                "note": "down for 1 hours and 1 minutes"
            //            },
            //            "2024-04-05": {
            //                "downtime": 3786.9636649999998,
            //                "note": "down for 1 hours and 3 minutes"
            //            },
            //            "2024-04-06": {
            //                "downtime": 20034.104358,
            //                "note": "down for 5 hours and 33 minutes"
            //            },
            //            "2024-04-08": {
            //                "downtime": 7517.7227060000005,
            //                "note": "down for 2 hours and 5 minutes"
            //            },
            //            "2024-04-09": {
            //                "downtime": 1167.1871910000002,
            //                "note": "down for 0 hours and 19 minutes"
            //            },
            //            "2024-04-10": {
            //                "downtime": 54.39450000000001,
            //                "note": "down for 0 hours and 0 minutes"
            //            },
            //            "2024-04-12": {
            //                "downtime": 89337.98249,
            //                "note": "down for 24 hours and 48 minutes"
            //            },
            //            "2024-04-13": {
            //                "downtime": 33842.795513,
            //                "note": "down for 9 hours and 24 minutes"
            //            }
            //        },
            //        "supplier_name": "io.net",
            //        "supplier_icon": "https://mxtfdkppxyflmmglullf.supabase.co/storage/v1/object/public/icons/io-net-logo-symbol-only-final.svg",
            //        "is_active": true,
            //        "busy_percent": 0.0
            //    }
            //}
            log.info("请求结果:{}", result);
            Map<String, Object> r = JsonUtil.string2Obj(result);
            if(!r.containsKey("data")){
                throw new Exception("格式返回错误");
            }
            Map<String, Object> data = (Map<String, Object>) r.get("data");
            node.setData(JsonUtil.object2String(data));
            String status = (String) data.get("status");
            node.setState(status);
            if(!"up".equals(status)){
                node.setLastStopTime(new Date());
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            node.setState("error");
            node.setError(e.getMessage());
        }
        ethNodeService.update(node);
    }

    public static void main(String[] args) throws IOException, UnirestException {
        // 设置代理地址
        String proxy = "127.0.0.1:7890";
        Map<String, Object> cookieMap = new HashMap<>();
        cookieMap.put("sb-access-token","eyJhbGciOiJIUzI1NiIsImtpZCI6IlNFUGRXMkpHWXlzc0ZqU3ciLCJ0eXAiOiJKV1QifQ.eyJhdWQiOiJhdXRoZW50aWNhdGVkIiwiZXhwIjoxNzEzMTA5NzI1LCJpYXQiOjE3MTMxMDYxMjUsImlzcyI6Imh0dHBzOi8vbXh0ZmRrcHB4eWZsbW1nbHVsbGYuc3VwYWJhc2UuY28vYXV0aC92MSIsInN1YiI6IjM0OTcyNTU3LWRlNjEtNDkwMi05Nzg3LTQ1MWQ0MTNiZWRjZCIsImVtYWlsIjoianptNjEzMjA2MjM1QGdtYWlsLmNvbSIsInBob25lIjoiIiwiYXBwX21ldGFkYXRhIjp7InByb3ZpZGVyIjoiZ29vZ2xlIiwicHJvdmlkZXJzIjpbImdvb2dsZSJdfSwidXNlcl9tZXRhZGF0YSI6eyJhdmF0YXJfdXJsIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jS3JKSVRweTM5cHM3ZWRGSU1fZC1HMjJzdF9CWmlaajlDN3p4TjlzQlpXVU9hNW13PXM5Ni1jIiwiZW1haWwiOiJqem02MTMyMDYyMzVAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImZ1bGxfbmFtZSI6IkogbW1tIiwiaXNzIjoiaHR0cHM6Ly9hY2NvdW50cy5nb29nbGUuY29tIiwibmFtZSI6IkogbW1tIiwicGhvbmVfdmVyaWZpZWQiOmZhbHNlLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jS3JKSVRweTM5cHM3ZWRGSU1fZC1HMjJzdF9CWmlaajlDN3p4TjlzQlpXVU9hNW13PXM5Ni1jIiwicHJvdmlkZXJfaWQiOiIxMDI2NDE1MjEyMzA0NzIzNTY2MDgiLCJzdWIiOiIxMDI2NDE1MjEyMzA0NzIzNTY2MDgifSwicm9sZSI6ImF1dGhlbnRpY2F0ZWQiLCJhYWwiOiJhYWwxIiwiYW1yIjpbeyJtZXRob2QiOiJvYXV0aCIsInRpbWVzdGFtcCI6MTcxMzEwNjAxMX1dLCJzZXNzaW9uX2lkIjoiNzNlMjRiZDctNDViYS00MTU0LWI3OWEtZDE0ODNkMjU4YTI3IiwiaXNfYW5vbnltb3VzIjpmYWxzZX0.K8o7_mN8ArWAxYeAh6WWflk_CK47FUhm-vKwFeJ_GF8");
        cookieMap.put("sb-refresh-token", "B85Hxy3i34L2dcU7bpQuOQ");
        OkHttpClient client = OkHttpClientUtil.getUnsafeOkHttpClient(proxy).cookieJar(OkHttpClientUtil.getCookieJar("id.io.net", cookieMap)).build();

        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://api.io.solutions/v1/io-worker/devices/e7626461-3260-4c1f-a2e2-f9189eed5d0e/details")
                .get()
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("accept-language", "zh-CN,zh;q=0.9")
                .addHeader("origin", "https://cloud.io.net")
                .addHeader("priority", "u=1, i")
                .addHeader("referer", "https://cloud.io.net/")
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"125\", \"Chromium\";v=\"125\", \"Not.A/Brand\";v=\"24\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "cross-site")
                .addHeader("token", "eyJhbGciOiJIUzI1NiIsImtpZCI6IlNFUGRXMkpHWXlzc0ZqU3ciLCJ0eXAiOiJKV1QifQ.eyJhdWQiOiJhdXRoZW50aWNhdGVkIiwiZXhwIjoxNzEzMTA5ODkyLCJpYXQiOjE3MTMxMDYyOTIsImlzcyI6Imh0dHBzOi8vbXh0ZmRrcHB4eWZsbW1nbHVsbGYuc3VwYWJhc2UuY28vYXV0aC92MSIsInN1YiI6IjM0OTcyNTU3LWRlNjEtNDkwMi05Nzg3LTQ1MWQ0MTNiZWRjZCIsImVtYWlsIjoianptNjEzMjA2MjM1QGdtYWlsLmNvbSIsInBob25lIjoiIiwiYXBwX21ldGFkYXRhIjp7InByb3ZpZGVyIjoiZ29vZ2xlIiwicHJvdmlkZXJzIjpbImdvb2dsZSJdfSwidXNlcl9tZXRhZGF0YSI6eyJhdmF0YXJfdXJsIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jS3JKSVRweTM5cHM3ZWRGSU1fZC1HMjJzdF9CWmlaajlDN3p4TjlzQlpXVU9hNW13PXM5Ni1jIiwiZW1haWwiOiJqem02MTMyMDYyMzVAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImZ1bGxfbmFtZSI6IkogbW1tIiwiaXNzIjoiaHR0cHM6Ly9hY2NvdW50cy5nb29nbGUuY29tIiwibmFtZSI6IkogbW1tIiwicGhvbmVfdmVyaWZpZWQiOmZhbHNlLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jS3JKSVRweTM5cHM3ZWRGSU1fZC1HMjJzdF9CWmlaajlDN3p4TjlzQlpXVU9hNW13PXM5Ni1jIiwicHJvdmlkZXJfaWQiOiIxMDI2NDE1MjEyMzA0NzIzNTY2MDgiLCJzdWIiOiIxMDI2NDE1MjEyMzA0NzIzNTY2MDgifSwicm9sZSI6ImF1dGhlbnRpY2F0ZWQiLCJhYWwiOiJhYWwxIiwiYW1yIjpbeyJtZXRob2QiOiJvYXV0aCIsInRpbWVzdGFtcCI6MTcxMzEwNjI5MX1dLCJzZXNzaW9uX2lkIjoiYTQyZDNmYTgtYzEwNS00MmViLThlNGItMjc1OWM0ZmJhNTkzIiwiaXNfYW5vbnltb3VzIjpmYWxzZX0.vjApEQbrfNc-PcZM10_p5eudx29tgP-zwRX3AatHwwU")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }
}
