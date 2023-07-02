package com.crawler.base.utils;

import org.openqa.selenium.chrome.ChromeDriver;

public class ChromeDriverWapper {

    private ChromeDriver driver;
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
    }
    ChromeDriverWapper(ChromeDriver driver, OkHttpClientUtil okHttpClientUtil, Integer port){
        this.driver = driver;
        this.okHttpClientUtil = okHttpClientUtil;
        this.port = port;
    }
    public ChromeDriver getDriver() {
        return driver;
    }

    public void setDriver(ChromeDriver driver) {
        this.driver = driver;
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
