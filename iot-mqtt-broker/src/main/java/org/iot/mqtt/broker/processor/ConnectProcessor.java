package org.iot.mqtt.broker.processor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.acl.ConnectPermission;
import org.iot.mqtt.broker.recover.ReSendMessageService;
import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.subscribe.SubscriptionMatcher;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.broker.utils.NettyUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.bean.MessageHeader;
import org.iot.mqtt.common.bean.Subscription;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.FlowMessageStore;
import org.iot.mqtt.store.OfflineMessageStore;
import org.iot.mqtt.store.SessionStore;
import org.iot.mqtt.store.SubscriptionStore;
import org.iot.mqtt.store.WillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.timeout.IdleStateHandler;


public class ConnectProcessor implements RequestProcessor {

    private static final Logger log = LoggerFactory.getLogger(ConnectProcessor.class);

    private FlowMessageStore flowMessageStore;
    private WillMessageStore willMessageStore;
    private OfflineMessageStore offlineMessageStore;
    private SubscriptionStore subscriptionStore;
    private SessionStore sessionStore;
    private ConnectPermission connectPermission;
    private ReSendMessageService reSendMessageService;
    private SubscriptionMatcher subscriptionMatcher;
    private ConnectManager connectManager;
    private MqttConfig mqttConfig;
    
    public ConnectProcessor(BrokerRoom brokerRoom){
        this.flowMessageStore = brokerRoom.getFlowMessageStore();
        this.willMessageStore = brokerRoom.getWillMessageStore();
        this.offlineMessageStore = brokerRoom.getOfflineMessageStore();
        this.subscriptionStore = brokerRoom.getSubscriptionStore();
        this.sessionStore = brokerRoom.getSessionStore();
        this.connectPermission = brokerRoom.getConnectPermission();
        this.reSendMessageService = brokerRoom.getReSendMessageService();
        this.subscriptionMatcher = brokerRoom.getSubscriptionMatcher();
        this.connectManager = brokerRoom.getConnectManager();
        this.mqttConfig = brokerRoom.getMqttConfig();
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttConnectMessage connectMessage = (MqttConnectMessage)mqttMessage;
        MqttConnectReturnCode returnCode = null;
        int mqttVersion = connectMessage.variableHeader().version();
        String clientId = connectMessage.payload().clientIdentifier();
        String userName = connectMessage.payload().userName();
        byte[] password = connectMessage.payload().passwordInBytes();
        boolean sessionPresent = false;
        try{
            if(!versionValid(mqttVersion)){
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;
            } else if(!checkServer()){
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE;
            } else if(!clientIdVerfy(clientId)){
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
            } else if(onBlackList(NettyUtil.getRemoteAddr(ctx.channel()),clientId)){
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED;
            } else if(!authentication(clientId,userName,password)){
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
            }else{
                int heartbeatSec = connectMessage.variableHeader().keepAliveTimeSeconds();
                if(!keepAlive(clientId,ctx,heartbeatSec)){
                    log.warn("[CONNECT] {} -> set heartbeat failure,clientId:{},heartbeatSec:{}",mqttConfig.getServerName(),clientId,heartbeatSec);
                    throw new Exception("set heartbeat failure");
                }
                
                Object lastState = sessionStore.getLastSession(clientId);
                if(Objects.nonNull(lastState) && lastState.equals(true)){
                    ClientSession previousClient = connectManager.getClient(clientId);
                    if(previousClient != null){
                    	if(previousClient.getCtx()!=null) {
                    		previousClient.getCtx().close();
                    	}
                        connectManager.removeClient(clientId);
                    }
                }
                boolean cleansession = connectMessage.variableHeader().isCleanSession();
                ClientSession clientSession = null;
                if(cleansession){
                    clientSession = createNewClientSession(clientId,ctx);
                    sessionPresent = false;
                }else{
                    if(Objects.nonNull(lastState)){
                        clientSession = reloadClientSession(ctx,clientId);
                        sessionPresent = true;
                    }else{
                        clientSession = new ClientSession(clientId,false,ctx);
                        sessionPresent = false;
                    }
                }
                sessionStore.setSession(clientId,true);
                
                boolean willFlag = connectMessage.variableHeader().isWillFlag();
                if(willFlag){
                    boolean willRetain = connectMessage.variableHeader().isWillRetain();
                    int willQos = connectMessage.variableHeader().willQos();
                    String willTopic = connectMessage.payload().willTopic();
                    byte[] willPayload = connectMessage.payload().willMessageInBytes();
                    storeWillMsg(clientId,willRetain,willQos,willTopic,willPayload);
                }
                returnCode = MqttConnectReturnCode.CONNECTION_ACCEPTED;
                NettyUtil.setClientId(ctx.channel(),clientId);
                connectManager.putClient(clientId,clientSession);
            }
            MqttConnAckMessage ackMessage = MessageUtil.getConnectAckMessage(returnCode,sessionPresent);
            ctx.writeAndFlush(ackMessage);
            if(returnCode != MqttConnectReturnCode.CONNECTION_ACCEPTED){
                ctx.close();
                log.warn("[CONNECT] {} -> {} connect failure,returnCode={}",mqttConfig.getServerName(),clientId,returnCode);
                return;
            }
            log.info("[CONNECT] {} -> {} connect to this mqtt server",mqttConfig.getServerName(),clientId);
            
            reConnect2SendMessage(clientId);
            
        }catch(Exception ex){
            log.warn("[CONNECT] {} -> Service Unavailable: cause={}",mqttConfig.getServerName(),ex);
            returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE;
            MqttConnAckMessage ackMessage = MessageUtil.getConnectAckMessage(returnCode,sessionPresent);
            ctx.writeAndFlush(ackMessage);
            ctx.close();
        }
    }
    
	private boolean checkServer() {
		return true;
	}

	private boolean keepAlive(String clientId,ChannelHandlerContext ctx,int heatbeatSec){
        if(this.connectPermission.verifyHeartbeatTime(clientId,heatbeatSec)){
            int keepAlive = (int)(heatbeatSec * 1.5f);
            String idle = "idleStateHandler";
			if(ctx.pipeline().names().contains(idle)){
                ctx.pipeline().remove(idle);
            }
            ctx.pipeline().addFirst(idle,new IdleStateHandler(keepAlive,0,0));
            return true;
        }
        return false;
    }

    private void storeWillMsg(String clientId,boolean willRetain,int willQos,String willTopic,byte[] willPayload){
        Map<String,Object> headers = new HashMap<>();
        headers.put(MessageHeader.RETAIN,willRetain);
        headers.put(MessageHeader.QOS,willQos);
        headers.put(MessageHeader.TOPIC,willTopic);
        headers.put(MessageHeader.WILL,true);
        Message message = new Message(Message.Type.WILL,headers,willPayload);
        message.setClientId(clientId);
        willMessageStore.storeWillMessage(clientId,message);
        log.info("[WillMessageStore] {} -> {} store will message:{}",mqttConfig.getServerName(),clientId,message);
    }

    private ClientSession createNewClientSession(String clientId,ChannelHandlerContext ctx){
        ClientSession clientSession = new ClientSession(clientId,true);
        clientSession.setCtx(ctx);
        //clear previous sessions
        flowMessageStore.clearClientCache(clientId);
        offlineMessageStore.clearOfflineMsgCache(clientId);
        subscriptionStore.clearSubscription(clientId);
        sessionStore.clearSession(clientId);
        return clientSession;
    }

    /**
     * cleansession is false, reload client session
     */
    private ClientSession reloadClientSession(ChannelHandlerContext ctx,String clientId){
            ClientSession clientSession = new ClientSession(clientId,false);
            clientSession.setCtx(ctx);
            Collection<Subscription> subscriptions = subscriptionStore.getSubscriptions(clientId);
            for(Subscription subscription : subscriptions){
                subscriptionMatcher.subscribe(subscription);
            }
            return clientSession;
    }

    private void reConnect2SendMessage(String clientId){
        this.reSendMessageService.put(clientId);
    }

    private boolean authentication(String clientId,String username,byte[] password){
        return this.connectPermission.authentication(clientId,username,password);
    }

    private boolean onBlackList(String remoteAddr,String clientId){
        return this.connectPermission.onBlacklist(remoteAddr,clientId);
    }

    private boolean clientIdVerfy(String clientId){
        return this.connectPermission.clientIdVerfy(clientId);
    }

    private boolean versionValid(int mqttVersion){
        if(mqttVersion == 3 || mqttVersion == 4){
            return true;
        }
        return false;
    }

}
