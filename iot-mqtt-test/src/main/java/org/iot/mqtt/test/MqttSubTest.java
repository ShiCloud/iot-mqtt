package org.iot.mqtt.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.iot.mqtt.common.config.TestConfig;
import org.iot.mqtt.handler.MqttBaseHandler;
import org.iot.mqtt.test.utils.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shifeng on 2018/10/31
 *
 */
public class MqttSubTest extends MqttBaseHandler {
	private static Logger logger = LoggerFactory.getLogger(MqttSubTest.class);

	private static final String topic0 = "/QOS0";
	private static final String topic1 = "/QOS1";
	private static final String topic2 = "/QOS2";
	private static AtomicInteger count = new AtomicInteger();

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			logger.error("please set config path");
			System.exit(-1);
		}
		MqttSubTest handler = new MqttSubTest();
		TestConfig properties = new TestConfig(args[0]);
		Topic[] topics = new Topic[] { new Topic(topic0, QoS.AT_MOST_ONCE), new Topic(topic1, QoS.AT_LEAST_ONCE),
				new Topic(topic2, QoS.EXACTLY_ONCE) };
		handler.init(properties, topics, "MqttSubTest", false);
		logger.info("MqttSubTest testConn inited");
		Thread.sleep(Integer.MAX_VALUE);
		
	}
	
	@Override
	public void processInput(String msg) {
		System.out.println(new String(ByteUtil.hexStr2Bytes(msg))+" count: "+count.getAndIncrement());
	}
}
