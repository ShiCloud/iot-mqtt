package org.iot.mqtt.store;

import java.util.Collection;

import org.iot.mqtt.common.bean.Message;

/**
 * cleansession message
 */
public interface OfflineMessageStore {

    void clearOfflineMsgCache(String clientId);

    boolean containOfflineMsg(String clientId);

    boolean addOfflineMessage(String clientId, Message message);

    Collection<Message> getAllOfflineMessage(String clientId);

	int getAllOfflineMessageCount(String clientId);

	Collection<Message> getOfflineMessage(String clientId, int nums);


}
