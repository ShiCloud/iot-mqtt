package org.iot.mqtt.test.support;

import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;

public class MyMqtt extends MQTT{
    public FutureConnection futureConnection(CallbackConnection callbackConnection) {
        return new FutureConnection(callbackConnection);
    }
}
