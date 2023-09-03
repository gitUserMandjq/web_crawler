package com.crawler.eth.node.service.impl;

import com.crawler.base.utils.JSchUtil;
import com.crawler.base.utils.JsonUtil;
import com.crawler.base.utils.StringUtils;
import com.crawler.eth.node.dao.EthNodeDao;
import com.crawler.eth.node.model.EthNodeModel;
import com.crawler.eth.node.service.IEthNodeService;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

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
        String status = getShardeumStatus(client, node.getUrl(), accessToken);
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
    /**
     * 获取opside状态
     * @param client
     * @param nodeList
     * @return
     * @throws Exception
     */
    @Override
    public void getOpsideStatus(OkHttpClient client, List<EthNodeModel> nodeList) throws Exception {
        String indexs = nodeList.stream().filter(node -> !StringUtils.isEmpty(node.getIndexNum())).map(EthNodeModel::getIndexNum).collect(Collectors.joining(","));
        Map<String, Object> indexMap = getOpsideStatusMap(client, indexs);
        for(EthNodeModel node:nodeList){
            if(!StringUtils.isEmpty(node.getIndexNum())){
                String status = (String) indexMap.get(node.getIndexNum());
                if(!status.equals(node.getState())){
                    node.setState(status);
                    if("active_offline".equals(status)){
                        node.setLastStopTime(new Date());
                    }
                }
                node.setLastUpdateTime(new Date());
                ethNodeDao.save(node);
            }
        }
    }
    /**
     * 开启opside节点
     * @param node
     * @return
     */
    @Override
    public void startOpsideNode(EthNodeModel node) throws IOException, JSchException {
        if(node.getLastStartTime() != null
                && new Date().getTime() - node.getLastStartTime().getTime() < 60 * 60 * 1000L){
            //如果最近一个小时重启过，那么不重启opside，因为opside的状态更新很慢
            return;
        }
        String userName = "ubuntu";
        String password = "opside123";
        log.info("连接节点:{},{},{}",node.getName(), node.getIndexNum(),node.getUrl());
        JSchUtil jSchUtil = JSchUtil.getInstance(node.getUrl(), userName, password);
        String command =
                "sudo su\n" +
//                "sudo chmod +x /root\n" +
                        "cd /root/testnet-auto-install-v2/opside-chain && sudo bash ./control-panel.sh\n" +
                        "2\n" +
                        "1\n" +
                        "exit\n";
//        String command = "ls";
        jSchUtil.execCommandByShell(command);
        jSchUtil.close();
        node.setLastStartTime(new Date());
        ethNodeDao.save(node);
    }

    Map<String, Object> getOpsideStatusMap(OkHttpClient client, String index) throws IOException {
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
        Map<String, Object> mm = new HashMap<>();
        for(List m:data){
            mm.put((String) m.get(1), m.get(3));
        }
        return mm;
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
    static String getShardeumStatus(OkHttpClient client, String url, String accessToken) throws IOException {
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
