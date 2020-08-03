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

import org.iot.mqtt.common.bean.StoreType;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class MqttConfig {
	
    private String version = "2.0.0";
    private String serverName;
    private int pollThreadNum = Runtime.getRuntime().availableProcessors() * 2;
    private String configPath;
    private String username;
    private String password;
    private String logBackXmlPath;
    private String rocksDbPath;
    private int tcpPort;
    private boolean isStartSslTcp;
    
    private int datacenterId;
    private int machineId;
    
    /**
     * store type default memory rocksdb rheaKV
     */
    private StoreType storeType = StoreType.ROCKSDB;
    
    private PerformanceConfig performanceConfig = new PerformanceConfig();
    private NettyConfig nettyConfig = new NettyConfig();
	

    public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public int getPollThreadNum() {
		return pollThreadNum;
	}
	public void setPollThreadNum(int pollThreadNum) {
		this.pollThreadNum = pollThreadNum;
	}
	public String getConfigPath() {
		return configPath;
	}
	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getLogBackXmlPath() {
		return logBackXmlPath;
	}
	public void setLogBackXmlPath(String logBackXmlPath) {
		this.logBackXmlPath = logBackXmlPath;
	}
	public String getRocksDbPath() {
		return rocksDbPath;
	}
	public void setRocksDbPath(String rocksDbPath) {
		this.rocksDbPath = rocksDbPath;
	}
	public int getTcpPort() {
		return tcpPort;
	}
	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}
	public boolean isStartSslTcp() {
		return isStartSslTcp;
	}
	public void setStartSslTcp(boolean isStartSslTcp) {
		this.isStartSslTcp = isStartSslTcp;
	}
	public StoreType getStoreType() {
		return storeType;
	}
	public void setStoreType(StoreType storeType) {
		this.storeType = storeType;
	}
	public PerformanceConfig getPerformanceConfig() {
		return performanceConfig;
	}
	public void setPerformanceConfig(PerformanceConfig performanceConfig) {
		this.performanceConfig = performanceConfig;
	}
	public NettyConfig getNettyConfig() {
		return nettyConfig;
	}
	public void setNettyConfig(NettyConfig nettyConfig) {
		this.nettyConfig = nettyConfig;
	}
	public int getDatacenterId() {
		return datacenterId;
	}
	public void setDatacenterId(int datacenterId) {
		this.datacenterId = datacenterId;
	}
	public int getMachineId() {
		return machineId;
	}
	public void setMachineId(int machineId) {
		this.machineId = machineId;
	}
}
