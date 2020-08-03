package org.iot.mqtt.broker.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.acl.PubSubPermission;
import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.subscribe.SubscriptionMatcher;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.bean.MessageHeader;
import org.iot.mqtt.common.bean.Subscription;
import org.iot.mqtt.common.bean.Topic;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.FlowMessageStore;
import org.iot.mqtt.store.RetainMessageStore;
import org.iot.mqtt.store.SubscriptionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;

public class SubscribeProcessor implements RequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(SubscribeProcessor.class);

    private SubscriptionMatcher subscriptionMatcher;
    private RetainMessageStore retainMessageStore;
    private FlowMessageStore flowMessageStore;
    private SubscriptionStore subscriptionStore;
    private PubSubPermission pubSubPermission;
    private ConnectManager connectManager;
    private MqttConfig mqttConfig;
    
    public SubscribeProcessor(BrokerRoom brokerRoom){
        this.subscriptionMatcher = brokerRoom.getSubscriptionMatcher();
        this.retainMessageStore = brokerRoom.getRetainMessageStore();
        this.flowMessageStore = brokerRoom.getFlowMessageStore();
        this.subscriptionStore = brokerRoom.getSubscriptionStore();
        this.pubSubPermission = brokerRoom.getPubSubPermission();
        this.connectManager = brokerRoom.getConnectManager();
        this.mqttConfig = brokerRoom.getMqttConfig();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage) mqttMessage;
        String clientId = NettyUtil.getClientId(ctx.channel());
        int messageId = subscribeMessage.variableHeader().messageId();
        ClientSession clientSession = connectManager.getClient(clientId);
        List<Topic> validTopicList =validTopics(clientSession,subscribeMessage.payload().topicSubscriptions());
        if(validTopicList == null || validTopicList.size() == 0){
            log.warn("[Subscribe] {} -> Valid all subscribe topic failure,clientId:{}",
            		mqttConfig.getServerName(),clientId);
            return;
        }
        List<Integer> ackQos = getTopicQos(validTopicList);
        MqttMessage subAckMessage = MessageUtil.getSubAckMessage(messageId,ackQos);
        ctx.writeAndFlush(subAckMessage);
        // send retain messages
        List<Message> retainMessages = subscribe(clientSession,validTopicList);
        dispatcherRetainMessage(clientSession,retainMessages);
    }

    private List<Integer> getTopicQos(List<Topic> topics){
        List<Integer> qoss = new ArrayList<>(topics.size());
        for(Topic topic : topics){
            qoss.add(topic.getQos());
        }
        return qoss;
    }

    private List<Message> subscribe(ClientSession clientSession,List<Topic> validTopicList){
        Collection<Message> retainMessages = null;
        List<Message> needDispatcher = new ArrayList<>();
        for(Topic topic : validTopicList){
            Subscription subscription = new Subscription(clientSession.getClientId(),topic.getTopicName(),topic.getQos());
            boolean subRs = this.subscriptionMatcher.subscribe(subscription);
            if(subRs){
                if(retainMessages == null){
                    retainMessages = retainMessageStore.getAllRetainMessage();
                }
                for(Message retainMsg : retainMessages){
                    String pubTopic = (String) retainMsg.getHeader(MessageHeader.TOPIC);
                    if(subscriptionMatcher.isMatch(pubTopic,subscription.getTopic())){
                        int minQos = MessageUtil.getMinQos((int)retainMsg.getHeader(MessageHeader.QOS),topic.getQos());
                        retainMsg.putHeader(MessageHeader.QOS,minQos);
                        needDispatcher.add(retainMsg);
                    }
                }
                this.subscriptionStore.storeSubscription(clientSession.getClientId(),subscription);
            }
        }
        retainMessages = null;
        return needDispatcher;
    }

    /**
     * 返回校验合法的topic
     */
    private List<Topic> validTopics(ClientSession clientSession,List<MqttTopicSubscription> topics){
        List<Topic> topicList = new ArrayList<>();
        for(MqttTopicSubscription subscription : topics){
            if(!pubSubPermission.subscribeVerfy(clientSession.getClientId(),subscription.topicName())){
                log.warn("[SubPermission] {} -> this clientId:{} have no permission to subscribe this topic:{}",
                		mqttConfig.getServerName(),clientSession.getClientId(),subscription.topicName());
                clientSession.getCtx().close();
                return null;
            }
            Topic topic = new Topic(subscription.topicName(),subscription.qualityOfService().value());
            topicList.add(topic);
        }
        return topicList;
    }

    private void dispatcherRetainMessage(ClientSession clientSession,List<Message> messages){
        for(Message message : messages){
            message.putHeader(MessageHeader.RETAIN,true);
            int qos = (int) message.getHeader(MessageHeader.QOS);
            if(qos > 0){
                flowMessageStore.cacheSendMsg(clientSession.getClientId(),message);
            }
            MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message,false,qos,clientSession.generateMessageId());
            clientSession.getCtx().writeAndFlush(publishMessage);
        }
    }

}
