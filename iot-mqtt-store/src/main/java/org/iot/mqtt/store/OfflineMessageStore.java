package org.iot.mqtt.store;

import java.util.Collection;

import org.iot.mqtt.common.bean.Message;

/**
 * cleansession message
 */
public interface OfflineMessageStore extends ResendMessageStore {

    void clearOfflineMsgCache(String clientId);

    boolean addOfflineMessage(String clientId, Message message);

	Collection<Message> getOfflineMessage(String clientId, int nums);
}
