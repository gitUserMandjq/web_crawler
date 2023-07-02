package com.crawler.account.model;

import com.crawler.base.common.pool.DriverPool;
import com.crawler.base.utils.ChromeDriverWapper;
import lombok.Data;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Date;

@Data
public class CrawlerClientInfo {
    private String account;
    private String type;
    private String password;
    private String session_id;
    private Date loginTime;
    private String status = STATUS_NEW;
    private String loginStaus = STATUS_NEW;
    private String qrcode;

    public ChromeDriverWapper obtainDriver() {
        return DriverPool.getDriver(type, account);
    }

    public void addDriver(ChromeDriverWapper driver){
        this.session_id = driver.getDriver().getSessionId().toString();
        DriverPool.addDriver(type, account, driver);
    }
    public void destoryDriver(){
        this.session_id = null;
        DriverPool.destoryDriver(type, account);
    }
    public static final String STATUS_NEW = "new";//新建
    public static final String STATUS_LOG_PROCESS = "log_process";//登录中
    public static final String STATUS_LOG_FAIL = "log_fail";//登录失败
    public static final String STATUS_LOG_IN = "log_in";//已登录
    public static final String STATUS_LOG_VERIFICA = "log_Verifica";//短信验证
    public static final String STATUS_LOG_QRCODE = "log_Qrcode";//二维码验证
    public static final String STATUS_LOG_LOSE = "log_lose";//登录失效
    private String message = MESSAGE_NEW;
    public static final String MESSAGE_NEW = "新建";
    public static final String MESSAGE_LOG_PROCESS = "登录中";
    public static final String MESSAGE_LOG_FAIL = "登录失败";
    public static final String MESSAGE_LOG_IN = "已登录";
    public static final String MESSAGE_LOG_VERIFICA = "短信验证";
    public static final String MESSAGE_LOG_QRCODE = "二维码验证";
    public static final String MESSAGE_LOG_LOSE = "登录失效";
    private String errMessage;
    public boolean canLog(){
        if(STATUS_LOG_IN.equals(status) || STATUS_LOG_PROCESS.equals(status)){
            //用户已登录或者正在登录中
            return false;
        }
        return true;
    }
    public void logProcess(){
        this.status = STATUS_LOG_PROCESS;
        this.loginStaus = STATUS_LOG_PROCESS;
        this.message = MESSAGE_LOG_PROCESS;
    }
    public void logVerifica(){
        this.status = MESSAGE_LOG_PROCESS;
        this.loginStaus = STATUS_LOG_VERIFICA;
        this.message = MESSAGE_LOG_VERIFICA;
    }
    public void logQrcode(String qrcode){
        this.status = MESSAGE_LOG_PROCESS;
        this.loginStaus = STATUS_LOG_QRCODE;
        this.message = MESSAGE_LOG_QRCODE;
        this.qrcode = qrcode;
    }
    public void logFail(String errMessage){
        this.status = STATUS_LOG_FAIL;
        this.loginStaus = STATUS_LOG_FAIL;
        this.message = MESSAGE_LOG_FAIL;
        this.errMessage = errMessage;
    }
    public void logIn(){
        this.status = STATUS_LOG_IN;
        this.loginStaus = STATUS_LOG_IN;
        this.message = MESSAGE_LOG_IN;
        this.loginTime = new Date();
    }
    public void logLose(){
        this.status = STATUS_LOG_LOSE;
        this.loginStaus = STATUS_LOG_LOSE;
        this.message = MESSAGE_LOG_LOSE;
    }
}
