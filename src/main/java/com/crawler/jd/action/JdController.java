package com.crawler.jd.action;

import com.crawler.account.model.CrawlerClientInfo;
import com.crawler.account.pool.CrawlerClientPool;
import com.crawler.base.common.model.WebApiBaseResult;
import com.crawler.base.common.pool.DriverPool;
import com.crawler.base.utils.StringUtils;
import com.crawler.jd.service.IJdService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/jd")
public class JdController {
    @Resource
    IJdService jdService;
    @GetMapping("/index")
    public String index(){
        return "jd/jdhome"; //当浏览器输入/index时，会返回 /static/home.html的页面
    }

    @RequestMapping("/loginByPassword")
    @ResponseBody
    public WebApiBaseResult loginByPassword(HttpSession httpSession, HttpServletRequest request
            ,@RequestParam(value = "type", required = false) String type
            ,@RequestParam(value = "mobile", required = false) String mobile
            ,@RequestParam(value = "password", required = false) String password) throws Exception {
        if(StringUtils.isEmpty(type)){
            throw new Exception("类型不能为空");
        }
        if(StringUtils.isEmpty(mobile)){
            throw new Exception("手机号不能为空");
        }
        CrawlerClientInfo client = CrawlerClientPool.getAndAddAccount(type, mobile);
        if(!client.canLog()){
            throw new Exception("用户已登录或者正在登录");
        }
        jdService.loginByPassword(mobile, password, client);
        return WebApiBaseResult.success(client);
    }
    @RequestMapping("/loginVerifica")
    @ResponseBody
    public WebApiBaseResult loginVerifica(HttpSession httpSession, HttpServletRequest request
            ,@RequestParam(value = "type", required = false) String type
            ,@RequestParam(value = "mobile", required = false) String mobile
            ,@RequestParam(value = "verification", required = false) String verification) throws Exception {
        if(StringUtils.isEmpty(type)){
            throw new Exception("类型不能为空");
        }
        if(StringUtils.isEmpty(mobile)){
            throw new Exception("手机号不能为空");
        }
        CrawlerClientInfo client = CrawlerClientPool.getAndAddAccount(type, mobile);
        if(!CrawlerClientInfo.STATUS_LOG_VERIFICA.equals(client.getLoginStaus())){
            throw new Exception("用户不在短信验证状态");
        }
        jdService.loginVerifica(client, verification);
        return WebApiBaseResult.success(client);
    }
    @RequestMapping("/resetAccount")
    @ResponseBody
    public WebApiBaseResult resetAccount(HttpSession httpSession, HttpServletRequest request
            ,@RequestParam(value = "type", required = false) String type
            ,@RequestParam(value = "mobile", required = false) String mobile) throws Exception {
        if(StringUtils.isEmpty(type)){
            throw new Exception("类型不能为空");
        }
        if(StringUtils.isEmpty(mobile)){
            throw new Exception("手机号不能为空");
        }
        CrawlerClientInfo client = CrawlerClientPool.getAccount(type, mobile);
        if(client != null){
            //销毁driver
            client.destoryDriver();
            client.logFail("重置登录状态");
            CrawlerClientPool.destoryAccount(type, mobile);
        }
        return WebApiBaseResult.success();
    }
}
