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
package org.iot.mqtt.test.support;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class TestConfig {
	
	String url;
	String username;
	String password;
	Short keepAlive;
	Boolean retained;
	Integer reconnectAttemptsMax;
	Integer reconnectDelay;
	String logBackXmlPath;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public Short getKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(Short keepAlive) {
		this.keepAlive = keepAlive;
	}

	public Boolean getRetained() {
		return retained;
	}

	public void setRetained(Boolean retained) {
		this.retained = retained;
	}

	public Integer getReconnectAttemptsMax() {
		return reconnectAttemptsMax;
	}

	public void setReconnectAttemptsMax(Integer reconnectAttemptsMax) {
		this.reconnectAttemptsMax = reconnectAttemptsMax;
	}

	public Integer getReconnectDelay() {
		return reconnectDelay;
	}

	public void setReconnectDelay(Integer reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}

	public String getLogBackXmlPath() {
		return logBackXmlPath;
	}

	public void setLogBackXmlPath(String logBackXmlPath) {
		this.logBackXmlPath = logBackXmlPath;
	}

}
