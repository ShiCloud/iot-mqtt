package org.iot.mqtt.test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.iot.mqtt.common.config.TestConfig;
import org.iot.mqtt.handler.MqttBaseHandler;
import org.iot.mqtt.test.support.TestSize;
import org.iot.mqtt.test.utils.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shifeng on 2018/10/31
 *
 */
public class MqttMutiClientSubTest extends MqttBaseHandler {
	private static Logger logger = LoggerFactory.getLogger(MqttMutiClientSubTest.class);

	private static final String topic0 = "QOS0";
	private static final String topic1 = "QOS1";
	private static final String topic2 = "QOS2";
	private static AtomicInteger count = new AtomicInteger();

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			logger.error("please set config path");
			System.exit(-1);
		}
		if(args.length > 1) {
			TestSize.setClientSize(Integer.valueOf(args[1]));
		}
		
		TestConfig properties = new TestConfig(args[0]);
		
		ForkJoinPool forkJoinPool = new ForkJoinPool(TestSize.getClientSize());
		ForkJoinTask<?>[] list = new ForkJoinTask[TestSize.getClientSize()];
		for (int i = 0; i < TestSize.getClientSize(); i++) {
			int index = i;
			ForkJoinTask<?> fork = forkJoinPool.submit(new Thread(new Runnable() {
				@Override
				public void run() {
					MqttMutiClientSubTest handler = new MqttMutiClientSubTest();
					String clientId = "client"+index;
					String clientName = clientId+"Sub";
					Topic[] topics = new Topic[] { new Topic(clientId+topic0, QoS.AT_MOST_ONCE), 
							new Topic(clientId+topic1, QoS.AT_LEAST_ONCE),
							new Topic(clientId+topic2, QoS.EXACTLY_ONCE) };
					handler.init(properties, topics, clientName, false);
					logger.info(clientName+" testConn inited");
				}
			}));
			list[i] = fork;
		}
		for (int i = 0; i < TestSize.getClientSize(); i++) {
			list[i].join();
		}
		Thread.sleep(Integer.MAX_VALUE);
	}
	
	@Override
	public void processInput(String msg) {
		logger.info(new String(ByteUtil.hexStr2Bytes(msg))+" count: "+count.getAndIncrement());
	}
}
