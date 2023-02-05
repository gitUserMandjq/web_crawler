package com.crawler.base.common.pool;

import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DriverPool {
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, ChromeDriver>> masterDriverPool = new ConcurrentHashMap<>();
    public static final String TYPE_JD = "jd";

    /**
     * 新增浏览器驱动
     * @param type
     * @param mobile
     * @param driver
     */
    public static void addDriver(String type, String mobile, ChromeDriver driver){
        ConcurrentHashMap<String, ChromeDriver> map = masterDriverPool.get(type);
        if(map == null){
            map = new ConcurrentHashMap<>();
            masterDriverPool.put(type, map);
        }
        if(map.contains(mobile)){
            ChromeDriver d = map.get(mobile);
            d.quit();
            d.close();
        }
        map.put(mobile, driver);
    }
    /**
     * 获得浏览器驱动
     * @param type
     * @param mobile
     */
    public static ChromeDriver getDriver(String type, String mobile){
        ConcurrentHashMap<String, ChromeDriver> map = masterDriverPool.get(type);
        if(map == null){
            return null;
        }
        ChromeDriver driver = map.get(mobile);
        return driver;
    }
    /**
     * 获得浏览器驱动
     * @param type
     * @param mobile
     */
    public static ChromeDriver getAndAddDriver(String type, String mobile){
        ConcurrentHashMap<String, ChromeDriver> map = masterDriverPool.get(type);
        if(map == null){
            return null;
        }
        ChromeDriver driver = map.get(mobile);
        return driver;
    }

    /**
     * 销毁浏览器驱动
     * @param type
     * @param mobile
     */
    public static void destoryDriver(String type, String mobile){
        ConcurrentHashMap<String, ChromeDriver> map = masterDriverPool.get(type);
        if(map == null){
            return;
        }
        if(map.contains(mobile)){
            ChromeDriver d = map.get(mobile);
            d.quit();
            d.close();
            map.remove(mobile);
        }
    }
    /**
     * 销毁所有浏览器驱动
     */
    public static void destoryAllDriver(){
        Iterator<Map.Entry<String, ConcurrentHashMap<String, ChromeDriver>>> iterator = masterDriverPool.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, ConcurrentHashMap<String, ChromeDriver>> next = iterator.next();
            String type = next.getKey();
            ConcurrentHashMap<String, ChromeDriver> map = next.getValue();
            Iterator<Map.Entry<String, ChromeDriver>> iterator1 = map.entrySet().iterator();
            while(iterator1.hasNext()){
                Map.Entry<String, ChromeDriver> next1 = iterator1.next();
                ChromeDriver d = next1.getValue();
                d.quit();
                d.close();
                iterator1.remove();
            }
            iterator.remove();
        }
    }
}
