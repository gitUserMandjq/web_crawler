package com.crawler.eth.node.service.impl;

import com.crawler.base.utils.JsonUtil;
import com.crawler.base.utils.OkHttpClientUtil;
import com.crawler.base.utils.PushUtils;
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
import java.text.SimpleDateFormat;
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
    public void getionetDeviceStatus(EthNodeDetailModel node) throws IOException {
        String oldState = node.getState();
        EthBrowserModel browser = ethBrowserDao.getById(node.getBrowserId());
        Map<String, Object> session = null;
        try {
            session = JsonUtil.string2Obj(browser.getSession());
            String access_token = (String) session.get("access_token");
            OkHttpClient client = OkHttpClientUtil.getUnsafeOkHttpClient(proxy).build();
//        MediaType mediaType = MediaType.parse("text/plain");
//        RequestBody body = RequestBody.create(mediaType, "");
            String url = "https://api.io.solutions/v1/io-worker/devices/" + node.getDeviceId() + "/summary";
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
            //        "device_id": "5acd1666-6cf7-4732-b98e-87af45e46da1",
            //        "device_name": "posdao-windows-1",
            //        "status": "up",
            //        "status_duration": "0 days 21:40:30",
            //        "is_working": false,
            //        "last_audit_successful": true,
            //        "last_challenge_successful": false,
            //        "total_download_traffic": 1764.0,
            //        "total_upload_traffic": 640.0,
            //        "total_compute_hours_served": 0,
            //        "total_jobs": 1,
            //        "total_earnings": 0.0,
            //        "total_earnings_io_coin": 0.0,
            //        "total_slashed_earnings": 0.0,
            //        "total_slahed_io_coin": 0.0,
            //        "download_speed_mbps": 96.0,
            //        "upload_speed_mbps": 61.0,
            //        "connectivity_tier": 1,
            //        "down_percentage": 14,
            //        "downtime_by_date": {
            //            "2024-07-14": {
            //                "downtime": 264.700717,
            //                "note": "down for 0 hours and 4 minutes"
            //            },
            //            "2024-07-18": {
            //                "downtime": 118.053549,
            //                "note": "down for 0 hours and 1 minutes"
            //            },
            //            "2024-07-19": {
            //                "downtime": 183.017841,
            //                "note": "down for 0 hours and 3 minutes"
            //            },
            //            "2024-07-20": {
            //                "downtime": 246.150837,
            //                "note": "down for 0 hours and 4 minutes"
            //            },
            //            "2024-07-21": {
            //                "downtime": 48899.985549,
            //                "note": "down for 13 hours and 34 minutes"
            //            },
            //            "2024-07-22": {
            //                "downtime": 5766.763967999999,
            //                "note": "down for 1 hours and 36 minutes"
            //            },
            //            "2024-07-24": {
            //                "downtime": 70619.993505,
            //                "note": "down for 19 hours and 36 minutes"
            //            },
            //            "2024-07-25": {
            //                "downtime": 86399.999999,
            //                "note": "down for 23 hours and 59 minutes"
            //            },
            //            "2024-07-26": {
            //                "downtime": 5523.751646,
            //                "note": "down for 1 hours and 32 minutes"
            //            },
            //            "2024-07-27": {
            //                "downtime": 9119.99375,
            //                "note": "down for 2 hours and 31 minutes"
            //            },
            //            "2024-07-28": {
            //                "downtime": 86399.999999,
            //                "note": "down for 23 hours and 59 minutes"
            //            },
            //            "2024-07-29": {
            //                "downtime": 38706.475751,
            //                "note": "down for 10 hours and 45 minutes"
            //            }
            //        }
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
            Boolean is_working = (Boolean) data.get("last_challenge_successful");
            if(!is_working){
                status += "(notWorking)";
            }
            node.setState(status);
            SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(!"up".equals(status) && !oldState.equals(status)){
                node.setError("节点不在线,状态：" + status + ",时间:"+yyyyMMddHHmmss.format(new Date()));
                node.setLastStopTime(new Date());
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            node.setState("error");
            node.setError(e.getMessage());
        }
        if(!"up".equals(node.getState()) && (!node.getState().equals(oldState)
                || node.getLastRemindTime() == null || new Date().getTime() - node.getLastRemindTime().getTime() >= 60*60*1000)){
            //节点状态不是工作，或者每小时提醒一次
            node.setLastRemindTime(new Date());
            PushUtils.pushMessageByWxPusher("节点:" + node.getNodeName(), node.getError());
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
