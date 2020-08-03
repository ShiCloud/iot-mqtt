/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.iot.mqtt.common.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class SerializeHelper {

    private static final Logger log = LoggerFactory.getLogger(SerializeHelper.class);
    private static ObjectMapper mapper = new ObjectMapper();
    
    public static <T> byte[] serialize(T obj){
    	if(obj == null) {
    		return null;
    	}
        if(obj instanceof String){
            return ((String) obj).getBytes();
        }
        try {
			return mapper.writeValueAsBytes(obj);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
        return null;
    }

    @SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] bytes,Class<T> clazz){
    	if(bytes == null) {
    		return null;
    	}
        try{
            if(clazz == String.class){
                return (T) new String(bytes);
            }
            return mapper.readValue(bytes,clazz);
        }catch(Exception ex){
            log.warn("Deserialize failure,cause={}",ex);
        }
        return null;
    }

    public static <T> List<T> deserializeList(byte[] bytes, Class<T> clazz){
    	JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        try {
        	return mapper.readValue(bytes, javaType);
        } catch (Exception e) {
        	log.error(e.getMessage());
        }
        return null;
    }
}
