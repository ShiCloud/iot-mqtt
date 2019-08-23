package org.iot.mqtt.broker;

import org.iot.mqtt.broker.netty.NettyConnectHandler;
import org.iot.mqtt.broker.netty.NettySslHandler;
import org.iot.mqtt.broker.processor.ConnectProcessor;
import org.iot.mqtt.broker.processor.DisconnectProcessor;
import org.iot.mqtt.broker.processor.PingProcessor;
import org.iot.mqtt.broker.processor.PubAckProcessor;
import org.iot.mqtt.broker.processor.PubCompProcessor;
import org.iot.mqtt.broker.processor.PubRecProcessor;
import org.iot.mqtt.broker.processor.PubRelProcessor;
import org.iot.mqtt.broker.processor.PublishProcessor;
import org.iot.mqtt.broker.processor.RequestProcessor;
import org.iot.mqtt.broker.processor.SubscribeProcessor;
import org.iot.mqtt.broker.processor.UnSubscribeProcessor;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.test.utils.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.timeout.IdleStateHandler;

public class BrokerService {

	private static Logger log = LoggerFactory.getLogger(BrokerService.class);

	MqttConfig mqttConfig;
	EventLoopGroup selectorGroup;
	EventLoopGroup ioGroup;
	Class<? extends ServerChannel> clazz;

	BrokerRoom brokerRoom;

	RequestProcessor connectProcessor;
	RequestProcessor disconnectProcessor;
	RequestProcessor pingProcessor;
	RequestProcessor publishProcessor;
	RequestProcessor pubRelProcessor;
	RequestProcessor subscribeProcessor;
	RequestProcessor unSubscribeProcessor;
	RequestProcessor pubRecProcessor;
	RequestProcessor pubAckProcessor;
	RequestProcessor pubCompProcessor;

	public BrokerService(MqttConfig mqttConfig) {
		this.mqttConfig = mqttConfig;

		this.brokerRoom = new BrokerRoom(mqttConfig);

		connectProcessor = new ConnectProcessor(brokerRoom);
		disconnectProcessor = new DisconnectProcessor(brokerRoom);
		pingProcessor = new PingProcessor();
		publishProcessor = new PublishProcessor(brokerRoom);
		pubRelProcessor = new PubRelProcessor(brokerRoom);
		subscribeProcessor = new SubscribeProcessor(brokerRoom);
		unSubscribeProcessor = new UnSubscribeProcessor(brokerRoom);
		pubRecProcessor = new PubRecProcessor(brokerRoom);
		pubAckProcessor = new PubAckProcessor(brokerRoom);
		pubCompProcessor = new PubCompProcessor(brokerRoom);

		if (!mqttConfig.isUseEpoll()) {
			this.selectorGroup = new NioEventLoopGroup(mqttConfig.getSelectorThreadNum(),
					new ThreadFactoryImpl("SelectorEventGroup"));
			this.ioGroup = new NioEventLoopGroup(mqttConfig.getIoThreadNum(), new ThreadFactoryImpl("IOEventGroup"));
			this.clazz = NioServerSocketChannel.class;
		} else {
			this.selectorGroup = new EpollEventLoopGroup(mqttConfig.getSelectorThreadNum(),
					new ThreadFactoryImpl("SelectorEventGroup"));
			this.ioGroup = new EpollEventLoopGroup(mqttConfig.getIoThreadNum(), new ThreadFactoryImpl("IOEventGroup"));
			this.clazz = EpollServerSocketChannel.class;
		}

	}

	public void start() {
		this.brokerRoom.start();
		startTcpServer(mqttConfig.isStartSslTcp(), mqttConfig.getTcpPort());
	}

	public void shutdown() {
		if (selectorGroup != null) {
			selectorGroup.shutdownGracefully();
		}
		if (ioGroup != null) {
			ioGroup.shutdownGracefully();
		}

		this.brokerRoom.shutdown();
	}

	private void startTcpServer(boolean useSsl, Integer port) {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(selectorGroup, ioGroup).channel(clazz)
				.option(ChannelOption.SO_BACKLOG, mqttConfig.getTcpBackLog())
				.childOption(ChannelOption.TCP_NODELAY, mqttConfig.isTcpNoDelay())
				.childOption(ChannelOption.SO_SNDBUF, mqttConfig.getTcpSndBuf())
				.option(ChannelOption.SO_RCVBUF, mqttConfig.getTcpRcvBuf())
				.option(ChannelOption.SO_REUSEADDR, mqttConfig.isTcpReuseAddr())
				.childOption(ChannelOption.SO_KEEPALIVE, mqttConfig.isTcpKeepAlive())
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						ChannelPipeline pipeline = socketChannel.pipeline();
						if (useSsl) {
							pipeline.addLast("ssl",
									NettySslHandler.getSslHandler(socketChannel, mqttConfig.isUseClientCA(),
											mqttConfig.getSslKeyStoreType(), mqttConfig.getSslKeyFilePath(),
											mqttConfig.getSslManagerPwd(), mqttConfig.getSslStorePwd()));
						}
						pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 0, 0))
								.addLast("mqttEncoder", MqttEncoder.INSTANCE)
								.addLast("mqttDecoder", new MqttDecoder(mqttConfig.getMaxMsgSize()))
								.addLast("nettyConnectionManager", new NettyConnectHandler(brokerRoom.getNettyEventExcutor()))
								.addLast("nettyMqttHandler", new NettyMqttHandler());
					}
				});
		if (mqttConfig.isPooledByteBufAllocatorEnable()) {
			bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		}
		try {
			bootstrap.bind(port).sync();
			log.info("[Server] -> start tcp server {} success,port = {}", useSsl ? "with ssl" : "", port);
		} catch (InterruptedException ex) {
			log.error("[Server] -> start tcp server {} failure.cause={}", useSsl ? "with ssl" : "", ex);
		}
	}

	class NettyMqttHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object obj) {
			MqttMessage mqttMessage = (MqttMessage) obj;
			if (mqttMessage != null && mqttMessage.decoderResult().isSuccess()) {
				prcoessMsg(ctx, mqttMessage);
			} else {
				ctx.close();
			}
		}

		private void prcoessMsg(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
			MqttMessageType messageType = mqttMessage.fixedHeader().messageType();
			log.debug("[Server] -> receive mqtt code,type:{}", messageType.name());
			switch (messageType) {
			case CONNECT:
				brokerRoom.getConnectExecutor().submit(getTask(connectProcessor, ctx, mqttMessage));
				break;
			case DISCONNECT:
				brokerRoom.getConnectExecutor().submit(getTask(disconnectProcessor, ctx, mqttMessage));
				break;
			case SUBSCRIBE:
				brokerRoom.getSubExecutor().submit(getTask(subscribeProcessor, ctx, mqttMessage));
				break;
			case UNSUBSCRIBE:
				brokerRoom.getSubExecutor().submit(getTask(unSubscribeProcessor, ctx, mqttMessage));
				break;	
			case PUBLISH:
				brokerRoom.getPubExecutor().submit(getTask(publishProcessor, ctx, mqttMessage));
				break;
			case PUBACK:
				brokerRoom.getPubExecutor().submit(getTask(pubAckProcessor, ctx, mqttMessage));
				break;
			case PUBREL:
				brokerRoom.getPubExecutor().submit(getTask(pubRelProcessor, ctx, mqttMessage));
				break;
			case PUBREC:
				brokerRoom.getSubExecutor().submit(getTask(pubRecProcessor, ctx, mqttMessage));
				break;	
			case PUBCOMP:
				brokerRoom.getSubExecutor().submit(getTask(pubCompProcessor, ctx, mqttMessage));
				break;
			case PINGREQ:
				brokerRoom.getPingExecutor().submit(getTask(pingProcessor, ctx, mqttMessage));
				break;	
			default:
				break;
			}
		}

		private Runnable getTask(RequestProcessor processor, ChannelHandlerContext ctx, MqttMessage mqttMessage) {
			return new Runnable() {
				@Override
				public void run() {
					processor.processRequest(ctx, mqttMessage);
				}
			};
		}
	}
}
