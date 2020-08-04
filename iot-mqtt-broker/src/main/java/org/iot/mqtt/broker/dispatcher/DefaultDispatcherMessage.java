package org.iot.mqtt.broker.dispatcher;


import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.store.rocksdb.RDBAppendMessageStore;
import org.iot.mqtt.store.rocksdb.RDBMqttStore;


public class DefaultDispatcherMessage extends MessageDispatcher {

    public DefaultDispatcherMessage(BrokerRoom brokerRoom,RDBMqttStore store) {
        super(brokerRoom,new RDBAppendMessageStore(store.getRdb()));
    }
}
