package org.iot.mqtt.test;

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
public class MqttSysTest extends MqttBaseHandler {
	private static Logger logger = LoggerFactory.getLogger(MqttSysTest.class);

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			logger.error("please set config path");
			System.exit(-1);
		}
		MqttSysTest handler = new MqttSysTest();
		TestConfig properties = new TestConfig(args[0]);
		String topic = "$SYS/";
		Topic[] topics = new Topic[]{new Topic(topic, QoS.AT_MOST_ONCE)};
		handler.init(properties,topics,"MqttSysTest",false);
		logger.info("MqttSysTest testConn inited");
		Thread.sleep(Integer.MAX_VALUE);
		
	}
	
	@Override
	public void processInput(String msg) {
		logger.info(new String(ByteUtil.hexStr2Bytes(msg)));
	}
}
