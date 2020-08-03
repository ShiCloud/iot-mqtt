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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.iot.mqtt.test.support.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class SimpleSendTest {
	private static Logger logger = LoggerFactory.getLogger(SimpleSendTest.class);
	private static int clientSize = 30;
	private static int poolSize = 1000;
	private static int msgNums = 1000;
	private static int sleepTimes = 200;
	public static AtomicInteger WCOUNT = new AtomicInteger(1);
	public static MqttClient[] clientList = new MqttClient[clientSize];
	public static ForkJoinPool forkJoinPool = new ForkJoinPool(poolSize);
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < clientSize; i++) {
			final int index = i;
			forkJoinPool.submit(()->{  
				String path = String.format("src/main/resources/cluster/client%d.yaml", (index%3)+1);
				try {
					MqttClient client = BaseTest.createClient(path,"sendId"+index);
					clientList[index] = client;
				} catch (Exception e) {
					e.printStackTrace();
				}
	            
			}).get();
		}
		
		long start = System.currentTimeMillis();
		for (int i = 0; i < msgNums; i++) {
			forkJoinPool.submit(()->{
				for (int j = 0; j < clientSize; j++) {
					int qos = j%3;
					String str = String.format("sendId%d 发送 %s num %d", j,BaseTest.TOPIC_PREFIX+qos,WCOUNT.getAndIncrement());
					try {
						clientList[j].publish(BaseTest.TOPIC_PREFIX+qos, str.getBytes("utf-8"),qos, false);
						logger.info(str);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).get();
			Thread.sleep(sleepTimes);
		}
		logger.info("total time "+(System.currentTimeMillis() - start));
	}


}
