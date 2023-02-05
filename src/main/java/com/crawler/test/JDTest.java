package com.crawler.test;

import com.crawler.base.common.model.MutiResult;
import com.crawler.jd.constant.JdConst;
import com.crawler.jd.service.impl.JdServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.interactions.PointerInput;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.interactions.PointerInput.Kind.MOUSE;
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
        //通过ChromeOptions的setExperimentalOption方法，传下面两个参数来禁止掉谷歌受自动化控制的信息栏
//    option.setExperimentalOption("useAutomationExtension", false);
//    option.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        ChromeDriver driver = new ChromeDriver(option);
        try {
            // 窗口最大化
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
            //进入百度首页
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