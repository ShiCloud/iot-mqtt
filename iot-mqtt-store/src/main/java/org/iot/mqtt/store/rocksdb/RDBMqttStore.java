package org.iot.mqtt.store.rocksdb;

import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.AbstractMqttStore;

public class RDBMqttStore extends AbstractMqttStore {

    private RDB rdb;


    public RDBMqttStore(MqttConfig mqttConfig){
        this.rdb = new RDB(mqttConfig);
    }

    @Override
    public void init() throws Exception {
        this.getRdb().init();
        this.flowMessageStore = new RDBFlowMessageStore(getRdb());
        this.willMessageStore = new RDBWillMessageStore(getRdb());
        this.retainMessageStore = new RDBRetainMessageStore(getRdb());
        this.offlineMessageStore = new RDBOfflineMessageStore(getRdb());
        this.subscriptionStore = new RDBSubscriptionStore(getRdb());
        this.sessionStore = new RDBSessionStore(getRdb());
    }

    @Override
    public void shutdown() {
        this.getRdb().close();
    }

	public RDB getRdb() {
		return rdb;
	}
    
    
	
}
