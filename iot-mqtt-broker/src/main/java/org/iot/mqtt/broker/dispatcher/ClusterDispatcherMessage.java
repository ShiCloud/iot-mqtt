package org.iot.mqtt.broker.dispatcher;


import org.iot.mqtt.broker.BrokerRoom;
import org.iot.mqtt.store.rheakv.RheakvAppendMessageStore;
import org.iot.mqtt.store.rheakv.RheakvMqttStore;


public class ClusterDispatcherMessage extends MessageDispatcher {
    
    public ClusterDispatcherMessage(BrokerRoom brokerRoom, RheakvMqttStore store) {
        super(brokerRoom, new RheakvAppendMessageStore(store,brokerRoom.getSnowFlake()));
    }
}
