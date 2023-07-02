package com.crawler.eth.node.action;

import com.crawler.account.model.CrawlerClientInfo;
import com.crawler.account.pool.CrawlerClientPool;
import com.crawler.base.common.model.WebApiBaseResult;
import com.crawler.base.utils.OkHttpClientUtil;
import com.crawler.base.utils.StringUtils;
import com.crawler.eth.node.model.EthNodeModel;
import com.crawler.eth.node.service.IEthNodeService;
import com.crawler.jd.service.IJdService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/eth/node")
@Slf4j
public class EthNodeController {
    @Resource
    IEthNodeService ethNodeService;
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
     * @param type
     * @param mobile
     * @param password
     * @return
     * @throws Exception
     */
    @RequestMapping("/dealShardeumNodeList")
    @ResponseBody
    public WebApiBaseResult dealShardeumNodeList(HttpSession httpSession, HttpServletRequest request
            ,@RequestParam(value = "type", required = false) String type
            ,@RequestParam(value = "mobile", required = false) String mobile
            ,@RequestParam(value = "password", required = false) String password) throws Exception {
        List<EthNodeModel> ethNodeModels = ethNodeService.listNodeByNodeType(EthNodeModel.NODETYPE_SHARDEUM);
        OkHttpClient client = OkHttpClientUtil.getUnsafeOkHttpClient();
        for(EthNodeModel node:ethNodeModels){
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
        return WebApiBaseResult.success(ethNodeModels);
    }
}
