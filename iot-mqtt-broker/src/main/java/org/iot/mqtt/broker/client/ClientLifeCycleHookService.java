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
package org.iot.mqtt.broker.client;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.dispatcher.MessageDispatcher;
import org.iot.mqtt.broker.netty.ChannelEventListener;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class ClientLifeCycleHookService implements ChannelEventListener {

	private static final Logger log = LoggerFactory.getLogger(ClientLifeCycleHookService.class);
    private WillMessageStore willMessageStore;
    private MessageDispatcher messageDispatcher;
    private ConnectManager connectManager;
    private MqttConfig mqttConfig;
    
    public ClientLifeCycleHookService(BrokerRoom brokerRoom){
    	this.mqttConfig = brokerRoom.getMqttConfig();
    	this.connectManager = brokerRoom.getConnectManager();
        this.willMessageStore = brokerRoom.getWillMessageStore();
        this.messageDispatcher = brokerRoom.getMessageDispatcher();
    }

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {
        String clientId = NettyUtil.getClientId(channel);
        if(clientId !=null && clientId != ""){
            Message willMessage = willMessageStore.getWillMessage(clientId);
	        if(willMessage!=null){
	            messageDispatcher.appendMessage(willMessage);
            }
        }
    }

    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
    	String clientId = NettyUtil.getClientId(channel);
    	connectManager.removeClient(clientId);
		log.warn("[ClientLifeCycleHook] {} -> {} channelException,close channel and remove ConnectCache!",
				mqttConfig.getServerName(),clientId);
    }
}
