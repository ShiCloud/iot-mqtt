package org.iot.mqtt.store.rocksdb;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.utils.SerializeHelper;
import org.iot.mqtt.store.RetainMessageStore;
import org.iot.mqtt.store.StorePrefix;
import org.rocksdb.ColumnFamilyHandle;

public class RDBRetainMessageStore implements RetainMessageStore {

    //private static final Logger log = LoggerFactory.getLogger(RDBRetainMessageStore.class);
    private RDB rdb;

    public RDBRetainMessageStore(RDB rdb){
        this.rdb = rdb;
    }

    @Override
    public Collection<Message> getAllRetainMessage() {
        Collection<byte[]> values = this.rdb.getByPrefix(columnFamilyHandle(),key(""));
        Collection<Message> retainMessages = new ArrayList<>();
        for(byte[] value : values){
            Message retainMsg = SerializeHelper.deserialize(value,Message.class);
            if(Objects.nonNull(retainMsg)){
                retainMessages.add(retainMsg);
            }
        }
        return retainMessages;
    }

    @Override
    public void storeRetainMessage(String topic, Message message) {
        this.rdb.putAsync(columnFamilyHandle(),key(topic),SerializeHelper.serialize(message));
    }

    @Override
    public void removeRetainMessage(String topic) {
        this.rdb.delete(columnFamilyHandle(),key(topic));
    }

    private byte[] key(String topic){
        return (StorePrefix.RETAIN_MESSAGE + topic).getBytes(Charset.forName("UTF-8"));
    }


    private ColumnFamilyHandle columnFamilyHandle(){
        return this.rdb.getColumnFamilyHandle(StorePrefix.RETAIN_MESSAGE);
    }
}
