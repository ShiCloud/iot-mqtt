package org.iot.mqtt.store.rocksdb;

import java.nio.charset.Charset;

import org.iot.mqtt.store.SessionStore;
import org.iot.mqtt.store.rocksdb.db.RDB;
import org.iot.mqtt.store.rocksdb.db.RDBStorePrefix;
import org.iot.mqtt.test.utils.SerializeHelper;
import org.rocksdb.ColumnFamilyHandle;

public class RDBSessionStore implements SessionStore {

    //private static final Logger log = LoggerFactory.getLogger(RDBSessionStore.class);

    private RDB rdb;

    public RDBSessionStore(RDB rdb){
        this.rdb = rdb;
    }

    @Override
    public boolean containSession(String clientId) {
        return rdb.get(columnFamilyHandle(),key(clientId)) != null;
    }

    @Override
    public Object setSession(String clientId, Object obj) {
        this.rdb.putAsync(columnFamilyHandle(),key(clientId),SerializeHelper.serialize(obj));
        return obj;
    }

    @Override
    public Object getLastSession(String clientId) {
        byte[] sessionBytes = rdb.get(columnFamilyHandle(),key(clientId));
        if(sessionBytes != null){
            return SerializeHelper.deserialize(sessionBytes,Object.class);
        }
        return null;
    }

    @Override
    public boolean clearSession(String clientId) {
        return rdb.delete(columnFamilyHandle(),key(clientId));
    }

    private byte[] key(String clientId){
        return (RDBStorePrefix.SESSION + clientId).getBytes(Charset.forName("UTF-8"));
    }


    private ColumnFamilyHandle columnFamilyHandle(){
        return this.rdb.getColumnFamilyHandle(RDBStorePrefix.SESSION);
    }
}
