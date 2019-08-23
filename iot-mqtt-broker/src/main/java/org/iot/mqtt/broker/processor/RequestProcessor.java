package org.iot.mqtt.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public interface RequestProcessor {

    /**
     * handle mqtt message processor
     */
    void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage);
}
