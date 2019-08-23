package org.iot.mqtt.broker.exception;

import java.io.Serializable;

public class BrokerException extends Exception implements Serializable {
    private static final long serialVersionUID = -46545454570505110L;

    public BrokerException(String message){
        super(message);
    }

    public BrokerException(String message,Throwable throwable){
        super(message,throwable);
    }

}
