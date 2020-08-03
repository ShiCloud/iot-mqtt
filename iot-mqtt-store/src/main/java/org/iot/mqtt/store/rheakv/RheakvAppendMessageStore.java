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
package org.iot.mqtt.store.rheakv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.common.utils.SerializeHelper;
import org.iot.mqtt.common.utils.SnowFlake;
import org.iot.mqtt.store.StorePrefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class RheakvAppendMessageStore {
	private static final Logger log = LoggerFactory.getLogger(RheakvAppendMessageStore.class);
	
	private SnowFlake snowFlake;
    private RheaKVStore rheaKVStore;
    private String[] serverList;
    private String serverName;
    
    String keyPrefix(String server){
    	return StorePrefix.MESSAGE+server+StorePrefix.SPLIT;
    }
    
    String key(String server){
    	return keyPrefix(server)+RheakvUtils.getFormatId(snowFlake.nextId());
    }
    
    public RheakvAppendMessageStore(MqttConfig mqttConfig,RheakvMqttStore store,SnowFlake snowFlake){
    	this.snowFlake= snowFlake;
    	this.rheaKVStore = store.getRheaKVStore();
    	String ip = store.getNode().getRheaKVConfig().getStoreEngineOptions().getServerAddress().getIp();
    	int port = store.getNode().getRheaKVConfig().getStoreEngineOptions().getServerAddress().getPort();
    	this.serverList = store.getNode().getRheaKVConfig().getInitialServerList().split(",");
    	this.serverName = ip+":"+port;
    }
    
	public boolean offer(Message message) {
		byte[] serialize = SerializeHelper.serialize(message);
		List<KVEntry> entries = new ArrayList<>();
		for (int i = 0; i < serverList.length; i++) {
			String key = key(serverList[i]);
			log.debug("offer key:"+key);
			entries.add(new KVEntry(key.getBytes(),serialize));
		}
		return rheaKVStore.bPut(entries);
	}
	
	public Collection<Message> pop(int nums) {
		return RheakvUtils.popByPrefix(rheaKVStore,keyPrefix(serverName),Message.class,nums);
	}
}
