package com.crawler.base.common.pool;

import com.crawler.base.utils.ChromeDriverWapper;
import com.crawler.base.utils.DriverUtils;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DriverPool {
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, ChromeDriverWapper>> masterDriverPool = new ConcurrentHashMap<>();
    /**
     * 新增浏览器驱动
     * @param type
     * @param mobile
     * @param driver
     */
    public static void addDriver(String type, String mobile, ChromeDriverWapper driver){
        ConcurrentHashMap<String, ChromeDriverWapper> map = masterDriverPool.get(type);
        if(map == null){
            map = new ConcurrentHashMap<>();
            masterDriverPool.put(type, map);
        }
        if(map.contains(mobile)){
            DriverUtils.destoryDrive(map.get(mobile));
        }
        map.put(mobile, driver);
    }
    /**
     * 获得浏览器驱动
     * @param type
     * @param mobile
     */
    public static ChromeDriverWapper getDriver(String type, String mobile){
        ConcurrentHashMap<String, ChromeDriverWapper> map = masterDriverPool.get(type);
        if(map == null){
            return null;
        }
        ChromeDriverWapper driver = map.get(mobile);
        return driver;
    }
    /**
     * 获得浏览器驱动
     * @param type
     * @param mobile
     */
    public static ChromeDriverWapper getAndAddDriver(String type, String mobile){
        ConcurrentHashMap<String, ChromeDriverWapper> map = masterDriverPool.get(type);
        if(map == null){
            return null;
        }
        ChromeDriverWapper driver = map.get(mobile);
        return driver;
    }

    /**
     * 销毁浏览器驱动
     * @param type
     * @param mobile
     */
    public static void destoryDriver(String type, String mobile){
        ConcurrentHashMap<String, ChromeDriverWapper> map = masterDriverPool.get(type);
        if(map == null){
            return;
        }
        if(map.contains(mobile)){
            DriverUtils.destoryDrive(map.get(mobile));
            map.remove(mobile);
        }
    }
    /**
     * 销毁所有浏览器驱动
     */
    public static void destoryAllDriver(){
        Iterator<Map.Entry<String, ConcurrentHashMap<String, ChromeDriverWapper>>> iterator = masterDriverPool.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, ConcurrentHashMap<String, ChromeDriverWapper>> next = iterator.next();
            String type = next.getKey();
            ConcurrentHashMap<String, ChromeDriverWapper> map = next.getValue();
            Iterator<Map.Entry<String, ChromeDriverWapper>> iterator1 = map.entrySet().iterator();
            while(iterator1.hasNext()){
                Map.Entry<String, ChromeDriverWapper> next1 = iterator1.next();
                DriverUtils.destoryDrive(next1.getValue());
                iterator1.remove();
            }
            iterator.remove();
        }
    }
}
