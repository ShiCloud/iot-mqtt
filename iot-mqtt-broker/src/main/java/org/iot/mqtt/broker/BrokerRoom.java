package org.iot.mqtt.broker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.iot.mqtt.broker.acl.ConnectPermission;
import org.iot.mqtt.broker.acl.PubSubPermission;
import org.iot.mqtt.broker.acl.impl.DefaultConnectPermission;
import org.iot.mqtt.broker.acl.impl.DefaultPubSubPermission;
import org.iot.mqtt.broker.client.ClientLifeCycleHookService;
import org.iot.mqtt.broker.dispatcher.DefaultDispatcherMessage;
import org.iot.mqtt.broker.dispatcher.MessageDispatcher;
import org.iot.mqtt.broker.netty.ChannelEventListener;
import org.iot.mqtt.broker.netty.NettyEventExcutor;
import org.iot.mqtt.broker.recover.ReSendMessageService;
import org.iot.mqtt.broker.subscribe.DefaultSubscriptionTreeMatcher;
import org.iot.mqtt.broker.subscribe.SubscriptionMatcher;
import org.iot.mqtt.broker.sys.SysMessageService;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.AbstractMqttStore;
import org.iot.mqtt.store.FlowMessageStore;
import org.iot.mqtt.store.OfflineMessageStore;
import org.iot.mqtt.store.RetainMessageStore;
import org.iot.mqtt.store.SessionStore;
import org.iot.mqtt.store.SubscriptionStore;
import org.iot.mqtt.store.WillMessageStore;
import org.iot.mqtt.store.memory.DefaultMqttStore;
import org.iot.mqtt.store.rocksdb.RDBMqttStore;
import org.iot.mqtt.test.utils.RejectHandler;
import org.iot.mqtt.test.utils.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrokerRoom {

	private static Logger log = LoggerFactory.getLogger(BrokerRoom.class);

	int coreThreadNum = Runtime.getRuntime().availableProcessors();
	
	LinkedBlockingQueue<Runnable> connectQueue = new LinkedBlockingQueue<>(100000);
	LinkedBlockingQueue<Runnable> pubQueue = new LinkedBlockingQueue<>(100000);
	LinkedBlockingQueue<Runnable> subQueue = new LinkedBlockingQueue<>(100000);
	LinkedBlockingQueue<Runnable> pingQueue = new LinkedBlockingQueue<>(10000);

	private ExecutorService connectExecutor;
	private ExecutorService pubExecutor;
	private ExecutorService subExecutor;
	private ExecutorService pingExecutor;

	private AbstractMqttStore abstractMqttStore;

	private NettyEventExcutor nettyEventExcutor;
	private ReSendMessageService reSendMessageService;
	private SysMessageService sysMessageService;
	
	
	private ChannelEventListener channelEventListener;
	private MessageDispatcher messageDispatcher;
	private FlowMessageStore flowMessageStore;
	private SubscriptionMatcher subscriptionMatcher;
	private WillMessageStore willMessageStore;
	private RetainMessageStore retainMessageStore;
	private OfflineMessageStore offlineMessageStore;
	private SubscriptionStore subscriptionStore;
	private SessionStore sessionStore;
	private ConnectPermission connectPermission;
	private PubSubPermission pubSubPermission;
	
	public BrokerRoom(MqttConfig mqttConfig) {
		switch (mqttConfig.getStoreType()) {
		case 1:
			this.abstractMqttStore = new DefaultMqttStore();
			break;
		case 2:
			this.abstractMqttStore = new RDBMqttStore(mqttConfig);
			break;	
		default:
			break;
		}

		try {
			this.abstractMqttStore.init();
		} catch (Exception e) {
			log.info("init store failure,exception=" + e);
		}

		this.flowMessageStore = this.abstractMqttStore.getFlowMessageStore();
		this.willMessageStore = this.abstractMqttStore.getWillMessageStore();
		this.retainMessageStore = this.abstractMqttStore.getRetainMessageStore();
		this.offlineMessageStore = this.abstractMqttStore.getOfflineMessageStore();
		this.subscriptionStore = this.abstractMqttStore.getSubscriptionStore();
		this.sessionStore = this.abstractMqttStore.getSessionStore();

		this.connectPermission = new DefaultConnectPermission(mqttConfig);
		this.pubSubPermission = new DefaultPubSubPermission();

		this.subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
		this.messageDispatcher = new DefaultDispatcherMessage(mqttConfig.getPollThreadNum(), subscriptionMatcher,
				flowMessageStore, offlineMessageStore);

		this.reSendMessageService = new ReSendMessageService(offlineMessageStore, flowMessageStore);
		this.sysMessageService = new SysMessageService(subscriptionStore,offlineMessageStore,flowMessageStore);
		this.channelEventListener = new ClientLifeCycleHookService(willMessageStore, messageDispatcher, sysMessageService);
		

		this.nettyEventExcutor = new NettyEventExcutor(channelEventListener);

		this.connectExecutor = createThreadPool(connectQueue, "connectQueue", coreThreadNum * 2, coreThreadNum * 2);
		this.pubExecutor = createThreadPool(pubQueue, "pubQueue", coreThreadNum * 2, coreThreadNum * 2);
		this.subExecutor = createThreadPool(subQueue, "subQueue", coreThreadNum * 2, coreThreadNum * 2);
		this.pingExecutor = createThreadPool(pingQueue, "pingQueue", coreThreadNum, coreThreadNum);
		
		
	}

	private ExecutorService createThreadPool(LinkedBlockingQueue<Runnable> queue, String taskName, int corePoolSize,
			int maximumPoolSize) {
		return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60000, TimeUnit.MILLISECONDS, queue,
				new ThreadFactoryImpl(taskName), new RejectHandler(taskName, 100000));
	}

	public void start() {
		this.nettyEventExcutor.start();
		this.messageDispatcher.start();
		this.reSendMessageService.start();
		this.sysMessageService.start();
	}

	public void shutdown() {
		this.nettyEventExcutor.shutdown();
		this.abstractMqttStore.shutdown();
		this.connectExecutor.shutdown();
		this.pubExecutor.shutdown();
		this.subExecutor.shutdown();
		this.pingExecutor.shutdown();
		this.messageDispatcher.shutdown();
		this.reSendMessageService.shutdown();
		this.sysMessageService.shutdown();
	}

	public ExecutorService getConnectExecutor() {
		return connectExecutor;
	}

	public void setConnectExecutor(ExecutorService connectExecutor) {
		this.connectExecutor = connectExecutor;
	}

	public ExecutorService getPubExecutor() {
		return pubExecutor;
	}

	public void setPubExecutor(ExecutorService pubExecutor) {
		this.pubExecutor = pubExecutor;
	}

	public ExecutorService getSubExecutor() {
		return subExecutor;
	}

	public void setSubExecutor(ExecutorService subExecutor) {
		this.subExecutor = subExecutor;
	}

	public ExecutorService getPingExecutor() {
		return pingExecutor;
	}

	public void setPingExecutor(ExecutorService pingExecutor) {
		this.pingExecutor = pingExecutor;
	}

	public NettyEventExcutor getNettyEventExcutor() {
		return nettyEventExcutor;
	}

	public void setNettyEventExcutor(NettyEventExcutor nettyEventExcutor) {
		this.nettyEventExcutor = nettyEventExcutor;
	}

	public ChannelEventListener getChannelEventListener() {
		return channelEventListener;
	}

	public void setChannelEventListener(ChannelEventListener channelEventListener) {
		this.channelEventListener = channelEventListener;
	}

	public MessageDispatcher getMessageDispatcher() {
		return messageDispatcher;
	}

	public void setMessageDispatcher(MessageDispatcher messageDispatcher) {
		this.messageDispatcher = messageDispatcher;
	}

	public FlowMessageStore getFlowMessageStore() {
		return flowMessageStore;
	}

	public void setFlowMessageStore(FlowMessageStore flowMessageStore) {
		this.flowMessageStore = flowMessageStore;
	}

	public SubscriptionMatcher getSubscriptionMatcher() {
		return subscriptionMatcher;
	}

	public void setSubscriptionMatcher(SubscriptionMatcher subscriptionMatcher) {
		this.subscriptionMatcher = subscriptionMatcher;
	}

	public WillMessageStore getWillMessageStore() {
		return willMessageStore;
	}

	public void setWillMessageStore(WillMessageStore willMessageStore) {
		this.willMessageStore = willMessageStore;
	}

	public RetainMessageStore getRetainMessageStore() {
		return retainMessageStore;
	}

	public void setRetainMessageStore(RetainMessageStore retainMessageStore) {
		this.retainMessageStore = retainMessageStore;
	}

	public OfflineMessageStore getOfflineMessageStore() {
		return offlineMessageStore;
	}

	public void setOfflineMessageStore(OfflineMessageStore offlineMessageStore) {
		this.offlineMessageStore = offlineMessageStore;
	}

	public SubscriptionStore getSubscriptionStore() {
		return subscriptionStore;
	}

	public void setSubscriptionStore(SubscriptionStore subscriptionStore) {
		this.subscriptionStore = subscriptionStore;
	}

	public SessionStore getSessionStore() {
		return sessionStore;
	}

	public void setSessionStore(SessionStore sessionStore) {
		this.sessionStore = sessionStore;
	}

	public ConnectPermission getConnectPermission() {
		return connectPermission;
	}

	public void setConnectPermission(ConnectPermission connectPermission) {
		this.connectPermission = connectPermission;
	}

	public PubSubPermission getPubSubPermission() {
		return pubSubPermission;
	}

	public void setPubSubPermission(PubSubPermission pubSubPermission) {
		this.pubSubPermission = pubSubPermission;
	}

	public ReSendMessageService getReSendMessageService() {
		return reSendMessageService;
	}

	public void setReSendMessageService(ReSendMessageService reSendMessageService) {
		this.reSendMessageService = reSendMessageService;
	}

	public SysMessageService getSysMessageService() {
		return sysMessageService;
	}

	public void setSysMessageService(SysMessageService sysMessageService) {
		this.sysMessageService = sysMessageService;
	}

}
