package org.iot.mqtt.broker.processor;

import java.util.HashMap;
import java.util.Map;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.acl.PubSubPermission;
import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.bean.MessageHeader;
import org.iot.mqtt.store.FlowMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.ReferenceCountUtil;

public class PublishProcessor extends AbstractMessageProcessor implements RequestProcessor {
	
    private Logger log = LoggerFactory.getLogger(PublishProcessor.class);

    private FlowMessageStore flowMessageStore;

    private PubSubPermission pubSubPermission;

    public PublishProcessor(BrokerRoom brokerRoom){
        super(brokerRoom.getMessageDispatcher(),brokerRoom.getRetainMessageStore());
        this.flowMessageStore = brokerRoom.getFlowMessageStore();
        this.pubSubPermission = brokerRoom.getPubSubPermission();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        try{
            MqttPublishMessage publishMessage = (MqttPublishMessage) mqttMessage;
            MqttQoS qos = publishMessage.fixedHeader().qosLevel();
            Message innerMsg = new Message();
            String clientId = NettyUtil.getClientId(ctx.channel());
            ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
            String topic = publishMessage.variableHeader().topicName();
            if(!this.pubSubPermission.publishVerfy(clientId,topic)){
                log.warn("[PubMessage] permission is not allowed");
                clientSession.getCtx().close();
                return;
            }
            innerMsg.setPayload(MessageUtil.readBytesFromByteBuf(((MqttPublishMessage) mqttMessage).payload()));
            innerMsg.setClientId(clientId);
            innerMsg.setType(Message.Type.valueOf(mqttMessage.fixedHeader().messageType().value()));
            Map<String,Object> headers = new HashMap<>();
            headers.put(MessageHeader.TOPIC,publishMessage.variableHeader().topicName());
            headers.put(MessageHeader.QOS,publishMessage.fixedHeader().qosLevel().value());
            headers.put(MessageHeader.RETAIN,publishMessage.fixedHeader().isRetain());
            headers.put(MessageHeader.DUP,publishMessage.fixedHeader().isDup());
            innerMsg.setHeaders(headers);
            innerMsg.setMsgId(publishMessage.variableHeader().packetId());
            switch (qos){
                case AT_MOST_ONCE:
                    processMessage(innerMsg);
                    break;
                case AT_LEAST_ONCE:
                    processQos1(ctx,innerMsg);
                    break;
                case EXACTLY_ONCE:
                    processQos2(ctx,innerMsg);
                    break;
                default:
                    log.warn("[PubMessage] -> Wrong mqtt message,clientId={}", clientId);
            }
            //增加发送数量
            clientSession.addSendIdCounter();//发送方发送一条
        }catch (Throwable tr){
            log.warn("[PubMessage] -> Solve mqtt pub message exception:{}",tr);
        }finally {
            ReferenceCountUtil.release(mqttMessage.payload());
        }
    }

    private void processQos2(ChannelHandlerContext ctx,Message innerMsg){
        log.debug("[PubMessage] -> Process qos2 message,clientId={}",innerMsg.getClientId());
        boolean flag = flowMessageStore.cacheRecMsg(innerMsg.getClientId(),innerMsg);
        if(!flag){
            log.warn("[PubMessage] -> cache qos2 pub message failure,clientId={}",innerMsg.getClientId());
        }
        MqttMessage pubRecMessage = MessageUtil.getPubRecMessage(innerMsg.getMsgId());
        ctx.writeAndFlush(pubRecMessage);
    }

    private void processQos1(ChannelHandlerContext ctx,Message innerMsg){
        processMessage(innerMsg);
        log.debug("[PubMessage] -> Process qos1 message,clientId={}",innerMsg.getClientId());
        MqttPubAckMessage pubAckMessage = MessageUtil.getPubAckMessage(innerMsg.getMsgId());
        ctx.writeAndFlush(pubAckMessage);
    }

}
