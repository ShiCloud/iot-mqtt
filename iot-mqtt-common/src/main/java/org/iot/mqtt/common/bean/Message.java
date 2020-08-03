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
package org.iot.mqtt.common.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * inner message transfer from MqttMessage
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class Message {

    private int msgId;
    
    private Map<String,Object> headers;

    private String clientId;

    private Type type;

    private byte[] payload;

    public Message(){};

    public Message(Type type,Map<String,Object> headers,byte[] payload){
        this.type = type;
        this.headers = headers;
        this.payload = payload;
    }

    public Object putHeader(String key,Object value){
        if(headers == null){
            headers = new HashMap<>();
        }
        return headers.put(key,value);
    };


    public Object removeHeader(String key){
        return headers.remove(key);
    }

    public Object getHeader(String key){
        return headers.get(key);
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    

	/**
     * mqtt message type
     */
    public enum Type{
        CONNECT(1),
        CONNACK(2),
        PUBLISH(3),
        PUBACK(4),
        PUBREC(5),
        PUBREL(6),
        PUBCOMP(7),
        SUBSCRIBE(8),
        SUBACK(9),
        UNSUBSCRIBE(10),
        UNSUBACK(11),
        PINGREQ(12),
        PINGRESP(13),
        DISCONNECT(14),
        WILL(15);

        private int value;

        private Type(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public static Type valueOf(int type) {
            Type[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                Type t = var1[var3];
                if (t.value == type) {
                    return t;
                }
            }
            throw new IllegalArgumentException("unknown message type: " + type);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return msgId == message.msgId &&
                Objects.equals(headers, message.headers) &&
                Objects.equals(clientId, message.clientId) &&
                type == message.type &&
                Objects.equals(payload, message.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgId, headers, clientId, type, payload);
    }

    @Override
    public String toString() {
        return "Message{" +
                "msgId=" + msgId +
                ", headers=" + headers +
                ", clientId=" + clientId +
                ", type=" + type +
                ", payload=" + payload +
                '}';
    }
}
