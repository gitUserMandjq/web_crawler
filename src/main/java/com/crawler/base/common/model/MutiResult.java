package com.crawler.base.common.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MutiResult<K extends Enum<K>,V>{
    private Map<K, V> resultMap;

    private MutiResult(Map<K, V> resultMap){
        this.resultMap = resultMap;
    }

    /**
     * 获取返回值
     *
     * @param key
     * @return
     */
    public V get(K key){
        return resultMap.get(key);
    }


    public static <K extends Enum<K>,V> Builder<K, V> build(){
        return new Builder<>();
    }

    /**
     * @param keyClass key的类型
     * @param valueClass value类型
     * @return 建造者对象
     */
    public static <K extends Enum<K>,V> Builder<K, V> build(Class<K> keyClass,Class<V> valueClass){
        return new Builder<>();
    }

    /**
     * 建造者
     *
     * @param <K>
     * @param <V>
     */
    public static final class Builder<K extends Enum<K>,V>{
        private Map<K, V> resultMap;

        Builder(Map<K, V> resultMap){
            this.resultMap = resultMap;
        }

        Builder(){
            this(new ConcurrentHashMap<>());
        }

        /**
         * 生成目标类
         *
         * @return
         */
        public MutiResult<K, V> build(){
            return new MutiResult<>(resultMap);
        }

        /**
         * @param key
         * @param value
         * @return
         * @throws IllegalArgumentException 多次添加同一个key抛出此异常
         */
        public Builder<K, V> add(K key,V value){
            if(resultMap.get(key) != null){
                throw new IllegalArgumentException("重复添加key：" + key);
            }
            resultMap.put(key,value);
            return this;
        }
    }
}