package com.crawler.eth.node.action;

import com.crawler.account.model.CrawlerClientInfo;
import com.crawler.account.pool.CrawlerClientPool;
import com.crawler.base.common.model.WebApiBaseResult;
import com.crawler.base.utils.*;
import com.crawler.eth.node.model.EthNodeBackupModel;
import com.crawler.eth.node.model.EthNodeDetailModel;
import com.crawler.eth.node.model.EthNodeDetailTaskModel;
import com.crawler.eth.node.model.EthNodeModel;
import com.crawler.eth.node.service.IEthBrowserService;
import com.crawler.eth.node.service.IEthNodeDetailTaskService;
import com.crawler.eth.node.service.IEthNodeService;
import com.crawler.jd.service.IJdService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/eth/node")
@Slf4j
public class EthNodeController {
    @Resource
    IEthNodeService ethNodeService;
    @Resource
    IEthBrowserService ethBrowserService;
    @Resource
    IEthNodeDetailTaskService ethNodeDetailTaskService;
    @GetMapping("/index")
    public String index(){
        return "eth/nodehome"; //当浏览器输入/index时，会返回 /static/home.html的页面
    }

    /**
     * 获取shardeum节点列表
     * @param httpSession
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getShardeumNodeList")
    @ResponseBody
    public WebApiBaseResult getShardeumNodeList(HttpSession httpSession, HttpServletRequest request) throws Exception {
        List<EthNodeModel> ethNodeModels = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_SHARDEUM);
        return WebApiBaseResult.success(ethNodeModels);
    }
    /**
     * 获取shardeum节点列表
     * @param httpSession
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getNodeDetailList")
    @ResponseBody
    public WebApiBaseResult getNodeDetailList(HttpSession httpSession, HttpServletRequest request
            , @RequestParam(value="nodeType") String nodeType
            , @RequestParam(value="enabled", defaultValue = "1") Integer enabled) throws Exception {
        List<EthNodeDetailModel> ethNodeModels = ethNodeService.listNodeDetailByNodeType(nodeType, enabled);
        return WebApiBaseResult.success(ethNodeModels);
    }
    @RequestMapping("/getQuiliDetailList")
    @ResponseBody
    public WebApiBaseResult getQuiliDetailList(HttpSession httpSession, HttpServletRequest request
            , @RequestParam(value="nodeType") String nodeType
            , @RequestParam(value="enabled", defaultValue = "1") Integer enabled) throws Exception {
        List<EthNodeDetailModel> ethNodeModels = ethNodeService.listNodeDetailByNodeType(EthNodeModel.NODETYPE_QUILIBRIUM, enabled);
        List<Long> nodeIds = new ArrayList<>();
        List<Long> detailIds = new ArrayList<>();
        for (EthNodeDetailModel ethNodeModel : ethNodeModels) {
            nodeIds.add(ethNodeModel.getNodeId());
            detailIds.add(ethNodeModel.getId());
        }
        List<EthNodeModel> nodeList = ethNodeService.listNodeById(nodeIds);
        Map<Long, EthNodeModel> nodeMap = nodeList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));
        List<EthNodeDetailTaskModel> taskList = ethNodeDetailTaskService.listTaskByDetailId(detailIds);
        Map<Long, EthNodeDetailTaskModel> taskMap = taskList.stream().collect(Collectors.toMap(v -> v.getNodeDetailId(), v -> v));
        for (EthNodeDetailModel ethNodeModel : ethNodeModels) {
            EthNodeModel node = nodeMap.get(ethNodeModel.getNodeId());
            ethNodeModel.setNode(node);
            EthNodeDetailTaskModel task = taskMap.get(ethNodeModel.getId());
            ethNodeModel.setTask(task);
        }
        return WebApiBaseResult.success(ethNodeModels);
    }
    /**
     * 获取opside节点列表
     * @param httpSession
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getOpsideNodeList")
    @ResponseBody
    public WebApiBaseResult getOpsideList(HttpSession httpSession, HttpServletRequest request) throws Exception {
        List<EthNodeModel> ethNodeModels = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_OPSIDE);
        return WebApiBaseResult.success(ethNodeModels);
    }
    /**
     * 获取opside节点列表
     * @param httpSession
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getionetNodeList")
    @ResponseBody
    public WebApiBaseResult getionetNodeList(HttpSession httpSession, HttpServletRequest request) throws Exception {
        List<EthNodeDetailModel> ethNodeModels = ethNodeService.listNodeDetailByNodeType(EthNodeModel.NODETYPE_IONET, 1);
        return WebApiBaseResult.success(ethNodeModels);
    }
    /**
     * 获取avail节点列表
     * @param httpSession
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getAvailNodeList")
    @ResponseBody
    public WebApiBaseResult getAvailNodeList(HttpSession httpSession, HttpServletRequest request) throws Exception {
        List<EthNodeModel> ethNodeModels = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_AVAIL);
        return WebApiBaseResult.success(ethNodeModels);
    }
    /**
     * 获取shardeum节点列表
     * @param httpSession
     * @param request
     * @param type
     * @param mobile
     * @param password
     * @return
     * @throws Exception
     */
    @RequestMapping("/dealShardeumNodeList")
    @ResponseBody
    public WebApiBaseResult dealShardeumNodeList(HttpSession httpSession, HttpServletRequest request) throws Exception {
        List<EthNodeModel> ethNodeModels = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_SHARDEUM);
        OkHttpClient client = OkHttpClientUtil.getUnsafeOkHttpClient().build();
        ThreadUtils.ChokeLimitThreadPool chokeLimitThreadPool = ThreadUtils.getInstance().chokeLimitThreadPool(ethNodeModels.size(), 10);
        for(EthNodeModel node:ethNodeModels){
            chokeLimitThreadPool.run(new ThreadUtils.ChokeLimitThreadPool.RunThread() {
                @Override
                public void run() throws InterruptedException {
                    try {
                        String accessToken = ethNodeService.loginShardeum(client, node);
                        Map<String, Object> shardeumStatus = ethNodeService.getShardeumStatus(client, node, accessToken);
                        String state = (String) shardeumStatus.get("state");
                        if("stopped".equals(state)){//如果节点停止了，则开启节点
                            ethNodeService.startShardeumNode(client, node, accessToken);
                            ethNodeService.getShardeumStatus(client, node, accessToken);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });

        }
        chokeLimitThreadPool.choke();
        return WebApiBaseResult.success(ethNodeModels);
    }
    /**
     * 获取shardeum节点列表
     * @param httpSession
     * @param request
     * @param type
     * @param mobile
     * @param password
     * @return
     * @throws Exception
     */
    @RequestMapping("/dealIonetNodeList")
    @ResponseBody
    public WebApiBaseResult dealIonetNodeList(HttpSession httpSession, HttpServletRequest request) throws Exception {
//        ethBrowserService.ionetRefreshToken(1L);
        List<EthNodeDetailModel> list = ethNodeService.listNodeDetailByNodeType(EthNodeModel.NODETYPE_IONET, 1);
        for(EthNodeDetailModel node:list){
            ethBrowserService.getionetDeviceStatus(node);
//            if(!"up".equals(node.getState())){
//                ethNodeService.restartIonetNode(node);
//            }
        }
        return WebApiBaseResult.success(list);
    }
    /**
     * 重启opside节点
     * @param httpSession
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/dealOpsideNodeList")
    @ResponseBody
    public WebApiBaseResult dealOpsideNodeList(HttpSession httpSession, HttpServletRequest request) throws Exception {
        List<EthNodeModel> ethNodeModels = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_OPSIDE);
        OkHttpClient client = OkHttpClientUtil.getUnsafeOkHttpClient().build();
        ethNodeService.getOpsideStatus(client, ethNodeModels);
        for(EthNodeModel node:ethNodeModels){
            try {
                if(0 == node.getEnabled()){//节点停用则不进行处理
                    continue;
                }
                if("active_offline".equals(node.getState())){
                    ethNodeService.startOpsideNode(node);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
        return WebApiBaseResult.success(ethNodeModels);
    }
    @RequestMapping("/getAvailSessionKey")
    @ResponseBody
    public WebApiBaseResult getAvailSessionKey(HttpSession httpSession, HttpServletRequest request) throws Exception {
        List<EthNodeModel> ethNodeModels = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_AVAIL);
        OkHttpClient client = OkHttpClientUtil.getUnsafeOkHttpClient().build();
        ethNodeService.getOpsideStatus(client, ethNodeModels);
        for(EthNodeModel node:ethNodeModels){
            try {
                if(0 == node.getEnabled()){//节点停用则不进行处理
                    continue;
                }
                ethNodeService.getAvailSessionKey(node);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
        return WebApiBaseResult.success(ethNodeModels);
    }

    /**
     * 获得Quili的余额
     * @param httpSession
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/obtainQuiliBalance")
    @ResponseBody
    public WebApiBaseResult obtainQuiliBalance(HttpSession httpSession, HttpServletRequest request, @RequestParam(required = false, value = "detailId") Long detailId) throws Exception {
        if(detailId != null){
            ethNodeService.obtainQuiliBalance(detailId);
        }else{
            ethNodeService.obtainQuiliBalance();
        }
        return WebApiBaseResult.success();
    }
    /**
     * 获得Quili的余额
     * @param httpSession
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateQuiliBalance")
    @ResponseBody
    public WebApiBaseResult updateQuiliBalance(@RequestParam(required = false, value = "nodeName") String nodeName,
                                               @RequestParam(required = false, value = "version") String version,
                                               @RequestParam(required = false, value = "balance") String balance,
                                               @RequestParam(required = false, value = "increment") String increment,
                                               @RequestParam(required = false, value = "frame_number") String frame_number,
                                               @RequestParam(required = false, value = "processNum") String processNum,
                                               @RequestParam(required = false, value = "nproc") String nproc) throws Exception {
        ethNodeService.updateQuiliBalance(nodeName, version, balance, increment, frame_number, processNum, nproc);
        return WebApiBaseResult.success();
    }
    @RequestMapping("/addBackupTask")
    @ResponseBody
    public WebApiBaseResult addBackupTask(@RequestParam(required = false, value = "nodeName") String nodeName) throws Exception {
        if("all".equals(nodeName)){
            List<EthNodeDetailModel> detailList = ethNodeService.listNodeDetailByNodeType(EthNodeModel.NODETYPE_QUILIBRIUM, 1);
            for (EthNodeDetailModel detail : detailList) {
                ethNodeDetailTaskService.addBackupTask(detail, true);
            }
        }else{
            EthNodeDetailModel detail = ethNodeService.getNodeDetailByName(EthNodeModel.NODETYPE_QUILIBRIUM, nodeName);
            ethNodeDetailTaskService.addBackupTask(detail, false);
        }
        return WebApiBaseResult.success();
    }
    @RequestMapping("/isBackup")
    @ResponseBody
    public WebApiBaseResult isBackup(@RequestParam(required = false, value = "nodeName") String nodeName) throws Exception {
        EthNodeBackupModel nodeBackup = ethNodeService.getEthNodeBackup();
        EthNodeDetailModel detail = ethNodeService.getNodeDetailByName(EthNodeModel.NODETYPE_QUILIBRIUM, nodeName);
        boolean isBackup = ethNodeDetailTaskService.isBackup(detail, nodeBackup);
        Map<String, Object> map = new HashMap<>();
        map.put("isBackup", isBackup);
        if(isBackup){
            map.put("user", nodeBackup.getAdmin());
            map.put("password", nodeBackup.getPassword());
            map.put("ip", nodeBackup.getIp());
            map.put("filePath", nodeBackup.getFilePath());
            map.put("port", nodeBackup.getPort());
        }
        return WebApiBaseResult.success(map);
    }
    @RequestMapping("/finishBackup")
    @ResponseBody
    public WebApiBaseResult finishBackup(@RequestParam(required = false, value = "nodeName") String nodeName) throws Exception {
        EthNodeDetailModel detail = ethNodeService.getNodeDetailByName(EthNodeModel.NODETYPE_QUILIBRIUM, nodeName);
        ethNodeDetailTaskService.finishBackup(detail);
        return WebApiBaseResult.success();
    }

    /**
     * 统计每日余额
     * @param httpSession
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/quiliDailyStat")
    @ResponseBody
    public WebApiBaseResult quiliDailyStat(HttpSession httpSession, HttpServletRequest request) throws Exception {
        ethNodeService.quiliDailyStat(DateUtils.addDays(DateUtils.parseAndFormat(new Date(), "yyyy-MM-dd"), -1));
        return WebApiBaseResult.success();
    }
    @RequestMapping("/updateShowName")
    @ResponseBody
    public WebApiBaseResult updateShowName(HttpSession httpSession, HttpServletRequest request
            , @RequestParam(required = false, value = "id") Long id
            , @RequestParam(required = false, value = "showName") String showName) throws Exception {
        ethNodeService.updateShowName(id, showName);
        return WebApiBaseResult.success();
    }
    @RequestMapping("/updateExpireDate")
    @ResponseBody
    public WebApiBaseResult updateExpireDate(HttpSession httpSession, HttpServletRequest request
            , @RequestParam(required = false, value = "id") Long id
            , @RequestParam(required = false, value = "expireDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date expireDate) throws Exception {
        ethNodeService.updateExpireDate(id, expireDate);
        return WebApiBaseResult.success();
    }
}
