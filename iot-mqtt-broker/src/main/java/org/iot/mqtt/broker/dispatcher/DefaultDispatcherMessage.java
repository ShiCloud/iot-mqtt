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

import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.subscribe.SubscriptionMatcher;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.bean.MessageHeader;
import org.iot.mqtt.common.bean.Subscription;
import org.iot.mqtt.store.FlowMessageStore;
import org.iot.mqtt.store.OfflineMessageStore;
import org.iot.mqtt.test.utils.RejectHandler;
import org.iot.mqtt.test.utils.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.mqtt.MqttPublishMessage;


public class DefaultDispatcherMessage implements MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(DefaultDispatcherMessage.class);
    private boolean stoped = false;
    private static final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(100000);
    private ThreadPoolExecutor pollThread;
    private int pollThreadNum;
    private SubscriptionMatcher subscriptionMatcher;
    private FlowMessageStore flowMessageStore;
    private OfflineMessageStore offlineMessageStore;

    public DefaultDispatcherMessage(int pollThreadNum, SubscriptionMatcher subscriptionMatcher, FlowMessageStore flowMessageStore, OfflineMessageStore offlineMessageStore) {
        this.pollThreadNum = pollThreadNum;
        this.subscriptionMatcher = subscriptionMatcher;
        this.flowMessageStore = flowMessageStore;
        this.offlineMessageStore = offlineMessageStore;
    }

    @Override
    public void start() {
        this.pollThread = new ThreadPoolExecutor(pollThreadNum,
                pollThreadNum,
                60 * 1000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100000),
                new ThreadFactoryImpl("pollMessage2Subscriber"),
                new RejectHandler("pollMessage", 100000));

        new Thread(new Runnable() {
            @Override
            public void run() {
                int waitTime = 1000;
                while (!stoped) {
                    try {
                        List<Message> messageList = new ArrayList<>(32);
                        Message message;
                        for (int i = 0; i < 32; i++) {
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
                        log.warn("poll message wrong.");
                    } catch (ExecutionException e) {
                        log.warn("AsyncDispatcher get() wrong.");
                    }
                }
            }
        }).start();
    }

    @Override
    public boolean appendMessage(Message message) {
        boolean isNotFull = messageQueue.offer(message);
        if (!isNotFull) {
            log.warn("[PubMessage] -> the buffer queue is full");
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
                            ClientSession clientSession = ConnectManager.getInstance().getClient(subscription.getClientId());
                            if (ConnectManager.getInstance().containClient(clientId)) {
                                int qos = MessageUtil.getMinQos((int) message.getHeader(MessageHeader.QOS), subscription.getQos());
                                message.putHeader(MessageHeader.QOS, qos);
                                if (qos > 0) {
                                    flowMessageStore.cacheSendMsg(clientId, message);
                                }
                                MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message, false, qos, message.getMsgId());
                                clientSession.getCtx().writeAndFlush(publishMessage);
                                clientSession.addReceiveIdCounter();//收到一条
                            } else {
                                offlineMessageStore.addOfflineMessage(clientId, message);
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Dispatcher message failure,cause={}", ex);
                }
            }
        }

    }
}
