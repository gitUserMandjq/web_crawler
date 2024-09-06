package com.crawler.base.utils;

import ai.djl.Application;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class DriverUtils {

    private final static String driver = "webdriver.chrome.driver";
    private static String chromeDriver = "D://ruanjian//chromedriver_win32//chromedriver.exe";
//    private static String chromeDriver = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";

    private static String openCvDll = "D:\\ruanjian\\opencv\\build\\java\\x64\\opencv_java460.dll";

    public static final int min_port = 1000;
    public static final int max_port_number = 1000;
    private static boolean[] portArr = new boolean[max_port_number];
    static {

        try {
            InputStream is = DriverUtils.class.getClassLoader().getResourceAsStream("application.properties");
            Properties property = new Properties();
            property.load(is);
            chromeDriver = PropertiesUtils.readValue(property, "driver.chromeDriver");
            openCvDll = PropertiesUtils.readValue(property, "opencv.path");
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        // 引入谷歌驱动 控制浏览器
        System.setProperty(driver, chromeDriver);
        // 引入opencv的dll
        System.load(openCvDll);
    }
    public static ChromeDriverWapper buildDriver(){
        return buildDriver(getUsedPort());
    }
    public static ChromeDriverWapper buildDriver(String dataDir){
        return buildDriver(getUsedPort(), dataDir);
    }
    public static ChromeDriverWapper buildDriver(int port){
        return buildDriver(port, "");
    }
    public static ChromeDriverWapper buildDriver(int port, String dataDir){
        ChromeDriverManager.getInstance().setup();
        ChromeOptions option = new ChromeOptions();
        option.addArguments("no-sandbox");//禁用沙盒
        option.addArguments("start-maximized");//最大化
        option.addArguments("user-data-dir="+dataDir);
        option.addArguments("--disable-blink-features=AutomationControlled");
        //通过ChromeOptions的setExperimentalOption方法，传下面两个参数来禁止掉谷歌受自动化控制的信息栏
        option.setExperimentalOption("useAutomationExtension", false);
        option.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        return buildDriver(option, port);
    }
    public static ChromeDriverWapper buildDriver(ChromeOptions option, int port){

        if(HostUtils.isUsed(port)){
            //重连
            log.info("重连已有debug浏览器");
            option.setExperimentalOption("debuggerAddress","127.0.0.1:"+port);
        }else{
            log.info("使用debug模式启动浏览器");
            //远程debug模式
            option.addArguments("--remote-debugging-port="+port);
        }
        ChromeDriver driver = new ChromeDriver(option);
        usePort(port);
        return ChromeDriverWapper.init(driver, port);
    }
    public static void destoryDrive(ChromeDriverWapper driverWapper){
        ChromeDriver driver = driverWapper.getDriver();
        driver.quit();
        driver.close();
        destoryPort(driverWapper.getPort());
    }
    private static void usePort(int port){
        portArr[port - min_port] = true;
    }
    private static void destoryPort(int port){
        portArr[port - min_port] = true;
    }
    public static boolean isPortOccupy(int port){
        return portArr[port - min_port];
    }
    public static int getUsedPort(){
        for(int i=0;i < max_port_number;i++){
            if(!portArr[i]){
                return i + min_port;
            }
        }
        return min_port;
    }
    public static void getUrl(ChromeDriverWapper driverWapper, String url){
        if(!url.equals(driverWapper.getDriver().getCurrentUrl())){
            driverWapper.getDriver().get(url);
        }
    }
    public static void switchToWindows(ChromeDriverWapper driverWapper, String url){
        switchToWindows(driverWapper, url, null);
    }
    public static void switchToWindows(ChromeDriverWapper driverWapper, String contain, String[] notContain){
        driverWapper.getWait().until((dr) ->{
            Set<String> windowHandles = dr.getWindowHandles();
            for (Object o : windowHandles.toArray()) {
                String currentUrl = "";
                try {
                    dr.switchTo().window((String) o);
                    currentUrl = dr.getCurrentUrl();
                } catch (JavascriptException e) {
                    return false;
                }
                log.info("switchTo:{}", currentUrl);
                if(currentUrl.contains(contain) && notContain(currentUrl, notContain)){
                    return true;
                }
            }
            return false;
        });
    }
    private static boolean notContain(String url, String[] notContain){
        if(notContain == null || notContain.length == 0){
            return true;
        }
        for (String s : notContain) {
            if(url.contains(s)){
                return false;
            }
        }
        return true;
    }
    public static void untilElement(ChromeDriverWapper driverWapper, By by){
        Date beginTime = new Date();
        try {
            driverWapper.getWait().until(ExpectedConditions.presenceOfElementLocated(by));
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        log.info("url:{},waitTime:{}ms", driverWapper.getDriver().getCurrentUrl(), new Date().getTime() - beginTime.getTime());
    }
    public static WebElement getAndUntilElement(ChromeDriverWapper driverWapper, By by){
        Date beginTime = new Date();
        try {
            driverWapper.getWait().until(ExpectedConditions.presenceOfElementLocated(by));
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        log.info("url:{},waitTime:{}ms", driverWapper.getDriver().getCurrentUrl(), new Date().getTime() - beginTime.getTime());
        WebElement element = driverWapper.getDriver().findElement(by);
        return element;
    }
    public static WebElement getElementByScript(ChromeDriverWapper driverWapper, String js){
        WebElement element = (WebElement) driverWapper.getDriver().executeScript(js);
        return element;
    }
    public static Object getObjectByScript(ChromeDriverWapper driverWapper, String js){
        Object o = driverWapper.getDriver().executeScript(js);
        return o;
    }
    public static WebElement getElementByScript(ChromeDriverWapper driverWapper, String js, WebElement e){
        WebElement element = (WebElement) driverWapper.getDriver().executeScript(js, e);
        return element;
    }
    public static void clearInput(WebElement element){
        element.sendKeys(Keys.CONTROL+"a");
        element.sendKeys(Keys.DELETE);
    }
    public static void main(String[] args) {
        System.out.println(portArr.length);
        System.out.println(portArr[0]);
    }
}
