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
package org.iot.mqtt.broker;

import java.util.concurrent.ExecutorService;

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
import org.iot.mqtt.common.config.NettyConfig;
import org.iot.mqtt.common.config.PerformanceConfig;
import org.iot.mqtt.common.utils.ThreadFactoryImpl;
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

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class BrokerService {

	private static Logger log = LoggerFactory.getLogger(BrokerService.class);

	private MqttConfig mqttConfig;
	private EventLoopGroup selectorGroup;
	private EventLoopGroup ioGroup;
	private Class<? extends ServerChannel> clazz;

	private BrokerRoom brokerRoom;

	private RequestProcessor connectProcessor;
	private RequestProcessor disconnectProcessor;
	private RequestProcessor pingProcessor;
	private RequestProcessor publishProcessor;
	private RequestProcessor pubRelProcessor;
	private RequestProcessor subscribeProcessor;
	private RequestProcessor unSubscribeProcessor;
	private RequestProcessor pubRecProcessor;
	private RequestProcessor pubAckProcessor;
	private RequestProcessor pubCompProcessor;

	

	public BrokerService(MqttConfig mqttConfig) {
		this.mqttConfig = mqttConfig;
		
		this.brokerRoom = new BrokerRoom(mqttConfig);

		this.connectProcessor = new ConnectProcessor(brokerRoom);
		this.disconnectProcessor = new DisconnectProcessor(brokerRoom);
		this.pingProcessor = new PingProcessor(brokerRoom);
		this.publishProcessor = new PublishProcessor(brokerRoom);
		this.pubRelProcessor = new PubRelProcessor(brokerRoom);
		this.subscribeProcessor = new SubscribeProcessor(brokerRoom);
		this.unSubscribeProcessor = new UnSubscribeProcessor(brokerRoom);
		this.pubRecProcessor = new PubRecProcessor(brokerRoom);
		this.pubAckProcessor = new PubAckProcessor(brokerRoom);
		this.pubCompProcessor = new PubCompProcessor(brokerRoom);

		PerformanceConfig performance = mqttConfig.getPerformanceConfig();
		if (!performance.isUseEpoll()) {
			this.selectorGroup = new NioEventLoopGroup(performance.getSelectorThreadNum(),
					new ThreadFactoryImpl(mqttConfig.getServerName()+" SelectorEventGroup"));
			this.ioGroup = new NioEventLoopGroup(performance.getIoThreadNum(), new ThreadFactoryImpl("IOEventGroup"));
			this.clazz = NioServerSocketChannel.class;
		} else {
			this.selectorGroup = new EpollEventLoopGroup(performance.getSelectorThreadNum(),
					new ThreadFactoryImpl("SelectorEventGroup"));
			this.ioGroup = new EpollEventLoopGroup(performance.getIoThreadNum(), new ThreadFactoryImpl("IOEventGroup"));
			this.clazz = EpollServerSocketChannel.class;
		}

	}

	public void start() {
		brokerRoom.start();
		startTcpServer(mqttConfig.isStartSslTcp(), mqttConfig.getTcpPort());
	}

	public void shutdown() {
		if (selectorGroup != null) {
			selectorGroup.shutdownGracefully();
		}
		if (ioGroup != null) {
			ioGroup.shutdownGracefully();
		}

		brokerRoom.shutdown();
	}

	private void startTcpServer(boolean useSsl, Integer port) {
		ServerBootstrap bootstrap = new ServerBootstrap();
		NettyConfig nettyConfig = mqttConfig.getNettyConfig();
		PerformanceConfig performance = mqttConfig.getPerformanceConfig();
		bootstrap.group(selectorGroup, ioGroup).channel(clazz)
				.option(ChannelOption.SO_BACKLOG, performance.getTcpBackLog())
				.childOption(ChannelOption.TCP_NODELAY, performance.isTcpNoDelay())
				.childOption(ChannelOption.SO_SNDBUF, performance.getTcpSndBuf())
				.option(ChannelOption.SO_RCVBUF, performance.getTcpRcvBuf())
				.option(ChannelOption.SO_REUSEADDR, performance.isTcpReuseAddr())
				.childOption(ChannelOption.SO_KEEPALIVE, performance.isTcpKeepAlive())
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						ChannelPipeline pipeline = socketChannel.pipeline();
						if (useSsl) {
							pipeline.addLast("ssl",
									NettySslHandler.getSslHandler(socketChannel, nettyConfig.isUseClientCA(),
											nettyConfig.getSslKeyStoreType(), nettyConfig.getSslKeyFilePath(),
											nettyConfig.getSslManagerPwd(), nettyConfig.getSslStorePwd()));
						}
						pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 0, 0))
								.addLast("mqttEncoder", MqttEncoder.INSTANCE)
								.addLast("mqttDecoder", new MqttDecoder(nettyConfig.getMaxMsgSize()))
								.addLast("nettyConnectionManager", new NettyConnectHandler(brokerRoom.getNettyEventExcutor()))
								.addLast("nettyMqttHandler", new NettyMqttHandler());
					}
				});
		if (performance.isPooledByteBufAllocatorEnable()) {
			bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		}
		try {
			bootstrap.bind(port).sync();
			log.info("[{}] -> start tcp server success,port = {} {}", mqttConfig.getServerName(), port,(useSsl ? "with ssl" : ""));
		} catch (InterruptedException ex) {
			log.error("[{}] -> start tcp server failure.cause={} {}", mqttConfig.getServerName(), ex,(useSsl ? "with ssl" : ""));
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
			switch (messageType) {
			case CONNECT:
				prcoess(brokerRoom.getConnectExecutor(), connectProcessor, ctx, mqttMessage);
				break;
			case DISCONNECT:
				prcoess(brokerRoom.getConnectExecutor(), disconnectProcessor, ctx, mqttMessage);
				break;
			case SUBSCRIBE:
				prcoess(brokerRoom.getSubExecutor(), subscribeProcessor, ctx, mqttMessage);
				break;
			case UNSUBSCRIBE:
				prcoess(brokerRoom.getSubExecutor(), unSubscribeProcessor, ctx, mqttMessage);
				break;	
			case PUBLISH:
				prcoess(brokerRoom.getPubExecutor(), publishProcessor, ctx, mqttMessage);
				break;
			case PUBACK:
				prcoess(brokerRoom.getPubExecutor(), pubAckProcessor, ctx, mqttMessage);
				break;
			case PUBREL:
				prcoess(brokerRoom.getPubExecutor(), pubRelProcessor, ctx, mqttMessage);
				break;
			case PUBREC:
				prcoess(brokerRoom.getSubExecutor(), pubRecProcessor, ctx, mqttMessage);
				break;	
			case PUBCOMP:
				prcoess(brokerRoom.getSubExecutor(), pubCompProcessor, ctx, mqttMessage);
				break;
			case PINGREQ:
				prcoess(brokerRoom.getPingExecutor(), pingProcessor, ctx, mqttMessage);
				break;	
			default:
				break;
			}
		}

		private void prcoess(ExecutorService executor,RequestProcessor processor, 
				ChannelHandlerContext ctx, MqttMessage mqttMessage) {
			executor.submit(()->processor.processRequest(ctx, mqttMessage));
		}
	}
}
