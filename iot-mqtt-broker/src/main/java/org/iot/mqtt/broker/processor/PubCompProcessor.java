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

public class PubCompProcessor implements RequestProcessor {

    private Logger log = LoggerFactory.getLogger(PubCompProcessor.class);
    private MqttConfig mqttConfig;
	private FlowMessageStore flowMessageStore;
	
	public PubCompProcessor(BrokerRoom brokerRoom){
        this.flowMessageStore = brokerRoom.getFlowMessageStore();
        this.mqttConfig = brokerRoom.getMqttConfig();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = MessageUtil.getMessageId(mqttMessage);
        boolean isContain = flowMessageStore.releaseSendMsg(clientId,messageId);
        log.debug("[PubComp] {} -> Recieve PubCom and remove the flow message,clientId={},msgId={}",
        		mqttConfig.getServerName(),clientId,messageId);
        if(!isContain){
            log.warn("[PubComp] {} -> The message is not in Flow cache,clientId={},msgId={}",
            		mqttConfig.getServerName(),clientId,messageId);
        }
    }
}
