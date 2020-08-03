package org.iot.mqtt.broker.processor;

import java.util.Objects;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.FlowMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public class PubRelProcessor extends AbstractMessageProcessor implements RequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(PubRelProcessor.class);
    private ConnectManager connectManager;
    private MqttConfig mqttConfig;
    private FlowMessageStore flowMessageStore;

    public PubRelProcessor(BrokerRoom brokerRoom) {
        super(brokerRoom.getMessageDispatcher(),brokerRoom.getRetainMessageStore());
        this.flowMessageStore = brokerRoom.getFlowMessageStore();
        this.connectManager = brokerRoom.getConnectManager();
        this.mqttConfig = brokerRoom.getMqttConfig();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = MessageUtil.getMessageId(mqttMessage);
        if(connectManager.containClient(clientId)){
            Message message = flowMessageStore.releaseRecMsg(clientId,messageId);
            if(Objects.nonNull(message)){
                super.processMessage(message);
            }else{
                log.warn("[PubRelMessage] {} -> the message is not exist,clientId={},messageId={}.",
                		mqttConfig.getServerName(),clientId,messageId);
                return;
            }
            MqttMessage pubComMessage = MessageUtil.getPubComMessage(messageId);
            ctx.writeAndFlush(pubComMessage);
        }else{
            log.warn("[PubRelMessage] {} -> the client: {} disconnect to this server.",
            		mqttConfig.getServerName(),clientId);
            NettyUtil.closeChannel(ctx.channel());
        }
    }
}
