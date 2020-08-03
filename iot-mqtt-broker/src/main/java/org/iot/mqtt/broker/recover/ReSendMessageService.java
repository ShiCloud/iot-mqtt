package org.iot.mqtt.broker.recover;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.bean.MessageHeader;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.common.config.PerformanceConfig;
import org.iot.mqtt.common.utils.ThreadFactoryImpl;
import org.iot.mqtt.store.FlowMessageStore;
import org.iot.mqtt.store.OfflineMessageStore;
import org.iot.mqtt.store.ResendMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.mqtt.MqttPublishMessage;

/**
 * send offline message and flow message when client re connect and cleanSession
 * is false
 */
public class ReSendMessageService {

	private Logger log = LoggerFactory.getLogger(ReSendMessageService.class);

	private Thread thread;
	private boolean stoped = false;
	private BlockingQueue<String> clients = new LinkedBlockingQueue<>();
	private OfflineMessageStore offlineMessageStore;
	private FlowMessageStore flowMessageStore;
	private ConnectManager connectManager;
    private MqttConfig mqttConfig;
    private PerformanceConfig config;
	private int clientSize;
	private ThreadPoolExecutor sendMessageExecutor;

	public ReSendMessageService(BrokerRoom brokerRoom) {
		this.offlineMessageStore = brokerRoom.getOfflineMessageStore();
		this.flowMessageStore = brokerRoom.getFlowMessageStore();
		this.connectManager = brokerRoom.getConnectManager();
        this.mqttConfig = brokerRoom.getMqttConfig();
        this.config = mqttConfig.getPerformanceConfig();
		this.clientSize = config.getClientSize();
        int coreThreadNum = config.getCoreThreadNum();
		this.sendMessageExecutor = new ThreadPoolExecutor(coreThreadNum, coreThreadNum,60, TimeUnit.SECONDS,
    			new LinkedBlockingQueue<>(config.getQueueSize()), 
    			new ThreadFactoryImpl(mqttConfig.getServerName()+" -> Resend MessageThread"));
		this.thread = new Thread(new PutClient());
	};

	public boolean put(String clientId) {
		if (this.clients.size() > clientSize) {
			log.warn("[Resend] {} -> message busy! the client queue size is over {}",
					mqttConfig.getServerName(), clientSize);
			return false;
		}
		this.clients.offer(clientId);
		return true;
	}

	public void start() {
		thread.start();
	}

	public void shutdown() {
		if (!stoped) {
			stoped = true;
		}
	}

	public boolean dispatcherMessage(String clientId, Message message,boolean isOffline) {
		ClientSession clientSession = connectManager.getClient(clientId);
		// client off line again
		if (clientSession == null) {
			log.warn("[Resend] {} -> The client offline again, put the message to the offline queue,clientId:{}",
					mqttConfig.getServerName(), clientId);
			offlineMessageStore.addOfflineMessage(clientId, message);
			return false;
		}
		int qos = (int) message.getHeader(MessageHeader.QOS);
		if (qos > 0) {
			if(isOffline) {
				message.setMsgId(clientSession.generateMessageId());
			} 
			flowMessageStore.cacheSendMsg(clientId, message);
		}
		MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message, false, qos, message.getMsgId());
		clientSession.getCtx().writeAndFlush(publishMessage);
		return true;
	}

	class ResendMessageTask implements Callable<Boolean> {

		private int getNums;
		private String clientId;
		
		public ResendMessageTask(String clientId,int getNums) {
			this.clientId = clientId;
			this.getNums = getNums;
		}
		
		@Override
		public Boolean call() {
			boolean flag = resendMsg(flowMessageStore);
			if(!flag) {
				return flag;
			}
			flag = resendMsg(offlineMessageStore);
			if(!flag) {
				return flag;
			}
			return flag;
		}

		private boolean resendMsg(ResendMessageStore resendMessageStore) {
			boolean flag = true;
			Collection<Message> flowMsgs = resendMessageStore.getReSendMsg(clientId, getNums);
			while (flowMsgs != null && !flowMsgs.isEmpty()) {
				for (Message message : flowMsgs) {
					if (!dispatcherMessage(clientId, message,true)) {
						flag = false;
					}
				}
				if(!flag) {
					return flag;
				}
				flowMsgs = resendMessageStore.getReSendMsg(clientId, getNums);
			}
			return flag;
		}
	}

	class PutClient implements Runnable {
		@Override
		public void run() {
			while (!stoped) {
				String clientId = "";
				try {
					clientId = clients.poll();
					if (clientId == null) {
						Thread.sleep(config.getPopInterval());
						continue;
					}
					ResendMessageTask resendMessageTask = new ResendMessageTask(clientId,config.getPopSize());
					long start = System.currentTimeMillis();
					boolean rs = sendMessageExecutor.submit(resendMessageTask).get();
					if (!rs) {
						log.warn("[Resend] {} -> message is interrupted,the client offline again,clientId={}",mqttConfig.getServerName(), clientId);
					}
					long cost = System.currentTimeMillis() - start;
					log.debug("[Resend] {} -> message clientId:{} cost time:{}",mqttConfig.getServerName(), clientId, cost);
				} catch (Exception e) {
					log.warn("[Resend] {} -> message failure,clientId:{}",mqttConfig.getServerName(), clientId);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
				}
			}
			log.info("[Resend] {} -> Shutdown resend message service success.",mqttConfig.getServerName());
		}
	}

}
