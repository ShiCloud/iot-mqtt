package org.iot.mqtt.broker.processor;

import java.util.List;
import java.util.Objects;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.subscribe.SubscriptionMatcher;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.SubscriptionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;

public class UnSubscribeProcessor implements RequestProcessor {

    private Logger log = LoggerFactory.getLogger(UnSubscribeProcessor.class);

    private SubscriptionMatcher subscriptionMatcher;
    private SubscriptionStore subscriptionStore;
    private ConnectManager connectManager;
    private MqttConfig mqttConfig;
    
    public UnSubscribeProcessor(BrokerRoom brokerRoom){
        this.subscriptionMatcher = brokerRoom.getSubscriptionMatcher();
        this.subscriptionStore = brokerRoom.getSubscriptionStore();
        this.connectManager = brokerRoom.getConnectManager();
        this.mqttConfig = brokerRoom.getMqttConfig();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttUnsubscribeMessage unsubscribeMessage = (MqttUnsubscribeMessage) mqttMessage;
        MqttUnsubscribePayload unsubscribePayload = unsubscribeMessage.payload();
        List<String> topics = unsubscribePayload.topics();
        String clientId = NettyUtil.getClientId(ctx.channel());
        ClientSession clientSession = connectManager.getClient(clientId);
        if(Objects.isNull(clientSession)){
            log.warn("[UnSubscribe] {} -> The client is not online.clientId={}",
            		mqttConfig.getServerName(),clientId);
        }
        topics.forEach( topic -> {
            subscriptionMatcher.unSubscribe(topic,clientId);
            subscriptionStore.removeSubscription(clientId,topic);
        });
        MqttUnsubAckMessage unsubAckMessage = MessageUtil.getUnSubAckMessage(MessageUtil.getMessageId(mqttMessage));
        ctx.writeAndFlush(unsubAckMessage);
    }
}
