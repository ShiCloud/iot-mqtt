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
import org.iot.mqtt.broker.dispatcher.ClusterDispatcherMessage;
import org.iot.mqtt.broker.dispatcher.DefaultDispatcherMessage;
import org.iot.mqtt.broker.dispatcher.MessageDispatcher;
import org.iot.mqtt.broker.netty.ChannelEventListener;
import org.iot.mqtt.broker.netty.NettyEventExcutor;
import org.iot.mqtt.broker.recover.ReSendMessageService;
import org.iot.mqtt.broker.session.ConnectManager;
import org.iot.mqtt.broker.subscribe.DefaultSubscriptionTreeMatcher;
import org.iot.mqtt.broker.subscribe.SubscriptionMatcher;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.common.config.PerformanceConfig;
import org.iot.mqtt.common.utils.RejectHandler;
import org.iot.mqtt.common.utils.SnowFlake;
import org.iot.mqtt.common.utils.ThreadFactoryImpl;
import org.iot.mqtt.store.AbstractMqttStore;
import org.iot.mqtt.store.FlowMessageStore;
import org.iot.mqtt.store.OfflineMessageStore;
import org.iot.mqtt.store.RetainMessageStore;
import org.iot.mqtt.store.SessionStore;
import org.iot.mqtt.store.SubscriptionStore;
import org.iot.mqtt.store.WillMessageStore;
import org.iot.mqtt.store.rheakv.RheakvMqttStore;
import org.iot.mqtt.store.rocksdb.RDBMqttStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrokerRoom {

	private static Logger log = LoggerFactory.getLogger(BrokerRoom.class);

	LinkedBlockingQueue<Runnable> connectQueue;
	LinkedBlockingQueue<Runnable> pubQueue;
	LinkedBlockingQueue<Runnable> subQueue;
	LinkedBlockingQueue<Runnable> pingQueue;

	private ExecutorService connectExecutor;
	private ExecutorService pubExecutor;
	private ExecutorService subExecutor;
	private ExecutorService pingExecutor;

	private AbstractMqttStore abstractMqttStore;

	private NettyEventExcutor nettyEventExcutor;
	private ReSendMessageService reSendMessageService;
	
	private MqttConfig mqttConfig;
	private ConnectManager connectManager;
	private SnowFlake snowFlake;
	
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
	
	
	private void initMqttStore(MqttConfig mqttConfig) {
		switch (mqttConfig.getStoreType()) {
		case ROCKSDB:
			this.abstractMqttStore = new RDBMqttStore(mqttConfig);
			break;		
		case RHEAKV:
			this.snowFlake = new SnowFlake(mqttConfig.getDatacenterId(),mqttConfig.getMachineId());
			RheakvMqttStore rheakvMqttStore = new RheakvMqttStore(mqttConfig);
			this.abstractMqttStore = rheakvMqttStore;
			break;
		default:
			log.error("unknown store type");
			System.exit(-1);
			break;
		}
		try {
			abstractMqttStore.init();
		} catch (Exception e) {
			log.info("init store failure,exception=" + e);
		}
	}
	
	private void initDispatcher(MqttConfig mqttConfig) {
		switch (mqttConfig.getStoreType()) {
		case ROCKSDB:
			this.messageDispatcher = new DefaultDispatcherMessage(this);
			break;		
		case RHEAKV:
			this.messageDispatcher = new ClusterDispatcherMessage(this,(RheakvMqttStore)abstractMqttStore);
			break;
		default:
			log.error("unknown store type");
			System.exit(-1);
			break;
		}
	}
	
	
	public BrokerRoom(MqttConfig mqttConfig) {
		this.mqttConfig = mqttConfig;
		PerformanceConfig config = mqttConfig.getPerformanceConfig();
		this.connectQueue = new LinkedBlockingQueue<>(config.getClientSize()/5);
		this.pubQueue = new LinkedBlockingQueue<>(config.getQueueSize());
		this.subQueue = new LinkedBlockingQueue<>(config.getQueueSize());
		this.pingQueue = new LinkedBlockingQueue<>(config.getClientSize()/5);
		
		initMqttStore(mqttConfig);

		this.flowMessageStore = this.abstractMqttStore.getFlowMessageStore();
		this.willMessageStore = this.abstractMqttStore.getWillMessageStore();
		this.retainMessageStore = this.abstractMqttStore.getRetainMessageStore();
		this.offlineMessageStore = this.abstractMqttStore.getOfflineMessageStore();
		this.subscriptionStore = this.abstractMqttStore.getSubscriptionStore();
		this.sessionStore = this.abstractMqttStore.getSessionStore();

		this.connectManager = new ConnectManager();
		this.connectPermission = new DefaultConnectPermission(mqttConfig);
		this.pubSubPermission = new DefaultPubSubPermission();
		this.subscriptionMatcher = new DefaultSubscriptionTreeMatcher();
		this.reSendMessageService = new ReSendMessageService(this);
		this.channelEventListener = new ClientLifeCycleHookService(this);
		
		initDispatcher(mqttConfig);
		
		int coreThreadNum =config.getCoreThreadNum();
		
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
	}

	public ExecutorService getConnectExecutor() {
		return connectExecutor;
	}

	public ExecutorService getPubExecutor() {
		return pubExecutor;
	}

	public ExecutorService getSubExecutor() {
		return subExecutor;
	}

	public ExecutorService getPingExecutor() {
		return pingExecutor;
	}

	public NettyEventExcutor getNettyEventExcutor() {
		return nettyEventExcutor;
	}

	public ChannelEventListener getChannelEventListener() {
		return channelEventListener;
	}

	public MessageDispatcher getMessageDispatcher() {
		return messageDispatcher;
	}

	public FlowMessageStore getFlowMessageStore() {
		return flowMessageStore;
	}

	public SubscriptionMatcher getSubscriptionMatcher() {
		return subscriptionMatcher;
	}

	public WillMessageStore getWillMessageStore() {
		return willMessageStore;
	}

	public RetainMessageStore getRetainMessageStore() {
		return retainMessageStore;
	}

	public OfflineMessageStore getOfflineMessageStore() {
		return offlineMessageStore;
	}

	public SubscriptionStore getSubscriptionStore() {
		return subscriptionStore;
	}

	public SessionStore getSessionStore() {
		return sessionStore;
	}

	public ConnectPermission getConnectPermission() {
		return connectPermission;
	}

	public PubSubPermission getPubSubPermission() {
		return pubSubPermission;
	}

	public ReSendMessageService getReSendMessageService() {
		return reSendMessageService;
	}

	public MqttConfig getMqttConfig() {
		return mqttConfig;
	}

	public ConnectManager getConnectManager() {
		return connectManager;
	}

	public SnowFlake getSnowFlake() {
		return snowFlake;
	}
	
}
