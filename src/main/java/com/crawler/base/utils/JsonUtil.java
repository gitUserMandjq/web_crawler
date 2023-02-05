package com.crawler.base.utils;


import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.cglib.beans.BeanMap;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

public class JsonUtil {

    public static ObjectMapper om = new ObjectMapper();
    static {
    	om.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;  
    	om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    /** 
     * json化
      * 
     * @param data
     * @return
     * @throws IOException
      */
    public static String object2String(Object data) throws IOException {
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().setFailOnUnknownId(false);
        om.setFilters(filterProvider);
        return om.writeValueAsString(data);
    }

//    public static List<LinkedHashMap<String, String>> string2ListMap(String json) throws IOException {
//        return om.readValue(json, new TypeReference<List<LinkedHashMap<String, String>>> () {
//        });
//    }
    public static <T> List<T> string2ListMap(String json) throws IOException {
        return om.readValue(json, new TypeReference<List<T>> () {
        });
    }
    public static <T> List<T> string2ListObject(String json,Class<T> clazz) throws IOException {
        JavaType type = getCollectionType(ArrayList.class, clazz);
        return om.readValue(json, type);
    }
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {   
    	return om.getTypeFactory().constructParametricType(collectionClass, elementClasses);   
    }   
    public static JavaType getMapCollectionType( Class<?> elementClasses) {   
        return om.getTypeFactory().constructParametricType(HashMap.class, String.class, elementClasses);   
    }
    public static <T>  Map<String,T> string2Map(String json,Class<T> clazz) throws IOException {
        JavaType type = getMapCollectionType(clazz);
        return om.readValue(json, type);
    }
    public static <T> T string2Obj(String json, Class<T> clazz) throws IOException {
    	if(json.indexOf("{") < 0) {
    		json = URLDecoder.decode(json, "utf-8");
    	}
        return om.readValue(json, clazz);
    }
    public static <T> T string2Obj(String json) throws IOException {
    	if(json.indexOf("{") < 0) {
    		json = URLDecoder.decode(json, "utf-8");
    	}
        return om.readValue(json, new TypeReference<T>() {
        });
    }

    public static List<String> string2ListString(String json) throws IOException {
        return om.readValue(json, new TypeReference<List<String>>() {
        });
    }

    public static List<String> getListStrFromJson(String json, String key) throws IOException {
        if (json.contains(key)) {
            List<LinkedHashMap<String, String>> temp = string2ListMap(json);
            List<String> ids = new ArrayList<String> ();
            for (LinkedHashMap< String, String > t : temp) {
                ids.add(t.get(key));
            }
            return ids;
        } else {
            return string2ListString(json);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   
    


    public static <V,T> V string2CollectionObject(String json,Class<V> collectionClass, Class<T> elementClasses) throws IOException {
        JavaType type = getCollectionType(collectionClass, elementClasses);
        return om.readValue(json, type);
    }
    

    public static List<String> getListStrFromJson(String json) throws IOException {
        if (json.contains("id")) {
            List<LinkedHashMap<String, String>> temp = string2ListMap(json);
            List<String> ids = new ArrayList<String> ();
            for (LinkedHashMap< String, String > t : temp) {
                ids.add(t.get("id"));
            }
            return ids;
        } else {
            return string2ListString(json);
        }
    }
    /** 
     * 将map装换为javabean对象 
     * @param map 
     * @param bean 
     * @return 
     */  
    public static <T> T mapToBean(Map<String, Object> map,T bean) {
        BeanMap beanMap = BeanMap.create(bean);
        beanMap.putAll(map);  
        return bean;  
    }
}
