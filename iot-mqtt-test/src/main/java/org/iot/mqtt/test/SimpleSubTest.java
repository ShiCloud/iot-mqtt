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
package org.iot.mqtt.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.iot.mqtt.test.support.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class SimpleSubTest {
	private static Logger logger = LoggerFactory.getLogger(SimpleSubTest.class);
	private static int clientSize = 3;
	public static AtomicInteger RCOUNT = new AtomicInteger(1);
	public static MqttClient[] clientList = new MqttClient[clientSize];
	
	public static IMqttMessageListener messageListener = new IMqttMessageListener() {
		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			logger.info(String.format("topic:%s qos:%d %S %d", 
					topic,message.getQos(),new String(message.getPayload()),RCOUNT.getAndIncrement()));
		}
	};
	
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		for (int i = 0; i < clientSize; i++) {
			String path = String.format("src/main/resources/cluster/client%d.yaml", (i%3)+1);
			MqttClient client = BaseTest.createClient(path,"subId"+i);
            clientList[i] = client;
            for (int j = 0; j < BaseTest.topics.length; j++) {
            	int qos = j%3;
            	client.subscribe(BaseTest.topics[qos],qos,messageListener);
            }
		}
		logger.info("total time "+(System.currentTimeMillis() - start));
	}
}
