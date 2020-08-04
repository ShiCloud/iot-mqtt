/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.iot.mqtt.store.rocksdb;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.iot.mqtt.common.bean.Message;
import org.iot.mqtt.common.utils.SerializeHelper;
import org.iot.mqtt.store.MessageQueue;
import org.iot.mqtt.store.StorePrefix;
import org.rocksdb.ColumnFamilyHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class RDBAppendMessageStore implements MessageQueue{
	private static final Logger log = LoggerFactory.getLogger(RDBAppendMessageStore.class);
	
    private RDB rdb;
    private AtomicInteger msgId = new AtomicInteger(1);

    public RDBAppendMessageStore(RDB rdb){
        this.rdb = rdb;
    }
    
    public int generateMessageId(){
        int messageId = msgId.getAndIncrement();
        messageId = Math.abs( messageId % 0xFFFF);
        if(messageId == 0){
            return generateMessageId();
        }
        return messageId;
    }
    
    
	public boolean offer(Message message) {
		try{
            this.rdb.putAsync(columnFamilyHandle(),newkey(),SerializeHelper.serialize(message));
            return true;
        }catch (Exception ex){
            log.warn("Cache Recive message failure,cause={}",ex);
        }
        return false;
	}
	
	public Collection<Message> pop(int nums) {
		Collection<byte[]> values = this.rdb.pollByPrefix(columnFamilyHandle(),keyPrefix(),nums);
        Collection<Message> messages = new ArrayList<>();
        for(byte[] value : values){
            messages.add(SerializeHelper.deserialize(value,Message.class));
        }
        return messages;
	}
	
	private byte[] keyPrefix(){
        return (StorePrefix.MESSAGE).getBytes(Charset.forName("UTF-8"));
    }
	
	private byte[] newkey(){
        return (StorePrefix.MESSAGE+generateMessageId()).getBytes(Charset.forName("UTF-8"));
    }

    private ColumnFamilyHandle  columnFamilyHandle(){
        return this.rdb.getColumnFamilyHandle(StorePrefix.MESSAGE);
    }
}
