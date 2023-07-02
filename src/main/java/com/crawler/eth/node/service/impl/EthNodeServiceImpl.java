package com.crawler.eth.node.service.impl;

import com.crawler.base.utils.JsonUtil;
import com.crawler.eth.node.dao.EthNodeDao;
import com.crawler.eth.node.model.EthNodeModel;
import com.crawler.eth.node.service.IEthNodeService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EthNodeServiceImpl implements IEthNodeService {
    @Resource
    EthNodeDao ethNodeDao;
    /**
     * 获得节点信息
     * @param nodeType
     * @return
     */
    @Override
    public List<EthNodeModel> listNodeByNodeType(String nodeType) {
        List<EthNodeModel> nodeList = ethNodeDao.findByNodeType(nodeType);
        return nodeList;
    }
    /**
     * 登陆shardeum
     * @param client
     * @param node
     * @return
     */
    @Override
    public String loginShardeum(OkHttpClient client, EthNodeModel node) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return login(client, node.getUrl());
    }

    /**
     * 停止shardeum节点
     * @param client
     * @param node
     * @return
     */
    @Override
    public Map<String, Object> getShardeumStatus(OkHttpClient client, EthNodeModel node, String accessToken) throws IOException {
        String status = getStatus(client, node.getUrl(), accessToken);
        log.info(status);
        Map<String, Object> map = JsonUtil.string2Obj(status);
        String state = (String) map.get("state");
        if(!state.equals(node.getState())){
            node.setState(state);
            if("stopped".equals(state)){
                node.setLastStopTime(new Date());
            }
        }
        node.setLastUpdateTime(new Date());
        ethNodeDao.save(node);
        return map;
    }
    /**
     * 停止shardeum节点
     * @param client
     * @param node
     * @return
     */
    @Override
    public String stopShardeumNode(OkHttpClient client, EthNodeModel node, String accessToken) throws IOException {
        String result = stopNode(client, node.getUrl(), accessToken);
        return result;
    }
    /**
     * 开启shardeum节点
     * @param client
     * @param node
     * @return
     */
    @Override
    public String startShardeumNode(OkHttpClient client, EthNodeModel node, String accessToken) throws IOException {
        String result = startNode(client, node.getUrl(), accessToken);
        return result;
    }
    String login(OkHttpClient client, String url) throws IOException, NoSuchAlgorithmException, KeyManagementException {

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
}
