package org.iot.mqtt.broker.exception;

import java.io.Serializable;

public class TooMuchRequestException  extends BrokerException implements Serializable {

    private static final long serialVersionUID = -865546545670505110L;

    public TooMuchRequestException(String message) {
        super(message);
    }

    public TooMuchRequestException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
