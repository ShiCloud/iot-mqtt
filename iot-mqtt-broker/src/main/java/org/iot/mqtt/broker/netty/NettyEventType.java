package org.iot.mqtt.broker.netty;

public enum NettyEventType {

	/**
	 * channel connect
	 */
	CONNECT,
	/**
	 * channel close
	 */
	CLOSE,
	/**
	 * channel exception
	 */
	EXCEPTION,
	/**
	 * channel heart beat over time
	 */
	IDLE

}
