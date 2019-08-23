package org.iot.mqtt.broker.recover;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.iot.mqtt.broker.session.ClientSession;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.utils.MessageUtil;
import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.bean.MessageHeader;
import org.iot.mqtt.store.FlowMessageStore;
import org.iot.mqtt.store.OfflineMessageStore;
import org.iot.mqtt.test.utils.ThreadFactoryImpl;
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
	private int maxSize = 10000;
	private ThreadPoolExecutor sendMessageExecutor = new ThreadPoolExecutor(4, 4, 60, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(10000), new ThreadFactoryImpl("Resend MessageThread"));

	public ReSendMessageService(OfflineMessageStore offlineMessageStore, FlowMessageStore flowMessageStore) {
		this.offlineMessageStore = offlineMessageStore;
		this.flowMessageStore = flowMessageStore;
		this.thread = new Thread(new PutClient());
	};

	public boolean put(String clientId) {
		if (this.clients.size() > maxSize) {
			log.warn("[Resend] message busy! the client queue size is over {}", maxSize);
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

	public boolean dispatcherMessage(String clientId, Message message) {
		ClientSession clientSession = ConnectManager.getInstance().getClient(clientId);
		// client off line again
		if (clientSession == null) {
			log.warn("The client offline again, put the message to the offline queue,clientId:{}", clientId);
			return false;
		}
		int qos = (int) message.getHeader(MessageHeader.QOS);
		int messageId = message.getMsgId();
		if (qos > 0) {
			flowMessageStore.cacheSendMsg(clientId, message);
		}
		MqttPublishMessage publishMessage = MessageUtil.getPubMessage(message, false, qos, messageId);
		clientSession.getCtx().writeAndFlush(publishMessage);
		return true;
	}

	class ResendMessageTask implements Callable<Boolean> {

		private int getNums = 100;
		private String clientId;
		
		public ResendMessageTask(String clientId) {
			this.clientId = clientId;
		}
		
		@Override
		public Boolean call() {
			log.debug("[Resend] flowMessageStore count:{}",flowMessageStore.getAllSendMsgCount(clientId));
			Collection<Message> flowMsgs = flowMessageStore.getSendMsg(clientId, getNums);
			while (flowMsgs != null && !flowMsgs.isEmpty()) {
				for (Message message : flowMsgs) {
					if (!dispatcherMessage(clientId, message)) {
						return false;
					}
				}
				flowMsgs = flowMessageStore.getSendMsg(clientId, getNums);
			}
			log.debug("[Resend] offlineMessageStore count:{}",offlineMessageStore.getAllOfflineMessageCount(clientId));
			if (offlineMessageStore.containOfflineMsg(clientId)) {
				Collection<Message> offlineMsgs = offlineMessageStore.getOfflineMessage(clientId, getNums);
				while (offlineMsgs != null && !offlineMsgs.isEmpty()) {
					for (Message message : offlineMsgs) {
						if (!dispatcherMessage(clientId, message)) {
							return false;
						}
					}
					offlineMsgs = offlineMessageStore.getOfflineMessage(clientId, getNums);
				}
			}
			return true;
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
						Thread.sleep(3000);
						continue;
					}
					ResendMessageTask resendMessageTask = new ResendMessageTask(clientId);
					long start = System.currentTimeMillis();
					boolean rs = sendMessageExecutor.submit(resendMessageTask).get(2000, TimeUnit.MILLISECONDS);
					if (!rs) {
						log.warn("[Resend] message is interrupted,the client offline again,clientId={}", clientId);
					}
					long cost = System.currentTimeMillis() - start;
					log.debug("[Resend] message clientId:{} cost time:{}", clientId, cost);
				} catch (Exception e) {
					log.warn("[Resend] message failure,clientId:{}", clientId);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
				}
			}
			log.info("[Resend] Shutdown resend message service success.");
		}
	}

}
