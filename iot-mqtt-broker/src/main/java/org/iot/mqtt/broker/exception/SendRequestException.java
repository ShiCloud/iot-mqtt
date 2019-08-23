package org.iot.mqtt.broker.exception;

import java.io.Serializable;

public class SendRequestException extends BrokerException implements Serializable {

	private static final long serialVersionUID = 461889873713405796L;

	public SendRequestException(String message) {
        this(message,null);
    }

    public SendRequestException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
