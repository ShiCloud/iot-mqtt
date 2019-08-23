package org.iot.mqtt.test.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;


public class SerializeHelper {

    private static final Logger log = LoggerFactory.getLogger(SerializeHelper.class);

    public static <T> byte[] serialize(T obj){
        if(obj instanceof String){
            return ((String) obj).getBytes();
        }
        return JSONObject.toJSONBytes(obj);
    }

    @SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] bytes,Class<T> clazz){
        try{
            if(clazz == String.class){
                return (T) new String(bytes);
            }
            return JSONObject.parseObject(bytes,clazz);
        }catch(Exception ex){
            log.warn("Deserialize failure,cause={}",ex);
        }
        return null;
    }

    public static <T> List<T> deserializeList(byte[] bytes, Class<T> clazz){
        try{
            String json = JSONObject.toJSONString(bytes);
            List<T> result = JSONObject.parseArray(json,clazz);
            return result;
        }catch (Exception ex){
            log.warn("Deserialize failure,cause={}",ex);
        }
        return null;
    }

}
