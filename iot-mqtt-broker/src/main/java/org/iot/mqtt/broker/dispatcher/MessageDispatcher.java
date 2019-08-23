package org.iot.mqtt.broker.dispatcher;

import org.iot.mqtt.common.bean.Message;

public interface MessageDispatcher {

    void start();

    void shutdown();

    boolean appendMessage(Message message);

}
