package com.crawler.test;

import com.crawler.base.utils.ChromeDriverWapper;
import com.crawler.base.utils.DriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

@Slf4j
public class EthTest {
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

    private static void signIn(ChromeDriverWapper driverWapper) {
        {//
            DriverUtils.switchToWindows(driverWapper, "https://miles.plumenetwork.xyz/");
            WebElement element = DriverUtils.getAndUntilElement(driverWapper, By.xpath("//a[@data-testid=\"daily-checkin-early-access-nav-link\"]"));
            log.info("element:{}", element.getAttribute("outerHTML"));
            element.click();
        }
        {//
            WebElement element = DriverUtils.getAndUntilElement(driverWapper, By.xpath("//button[@class=\"chakra-button css-1nmn613\"]"));
            log.info("element:{}", element.getAttribute("outerHTML"));
            element.click();
        }
    }

    /**
     * 登陆plumenetwork
     * @param driverWapper
     */
    private static void loginPlumeByMetaMask(ChromeDriverWapper driverWapper) {
        DriverUtils.getUrl(driverWapper, "https://miles.plumenetwork.xyz/");
        {
            WebElement element = driverWapper.getDriver().findElement(By.xpath("//button[text()='Connect Wallet']"));
//            WebElement element = driverWapper.getDriver().findElement(By.xpath("//button[contains(text(),'Connect Wallet')]"));
            log.info("element:{}", element.getAttribute("outerHTML"));
            element.click();
        }
        {
            WebElement element = driverWapper.getDriver().findElement(By.xpath("//button[text()='Login with your Wallet']"));
            log.info("element:{}", element.getAttribute("outerHTML"));
            element.click();
        }
        {//MetaMask
            driverWapper.getWait().until(ExpectedConditions.visibilityOfElementLocated(By.tagName("w3m-modal")));
            driverWapper.getWait().until((dr) ->{
                WebElement element = DriverUtils.getElementByScript(driverWapper, "return document.querySelector(\"body > w3m-modal\").shadowRoot.querySelector(\"wui-flex > wui-card > w3m-router\").shadowRoot.querySelector(\"div > w3m-connect-view\").shadowRoot.querySelector(\"wui-flex > w3m-wallet-login-list\").shadowRoot.querySelector(\"wui-flex > w3m-connect-injected-widget\").shadowRoot.querySelector(\"wui-flex > wui-list-wallet\").shadowRoot.querySelector(\"button\")");
                if(element == null){
                    return false;
                }
                log.info("element:{}", element.getAttribute("outerHTML"));
                element.click();
                return true;
            });
        }
        {//MetaMask操作
            DriverUtils.switchToWindows(driverWapper, "notification.html");
            {//输入MetaMask密码并点击确认
                WebElement element = DriverUtils.getAndUntilElement(driverWapper, By.xpath("//*[@id='password']"));
                log.info("element:{}", element.getAttribute("outerHTML"));
                DriverUtils.clearInput(element);
                element.sendKeys("@Flagdai901385");
                element = driverWapper.getDriver().findElement(By.xpath("//button"));
                log.info("element:{}", element.getAttribute("outerHTML"));
                element.click();
            }
            {//下一步
                WebElement element = DriverUtils.getAndUntilElement(driverWapper, By.xpath("//button[@data-testid=\"page-container-footer-next\"]"));
                log.info("element:{}", element.getAttribute("outerHTML"));
                element.click();
            }
            {//下一步
                WebElement element = DriverUtils.getAndUntilElement(driverWapper, By.xpath("//button[@data-testid=\"page-container-footer-next\"]"));
                log.info("element:{}", element.getAttribute("outerHTML"));
                element.click();
            }
        }
        {
            DriverUtils.switchToWindows(driverWapper, "https://miles.plumenetwork.xyz/");
            WebElement element = DriverUtils.getAndUntilElement(driverWapper, By.xpath("//button[@data-testid=\"sign-message-button\"]"));
            log.info("element:{}", element.getAttribute("outerHTML"));
            element.click();
        }
        {//MetaMask操作
            DriverUtils.switchToWindows(driverWapper, "notification.html", new String[]{"confirm-permissions","#"});
            {//下一步
                WebElement element = DriverUtils.getAndUntilElement(driverWapper, By.xpath("//button[@data-testid=\"page-container-footer-next\"]"));
                log.info("element:{}", element.getAttribute("outerHTML"));
                element.click();
            }
        }
    }
}
