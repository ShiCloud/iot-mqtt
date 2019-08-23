package org.iot.mqtt.broker.client;

import org.apache.commons.lang3.StringUtils;
import org.iot.mqtt.broker.dispatcher.MessageDispatcher;
import org.iot.mqtt.broker.netty.ChannelEventListener;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.sys.SysMessageService;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

public class ClientLifeCycleHookService implements ChannelEventListener {

	private static final Logger log = LoggerFactory.getLogger(ClientLifeCycleHookService.class);
    private WillMessageStore willMessageStore;
    private MessageDispatcher messageDispatcher;
    private SysMessageService sysMessageService;
    
    public ClientLifeCycleHookService(WillMessageStore willMessageStore,MessageDispatcher messageDispatcher,
    		SysMessageService sysMessageService){
        this.willMessageStore = willMessageStore;
        this.messageDispatcher = messageDispatcher;
        this.sysMessageService = sysMessageService;
    }

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {
        String clientId = NettyUtil.getClientId(channel);
        if(StringUtils.isNotEmpty(clientId)){
            if(willMessageStore.hasWillMessage(clientId)){
                Message willMessage = willMessageStore.getWillMessage(clientId);
                messageDispatcher.appendMessage(willMessage);
            }
            //停止发送系统信息
            sysMessageService.removeClient(clientId);
        }
    }

    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
    	String clientId = NettyUtil.getClientId(channel);
    	sysMessageService.removeClient(clientId);
		ConnectManager.getInstance().removeClient(clientId);
		log.warn("[ClientLifeCycleHook] -> {} channelException,close channel and remove ConnectCache!",clientId);
    }
}
