package org.iot.mqtt.store.rocksdb;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.store.OfflineMessageStore;
import org.iot.mqtt.store.rocksdb.db.RDB;
import org.iot.mqtt.store.rocksdb.db.RDBStorePrefix;
import org.iot.mqtt.test.utils.SerializeHelper;
import org.rocksdb.ColumnFamilyHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDBOfflineMessageStore implements OfflineMessageStore {

    private static final Logger log = LoggerFactory.getLogger(RDBOfflineMessageStore.class);
    private RDB rdb;

    public RDBOfflineMessageStore(RDB rdb){
        this.rdb  = rdb;
    }

    @Override
    public void clearOfflineMsgCache(String clientId) {
        this.rdb.deleteByPrefix(columnFamilyHandle(),keyPrefix(clientId));
    }

    @Override

    public boolean containOfflineMsg(String clientId) {
        return true;
    }

    @Override
    public boolean addOfflineMessage(String clientId, Message message) {
        try{
            this.rdb.putAsync(columnFamilyHandle(),key(clientId,message.getMsgId()), SerializeHelper.serialize(message));
            return true;
        }catch (Exception ex){
            log.warn("Add Offline message failure,cause={}",ex);
        }
        return false;
    }

    @Override
    public Collection<Message> getAllOfflineMessage(String clientId) {
        Collection<byte[]> values = this.rdb.getByPrefix(columnFamilyHandle(),keyPrefix(clientId));
        Collection<Message> offlineMessages = new ArrayList<>(values.size());
        for(byte[] value : values){
            offlineMessages.add(SerializeHelper.deserialize(value,Message.class));
        }
        return offlineMessages;
    }
    
    @Override
    public Collection<Message> getOfflineMessage(String clientId,int nums) {
        Collection<byte[]> values = this.rdb.pollByPrefix(columnFamilyHandle(),keyPrefix(clientId),nums);
        Collection<Message> offlineMessages = new ArrayList<>(values.size());
        for(byte[] value : values){
            offlineMessages.add(SerializeHelper.deserialize(value,Message.class));
        }
        return offlineMessages;
    }
    
    @Override
    public int getAllOfflineMessageCount(String clientId) {
        return this.rdb.getCountByPrefix(columnFamilyHandle(),keyPrefix(clientId));
    }

    private byte[] keyPrefix(String clientId){
        return (RDBStorePrefix.OFFLINE_MESSAGE + clientId).getBytes(Charset.forName("UTF-8"));
    }

    private byte[] key(String clientId,int msgId){
        return (RDBStorePrefix.OFFLINE_MESSAGE + clientId + msgId).getBytes(Charset.forName("UTF-8"));
    }


    private ColumnFamilyHandle columnFamilyHandle(){
        return this.rdb.getColumnFamilyHandle(RDBStorePrefix.OFFLINE_MESSAGE);
    }
}
