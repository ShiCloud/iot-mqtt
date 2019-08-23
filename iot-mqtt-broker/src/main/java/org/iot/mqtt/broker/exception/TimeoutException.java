package org.iot.mqtt.broker.exception;

import java.io.Serializable;

public class TimeoutException extends BrokerException implements Serializable {

    private static final long serialVersionUID = -56656565470505110L;

    public TimeoutException(long timeoutMillis) {
        this(timeoutMillis,null);
    }

    public TimeoutException(long timeoutMillis, Throwable throwable) {
        super("Send timeout time: <" + timeoutMillis + ">", throwable);
    }
}
