package com.crawler.test;

import com.crawler.base.utils.ChromeDriverWapper;
import com.crawler.base.utils.DriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Slf4j
public class CommonTest {
    private static String password = "@Flagdai901385";








    public static void main(String[] args) throws InterruptedException {
//        ChromeDriverWapper driverWapper = DriverUtils.buildDriver(1000, "E:\\项目\\浏览器\\data\\jzm");
        ChromeDriverWapper driverWapper = DriverUtils.buildDriver(1000, "E:\\项目\\浏览器\\data\\1");
//        {
//            DriverUtils.switchToWindows(driverWapper, "notification.html");
//            {//下一步
////                WebElement element = driverWapper.getDriver().findElement(By.xpath("//button[@data-testid=\"page-container-footer-next\"]"));
//                WebElement element = DriverUtils.getElementUntil(driverWapper, By.xpath("//button[@data-testid=\"page-container-footer-next\"]"));
//                log.info("element:{}", element.getAttribute("outerHTML"));
////                element.click();
//            }
//            if(1 == 1){
//                return;
//            }
//        }
        {
            DriverUtils.getUrl(driverWapper, "https://miles.plumenetwork.xyz/faucet");
            DriverUtils.untilElement(driverWapper, By.xpath("//div[@id=\"cf-turnstile\"]/div"));
//            WebElement shadowRoot = DriverUtils.getShadowRoot(driverWapper, element);
            Thread.sleep(500);
            driverWapper.getWait().until((dr) ->{
                WebElement frame = DriverUtils.getElementByScript(driverWapper, "return document.querySelector(\"#cf-turnstile > div\").shadowRoot.querySelector(\"iframe\")");
                System.out.println(frame);
                if(frame == null){
                    return false;
                }
                log.info("element:{}", frame.getAttribute("outerHTML"));
                driverWapper.getDriver().switchTo().frame(frame);
                log.info("url:{}", driverWapper.getDriver().getCurrentUrl());
                return true;
            });
            //iframe嵌套了document
            DriverUtils.untilElement(driverWapper,By.tagName("body"));
            driverWapper.getWait().until((dr) ->{
                log.info("url:{}", driverWapper.getDriver().getTitle());
                Object shadowRoot = DriverUtils.getObjectByScript(driverWapper, "return document.querySelector(\"body\").shadowRoot");
                if(shadowRoot == null){
                    return false;
                }
                WebElement input = DriverUtils.getElementByScript(driverWapper, "return document.querySelector(\"body\").shadowRoot.querySelector(\"#MDjtu6 > div > label > input[type=checkbox]\")");
                if(input == null){
                    return false;
                }
                input.click();
                driverWapper.getDriver().switchTo().parentFrame();
                return true;
            });
        }
//        loginPlumeByMetaMask(driverWapper);
//        signIn(driverWapper);
    }

}
