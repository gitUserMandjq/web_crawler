package com.crawler;

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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

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
            driver.get("https://passport.jd.com/new/login.aspx");
            //找到输入框
            {
                WebElement button = driver.findElement(By.cssSelector("div.login-tab.login-tab-r"));
                button.click();
                WebElement loginname = driver.findElement(By.id("loginname"));
                loginname.sendKeys("15558113097");
                WebElement nloginpwd = driver.findElement(By.id("nloginpwd"));
                nloginpwd.sendKeys("123456");
                WebElement loginsubmit = driver.findElement(By.id("loginsubmit"));
                loginsubmit.click();
            }
            //7、这个时候可能会弹出来滑块
            Boolean flag = true;
            int i = 1;
            do {
                System.out.println("第" + i++ + "次进行验证");
                //8、这时候可能有滑块出现yidun_slider
                WebElement sliderBJ = null;
                WebElement sliderHK = null;
                Integer top = 0;
                try {
                    //9、如果没拿到，说明直接登录了，没有触发出来滑块
                    sliderBJ = driver.findElement(By.className("JDJRV-bigimg")).findElement(By.tagName("img"));
                    WebElement smallDiv = driver.findElement(By.className("JDJRV-smallimg"));
                    String topStr = smallDiv.getCssValue("top");
                    System.out.println("top:"+topStr);
                    top = Double.valueOf(topStr.substring(0, topStr.length() - 2)).intValue();
                    sliderHK = smallDiv.findElement(By.tagName("img"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Thread.sleep(100L);
                    WebElement loginsubmit = driver.findElement(By.id("loginsubmit"));
                    loginsubmit.click();
                    sliderBJ = driver.findElement(By.className("JDJRV-bigimg")).findElement(By.tagName("img"));
                    WebElement smallDiv = driver.findElement(By.className("JDJRV-smallimg"));
                    String topStr = smallDiv.getCssValue("top");
                    System.out.println("top:"+topStr);
                    top = Double.valueOf(topStr.substring(0, topStr.length() - 2)).intValue();
                    sliderHK = smallDiv.findElement(By.tagName("img"));
                }
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
                        driver.findElement(By.className("JDJRV-bigimg")).findElement(By.tagName("img"));
    //                    flag = false;
                    } catch (Exception e) {
                        flag = false;
                        System.out.println("验证成功");
                    }
                }
            } while (flag);
            System.out.println("循环结束，验证成功");
        } finally {
            sleep(10000);
    //        driver.quit();
        }
    }

    private static double calculateDistance(WebElement sliderBJ, WebElement sliderHK, Integer top) throws IOException {
        String srcBJ = sliderBJ.getAttribute("src");
        String srcHK = sliderHK.getAttribute("src");
        srcBJ = srcBJ.substring(srcBJ.indexOf(",") + 1);
        srcHK = srcHK.substring(srcHK.indexOf(",") + 1);
        //11.4、将图片下载到本地
        File bFile = downloadBase64Picture(srcBJ, "D:\\img\\bigimg.png");
        File sFile = downloadBase64Picture(srcHK, "D:\\img\\smallimg.png");
        BufferedImage sBI = ImageIO.read(sFile);
        BufferedImage bgBI = ImageIO.read(bFile);
        System.out.println("bgBI:"+bgBI.getWidth()+","+bgBI.getHeight());
        System.out.println("sBI:"+sBI.getWidth()+","+sBI.getHeight());
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
    private static File downloadBase64Picture(String base64FileStr, String path) {
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
    private static void downloadPicture(String urlList, String path) {
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
    private static void downloadScreenPicture(WebElement ele, String path) {
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
    public static void move(WebDriver driver, WebElement element, int distance) throws InterruptedException {
        int randomTime = 0;
        if (distance > 90) {
            randomTime = 100;
        } else if (distance > 80 && distance <= 90) {
            randomTime = 50;
        }
        List<Integer> track = getTrack(distance);
        int moveY = 0;
        try {
            //初始化鼠标对象
            Actions actions = new Actions(driver);
            //鼠标按住左键不动
            actions.clickAndHold(element).perform();
            Thread.sleep(200);
            for (int i = 0; i < track.size(); i++) {
                //把元素滑动到执行坐标
                actions.moveByOffset(track.get(i), moveY).perform();
                Thread.sleep(new Random().nextInt(50) + randomTime);
            }
            Thread.sleep(200);
            actions.release(element).perform();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private static List<Integer> getTrack(int distance) {
        List<Integer> track = new ArrayList<>();// 移动轨迹
        System.out.println("allMove:"+ distance);
        int distance1 = distance *4/5;
        distance -= distance1;
        int distance2 = distance + 8;
        distance -= distance2;
        int distance3 = distance - 15;
        distance -= distance3;
        int distance4 = distance + 10;
        distance -= distance4;
        int distance5 = distance - 5;
        distance -= distance5;
        getMoveTrack(distance1, track);
        getMoveTrack(distance2, track);
        getMoveTrack(distance3, track);
        getMoveTrack(distance4, track);
        getMoveTrack(distance5, track);
        getMoveTrack(distance - 2, track);
        System.out.println(track.toString());
        return track;
    }

    /**
     * 根据距离获取滑动轨迹
     *
     * @param
     * @return
     */
    public static List<Integer> getMoveTrack(int distance, List<Integer> track) {
        System.out.println("move:"+distance);
        Random random = new Random();
        int current = 0;// 已经移动的距离
        int mid = (int) Math.abs(distance) * 4 / 5;// 减速阈值
        int a = 0;
        int move = 0;// 每次循环移动的距离
        int speed = 0;
        while (true) {
            if(distance > 80){
                speed = 20;
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
}