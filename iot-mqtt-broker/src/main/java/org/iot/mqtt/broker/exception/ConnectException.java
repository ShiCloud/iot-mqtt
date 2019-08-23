package org.iot.mqtt.broker.exception;

import java.io.Serializable;

public class ConnectException extends BrokerException implements Serializable {

    private static final long serialVersionUID = -412312312370505110L;

    public ConnectException(String addr) {
        this(addr,null);
    }

    public ConnectException(String addr, Throwable throwable) {
        super("connect to <" + addr + "> failed", throwable);
    }
}
