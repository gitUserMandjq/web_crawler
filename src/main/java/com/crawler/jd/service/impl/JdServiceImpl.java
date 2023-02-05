package com.crawler.jd.service.impl;

import com.crawler.account.model.CrawlerClientInfo;
import com.crawler.base.common.model.MutiResult;
import com.crawler.base.utils.DriverUtils;
import com.crawler.base.utils.StringUtils;
import com.crawler.jd.constant.JdConst;
import com.crawler.jd.service.IJdService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.interactions.PointerInput.Kind.MOUSE;

@Service
@Slf4j
public class JdServiceImpl implements IJdService {
    /**
     * 使用密码登录
     * @param account
     * @param password
     */
    @Override
    public void loginByPassword(String account, String password, CrawlerClientInfo clientInfo) throws Exception{
        ChromeDriver driver = getChromeDriver(clientInfo);
        try {
            // 窗口最大化
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            Thread.sleep(1000L);
            //进入京东首页
            driver.get(JdConst.URL_LOGIN);
            //找到输入框
            {
//                String account = "15558113097";
//                String password = "jingdong123";
//                String account = "19559132900";
//                String password = "lin181022";
                loginStr(driver, account, password);
            }
            //7、这个时候可能会弹出来滑块
            boolean flag = true;
            int i = 1;
            while(flag) {
                log.info("第{}次进行验证", i++);
                if(i > 10){
                    throw new Exception("滑块次数过多");
                }
                //8、这时候可能有滑块出现yidun_slider
                MutiResult<SliderType, Object> mutiResult = getSliderElement(driver);
                WebElement sliderBJ = (WebElement) mutiResult.get(SliderType.sliderBJ);
                WebElement sliderHK = (WebElement) mutiResult.get(SliderType.sliderHK);
                Integer top = (Integer) mutiResult.get(SliderType.top);
                //10、解决图片下载不了的问题
                TimeUnit.SECONDS.sleep(1);
                //11、如果不为空需要处理滑块
                if (sliderBJ != null && sliderHK != null) {
                    //11.1、先得到距离，这里需要opencv  先搭建一下
                    //11.2、获取到背景图的地址和滑块的地址
                    double distance = calculateDistance(sliderBJ, sliderHK, top);
                    //11.11、模拟移动
                    move(driver, sliderHK, (int) distance + 20);
                    //11.12、移动后看看是否还存在不
                    try {
                        TimeUnit.SECONDS.sleep(2);
                        String currentUrl = driver.getCurrentUrl();
                        log.info("判断是否验证成功");
                        log.info("url:{}", currentUrl);
                        if(!currentUrl.contains(JdConst.URL_LOGIN)){
                            flag = false;
                            log.info("验证成功");
                            break;
                        }
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
                    log.info("url:{}", driver.getCurrentUrl());
                }
            }
            log.info("循环结束，验证成功");
        } catch(Exception e){
            log.error(e.getMessage(), e);
            clientInfo.logFail(e.getMessage());
        } finally {
            sleep(1000);
            if(driver.getCurrentUrl().contains(JdConst.URL_CERTIFIED)){//获取短信验证码
                sendVerificationCode(driver);
                clientInfo.logVerifica();
                return;
            }
            clientInfo.logIn();
            //        driver.quit();
        }
    }

    private static ChromeDriver getChromeDriver(CrawlerClientInfo clientInfo) {
        ChromeDriver driver;
        if(clientInfo.obtainDriver() != null){
            driver = clientInfo.obtainDriver();
        }else{
            driver = DriverUtils.buildDriver();
            clientInfo.addDriver(driver);
        }
        return driver;
    }

    /**
     * 填写验证码
     * @param clientInfo
     * @param verification
     * @throws Exception
     */
    @Override
    public void loginVerifica(CrawlerClientInfo clientInfo, String verification) throws Exception {
        if(StringUtils.isEmpty(verification) || verification.length() != 6){
            throw new Exception("验证码为空或者不是6位");
        }
        ChromeDriver driver = clientInfo.obtainDriver();
        if(driver == null){
            throw new Exception("浏览器驱动不存在");
        }
        String currentUrl = driver.getCurrentUrl();
        if(!currentUrl.contains(JdConst.URL_CERTIFIED)){
            throw new Exception("用户不在登录页面");
        }
        WebElement element = driver.findElement(By.cssSelector("div.item-input-wrap.item-input-w2"));
        if(element == null){
            throw new Exception("未打开验证窗口");
        }
        WebElement field = element.findElement(By.cssSelector("input.field"));
        field.sendKeys(verification);
        WebElement button = driver.findElement(By.cssSelector("button.btn-primary.btn-m"));
        button.click();
        sleep(2);
        String currentUrl1 = driver.getCurrentUrl();
        log.info("地址：{}", currentUrl1);
        clientInfo.logIn();
    }

    public enum SliderType {
        sliderBJ, sliderHK, top
    }

    /**
     * 获得滑块信息
     * @param driver
     * @return
     * @throws InterruptedException
     */
    public MutiResult<SliderType, Object> getSliderElement(ChromeDriver driver) throws InterruptedException {

        try {
            //9、如果没拿到，说明直接登录了，没有触发出来滑块
            WebElement sliderBJ = driver.findElement(By.className("JDJRV-bigimg")).findElement(By.tagName("img"));
            WebElement smallDiv = driver.findElement(By.className("JDJRV-smallimg"));
            String topStr = smallDiv.getCssValue("top");
            log.info("top:"+topStr);
            Integer top = Double.valueOf(topStr.substring(0, topStr.length() - 2)).intValue();
            WebElement sliderHK = smallDiv.findElement(By.tagName("img"));
            return MutiResult.build(SliderType.class, Object.class)
                    .add(SliderType.sliderBJ, sliderBJ)
                    .add(SliderType.sliderHK, sliderHK)
                    .add(SliderType.top, top)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            Thread.sleep(100L);
            WebElement loginsubmit = driver.findElement(By.id("loginsubmit"));
            loginsubmit.click();
            WebElement sliderBJ = driver.findElement(By.className("JDJRV-bigimg")).findElement(By.tagName("img"));
            WebElement smallDiv = driver.findElement(By.className("JDJRV-smallimg"));
            String topStr = smallDiv.getCssValue("top");
            log.info("top:"+topStr);
            Integer top = Double.valueOf(topStr.substring(0, topStr.length() - 2)).intValue();
            WebElement sliderHK = smallDiv.findElement(By.tagName("img"));
            return MutiResult.build(SliderType.class, Object.class)
                    .add(SliderType.sliderBJ, sliderBJ)
                    .add(SliderType.sliderHK, sliderHK)
                    .add(SliderType.top, top)
                    .build();
        }
    }
    public void loginStr(ChromeDriver driver, String account, String password) {
        log.info("填写登录信息 account:{}, password:{}", account, password);
        WebElement button = driver.findElement(By.cssSelector("div.login-tab.login-tab-r"));
        button.click();
        WebElement loginname = driver.findElement(By.id("loginname"));
        loginname.sendKeys(account);
        WebElement nloginpwd = driver.findElement(By.id("nloginpwd"));
        nloginpwd.sendKeys(password);
        WebElement loginsubmit = driver.findElement(By.id("loginsubmit"));
        loginsubmit.click();
    }

    public double calculateDistance(WebElement sliderBJ, WebElement sliderHK, Integer top) throws IOException {
        String srcBJ = sliderBJ.getAttribute("src");
        String srcHK = sliderHK.getAttribute("src");
        srcBJ = srcBJ.substring(srcBJ.indexOf(",") + 1);
        srcHK = srcHK.substring(srcHK.indexOf(",") + 1);
        //11.4、将图片下载到本地
        File bFile = downloadBase64Picture(srcBJ, "D:\\img\\bigimg.png");
        File sFile = downloadBase64Picture(srcHK, "D:\\img\\smallimg.png");
        BufferedImage sBI = ImageIO.read(sFile);
        BufferedImage bgBI = ImageIO.read(bFile);
        log.info("bgBI:"+bgBI.getWidth()+","+bgBI.getHeight());
        log.info("sBI:"+sBI.getWidth()+","+sBI.getHeight());
        bgBI = bgBI.getSubimage(0, top + 10, bgBI.getWidth(), sBI.getHeight() + 10);
        ImageIO.write(bgBI, "png", new File("D:\\img\\bigimg_sub.png"));
        setWhite(sBI);
        ImageIO.write(sBI, "png", sFile);
        //11.5、从本地读取背景原图
        Mat b_mat = Imgcodecs.imread("D:\\img\\bigimg_sub.png", Imgcodecs.IMREAD_UNCHANGED);
        Mat s_mat = Imgcodecs.imread("D:\\img\\smallimg.png", Imgcodecs.IMREAD_UNCHANGED);
        Imgproc.cvtColor(b_mat, b_mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(s_mat, s_mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(b_mat, b_mat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, -4);
        Imgproc.adaptiveThreshold(s_mat, s_mat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, -4);
        //11.6、创建一个新的背景图，方便做标记
        Mat clone = b_mat.clone();
        //11.7、匹配小图在大图中的位置  用标准模式去比较 然后把返回结果给result
        int result_rows = b_mat.rows() - s_mat.rows() + 1;
        int result_cols = b_mat.cols() - s_mat.cols() + 1;
        Mat g_result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
        Imgproc.matchTemplate(b_mat, s_mat, g_result, Imgproc.TM_CCORR_NORMED);
        Core.normalize(g_result, g_result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        //11.8、获取匹配结果坐标
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(g_result);
        Point matchLocation = minMaxLocResult.maxLoc;
        //11.9、在图上做标记
        Imgproc.rectangle(clone, matchLocation,
                new Point(matchLocation.x + s_mat.cols(), matchLocation.y + s_mat.rows()),
                new Scalar(0, 255, 0, 0));
        Imgcodecs.imwrite("D:\\img\\close.png", clone);
        //11.10、将背景图存储在本地
        Imgcodecs.imwrite("D:\\img\\bigimg_deal.png", b_mat);
        Imgcodecs.imwrite("D:\\img\\smallimg_deal.png", s_mat);
//                double distance = matchLocation.x - matchLocation.y;
        double distance = (matchLocation.x + s_mat.cols() - sBI.getWidth()) * 3 / 4 - 15;
        return distance;
    }

    /**
     * 透明区域变白
     *
     * @param image
     * @throws IOException
     */
    public static void setWhite(BufferedImage image) throws IOException {
        if (image == null) {
            return;
        } else {
            int rgb;
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    rgb = image.getRGB(i, j);
                    int A = (rgb & 0xFF000000) >>> 24;
                    if (A < 100) {
                        image.setRGB(i, j, new Color(255, 255, 255).getRGB());
                    }
                }
            }
        }
    }
    public static void sleep(int time) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /*
     *
     * 下载图片到本地
     * */
    public File downloadBase64Picture(String base64FileStr, String path) {
        try {
//            base64FileStr = base64FileStr.replace("\r\n", "");
            byte[] bytes = Base64.decodeBase64(base64FileStr);
            ByteArrayInputStream dataInputStream = new ByteArrayInputStream(bytes);
            File file = new File(path);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
            return file;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
     *
     * 下载图片到本地
     * */
    public void downloadPicture(String urlList, String path) {
        URL url = null;
        try {
            url = new URL(urlList);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
     *
     * 截图图片到本地
     * */
    public void downloadScreenPicture(WebElement ele, String path) {
        try {
            File screen = ele.getScreenshotAs(OutputType.FILE);
            FileInputStream dataInputStream = new FileInputStream(screen);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 模拟人工移动
     *
     * @param driver
     */
    public void move(WebDriver driver, WebElement element, int distance) throws InterruptedException {
        java.util.List<Integer> track = getTrack(distance);
        int moveY = 0;
        try {
            //初始化鼠标对象
            Actions actions = new Actions(driver);
            //鼠标按住左键不动
            actions.clickAndHold(element).perform();
            Thread.sleep(200);
            PointerInput defaultMouse = new PointerInput(MOUSE, "default mouse");
            for (int i = 0; i < track.size(); i++) {
                //把元素滑动到执行坐标
                actions.tick(defaultMouse.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.pointer(),track.get(i), moveY)).perform();
                //默认200ms卡顿
//                actions.moveByOffset(track.get(i), moveY).perform();
//                Thread.sleep(new Random().nextInt(50) + randomTime);
            }
            Thread.sleep(200);
            actions.release(element).perform();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public java.util.List<Integer> getTrack(int distance) {
        java.util.List<Integer> track = new ArrayList<>();// 移动轨迹
        log.info("allMove:"+ distance);
//        int distance1 = distance *4/5;
//        distance -= distance1;
//        int distance2 = distance + 8;
//        distance -= distance2;
//        int distance3 = distance - 15;
//        distance -= distance3;
//        int distance4 = distance + 10;
//        distance -= distance4;
//        int distance5 = distance - 5;
//        distance -= distance5;
//        getMoveTrack(distance1, track);
//        getMoveTrack(distance2, track);
//        getMoveTrack(distance3, track);
//        getMoveTrack(distance4, track);
//        getMoveTrack(distance5, track);
        getMoveTrack(distance, track);
        log.info(track.toString());
        return track;
    }

    /**
     * 根据距离获取滑动轨迹
     *
     * @param
     * @return
     */
    public static java.util.List<Integer> getMoveTrack2(int distance, java.util.List<Integer> track) {
        log.info("move:"+distance);
        Random random = new Random();
        int current = 0;// 已经移动的距离
        int mid = (int) Math.abs(distance) * 4 / 5;// 减速阈值
        int a = 0;
        int move = 0;// 每次循环移动的距离
        int speed = 0;
        while (true) {
            if(distance > 80){
                speed = 0;
            }else{
                speed = 0;
            }
            a = speed + random.nextInt(10);
            if (Math.abs(current) <= mid) {
                move += a;// 不断加速
            } else {
                move -= a;
            }
            if(distance < 0){
                if ((-current + move) < -distance) {
                    track.add(-move);
                } else {
                    track.add(distance - current);
                    break;
                }
                current -= move;
            }else{
                if ((current + move) < distance) {
                    track.add(move);
                } else {
                    track.add(distance - current);
                    break;
                }
                current += move;
            }
        }
        return track;
    }
    /**
     * 根据距离获取滑动轨迹
     *
     * @param
     * @return
     */
    public static java.util.List<Integer> getMoveTrack(int distance, List<Integer> track) {
        log.info("move:"+distance);
        Random random = new Random();
        double v = 0;
        double t = 0.2;
        double current = 0;// 已经移动的距离
        int mid = (int) Math.abs(distance) * 4 / 5;// 减速阈值
//        distance += 10;
        while (current < distance) {
            int a;
            if(current < mid){
                a = random.nextInt(2) + 1;
            }else{
                a = -random.nextInt(2) - 2;
            }
            double v0 = v;
            double add = 0.5*a*Math.pow(t, 2);
            double s = (v0 * t + add);
            current += (int)s;
//            log.info("size:"+track.size()+"a:"+a+",add:"+add+",s:"+s+",current:"+current+",distance:"+distance);
            track.add((int)s);
            v = (v0 + a * t);
        }
//        for(int i = 0;i<4;i++){
//            track.add(-random.nextInt(3) - 1);
//        }
        return track;
    }

    /**
     * 发送短信
     * @param driver
     */
    public void sendVerificationCode(WebDriver driver){
        //获取验证码按钮
        WebElement button1 = driver.findElement(By.cssSelector("button.btn-def.btn-xl.mb20"));
        button1.click();
        //获取短信验证码 btn-def btn-msg btn-l
        WebElement button2 = driver.findElement(By.cssSelector("button.btn-def.btn-msg.btn-l"));
        button2.click();
        log.info("发送短信");
    }
}
