package org.iot.mqtt.store.rocksdb;

import java.nio.charset.Charset;

import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.utils.SerializeHelper;
import org.iot.mqtt.store.StorePrefix;
import org.iot.mqtt.store.WillMessageStore;
import org.rocksdb.ColumnFamilyHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDBWillMessageStore implements WillMessageStore {

    private static final Logger log = LoggerFactory.getLogger(RDBWillMessageStore.class);

    private RDB rdb;

    public RDBWillMessageStore(RDB rdb){
        this.rdb = rdb;
    }

    @Override
    public Message getWillMessage(String clientId) {
        byte[] value = this.rdb.get(columnFamilyHandle(),key(clientId));
        if(value == null){
            log.warn("The will message is not exist,clientId = {}",clientId);
            return null;
        }
        return SerializeHelper.deserialize(value,Message.class);
    }

    @Override
    public boolean hasWillMessage(String clientId) {
        return this.rdb.get(columnFamilyHandle(),key(clientId)) != null;
    }

    @Override
    public void storeWillMessage(String clientId, Message message) {
        this.rdb.putAsync(columnFamilyHandle(),key(clientId),SerializeHelper.serialize(message));
    }

    @Override
    public boolean removeWillMessage(String clientId) {
        byte[] key = key(clientId);
        byte[] value = this.rdb.get(columnFamilyHandle(),key);
        if(value == null){
            log.warn("The will message is not exist,cause = {}",clientId);
        }
        this.rdb.delete(columnFamilyHandle(),key);
        return true;
    }

    private byte[] key(String clientId){
        return (StorePrefix.WILL_MESSAGE + clientId).getBytes(Charset.forName("UTF-8"));
    }


    private ColumnFamilyHandle columnFamilyHandle(){
        return this.rdb.getColumnFamilyHandle(StorePrefix.WILL_MESSAGE);
    }
}
