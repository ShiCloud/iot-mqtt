package org.iot.mqtt.broker.dispatcher;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.mqtt.MqttPublishMessage;


public class DefaultDispatcherMessage implements MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(DefaultDispatcherMessage.class);
    private boolean stoped = false;
    private static final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(100000);
    private ThreadPoolExecutor pollThread;
    private SubscriptionMatcher subscriptionMatcher;
    private FlowMessageStore flowMessageStore;
    private OfflineMessageStore offlineMessageStore;
    private ConnectManager connectManager;
    private MqttConfig mqttConfig;
    
    public DefaultDispatcherMessage(BrokerRoom brokerRoom) {
        this.subscriptionMatcher = brokerRoom.getSubscriptionMatcher();
        this.flowMessageStore = brokerRoom.getFlowMessageStore();
        this.offlineMessageStore = brokerRoom.getOfflineMessageStore();
        this.connectManager = brokerRoom.getConnectManager();
        this.mqttConfig = brokerRoom.getMqttConfig();
    }

    @Override
    public void start() {
    	PerformanceConfig config = mqttConfig.getPerformanceConfig();
    	int pollThreadNum =config.getCoreThreadNum();
    	
		this.pollThread = new ThreadPoolExecutor(pollThreadNum,pollThreadNum,
                60 * 1000,TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(config.getQueueSize()),
                new ThreadFactoryImpl(mqttConfig.getServerName()+" -> popMessage2Subscriber"),
                new RejectHandler(mqttConfig.getServerName()+"-> popMessage", config.getQueueSize()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                int waitTime = 1000;
                while (!stoped) {
                    try {
                        int size = mqttConfig.getPerformanceConfig().getPopSize();
						List<Message> messageList = new ArrayList<>(size);
                        Message message;
                        for (int i = 0; i < size; i++) {
                            if (i == 0) {
                                message = messageQueue.poll(waitTime, TimeUnit.MILLISECONDS);
                            } else {
                                message = messageQueue.poll();
                            }
                            if (Objects.nonNull(message)) {
                                messageList.add(message);
                            } else {
                                break;
                            }
                        }
                        if (messageList.size() > 0) {
                            AsyncDispatcher dispatcher = new AsyncDispatcher(messageList);
                            pollThread.submit(dispatcher).get();
                        }
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

        private List<Message> messages;

        AsyncDispatcher(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public void run() {
            if (Objects.nonNull(messages)) {
                try {
                    for (Message message : messages) {
                        Set<Subscription> subscriptions = subscriptionMatcher.match((String) message.getHeader(MessageHeader.TOPIC));
                        for (Subscription subscription : subscriptions) {
                            String clientId = subscription.getClientId();
                            if (connectManager.containClient(clientId)) {
                                int qos = MessageUtil.getMinQos((int) message.getHeader(MessageHeader.QOS), subscription.getQos());
                                message.putHeader(MessageHeader.QOS, qos);
                                if (qos > 0) {
                                    flowMessageStore.cacheSendMsg(clientId, message);
                                }
                                MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message, false, qos, message.getMsgId());
                                ClientSession clientSession = connectManager.getClient(subscription.getClientId());
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
