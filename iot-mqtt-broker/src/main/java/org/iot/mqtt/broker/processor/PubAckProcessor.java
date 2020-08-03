package org.iot.mqtt.broker.processor;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.FlowMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public class PubAckProcessor implements RequestProcessor {

    private Logger log = LoggerFactory.getLogger(PubAckProcessor.class);
    private FlowMessageStore flowMessageStore;
    private MqttConfig mqttConfig;
    
    public PubAckProcessor(BrokerRoom brokerRoom){
        this.flowMessageStore = brokerRoom.getFlowMessageStore();
        this.mqttConfig = brokerRoom.getMqttConfig();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = MessageUtil.getMessageId(mqttMessage);
        log.debug("[PubAck] {} -> Recieve PubAck message,clientId={},msgId={}",
        		mqttConfig.getServerName(),clientId,messageId);
        if(!flowMessageStore.containSendMsg(clientId,messageId)){
            log.warn("[PubAck] {} -> The message is not cached in Flow,clientId={},msgId={}",
            		mqttConfig.getServerName(),clientId,messageId);
            return;
        }
        flowMessageStore.releaseSendMsg(clientId,messageId);
        log.debug("[PubAck] {} -> release message,clientId={},msgId={}",
        		mqttConfig.getServerName(),clientId,messageId);
    }
}
