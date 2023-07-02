package com.crawler.test;

import com.crawler.base.common.model.MutiResult;
import com.crawler.base.utils.HostUtils;
import com.crawler.base.utils.JsonUtil;
import com.crawler.base.utils.OkHttpClientUtil;
import com.crawler.jd.constant.JdConst;
import com.crawler.jd.service.impl.JdServiceImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.openqa.selenium.*;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.remote.HttpCommandExecutor;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JDTest {

    private final static String driver = "webdriver.chrome.driver";
    private final static String chromeDriver = "D://ruanjian//chromedriver_win32//chromedriver.exe";
    private final static String openCvDll = "D:\\ruanjian\\opencv\\build\\java\\x64\\opencv_java460.dll";
    static {
        // 引入谷歌驱动 控制浏览器
        System.setProperty(driver, chromeDriver);
        // 引入opencv的dll
        System.load(openCvDll);
    }
    public static void main(String[] args) throws Exception {
//        List<Integer> track = getTrack(100);
        loginInfo();
    }

    private static void loginInfo() throws InterruptedException, IOException {
        JdServiceImpl jdService = new JdServiceImpl();
        ChromeOptions option = new ChromeOptions();
        option.addArguments("no-sandbox");//禁用沙盒
        option.addArguments("start-maximized");
        int port = 9527;
        if(HostUtils.isUsed(port)){
            //重连
            log.info("重连已有debug浏览器");
            option.setExperimentalOption("debuggerAddress","127.0.0.1:"+port);
        }else{
            log.info("使用debug模式启动浏览器");
            //远程debug模式
            option.addArguments("--remote-debugging-port="+port);
        }
        //通过ChromeOptions的setExperimentalOption方法，传下面两个参数来禁止掉谷歌受自动化控制的信息栏
//    option.setExperimentalOption("useAutomationExtension", false);
//    option.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        ChromeDriver driver = new ChromeDriver(option);
//        System.out.println(driver.getCurrentUrl());

//        driver.manage().window().maximize();//可能报错
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        Set<String> windowHandles = driver.getWindowHandles();
        log.info(windowHandles.toString());
        URL remoteServer = ((HttpCommandExecutor) driver.getCommandExecutor()).getAddressOfRemoteServer();
        log.info(String.valueOf(driver.getSessionId()));
        log.info(String.valueOf(remoteServer.getPort()));
        log.info(String.valueOf(remoteServer));
        // 窗口最大化
        Thread.sleep(1000);
        WebDriver.Options manage = driver.manage();
        OkHttpClientUtil okHttpClientUtil = new OkHttpClientUtil(OkHttpClientUtil.getBuilder());
        String url = "https://www.jd.com/";
        String cookie = "";
        {//设置cookie
            Set<Cookie> cookieSet = manage.getCookies();
            for(Cookie p:cookieSet){
//                System.out.println(JsonUtil.object2String(p));
                cookie += p.getName()+"="+p.getValue()+";";
            }
        }
        //执行js
//        Object o = driver.executeScript("return sessionStorage.getItem(\"shshshfpa\");");
//        SessionStorage sessionStorage = driver.getSessionStorage();
//        Set<String> set = sessionStorage.keySet();
//        for(String key:set){
//            String item = sessionStorage.getItem(key);
//            System.out.println(key+":"+item);
//        }
//        List<okhttp3.Cookie> cookies1 = okHttpClientUtil.getClient().cookieJar().loadForRequest(HttpUrl.get(url));
        System.out.println(cookie);
        addCart(okHttpClientUtil, cookie);
//        System.out.println(data.body().string());
//        driver.get(JdConst.URL_LOGIN);
//        login(jdService, driver);
    }

    private static void addCart(OkHttpClientUtil okHttpClientUtil, String cookie) throws IOException {
        Request request = new Request.Builder()
                .url("https://cart.jd.com/gate.action?pid=200427809292&pcount=1&ptype=3&sku=100038004389%2C100042412131")
                .get()
                .addHeader("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .addHeader("Sec-Fetch-Site", "none")
                .addHeader("Sec-Fetch-Mode", "navigate")
                .addHeader("Sec-Fetch-User", "?1")
                .addHeader("Sec-Fetch-Dest", "document")
                .addHeader("host", "cart.jd.com")
                .addHeader("Cookie", cookie)
                .build();
        Response response = okHttpClientUtil.getClient().newCall(request).execute();
    }

    private static void login(JdServiceImpl jdService, ChromeDriver driver) throws InterruptedException, IOException {
        try {
            //进入京东首页
            driver.get(JdConst.URL_LOGIN);
            //找到输入框
            {
                String account = "15558113097";
                String password = "jingdong123";
//                String account = "19559132900";
//                String password = "lin181022";
                jdService.loginStr(driver, account, password);
            }
            //7、这个时候可能会弹出来滑块
            Boolean flag = true;
            int i = 1;
            do {
                log.info("第{}次进行验证", i++);
                //8、这时候可能有滑块出现yidun_slider
                MutiResult<JdServiceImpl.SliderType, Object> mutiResult = jdService.getSliderElement(driver);
                WebElement sliderBJ = (WebElement) mutiResult.get(JdServiceImpl.SliderType.sliderBJ);
                WebElement sliderHK = (WebElement) mutiResult.get(JdServiceImpl.SliderType.sliderHK);
                Integer top = (Integer) mutiResult.get(JdServiceImpl.SliderType.top);
                //10、解决图片下载不了的问题
                TimeUnit.SECONDS.sleep(1);
                //11、如果不为空需要处理滑块
                if (sliderBJ != null && sliderHK != null) {
                    //11.1、先得到距离，这里需要opencv  先搭建一下
                    //11.2、获取到背景图的地址和滑块的地址
                    double distance = jdService.calculateDistance(sliderBJ, sliderHK, top);
                    //11.11、模拟移动
                    jdService.move(driver, sliderHK, (int) distance + 20);
                    //11.12、移动后看看是否还存在不
                    try {
                        TimeUnit.SECONDS.sleep(2);
                        String currentUrl = driver.getCurrentUrl();
                        log.info("url:{}", currentUrl);
                        if(!currentUrl.contains(JdConst.URL_LOGIN)){
                            flag = false;
                            log.info("验证成功");
                            break;
                        }
                        log.info("判断是否验证成功");
                        WebElement submit = driver.findElement(By.id("JDJRV-wrap-loginsubmit"));
                        String display = submit.getCssValue("display");
                        if("none".equals(display)){
                            flag = false;
                            log.info("验证成功");
                        }
                        //                    flag = false;
                    } catch (Exception e) {
                        flag = false;
                        log.info("验证成功");
                    }
                }
            } while (flag);
            log.info("循环结束，验证成功");
        } finally {
            jdService.sleep(1000);
            if(driver.getCurrentUrl().contains(JdConst.URL_CERTIFIED)){//获取短信验证码
                jdService.sendVerificationCode(driver);
            }
    //        driver.quit();
        }
    }

}