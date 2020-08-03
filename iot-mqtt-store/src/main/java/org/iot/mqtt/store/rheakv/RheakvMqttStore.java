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

import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.common.config.Yaml;
import org.iot.mqtt.store.AbstractMqttStore;
import org.iot.mqtt.store.rocksdb.RDB;
import org.iot.mqtt.store.rocksdb.RDBFlowMessageStore;
import org.iot.mqtt.store.rocksdb.RDBOfflineMessageStore;
import org.iot.mqtt.store.rocksdb.RDBRetainMessageStore;
import org.iot.mqtt.store.rocksdb.RDBSessionStore;
import org.iot.mqtt.store.rocksdb.RDBSubscriptionStore;
import org.iot.mqtt.store.rocksdb.RDBWillMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class RheakvMqttStore extends AbstractMqttStore {

	private static final Logger log = LoggerFactory.getLogger(RheakvMqttStore.class);
	private Node node;
	private RheaKVStore rheaKVStore;
	private RDB rdb;
	private RheaKVStoreOptions rheaKVConfig;
	public RheakvMqttStore(MqttConfig mqttConfig) {
		this.rdb = new RDB(mqttConfig);
		this.rheaKVConfig = Yaml.readConfig(mqttConfig.getConfigPath(),RheaKVStoreOptions.class);
		this.node = RheakvUtils.initServer(rheaKVConfig);
		this.rheaKVStore = this.getNode().getRheaKVStore();
	}
	
	@Override
	public void init() throws Exception {
		this.rdb.init();
		this.flowMessageStore = new RDBFlowMessageStore(rdb);
        this.willMessageStore = new RDBWillMessageStore(rdb);
        this.retainMessageStore = new RDBRetainMessageStore(rdb);
        this.offlineMessageStore = new RDBOfflineMessageStore(rdb);
        this.subscriptionStore = new RDBSubscriptionStore(rdb);
        this.sessionStore = new RDBSessionStore(rdb);
        log.info("RheakvMqttStore init success. ClusterName {} ",rheaKVConfig.getClusterName());
	}

	@Override
    public void shutdown() {
        this.rheaKVStore.shutdown();
        this.getNode().stop();
    }
	
	public Node getNode() {
		return node;
	}

	public RheaKVStore getRheaKVStore() {
		return rheaKVStore;
	}
	
}
