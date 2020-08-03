package org.iot.mqtt.store.rocksdb;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.store.StorePrefix;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompressionType;
import org.rocksdb.DBOptions;
import org.rocksdb.LRUCache;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.rocksdb.util.SizeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDB {

    private static final Logger log = LoggerFactory.getLogger(RDB.class);

    private final DBOptions DB_OPTIONS = new DBOptions();
    private final ReadOptions READ_OPTIONS = new ReadOptions();
    private final WriteOptions WRITE_OPTIONS_SYNC = new WriteOptions();
    private final WriteOptions WRITE_OPTIONS_ASYNC = new WriteOptions();
    private final BloomFilter BLOOM_FILTER = new BloomFilter();
    private final BlockBasedTableConfig BLOCK_BASED_TABLE_CONFIG = new BlockBasedTableConfig();
    private final ColumnFamilyOptions COLUMN_FAMILY_OPTIONS = new ColumnFamilyOptions();
    private final List<CompressionType> COMPRESSION_TYPES = new ArrayList<>();
    private MqttConfig mqttConfig;
    private RocksDB DB;
    private Map<String,ColumnFamilyHandle> CF_HANDLES = new HashMap<>();

    public RDB(MqttConfig mqttConfig){
        this.mqttConfig = mqttConfig;
    }

    public RocksIterator newIterator(ColumnFamilyHandle cfh){
        return this.DB.newIterator(cfh,READ_OPTIONS);
    }

    public List<byte[]> getByPrefix(final ColumnFamilyHandle cfh,final byte[] prefixKey){
        List<byte[]> values = new ArrayList<>();
        try{
            RocksIterator iterator = this.newIterator(cfh);
            for(iterator.seek(prefixKey);iterator.isValid();iterator.next()){
            	if(new String(iterator.key()).startsWith(new String(prefixKey))) {
            		values.add(iterator.value());
            	}
            }
            log.debug("[RocksDB] -> succ while get by prefix, prefixKey:{}",new String(prefixKey));
        }catch(Exception e){
            log.error("[RocksDB] ->  error while get by prefix, prefixKey:{}, err:{}",new String(prefixKey), e);
        }
        return values;
    }
    
	public List<byte[]> pollByPrefix(final ColumnFamilyHandle cfh,final byte[] prefixKey,int nums){
        List<byte[]> values = new ArrayList<>();
        int count = 0;
        try{
            RocksIterator iterator = this.newIterator(cfh);
            WriteBatch writeBatch = new WriteBatch();
            for(iterator.seek(prefixKey);iterator.isValid();iterator.next()){
            	if(new String(iterator.key()).startsWith(new String(prefixKey))) {
            		values.add(iterator.value());
            		writeBatch.delete(cfh,iterator.key());
            		count++;
            	}
            	if(count>=nums) {
            		break;
            	}
            }
            if(count > 0){
                this.DB.write(WRITE_OPTIONS_SYNC,writeBatch);
            }
            log.debug("[RocksDB] -> succ while get by prefix,pollByPrefix:{}",new String(prefixKey));
        }catch(Exception e){
            log.error("[RocksDB] ->  error while get by prefix,pollByPrefix:{}, err:{}",new String(prefixKey), e);
        }
        return values;
    }
    
    public int getCountByPrefix(final ColumnFamilyHandle cfh,final byte[] prefixKey){
        int count = 0;
        try{
            RocksIterator iterator = this.newIterator(cfh);
            for(iterator.seek(prefixKey);iterator.isValid();iterator.next()){
            	if(new String(iterator.key()).startsWith(new String(prefixKey))) {
            		count++;
            	}
            }
            log.debug("[RocksDB] -> succ while get count by prefix, prefixKey:{}",new String(prefixKey));
        }catch(Exception e){
            log.error("[RocksDB] ->  error while get count by prefix, prefixKey:{}, err:{}",new String(prefixKey), e);
        }
        return count;
    }

    public boolean deleteByPrefix(final ColumnFamilyHandle cfh,final byte[] prefixKey){
        return deleteByPrefix(cfh,prefixKey,false);
    }
    
    public boolean deleteByPrefix(final ColumnFamilyHandle cfh,final byte[] prefixKey,boolean sync){
        try{
            RocksIterator iterator = this.newIterator(cfh);
            int item = 0;
            WriteBatch writeBatch = new WriteBatch();
            for(iterator.seek(prefixKey);iterator.isValid();iterator.next()){
            	if(new String(iterator.key()).startsWith(new String(prefixKey))) {
            		writeBatch.delete(cfh,iterator.key());
                    item++;
            	}
            }
            if(item > 0){
                this.DB.write(sync?WRITE_OPTIONS_SYNC:WRITE_OPTIONS_ASYNC,writeBatch);
            }
            log.debug("[RocksDB] -> succ while delete by prefix, prefixKey:{}, nums:{}",new String(prefixKey),item);
        }catch(RocksDBException e){
            log.error("[RocksDB] ->  error while delete by prefix, prefixKey:{}, err:{}",new String(prefixKey), e);
            return false;
        }
        return true;
    }

    public boolean deleteRange(final ColumnFamilyHandle cfh,final byte[] beginKey,final byte[] endKey){
        try {
            DB.deleteRange(cfh, beginKey, endKey);
            log.debug("[RocksDB] -> succ delete range, beginKey:{}, endKey:{}",
                     new String(beginKey), new String(endKey));
        } catch (RocksDBException e) {
            log.error("[RocksDB] ->  error while delete range, beginKey:{}, endKey:{}, err:{}",
                     new String(beginKey), new String(endKey), e.getMessage(), e);
            return false;
        }
        return true;
    }

    public byte[] get(final ColumnFamilyHandle cfh,final byte[] key){
        try {
            return DB.get(cfh, key);
        } catch (RocksDBException e) {
            log.error("[RocksDB] -> error while get, key:{}, err:{}",
                     new String(key), e.getMessage(), e);
            return null;
        }
    }

    public boolean writeAsync(final WriteBatch writeBatch){
        return this.write(WRITE_OPTIONS_ASYNC,writeBatch);
    }

    public boolean writeSync(final WriteBatch writeBatch){
        return this.write(WRITE_OPTIONS_SYNC,writeBatch);
    }

    public boolean write(final WriteOptions writeOptions,final WriteBatch writeBatch){
        try {
            this.DB.write(writeOptions,writeBatch);
            log.debug("[RocksDB] -> success write writeBatch, size:{}", writeBatch.count());
        } catch (RocksDBException e) {
            log.error("[RocksDB] -> error while write batch, err:{}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean putAsync(final ColumnFamilyHandle cfh,final byte[] key,final byte[] value){
        return this.put(cfh,WRITE_OPTIONS_ASYNC,key,value);
    }
    
    public boolean putSync(final ColumnFamilyHandle cfh,final byte[] key,final byte[] value){
        return this.put(cfh,WRITE_OPTIONS_SYNC,key,value);
    }

    public boolean delete(final ColumnFamilyHandle cfh, final byte[] key) {
        try {
            this.DB.delete(cfh, key);
            log.debug("[RocksDB] -> succ delete key, key:{}",  new String(key));
        } catch (RocksDBException e) {
            log.error("[RocksDB] -> while delete key, key:{}, err:{}",new String(key), e.getMessage(), e);
        }
        return true;
    }

    public boolean put(final ColumnFamilyHandle cfh,final WriteOptions writeOptions,final byte[] key,final byte[] value){
        try {
            this.DB.put(cfh, writeOptions, key, value);
            log.debug("[RocksDB] -> success put value");
        } catch (RocksDBException e) {
            log.error("[RocksDB] -> error while put, key:{}, err:{}",
                    cfh.isOwningHandle(), new String(key), e.getMessage(), e);
            return false;
    }
        return true;
    }

    public void init(){
        this.DB_OPTIONS.setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true)
                .setMaxBackgroundFlushes(this.mqttConfig.getPerformanceConfig().getMaxBackgroundFlushes())
                .setMaxBackgroundCompactions(this.mqttConfig.getPerformanceConfig().getMaxBackgroundCompactions())
                .setMaxOpenFiles(this.mqttConfig.getPerformanceConfig().getMaxOpenFiles())
                .setRowCache(new LRUCache(1024* SizeUnit.MB,16,true,5))
                .setMaxSubcompactions(this.mqttConfig.getPerformanceConfig().getMaxSubcompactions());
        this.DB_OPTIONS.setBaseBackgroundCompactions(this.mqttConfig.getPerformanceConfig().getBaseBackGroundCompactions());
        READ_OPTIONS.setPrefixSameAsStart(true);
        WRITE_OPTIONS_SYNC.setSync(true);
        WRITE_OPTIONS_ASYNC.setSync(false);
        BLOCK_BASED_TABLE_CONFIG.setFilter(BLOOM_FILTER)
                .setCacheIndexAndFilterBlocks(true)
                .setPinL0FilterAndIndexBlocksInCache(true);

        COMPRESSION_TYPES.addAll(Arrays.asList(
                CompressionType.NO_COMPRESSION,CompressionType.NO_COMPRESSION,
                CompressionType.LZ4_COMPRESSION,CompressionType.LZ4_COMPRESSION,
                CompressionType.LZ4_COMPRESSION,CompressionType.ZSTD_COMPRESSION,
                CompressionType.ZSTD_COMPRESSION
        ));

        COLUMN_FAMILY_OPTIONS.setTableFormatConfig(BLOCK_BASED_TABLE_CONFIG)
                .useFixedLengthPrefixExtractor(this.mqttConfig.getPerformanceConfig().getUseFixedLengthPrefixExtractor())
                .setWriteBufferSize(this.mqttConfig.getPerformanceConfig().getWriteBufferSize() * SizeUnit.MB)
                .setMaxWriteBufferNumber(this.mqttConfig.getPerformanceConfig().getMaxWriteBufferNumber())
                .setLevel0SlowdownWritesTrigger(this.mqttConfig.getPerformanceConfig().getLevel0SlowdownWritesTrigger())
                .setLevel0StopWritesTrigger(10)
                .setCompressionPerLevel(COMPRESSION_TYPES)
                .setTargetFileSizeBase(this.mqttConfig.getPerformanceConfig().getTargetFileSizeBase() * SizeUnit.MB)
                .setMaxBytesForLevelBase(this.mqttConfig.getPerformanceConfig().getMaxBytesForLevelBase() * SizeUnit.MB)
                .setOptimizeFiltersForHits(true);

        long start = System.currentTimeMillis();
        boolean result = createIfNotExistsDir(new File(mqttConfig.getRocksDbPath()));
        assert result;
        try {
            List<ColumnFamilyDescriptor> cfDescriptors = getCFDescriptors();
            List<ColumnFamilyHandle> cfHandles = new ArrayList<>();
            DB = RocksDB.open(DB_OPTIONS,mqttConfig.getRocksDbPath(),cfDescriptors,cfHandles);
            cacheCFHandles(cfHandles);
            long end = System.currentTimeMillis();
            log.info("[RocksDB] -> start RocksDB success,consumeTime:{}",(end-start));
        } catch (RocksDBException e) {
            log.error("[RocksDB] -> init RocksDB error,ex:{}",e);
            System.exit(-1);
        }

    }
    
    public static boolean createIfNotExistsDir(File file){
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }
    
    private List<ColumnFamilyDescriptor> getCFDescriptors(){
        List<ColumnFamilyDescriptor> list = new ArrayList<>();
        list.add(new ColumnFamilyDescriptor("default".getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(StorePrefix.SESSION.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(StorePrefix.SUBSCRIPTION.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(StorePrefix.WILL_MESSAGE.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(StorePrefix.OFFLINE_MESSAGE.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(StorePrefix.REC_FLOW_MESSAGE.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(StorePrefix.SEND_FLOW_MESSAGE.getBytes(Charset.forName("UTF-8"))));
        list.add(new ColumnFamilyDescriptor(StorePrefix.RETAIN_MESSAGE.getBytes(Charset.forName("UTF-8"))));
        return list;
    }

    private void cacheCFHandles(List<ColumnFamilyHandle> cfHandles) throws RocksDBException {
        if(cfHandles == null || cfHandles.size() == 0){
            log.error("[RocksDB] -> init columnFamilyHandle failure.");
            throw new RocksDBException("init columnFamilyHandle failure");
        }
        for (ColumnFamilyHandle cfHandle : cfHandles) {
            this.CF_HANDLES.put(new String(cfHandle.getName()),cfHandle);
        }
    }

    public void close(){
        this.DB_OPTIONS.close();
        this.WRITE_OPTIONS_SYNC.close();
        this.WRITE_OPTIONS_ASYNC.close();
        this.READ_OPTIONS.close();
        this.COLUMN_FAMILY_OPTIONS.close();
        CF_HANDLES.forEach((x,y) -> {
            y.close();
        });
        if(DB != null){
            DB.close();
        }
    }

    public ColumnFamilyHandle getColumnFamilyHandle(String cfhName){
        return this.CF_HANDLES.get(cfhName);
    }
}
