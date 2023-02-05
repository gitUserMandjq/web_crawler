package com.crawler.account.action;

import com.crawler.account.model.CrawlerClientInfo;
import com.crawler.account.pool.CrawlerClientPool;
import com.crawler.base.common.model.WebApiBaseResult;
import com.crawler.base.utils.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/account")
public class AccountController {

    /**
     * 获取爬虫信息
     * @param httpSession
     * @param request
     * @return
     */
    @RequestMapping(value = "/getCrawlerClientInfo", method = RequestMethod.GET)
    @ResponseBody
    public WebApiBaseResult getCrawlerClientInfo(HttpSession httpSession, HttpServletRequest request
            ,@RequestParam(value = "type", required = false) String type
            ,@RequestParam(value = "mobile", required = false) String mobile) throws Exception {
        if(StringUtils.isEmpty(type)){
            throw new Exception("类型不能为空");
        }
        if(StringUtils.isEmpty(mobile)){
            throw new Exception("手机号不能为空");
        }
        CrawlerClientInfo client = CrawlerClientPool.getAccount(type, mobile);
        if(client == null){
            throw new Exception("未登录");
        }
        return WebApiBaseResult.success(client);
    }
}
