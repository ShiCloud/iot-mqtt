package org.iot.mqtt.store.rocksdb;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.utils.SerializeHelper;
import org.iot.mqtt.store.FlowMessageStore;
import org.iot.mqtt.store.StorePrefix;
import org.rocksdb.ColumnFamilyHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDBFlowMessageStore implements FlowMessageStore {

    private static final Logger log = LoggerFactory.getLogger(RDBFlowMessageStore.class);

    private RDB rdb;

    public RDBFlowMessageStore(RDB rdb){
        this.rdb = rdb;
    }

    @Override
    public void clearClientCache(String clientId) {
        this.rdb.deleteByPrefix(sendColumnFamilyHandle(),sendKeyPrefix(clientId));
        this.rdb.deleteByPrefix(recColumnFamilyHandle(),recKeyPrefix(clientId));
    }

    @Override
    public Message getRecMsg(String clientId, int msgId) {
        byte[] value = this.rdb.get(recColumnFamilyHandle(),recKey(clientId,msgId));
        if(value == null){
            return null;
        }
        return SerializeHelper.deserialize(value,Message.class);
    }

    @Override
    public boolean cacheRecMsg(String clientId, Message message) {
        try{
            this.rdb.putAsync(recColumnFamilyHandle(),recKey(clientId,message.getMsgId()),SerializeHelper.serialize(message));
            return true;
        }catch (Exception ex){
            log.warn("Cache Recive message failure,cause={}",ex);
        }
        return false;
    }

    @Override
    public Message releaseRecMsg(String clientId, int msgId) {
        byte[] key = recKey(clientId,msgId);
        byte[] value = this.rdb.get(recColumnFamilyHandle(),key);
        if(value == null){
            log.warn("The message is not exist,clientId={},msgId={}",clientId,msgId);
            return null;
        }
        Message returnMessage = SerializeHelper.deserialize(value,Message.class);
        this.rdb.delete(recColumnFamilyHandle(),key);
        return returnMessage;
    }

    @Override
    public boolean cacheSendMsg(String clientId, Message message) {
        try{
            this.rdb.putAsync(sendColumnFamilyHandle(),sendKey(clientId,message.getMsgId()),SerializeHelper.serialize(message));
            return true;
        }catch (Exception ex){
            log.warn("Cache Send message failure,cause={}",ex);
        }
        return false;
    }

    
    @Override
    public Collection<Message> getSendMsg(String clientId,int nums) {
        Collection<byte[]> values = this.rdb.pollByPrefix(sendColumnFamilyHandle(),sendKeyPrefix(clientId),nums);
        Collection<Message> messages = new ArrayList<>();
        for(byte[] value : values){
            messages.add(SerializeHelper.deserialize(value,Message.class));
        }
        return messages;
    }
    
    @Override
    public Collection<Message> getReSendMsg(String clientId,int nums) {
        return this.getSendMsg(clientId,nums);
    }

    @Override
    public boolean releaseSendMsg(String clientId, int msgId) {
        return this.rdb.delete(sendColumnFamilyHandle(),sendKey(clientId,msgId));
    }

    @Override
    public boolean containSendMsg(String clientId, int msgId) {
        return this.rdb.get(sendColumnFamilyHandle(),sendKey(clientId,msgId)) != null;
    }

    private byte[] sendKey(String clientId,int msgId){
        return (StorePrefix.SEND_FLOW_MESSAGE + clientId + msgId).getBytes(Charset.forName("UTF-8"));
    }

    private byte[] recKey(String clientId,int msgId){
        return (StorePrefix.REC_FLOW_MESSAGE + clientId + msgId).getBytes(Charset.forName("UTF-8"));
    }

    private byte[] sendKeyPrefix(String clientId){
        return (StorePrefix.SEND_FLOW_MESSAGE + clientId).getBytes(Charset.forName("UTF-8"));
    }

    private byte[] recKeyPrefix(String clientId){
        return (StorePrefix.REC_FLOW_MESSAGE + clientId).getBytes(Charset.forName("UTF-8"));
    }


    private ColumnFamilyHandle  recColumnFamilyHandle(){
        return this.rdb.getColumnFamilyHandle(StorePrefix.REC_FLOW_MESSAGE);
    }

    private ColumnFamilyHandle  sendColumnFamilyHandle(){
        return this.rdb.getColumnFamilyHandle(StorePrefix.SEND_FLOW_MESSAGE);
    }

}
