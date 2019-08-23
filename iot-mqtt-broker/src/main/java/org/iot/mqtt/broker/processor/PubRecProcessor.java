package org.iot.mqtt.broker.processor;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.store.FlowMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public class PubRecProcessor implements RequestProcessor {

    private Logger log = LoggerFactory.getLogger(PubRecProcessor.class);
    
    private FlowMessageStore flowMessageStore;

    public PubRecProcessor(BrokerRoom brokerRoom){
        this.flowMessageStore = brokerRoom.getFlowMessageStore();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = MessageUtil.getMessageId(mqttMessage);
        log.debug("[PubRec] -> Recieve PubRec message,clientId={},msgId={}",clientId,messageId);
        if(!flowMessageStore.containSendMsg(clientId,messageId)){
            log.warn("[PubRec] -> The message is not cached in Flow,clientId={},msgId={}",clientId,messageId);
        }
        MqttMessage pubRelMessage = MessageUtil.getPubRelMessage(messageId);
        ctx.writeAndFlush(pubRelMessage);
    }
}
