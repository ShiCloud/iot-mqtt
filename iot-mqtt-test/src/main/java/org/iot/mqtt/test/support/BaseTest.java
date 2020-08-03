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
package org.iot.mqtt.test.support;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.iot.mqtt.common.config.Yaml;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class BaseTest{
	
	public final ReentrantLock lock = new ReentrantLock();
	public final Condition stop = lock.newCondition();
	public final static String TOPIC_PREFIX = "/QOS";
	
	public static String[] serverPath = new String[] {
			"src/main/resources/cluster/server1.yaml",
			"src/main/resources/cluster/server2.yaml",
			"src/main/resources/cluster/server3.yaml" };
	public static String[] topics = 
			new String[]{BaseTest.TOPIC_PREFIX+0,BaseTest.TOPIC_PREFIX+1,BaseTest.TOPIC_PREFIX+2};
	
	public static MqttClient createClient(String configPath,String clientId) throws MqttException, MqttSecurityException {
		TestConfig config = Yaml.readConfig(configPath, TestConfig.class);
		MemoryPersistence persistence = new MemoryPersistence();
		MqttClient client = new MqttClient(config.getUrl(), clientId, persistence);
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		connOpts.setAutomaticReconnect(true);
		connOpts.setConnectionTimeout(config.getReconnectDelay());
		connOpts.setKeepAliveInterval(config.getKeepAlive());
		connOpts.setUserName(config.getUsername());
		connOpts.setPassword(config.getPassword().toCharArray());
		client.connect(connOpts);
		return client;
	}

}
