package org.iot.mqtt.broker.sys;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.bean.MessageHeader;
import org.iot.mqtt.common.bean.Subscription;
import org.iot.mqtt.store.FlowMessageStore;
import org.iot.mqtt.store.OfflineMessageStore;
import org.iot.mqtt.store.SubscriptionStore;
import org.iot.mqtt.test.utils.SerializeHelper;
import org.iot.mqtt.test.utils.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * send offline message and flow message when client re connect and cleanSession is false
 */
public class SysMessageService {

    private Logger log = LoggerFactory.getLogger(SysMessageService.class);

    private Thread thread;
    private boolean stoped = false;
    private BlockingQueue<String> clients = new LinkedBlockingQueue<>();
    
    SubscriptionStore subscriptionStore;
    OfflineMessageStore offlineMessageStore;
    FlowMessageStore flowMessageStore;
    
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(4,
            4,60,TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            new ThreadFactoryImpl("SysMessageServiceThread"));

    public SysMessageService(SubscriptionStore subscriptionStore,OfflineMessageStore offlineMessageStore, 
    		FlowMessageStore flowMessageStore){
    	this.subscriptionStore = subscriptionStore;
    	this.offlineMessageStore = offlineMessageStore;
    	this.flowMessageStore = flowMessageStore;
        this.thread = new Thread(new SysMessageExec());
    };
    
    public void putClient(String clientId) {
    	if(!this.clients.contains(clientId)) {
	    	this.clients.add(clientId);
	    	log.debug("[SysMessageTask] -> putClient clientId:{}",clientId);
    	}
	}

	public void removeClient(String clientId) {
		if(this.clients.contains(clientId)) {
			this.clients.remove(clientId);
			log.debug("[SysMessageTask] -> removeClient clientId:{}",clientId);
		}
	}

	public void wakeUp(){
        LockSupport.unpark(thread);
    }

    public void start(){
        thread.start();
    }

    public void shutdown(){
        if(!stoped){
            stoped = true;
        }
    }
    
    class SysMessageExec implements Runnable{
        @Override
        public void run() {
            while (!stoped){
            	if(clients.size() == 0){
                    LockSupport.park(thread);
                }
            	try {
            		Thread.sleep(5000);
            		long start = System.currentTimeMillis();
	                SysMessageTask task = new SysMessageTask();
	                boolean rs = executor.submit(task).get(2000,TimeUnit.MILLISECONDS);
					if(!rs){
	                    log.warn("SysMessageTask is interrupted");
	                }
					long cost = System.currentTimeMillis() - start;
					log.debug("SysMessageTask cost time:{} clients size:{}",cost,clients.size());
				} catch (Exception e) {
					log.error("SysMessageTask exception.",e);
				}
            }
            log.info("SysMessageTask success.");
        }
    }
    
    class SysMessageTask implements Callable<Boolean> {
    	private AtomicInteger sysIdCounter = new AtomicInteger(0);
		public int generateSysId(){
            int messageId = sysIdCounter.getAndIncrement();
            messageId = Math.abs( messageId % 0xFFFF);
            if(messageId == 0){
                return generateSysId();
            }
            return messageId;
        }
		@Override
        public Boolean call() {
			Map<String, ClientSession> clientCache = ConnectManager.getInstance().getClientCache();
			for (String clientId : clients) {
				ClientSession clientSession = clientCache.get(clientId);
				Map<String,Object> headers = new HashMap<>();
		        headers.put(MessageHeader.RETAIN,false);
		        headers.put(MessageHeader.QOS,MqttQoS.AT_MOST_ONCE);
		        headers.put(MessageHeader.TOPIC,SysToipc.SYS);
		        headers.put(MessageHeader.WILL,true);
		        Message message = new Message(Message.Type.PUBLISH,headers,
		        		SerializeHelper.serialize(getSysInfo(clientCache)));
		        message.setClientId(clientId);
				MqttPublishMessage pubMessage = MessageUtil.getPubMessage(message , false, MqttQoS.AT_MOST_ONCE.value(), 
						generateSysId());
				clientSession.getCtx().writeAndFlush(pubMessage);
			}
            return true;
        }
    }

    public SysInfo getSysInfo(Map<String, ClientSession> clientCache){
    	SysInfo sysInfo = new SysInfo();
    	for (String clientId : clientCache.keySet()) {
    		ClientInfo clientInfo = new ClientInfo(clientId);
    		
    		long receiveIdCounter = clientCache.get(clientId).getReceiveIdCounter();
			clientInfo.setReceive(receiveIdCounter);
			
			long sendIdCounter = clientCache.get(clientId).getSendIdCounter();
			clientInfo.setSend(sendIdCounter);
			
			long offlineCount = offlineMessageStore.getAllOfflineMessageCount(clientId);    		
    		clientInfo.setOffline(offlineCount);
    		
    		long flowCount = flowMessageStore.getAllSendMsgCount(clientId);
    		clientInfo.setFlow(flowCount);
    		
    		Collection<Subscription> subscriptions = subscriptionStore.getSubscriptions(clientId);
    		for (Subscription sub : subscriptions) {
        		clientInfo.addTopic(sub.getTopic(),sub.getQos());
        	}
    		sysInfo.addClientInfo(clientInfo);
		}
        return sysInfo;
    }

    
}
