package com.crawler.jd.service;

import com.crawler.account.model.CrawlerClientInfo;

public interface IJdService {
    /**
     * 使用密码登录
     * @param account
     * @param password
     * @param clientInfo
     */
    void loginByPassword(String account, String password, CrawlerClientInfo clientInfo) throws Exception;
    /**
     * 使用密码登录
     * @param account
     * @param clientInfo
     */
    void loginByQrcode(String account, CrawlerClientInfo clientInfo) throws Exception;
    /**
     * 使用密码登录
     * @param account
     * @param clientInfo
     */
    void getQRLoginInfo(String account, CrawlerClientInfo clientInfo) throws Exception;

    /**
     * 填写验证码
     * @param clientInfo
     * @param verification
     * @throws Exception
     */
    void loginVerifica(CrawlerClientInfo clientInfo, String verification)throws Exception;
}
