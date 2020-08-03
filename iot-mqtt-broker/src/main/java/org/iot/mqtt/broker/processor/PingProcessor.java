package org.iot.mqtt.broker.processor;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.config.MqttConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public class PingProcessor implements RequestProcessor {
	private static final Logger log = LoggerFactory.getLogger(PingProcessor.class);

    private MqttConfig mqttConfig;
    
    public PingProcessor(BrokerRoom brokerRoom){
        this.mqttConfig = brokerRoom.getMqttConfig();
    }
	
	
	@Override
	public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
		String clientId = NettyUtil.getClientId(ctx.channel());
		log.debug("[Ping] {} -> clientId:{}",mqttConfig.getServerName(), clientId);
		MqttMessage pingRespMessage = MessageUtil.getPingRespMessage();
		ctx.writeAndFlush(pingRespMessage);
	}
}
