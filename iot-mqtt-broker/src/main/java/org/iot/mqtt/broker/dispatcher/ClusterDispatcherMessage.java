package org.iot.mqtt.broker.dispatcher;


import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.subscribe.SubscriptionMatcher;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.bean.MessageHeader;
import org.iot.mqtt.common.bean.Subscription;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.common.config.PerformanceConfig;
import org.iot.mqtt.common.utils.RejectHandler;
import org.iot.mqtt.common.utils.ThreadFactoryImpl;
import org.iot.mqtt.store.FlowMessageStore;
import org.iot.mqtt.store.OfflineMessageStore;
import org.iot.mqtt.store.rheakv.RheakvAppendMessageStore;
import org.iot.mqtt.store.rheakv.RheakvMqttStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.mqtt.MqttPublishMessage;


public class ClusterDispatcherMessage implements MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ClusterDispatcherMessage.class);
    private boolean stoped = false;
    private RheakvAppendMessageStore messageQueue;
    private ThreadPoolExecutor pollThread;
    private SubscriptionMatcher subscriptionMatcher;
    private FlowMessageStore flowMessageStore;
    private OfflineMessageStore offlineMessageStore;
    private ConnectManager connectManager;
    private MqttConfig mqttConfig;
    public ClusterDispatcherMessage(BrokerRoom brokerRoom, RheakvMqttStore store) {
        this.subscriptionMatcher = brokerRoom.getSubscriptionMatcher();
        this.flowMessageStore = brokerRoom.getFlowMessageStore();
        this.offlineMessageStore = brokerRoom.getOfflineMessageStore();
        this.connectManager = brokerRoom.getConnectManager();
        this.mqttConfig = brokerRoom.getMqttConfig();
        this.messageQueue = new RheakvAppendMessageStore(mqttConfig,store,brokerRoom.getSnowFlake());
    }

    @Override
    public void start() {
    	PerformanceConfig config = mqttConfig.getPerformanceConfig();
    	int pollThreadNum =config.getCoreThreadNum();
    	
		this.pollThread = new ThreadPoolExecutor(pollThreadNum,pollThreadNum,60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(config.getQueueSize()),
                new ThreadFactoryImpl(mqttConfig.getServerName()+" -> popMessage2Subscriber"),
                new RejectHandler(mqttConfig.getServerName()+"-> popMessage", config.getQueueSize()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stoped) {
                    try {
                        Collection<Message> messageList = messageQueue.pop(config.getPopSize());
                        if (messageList == null || messageList.size() == 0) {
                        	Thread.sleep(config.getPopInterval());
                        	continue;
                        }
                        AsyncDispatcher dispatcher = new AsyncDispatcher(messageList);
                        pollThread.submit(dispatcher).get();
                    } catch (InterruptedException e) {
                        log.warn("[AsyncDispatcher] {} -> poll message wrong.",mqttConfig.getServerName());
                    } catch (ExecutionException e) {
                        log.warn("[AsyncDispatcher] {} -> get() wrong.",mqttConfig.getServerName());
                    }
                }
            }
        }).start();
    }
    @Override
    public boolean appendMessage(Message message) {
        boolean isNotFull = messageQueue.offer(message);
        if (!isNotFull) {
            log.warn("[appendMessage] {} -> the buffer queue is full",mqttConfig.getServerName());
        }
        return isNotFull;
    }

    @Override
    public void shutdown() {
        this.stoped = true;
        this.pollThread.shutdown();
    }

    ;

    class AsyncDispatcher implements Runnable {

        private Collection<Message> messages;

        AsyncDispatcher(Collection<Message> messages) {
            this.messages = messages;
        }

        @Override
        public void run() {
            if (Objects.nonNull(messages)) {
                try {
                	log.debug("[AsyncDispatcher] {} -> message count {}",mqttConfig.getServerName(), messages.size());
                    for (Message message : messages) {
                    	log.debug("[AsyncDispatcher] {} -> message {}",mqttConfig.getServerName(), messages);
                        Set<Subscription> subscriptions = subscriptionMatcher.match((String) message.getHeader(MessageHeader.TOPIC));
                        for (Subscription subscription : subscriptions) {
                            String clientId = subscription.getClientId();
                            if (connectManager.containClient(clientId)) {
                            	ClientSession clientSession = connectManager.getClient(subscription.getClientId());
                                int qos = MessageUtil.getMinQos((int) message.getHeader(MessageHeader.QOS), subscription.getQos());
                                message.putHeader(MessageHeader.QOS, qos);
                                if (qos > 0) {
                                	message.setMsgId(clientSession.generateMessageId());
                                    flowMessageStore.cacheSendMsg(clientId, message);
                                }
                                MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message, false, qos, message.getMsgId());
                                clientSession.getCtx().writeAndFlush(publishMessage);
                            } else {
                                offlineMessageStore.addOfflineMessage(clientId, message);
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("[AsyncDispatcher] {} -> message failure,cause={}",mqttConfig.getServerName(), ex);
                }
            }
        }

    }
}
