package org.iot.mqtt.store.rocksdb;

import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.AbstractMqttStore;
import org.iot.mqtt.store.rocksdb.db.RDB;

public class RDBMqttStore extends AbstractMqttStore {

    private RDB rdb;


    public RDBMqttStore(MqttConfig mqttConfig){
        this.rdb = new RDB(mqttConfig);
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
    }

    @Override
    public void shutdown() {
        this.rdb.close();
    }


}
