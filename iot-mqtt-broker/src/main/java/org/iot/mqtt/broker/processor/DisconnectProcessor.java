package org.iot.mqtt.broker.processor;

import java.util.Collection;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.subscribe.SubscriptionMatcher;
import org.iot.mqtt.broker.sys.SysMessageService;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.bean.Subscription;
import org.iot.mqtt.store.SessionStore;
import org.iot.mqtt.store.SubscriptionStore;
import org.iot.mqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public class DisconnectProcessor implements RequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(DisconnectProcessor.class);
    private WillMessageStore willMessageStore;
    private SessionStore sessionStore;
    private SubscriptionStore subscriptionStore;
    private SubscriptionMatcher subscriptionMatcher;
	private SysMessageService sysMessageService;

    public DisconnectProcessor(BrokerRoom brokerRoom){
        this.willMessageStore = brokerRoom.getWillMessageStore();
        this.sessionStore = brokerRoom.getSessionStore();
        this.subscriptionStore = brokerRoom.getSubscriptionStore();
        this.subscriptionMatcher = brokerRoom.getSubscriptionMatcher();
        this.sysMessageService = brokerRoom.getSysMessageService();
    }
    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        if(!ConnectManager.getInstance().containClient(clientId)){
            log.warn("[DISCONNECT] -> {} hasn't connect before",clientId);
        }
        ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
        clearSession(clientSession);
        clearSubscriptions(clientSession);
        clearWillMessage(clientSession.getClientId());
        //停止发送系统信息
        sysMessageService.removeClient(clientId);
        ConnectManager.getInstance().removeClient(clientId);
        ctx.close();
        log.warn("[DISCONNECT] -> {} clientId:{}",clientId);
    }

    private void clearSubscriptions(ClientSession clientSession){
        if(clientSession.isCleanSession()){
            Collection<Subscription> subscriptions = subscriptionStore.getSubscriptions(clientSession.getClientId());
            for(Subscription subscription : subscriptions){
                this.subscriptionMatcher.unSubscribe(subscription.getTopic(),clientSession.getClientId());
            }
            subscriptionStore.clearSubscription(clientSession.getClientId());
        }
    }

    private void clearSession(ClientSession clientSession){
        if(clientSession.isCleanSession()){
            this.sessionStore.clearSession(clientSession.getClientId());
        }else{
            this.sessionStore.setSession(clientSession.getClientId(),System.currentTimeMillis());
        }
    }

    private void clearWillMessage(String clientId){
        if(willMessageStore.hasWillMessage(clientId)){
            willMessageStore.removeWillMessage(clientId);
        }
    }


}
