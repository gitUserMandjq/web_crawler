package com.crawler.account.pool;


import com.crawler.account.model.CrawlerClientInfo;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlerClientPool {
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, CrawlerClientInfo>> masterPool = new ConcurrentHashMap<>();
    /**
     * 新增浏览器驱动
     * @param type
     * @param mobile
     * @param clientInfo
     */
    public static void addAccount(String type, String mobile, CrawlerClientInfo clientInfo){
        ConcurrentHashMap<String, CrawlerClientInfo> map = masterPool.get(type);
        if(map == null){
            map = new ConcurrentHashMap<>();
            masterPool.put(type, map);
        }
        map.put(mobile, clientInfo);
    }
    /**
     * 获得浏览器驱动
     * @param type
     * @param mobile
     */
    public static CrawlerClientInfo getAccount(String type, String mobile){
        ConcurrentHashMap<String, CrawlerClientInfo> map = masterPool.get(type);
        if(map == null){
            return null;
        }
        CrawlerClientInfo client = map.get(mobile);
        return client;
    }
    /**
     * 获得浏览器驱动
     * @param type
     * @param mobile
     */
    public static CrawlerClientInfo getAndAddAccount(String type, String mobile){
        ConcurrentHashMap<String, CrawlerClientInfo> map = masterPool.get(type);
        if(map == null){
            map = new ConcurrentHashMap<>();
            masterPool.put(type, map);
        }
        CrawlerClientInfo client = map.get(mobile);
        if(client == null){
            client = new CrawlerClientInfo();
            client.setType(type);
            client.setAccount(mobile);
            map.put(mobile, client);
        }
        return client;
    }

    /**
     * 销毁浏览器驱动
     * @param type
     * @param mobile
     */
    public static void destoryAccount(String type, String mobile){
        ConcurrentHashMap<String, CrawlerClientInfo> map = masterPool.get(type);
        if(map == null){
            return;
        }
        if(map.contains(mobile)){
            CrawlerClientInfo d = map.get(mobile);
            map.remove(mobile);
        }
    }
    /**
     * 销毁所有浏览器驱动
     */
    public static void destoryAllAccount(){
        Iterator<Map.Entry<String, ConcurrentHashMap<String, CrawlerClientInfo>>> iterator = masterPool.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, ConcurrentHashMap<String, CrawlerClientInfo>> next = iterator.next();
            String type = next.getKey();
            ConcurrentHashMap<String, CrawlerClientInfo> map = next.getValue();
            Iterator<Map.Entry<String, CrawlerClientInfo>> iterator1 = map.entrySet().iterator();
            while(iterator1.hasNext()){
                Map.Entry<String, CrawlerClientInfo> next1 = iterator1.next();
                CrawlerClientInfo d = next1.getValue();
                iterator1.remove();
            }
            iterator.remove();
        }
    }
}
