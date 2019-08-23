package org.iot.mqtt.store.memory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.store.RetainMessageStore;

public class DefaultRetainMessageStore implements RetainMessageStore {

    private Map<String/*Topic*/,Message> retainTable = new ConcurrentHashMap<>();

    @Override
    public Collection<Message> getAllRetainMessage() {
        return retainTable.values();
    }

    @Override
    public void storeRetainMessage(String topic, Message message) {
        this.retainTable.put(topic,message);
    }

    @Override
    public void removeRetainMessage(String topic) {
        this.retainTable.remove(topic);
    }
}
