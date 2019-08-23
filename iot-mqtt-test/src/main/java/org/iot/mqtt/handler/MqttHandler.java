package org.iot.mqtt.handler;

public interface MqttHandler {
	void processInput(byte[] msg);
	void processInput(String msg);
}
