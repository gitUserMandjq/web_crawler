package com.crawler.eth.node.service.impl;

import com.crawler.base.utils.*;
import com.crawler.eth.node.dao.EthNodeDao;
import com.crawler.eth.node.dao.EthNodeDetailDao;
import com.crawler.eth.node.model.EthNodeDetailModel;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EthNodeServiceImpl implements IEthNodeService {
    @Resource
    EthNodeDao ethNodeDao;
    @Resource
    EthNodeDetailDao ethNodeDetailDao;
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
     * 获得节点信息
     * @param nodeType
     * @return
     */
    @Override
    public List<EthNodeDetailModel> listNodeDetailByNodeType(String nodeType) {
        List<EthNodeDetailModel> nodeList = ethNodeDetailDao.findByNodeType(nodeType);
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
        if(status.contains("shardeumVersion")){
            String version = status.split("\"shardeumVersion\":\"")[1].split("\",\"minVersion")[0];
            node.setVersion(version);
        }
        log.info(status);
        Map<String, Object> map = JsonUtil.string2Obj(status);
        String state = (String) map.get("state");
        if(!state.equals(node.getState())){
            node.setState(state);
            if("stopped".equals(state)){
                node.setLastStopTime(new Date());
            }
        }
        node.setData(status);
        update(node);
        return map;
    }
    @Override
    public void update(EthNodeModel node) {
        node.setLastUpdateTime(new Date());
        ethNodeDao.save(node);
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
     * 获得avail的sessionKey
     * @param node
     * @return
     */
    @Override
    public void upgradeShardeumNode(EthNodeModel node) throws IOException, JSchException {
        try {
            JSchUtil jSchUtil = connectToNode(node);
            String privateKey = node.getPrivateKey();
//            String command =
//                    "sudo su\n" +
//                            "cd /root\n" +
//                            "./.shardeum/shell.sh\n" +
//                            "operator-cli unstake\n" +
//                            privateKey + "";

            String command = "sudo su\n" +
                    "cd /root\n";
            String result = jSchUtil.execCommandByShell(command, new Function<JSchUtil.PrintProperty, String>() {
                @Override
                public String apply(JSchUtil.PrintProperty pp) {
                    switch (pp.stage){
                        case "0":
                            pp.printWriter.print("docker stop $(docker ps -a|grep -o -e '[^ ]*shardeum-dashboard')\n");
                            pp.printWriter.print("docker rm $(docker ps -a|grep -o -e '[^ ]*shardeum-dashboard')\n");
//                            pp.printWriter.print("./.shardeum/shell.sh\n");
                            pp.printWriter.flush();
                            pp.stage = "4";
                            break;
                        case "1":
                            if(pp.console.contains("Error: No such container: shardeum-dashboard")){
                                pp.printWriter.print("curl -O https://gitlab.com/shardeum/validator/dashboard/-/raw/main/installer.sh && chmod +x installer.sh && ./installer.sh\n");
                                pp.printWriter.flush();
                                pp.stage = "5";
                            }else if(pp.console.contains("Error response from daemon:")){
                                pp.printWriter.print("docker restart shardeum-dashboard\n");
                                pp.stage = "0";
                            }else if(pp.console.contains("UnhandledPromiseRejection: This error originated either by throwing inside of an async function without a catch block")){
                                pp.printWriter.print("exit\n");
                                pp.printWriter.flush();
                                pp.stage = "4";
                            }else if(pp.console.contains("~/app$")){
                                pp.printWriter.print("operator-cli unstake\n");
                                pp.printWriter.flush();
                            }else
                            if(pp.console.contains("Please enter your private key:")){
                                pp.printWriter.print(privateKey+"\n");
                                pp.printWriter.flush();
                                pp.stage = "2";
                            }
                            break;
                        case "2":
                            if(pp.console.contains("~/app$")){
                                pp.printWriter.print("exit\n");
                                pp.printWriter.flush();
                                pp.stage = "4";
                            }
                            break;
                        case "3":
                            if(pp.console.contains("~/app$")){//退出
                                pp.printWriter.print("exit\n");
                                pp.printWriter.flush();
                                pp.stage = "4";
                                return "";
                            }
                            break;
                        case "4":
                            if(pp.console.contains("~/app$")){//退出
                                pp.printWriter.print("exit\n");
                                pp.printWriter.flush();
                                return "";
                            }
                            if(pp.console.contains("]#") || pp.console.contains("~#")){
                                pp.printWriter.print("curl -O https://gitlab.com/shardeum/validator/dashboard/-/raw/main/installer.sh && chmod +x installer.sh && ./installer.sh\n");
                                pp.printWriter.flush();
                                pp.stage = "5";
                            }
                            break;
                        case "5":
                            if(pp.console.contains("By running this installer, you agree to allow the Shardeum team to collect this data. (Y/n)?")){
                                pp.printWriter.print("Y\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("What base directory should the node use (default ~/.shardeum):")){
                                pp.printWriter.print("\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Do you really want to upgrade now (y/N)")){
                                pp.printWriter.print("Y\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Do you want to run the web based Dashboard? (Y/n):")){
                                pp.printWriter.print("Y\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Set the password to access the Dashboard:")){
                                pp.printWriter.print("667889\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Do you want to change the password for the Dashboard? (y/N):")){
                                pp.printWriter.print("N\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Enter the port (1025-65536) to access the web based Dashboard (default 8080):")){
                                pp.printWriter.print("\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("enter an IPv4 address (default=auto)")){
                                pp.printWriter.print("\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("This allows p2p communication between nodes. Enter the first port (1025-65536) for p2p communication (default 9001):")){
                                pp.printWriter.print("\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Enter the second port (1025-65536) for p2p communication (default 10001):")){
                                pp.printWriter.print("\n");
                                pp.printWriter.flush();
                                pp.stage = "6";
                            }
                            break;
                        case "6":
                            if(pp.console.contains("]#") || pp.console.contains("~#")){
                                pp.printWriter.print("/root/.shardeum/shell.sh\n");
                                pp.printWriter.print("operator-cli gui start\n");
                                pp.printWriter.print("exit\n");
                                pp.endFlag = true;
                            }else if(pp.console.contains("Continue with the new image")){
                                pp.printWriter.print("y\n");
                                pp.printWriter.flush();
                            }
                            break;
                    }
                    return "";
                }
            });
            System.out.println("result:"+result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error:" + e.getMessage());
        }
    }

    @Override
    public JSchUtil connectToNode(EthNodeModel node) {
        int port = 22;
        log.info("连接节点:{},{},{}", node.getName(), node.getIndexNum(), node.getUrl());
        JSchUtil jSchUtil = null;
        jSchUtil = new JSchUtil(node.getUrl(), node.getAdmin(), node.getPassword(), port);
        return jSchUtil;
    }

    /**
     * 获得avail的sessionKey
     * @param node
     * @return
     */
    @Override
    public void restartShardeumNode(EthNodeModel node) throws IOException, JSchException {
        String privateKey = node.getPrivateKey();
        try {
            JSchUtil jSchUtil = connectToNode(node);
//            String command =
//                    "sudo su\n" +
//                            "cd /root\n" +
//                            "./.shardeum/shell.sh\n" +
//                            "operator-cli unstake\n" +
//                            privateKey + "";

            String command = "sudo su\n" +
                    "cd /root\n";
            String result = jSchUtil.execCommandByShell(command, new Function<JSchUtil.PrintProperty, String>() {
                @Override
                public String apply(JSchUtil.PrintProperty pp) {
                    switch (pp.stage){
                        case "0":
                            pp.printWriter.print("docker restart shardeum-dashboard\n");
                            pp.printWriter.print("exit\n");
                            pp.printWriter.flush();
                            break;
                    }
                    return "";
                }
            });
            System.out.println("result:"+result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error:" + e.getMessage());
        }
    }
    /**
     * 获得avail的sessionKey
     * @param node
     * @return
     */
    @Override
    public void restartIonetNode(EthNodeModel node) throws IOException, JSchException {
        String privateKey = node.getPrivateKey();
        try {
            JSchUtil jSchUtil = connectToNode(node);
//            String command =
//                    "sudo su\n" +
//                            "cd /root\n" +
//                            "./.shardeum/shell.sh\n" +
//                            "operator-cli unstake\n" +
//                            privateKey + "";

            String command = "sudo su\n" +
                    "cd /root\n";
            String result = jSchUtil.execCommandByShell(command, new Function<JSchUtil.PrintProperty, String>() {
                @Override
                public String apply(JSchUtil.PrintProperty pp) {
                    switch (pp.stage){
                        case "0":
                            pp.printWriter.print("curl -L https://github.com/ionet-official/io_launch_binaries/raw/main/launch_binary_linux -o launch_binary_linux\n");
                            pp.printWriter.print("chmod +x launch_binary_linux\n");
                            pp.printWriter.print(node.getRestartScript()+"\n");
                            pp.printWriter.print("exit\n");
                            pp.printWriter.flush();
                            break;
                    }
                    return "";
                }
            });
            System.out.println("result:"+result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error:" + e.getMessage());
        }
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
                update(node);
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
                        "cd /root/testnet-auto-install-v3/opside-chain && sudo bash ./control-panel.sh\n" +
                        "2\n" +
                        "1\n" +
                        "exit\n";
//        String command = "ls";
        jSchUtil.execCommandByShell(command);
        jSchUtil.close();
        node.setLastStartTime(new Date());
        update(node);
    }
    /**
     * 获得avail的sessionKey
     * @param node
     * @return
     */
    @Override
    public void getAvailSessionKey(EthNodeModel node) throws IOException, JSchException {
        if(node.getLastStartTime() != null
                && new Date().getTime() - node.getLastStartTime().getTime() < 60 * 60 * 1000L){
            //如果最近一个小时调用过，则不进行查询
            return;
        }
        String sessionKey = null;
        try {
            JSchUtil jSchUtil = connectToNode(node);
            String command =
                    "docker exec -it avail_validator_node /bin/bash\n" +
                            "curl -H \"Content-Type: application/json\" -d '{\"id\":1, \"jsonrpc\":\"2.0\", \"method\": \"author_rotateKeys\", \"params\":[]}' http://127.0.0.1:9944";

//        String command = "ls";
            sessionKey = jSchUtil.execCommandByShell(command, new Function<JSchUtil.PrintProperty, String>() {
                @Override
                public String apply(JSchUtil.PrintProperty pp) {
                    if(pp.console.contains("jsonrpc")){
                        pp.printWriter.println("exit");
                        pp.printWriter.flush();
                        String sessionKey = pp.console.split("result\":\"")[1].split("\",\"id\"")[0];
                        pp.endFlag = true;
                        return sessionKey;
                    }
                    return "";
                }
            });
            node.setCurrentSessionKey(sessionKey);
            if(!sessionKey.equals(node.getSessionKey())){
                node.setState("sessionKey不符");
            }else{
                node.setState("服务可用");
            }
            node.setLastStartTime(new Date());
            update(node);
        } catch (Exception e) {
            node.setState(e.getMessage());
            update(node);
        }
    }

    public static void main(String[] args) throws JSchException, IOException {
        OkHttpClient client = OkHttpClientUtil.getUnsafeOkHttpClient().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
//        Request request = new Request.Builder()
//                .url("https://52.23.236.161:8080/api/node/status")
//                .method("GET", null)
//                .addHeader("Accept", "*/*")
//                .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
//                .addHeader("Cache-Control", "no-cache")
//                .addHeader("Connection", "keep-alive")
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Cookie", "accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub2RlSWQiOiIiLCJpYXQiOjE3MDg2MDYzODMsImV4cCI6MTcwODYzNTE4M30.v-FLz-LzGMfVRi30dBzHe6-Skk1vpTPLIjW1M__0D4o")
//                .addHeader("Pragma", "no-cache")
//                .addHeader("Referer", "https://52.23.236.161:8080/")
//                .addHeader("Sec-Fetch-Dest", "empty")
//                .addHeader("Sec-Fetch-Mode", "cors")
//                .addHeader("Sec-Fetch-Site", "same-origin")
//                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
//                .addHeader("sec-ch-ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"")
//                .addHeader("sec-ch-ua-mobile", "?0")
//                .addHeader("sec-ch-ua-platform", "\"Windows\"")
//                .build();

        Request request = new Request.Builder()
                .url("https://52.23.236.161:8080/api/node/status")
                .get()
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("Content-Type", "application/json")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                .addHeader("Cookie", "accessToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub2RlSWQiOiIiLCJpYXQiOjE3MDg2MDYzODMsImV4cCI6MTcwODYzNTE4M30.v-FLz-LzGMfVRi30dBzHe6-Skk1vpTPLIjW1M__0D4o")
                .addHeader("Accept", "*/*")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("host", "52.23.236.161")
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
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
        String accessToken = (String) map.get("accessToken");
        if(accessToken == null){
            List<Cookie> cookies = Cookie.parseAll(request.url(), response.headers());
            for(Cookie c:cookies){
                if("accessToken".equals(c.name())){
                    accessToken = c.value();
                    break;
                }
            }
        }
        return accessToken;
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
                .addHeader("Cookie", "accessToken="+accessToken)
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
                .addHeader("Cookie", "accessToken="+accessToken)
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
                .addHeader("Cookie", "accessToken="+accessToken)
                .addHeader("Accept", "*/*")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("host", url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    @Override
    public void obtainQuiliBalance() throws InterruptedException {
        List<EthNodeDetailModel> detailList = ethNodeDetailDao.findByNodeType(EthNodeModel.NODETYPE_QUILIBRIUM);
        ThreadUtils.ChokeLimitThreadPool chokeLimitThreadPool = ThreadUtils.getInstance().chokeLimitThreadPool(detailList.size(), 5);
        for (EthNodeDetailModel ethNodeDetailModel : detailList) {
            chokeLimitThreadPool.run(new ThreadUtils.ChokeLimitThreadPool.RunThread() {
                @Override
                public void run() {
                    if(ethNodeDetailModel.getLastUpdateTime() != null
                            && new Date().getTime() - ethNodeDetailModel.getLastUpdateTime().getTime() < 30 * 60 * 1000){
                        return;
                    }
                    obtainQuiliBalance(ethNodeDetailModel);
                }
            });
        }
        chokeLimitThreadPool.choke();
    }
    @Override
    public void obtainQuiliBalance(EthNodeDetailModel ethNodeDetailModel) {
        EthNodeModel node = ethNodeDao.findById(ethNodeDetailModel.getNodeId()).get();
        JSchUtil jSchUtil = connectToNode(node);
        String command = "sudo su\n" +
                "cd /root && echo 6|./Quili.sh";
        try {
            String result = jSchUtil.execCommandByShell(command, new Function<JSchUtil.PrintProperty, String>() {
                @Override
                public String apply(JSchUtil.PrintProperty pp) {
                    switch (pp.stage){
                        case "0":
                            if(pp.console.contains("Unclaimed balance")){
                                pp.endFlag = true;
                                String balance = pp.console.split("Unclaimed balance: ")[1].split(" QUIL")[0];
                                String version = pp.console.split("Version: ")[1].split("\n")[0];
                                ethNodeDetailModel.setComment(balance);
                                ethNodeDetailModel.setVersion(version);
                                ethNodeDetailModel.setLastUpdateTime(new Date());
                                ethNodeDetailModel.setError("");
                                System.out.println(ethNodeDetailModel.getNodeName()+":"+balance);
                            }else if(pp.console.contains("error getting node info")){
                                pp.endFlag = true;
                                ethNodeDetailModel.setError("error");
                                ethNodeDetailModel.setErrorTime(new Date());
                            }
                            break;
                    }
                    return "";
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            ethNodeDetailModel.setError(e.getMessage());
            ethNodeDetailModel.setErrorTime(new Date());
        }
        ethNodeDetailDao.save(ethNodeDetailModel);
    }
    @Override
    public void obtainQuiliBalance(Long detailId) {
        EthNodeDetailModel detail = ethNodeDetailDao.findById(detailId).get();
        obtainQuiliBalance(detail);
    }
}
