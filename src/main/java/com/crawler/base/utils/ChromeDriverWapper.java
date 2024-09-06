package com.crawler.base.utils;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChromeDriverWapper {

    private ChromeDriver driver;
    private WebDriverWait wait;
    private OkHttpClientUtil okHttpClientUtil;
    private Integer port;
    public static ChromeDriverWapper init(ChromeDriver driver, Integer port){
        return new ChromeDriverWapper(driver, port);
    }
    public static ChromeDriverWapper init(ChromeDriver driver, OkHttpClientUtil okHttpClientUtil, Integer port){
        return new ChromeDriverWapper(driver, okHttpClientUtil, port);
    }
    ChromeDriverWapper(ChromeDriver driver, Integer port){
        this.driver = driver;
        this.port = port;
        this.wait = new WebDriverWait(driver, 20);
    }
    ChromeDriverWapper(ChromeDriver driver, OkHttpClientUtil okHttpClientUtil, Integer port){
        this.driver = driver;
        this.okHttpClientUtil = okHttpClientUtil;
        this.port = port;
        this.wait = new WebDriverWait(driver, 20);
    }
    public ChromeDriver getDriver() {
        return driver;
    }

    public void setDriver(ChromeDriver driver) {
        this.driver = driver;
    }

    public WebDriverWait getWait() {
        return wait;
    }

    public void setWait(WebDriverWait wait) {
        this.wait = wait;
    }

    public OkHttpClientUtil getOkHttpClientUtil() {
        return okHttpClientUtil;
    }

    public void setOkHttpClientUtil(OkHttpClientUtil okHttpClientUtil) {
        this.okHttpClientUtil = okHttpClientUtil;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
