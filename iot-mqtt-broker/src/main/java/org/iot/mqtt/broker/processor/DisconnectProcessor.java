/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.iot.mqtt.broker.processor;

import java.util.Collection;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.subscribe.SubscriptionMatcher;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.bean.Subscription;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.SessionStore;
import org.iot.mqtt.store.SubscriptionStore;
import org.iot.mqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class DisconnectProcessor implements RequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(DisconnectProcessor.class);
    private WillMessageStore willMessageStore;
    private SessionStore sessionStore;
    private SubscriptionStore subscriptionStore;
    private SubscriptionMatcher subscriptionMatcher;
    private ConnectManager connectManager;
    private MqttConfig mqttConfig;
    
    public DisconnectProcessor(BrokerRoom brokerRoom){
        this.willMessageStore = brokerRoom.getWillMessageStore();
        this.sessionStore = brokerRoom.getSessionStore();
        this.subscriptionStore = brokerRoom.getSubscriptionStore();
        this.subscriptionMatcher = brokerRoom.getSubscriptionMatcher();
        this.connectManager = brokerRoom.getConnectManager();
        this.mqttConfig = brokerRoom.getMqttConfig();
    }
    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        String clientId = NettyUtil.getClientId(ctx.channel());
        ClientSession clientSession = connectManager.getClient(clientId);
        if(clientSession == null){
            log.warn("[DISCONNECT] {} -> {} hasn't connect before",mqttConfig.getServerName(),clientId);
        }else {
            clearSession(clientSession);
            clearSubscriptions(clientSession);
            clearWillMessage(clientId);
            connectManager.removeClient(clientId);
        }
        ctx.close();
        log.warn("[DISCONNECT] {} -> clientId:{}",mqttConfig.getServerName(),clientId);
    }

    private void clearSubscriptions(ClientSession clientSession){
        if(clientSession.isCleanSession()){
            Collection<Subscription> subscriptions = subscriptionStore.getSubscriptions(clientSession.getClientId());
            for(Subscription subscription : subscriptions){
                subscriptionMatcher.unSubscribe(subscription.getTopic(),clientSession.getClientId());
            }
            subscriptionStore.clearSubscription(clientSession.getClientId());
        }
    }

    private void clearSession(ClientSession clientSession){
        if(clientSession.isCleanSession()){
            sessionStore.clearSession(clientSession.getClientId());
        }else{
            sessionStore.setSession(clientSession.getClientId(),System.currentTimeMillis());
        }
    }

    private void clearWillMessage(String clientId){
    	boolean removeWillMessage = willMessageStore.removeWillMessage(clientId);
    	if(!removeWillMessage){
            log.warn("{} -> The will message is not exist,cause = {}",
            		mqttConfig.getServerName(),clientId);
        }
    }


}
