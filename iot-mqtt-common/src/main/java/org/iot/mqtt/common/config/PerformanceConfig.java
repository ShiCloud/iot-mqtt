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
package org.iot.mqtt.common.config;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class PerformanceConfig {
	
	private int coreThreadNum = Runtime.getRuntime().availableProcessors() * 2;
	private int selectorThreadNum = 3;
	private int ioThreadNum = 8;
    private int tcpBackLog = 1024;
    private boolean tcpNoDelay = false;
    private boolean tcpReuseAddr = true;
    private boolean tcpKeepAlive = false;
    private int tcpSndBuf = 65536;
    private int tcpRcvBuf = 65536;
    private boolean useEpoll = false;
    private boolean pooledByteBufAllocatorEnable = false;
    private int maxBackgroundFlushes = 10;
    private int maxBackgroundCompactions = 10;
    private int maxOpenFiles = 2048;
    private int maxSubcompactions = 10;
    private int baseBackGroundCompactions = 10;
    private int useFixedLengthPrefixExtractor = 10;
    private int writeBufferSize = 128;
    private int maxWriteBufferNumber = 10;
    private int level0SlowdownWritesTrigger = 30;
    private int level0StopWritesTrigger = 50;
    private int maxBytesForLevelBase = 512;
    private int targetFileSizeBase = 128;
    private int delayedWriteRate = 64;
    
    
    private int popSize = 1000;
    private int popInterval = 1000;
    private int clientSize = 10000;
    private int queueSize = 100000;
    
    
	public int getCoreThreadNum() {
		return coreThreadNum;
	}
	public int getQueueSize() {
		return queueSize;
	}
	public int getClientSize() {
		return clientSize;
	}
	public int getSelectorThreadNum() {
		return selectorThreadNum;
	}
	public int getIoThreadNum() {
		return ioThreadNum;
	}
	public int getTcpBackLog() {
		return tcpBackLog;
	}
	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}
	public boolean isTcpReuseAddr() {
		return tcpReuseAddr;
	}
	public boolean isTcpKeepAlive() {
		return tcpKeepAlive;
	}
	public int getTcpSndBuf() {
		return tcpSndBuf;
	}
	public int getTcpRcvBuf() {
		return tcpRcvBuf;
	}
	public boolean isUseEpoll() {
		return useEpoll;
	}
	public boolean isPooledByteBufAllocatorEnable() {
		return pooledByteBufAllocatorEnable;
	}
	public int getMaxBackgroundFlushes() {
		return maxBackgroundFlushes;
	}
	public int getMaxBackgroundCompactions() {
		return maxBackgroundCompactions;
	}
	public int getMaxOpenFiles() {
		return maxOpenFiles;
	}
	public int getMaxSubcompactions() {
		return maxSubcompactions;
	}
	public int getBaseBackGroundCompactions() {
		return baseBackGroundCompactions;
	}
	public int getUseFixedLengthPrefixExtractor() {
		return useFixedLengthPrefixExtractor;
	}
	public int getWriteBufferSize() {
		return writeBufferSize;
	}
	public int getMaxWriteBufferNumber() {
		return maxWriteBufferNumber;
	}
	public int getLevel0SlowdownWritesTrigger() {
		return level0SlowdownWritesTrigger;
	}
	public int getLevel0StopWritesTrigger() {
		return level0StopWritesTrigger;
	}
	public int getMaxBytesForLevelBase() {
		return maxBytesForLevelBase;
	}
	public int getTargetFileSizeBase() {
		return targetFileSizeBase;
	}
	public int getDelayedWriteRate() {
		return delayedWriteRate;
	}
	public int getPopSize() {
		return this.popSize;
	}  
	public int getPopInterval() {
		return popInterval;
	}
}
