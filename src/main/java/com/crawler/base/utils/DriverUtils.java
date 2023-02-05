package com.crawler.base.utils;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.InputStream;
import java.util.Properties;
@Slf4j
public class DriverUtils {

    private final static String driver = "webdriver.chrome.driver";
    private static String chromeDriver = "D://ruanjian//chromedriver_win32//chromedriver.exe";
    private static String openCvDll = "D:\\ruanjian\\opencv\\build\\java\\x64\\opencv_java460.dll";
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
    public static ChromeDriver buildDriver(){
        ChromeOptions option = new ChromeOptions();
        option.addArguments("no-sandbox");//禁用沙盒
        //通过ChromeOptions的setExperimentalOption方法，传下面两个参数来禁止掉谷歌受自动化控制的信息栏
//    option.setExperimentalOption("useAutomationExtension", false);
//    option.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        return buildDriver(option);
    }
    public static ChromeDriver buildDriver(ChromeOptions option){
        ChromeDriver driver = new ChromeDriver(option);
        return driver;
    }
}
