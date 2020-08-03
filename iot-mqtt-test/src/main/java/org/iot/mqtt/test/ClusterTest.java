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

import org.iot.mqtt.broker.BrokerStartup;
import org.iot.mqtt.test.support.BaseTest;

import ch.qos.logback.core.joran.spi.JoranException;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class ClusterTest {
	public static String[] serverPath = new String[] {
			"src/main/resources/cluster/server1.yaml",
			"src/main/resources/cluster/server2.yaml",
			"src/main/resources/cluster/server3.yaml" };
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < serverPath.length; i++) {
			final int num = i;
			new Thread(new Runnable(){  
	            public void run(){  
	            	try {
						BrokerStartup.start(serverPath[num]);
					} catch (JoranException e) {
						e.printStackTrace();
					}
	            }}).start();  
		}
	}
}
