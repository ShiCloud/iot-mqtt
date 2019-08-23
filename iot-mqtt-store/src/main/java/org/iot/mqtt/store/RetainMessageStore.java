package org.iot.mqtt.store;

import java.util.Collection;

import org.iot.mqtt.common.bean.Message;


public interface RetainMessageStore {

    /**
     * 获取�?有的retain消息
     * @return
     */
    Collection<Message> getAllRetainMessage();

    /**
     * 存储retain消息
     * @param topic
     * @param message
     */
    void storeRetainMessage(String topic,Message message);

    /**
     * 移除retain消息
     * @param topic
     */
    void removeRetainMessage(String topic);
}
