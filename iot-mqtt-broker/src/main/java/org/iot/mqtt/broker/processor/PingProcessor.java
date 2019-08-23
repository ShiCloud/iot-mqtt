package org.iot.mqtt.broker.processor;

import org.iot.mqtt.broker.utils.MessageUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public class PingProcessor implements RequestProcessor {

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttMessage pingRespMessage = MessageUtil.getPingRespMessage();
        ctx.writeAndFlush(pingRespMessage);
    }
}
