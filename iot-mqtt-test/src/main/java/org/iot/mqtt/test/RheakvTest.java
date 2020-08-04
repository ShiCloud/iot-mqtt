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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.config.Yaml;
import org.iot.mqtt.common.utils.SnowFlake;
import org.iot.mqtt.store.StorePrefix;
import org.iot.mqtt.store.rheakv.Node;
import org.iot.mqtt.store.rheakv.RheakvUtils;
import org.iot.mqtt.test.support.BaseTest;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class RheakvTest {

	public static RheakvTest test = new RheakvTest();
	public static AtomicInteger WCOUNT = new AtomicInteger(1);
	public static AtomicInteger RCOUNT = new AtomicInteger(1);
	private static SnowFlake snowFlake = new SnowFlake(1, 1);
	
	public static void main(String[] args) throws Exception {
		List<Node> servers = new ArrayList<>();
		List<RheaKVStore> clients = new ArrayList<>();
		Map<Integer,RheaKVStore> stores = new ConcurrentHashMap<>();
		for (int i = 0; i < BaseTest.serverPath.length; i++) {
			final int num = i;
			new Thread(new Runnable(){  
	            public void run(){  
	            	Node node = RheakvUtils.initServer(Yaml.readConfig(BaseTest.serverPath[num], RheaKVStoreOptions.class));
            		servers.add(node);
            		RheaKVStore rheaKVStore = node.getRheaKVStore();
            		clients.add(rheaKVStore);
            		stores.put(num,rheaKVStore);
            		System.out.println("store"+num+" put ");
	            }}).start();
		}
		Thread.sleep(5000);
		final int msgNums = 5000;
		for (int i = 0; i < BaseTest.serverPath.length; i++) {
			final int num = i;
			final String clientId = "store"+num;
			new Thread(new Runnable(){  
	            public void run(){
	            	Collection<Message> list = RheakvUtils.popByPrefix(stores.get(num), clientId, Message.class, 100);
	            	while (true) {
	            		if(list==null || list.size() == 0) {
	            			try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
	            		}else {
	            			list.stream().forEach(m->{
	            				System.out.println(String.format("clientId %s read clientId %s MsgId %d",
		            					clientId,m.getClientId(),m.getMsgId())+" RCOUNT "+RCOUNT.getAndIncrement());
	            			});
	            		}
	            		list =  RheakvUtils.popByPrefix(stores.get(num), clientId, Message.class, 100);
					}
	            }}).start();			
			new Thread(new Runnable(){  
	            public void run(){
	            	for (int j = 0; j < msgNums; j++) {
	            		Message m = new Message();
						m.setClientId(clientId);
	            		int wc = WCOUNT.getAndIncrement();
						m.setMsgId(wc);
						String key = clientId+StorePrefix.SPLIT+RheakvUtils.getFormatId(snowFlake.nextId());
						RheakvUtils.add(stores.get(num),key, m);
	            		System.out.println(String.format("clientId %s write clientId %s MsgId %d ",
	            				clientId,m.getClientId(),m.getMsgId())+" WCOUNT "+wc);
					}
	            }}).start();
		}	
	}
}
