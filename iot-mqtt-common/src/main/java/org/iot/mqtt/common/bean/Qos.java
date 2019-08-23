package org.iot.mqtt.common.bean;

public interface Qos {
	int AT_MOST_ONCE = 0;
	int AT_LEAST_ONCE = 1;
	int EXACTLY_ONCE = 2;
}
