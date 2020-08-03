package org.iot.mqtt.store.rocksdb;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import org.iot.mqtt.common.bean.Subscription;
import org.iot.mqtt.common.utils.SerializeHelper;
import org.iot.mqtt.store.StorePrefix;
import org.iot.mqtt.store.SubscriptionStore;
import org.rocksdb.ColumnFamilyHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDBSubscriptionStore implements SubscriptionStore {

    private static final Logger log = LoggerFactory.getLogger(RDBSubscriptionStore.class);

    private RDB rdb;

    public RDBSubscriptionStore(RDB rdb){
        this.rdb = rdb;
    }
    

	@Override
    public boolean storeSubscription(String clientId, Subscription subscription) {
        try{
            this.rdb.putAsync(columnFamilyHandle(),key(clientId,subscription.getTopic()),SerializeHelper.serialize(subscription));
            return true;
        }catch(Exception ex){
            log.warn("Store subscription failure,clientId={},Subscription={}",clientId,subscription);
        }
        return false;
    }

    @Override
    public Collection<Subscription> getSubscriptions(String clientId) {
        Collection<byte[]> values = this.rdb.getByPrefix(columnFamilyHandle(),keyPrefix(clientId));
        Collection<Subscription> subscriptions = new ArrayList<>(values.size());
        for(byte[] value : values){
            subscriptions.add(SerializeHelper.deserialize(value,Subscription.class));
        }
        return subscriptions;
    }

    @Override
    public boolean clearSubscription(String clientId) {
    	try{
            this.rdb.deleteByPrefix(columnFamilyHandle(),keyPrefix(clientId));
            return true;
        }catch (Exception ex){
            log.warn("Clear subscription failure,clientId={}",clientId);
            return false;
        }
    }

    @Override
    public boolean removeSubscription(String clientId, String topic) {
        try{
            this.rdb.delete(columnFamilyHandle(),key(clientId,topic));
            return true;
        }catch (Exception ex){
            log.warn("Remove subscription failure,clientId={}",clientId);
            return false;
        }
    }

    private byte[] keyPrefix(String clientId){
        return (StorePrefix.SUBSCRIPTION + clientId).getBytes(Charset.forName("UTF-8"));
    }

    private byte[] key(String clientId,String topic){
        return (StorePrefix.SUBSCRIPTION + clientId + topic).getBytes(Charset.forName("UTF-8"));
    }


    private ColumnFamilyHandle columnFamilyHandle(){
        return this.rdb.getColumnFamilyHandle(StorePrefix.SUBSCRIPTION);
    }
}
