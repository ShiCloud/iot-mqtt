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
public class NettyConfig {

    /**
     * websocket port default 1884
     */
    private boolean startWebsocket = true;
    private int websocketPort = 1884;

    /**
     * tcp port with ssl default 8883
     */
    private boolean startSslTcp = false;
    private int SslTcpPort = 8883;

    /**
     * websocket port with ssl default 8884
     */
    private boolean startSslWebsocket = true;
    private int SslWebsocketPort = 8884;

    /**
     * SSL setting
     */
    private boolean useClientCA = false;
    private String sslKeyStoreType = "PKCS12";
    private String sslKeyFilePath = "/conf/server.pfx";
    private String sslManagerPwd = "654321";
    private String sslStorePwd = "654321";


    /**
     * max mqtt message size
     */
    private int maxMsgSize = 512*1024;


	public boolean isStartWebsocket() {
		return startWebsocket;
	}


	public void setStartWebsocket(boolean startWebsocket) {
		this.startWebsocket = startWebsocket;
	}


	public int getWebsocketPort() {
		return websocketPort;
	}


	public void setWebsocketPort(int websocketPort) {
		this.websocketPort = websocketPort;
	}


	public boolean isStartSslTcp() {
		return startSslTcp;
	}


	public void setStartSslTcp(boolean startSslTcp) {
		this.startSslTcp = startSslTcp;
	}


	public int getSslTcpPort() {
		return SslTcpPort;
	}


	public void setSslTcpPort(int sslTcpPort) {
		SslTcpPort = sslTcpPort;
	}


	public boolean isStartSslWebsocket() {
		return startSslWebsocket;
	}


	public void setStartSslWebsocket(boolean startSslWebsocket) {
		this.startSslWebsocket = startSslWebsocket;
	}


	public int getSslWebsocketPort() {
		return SslWebsocketPort;
	}


	public void setSslWebsocketPort(int sslWebsocketPort) {
		SslWebsocketPort = sslWebsocketPort;
	}


	public boolean isUseClientCA() {
		return useClientCA;
	}


	public void setUseClientCA(boolean useClientCA) {
		this.useClientCA = useClientCA;
	}


	public String getSslKeyStoreType() {
		return sslKeyStoreType;
	}


	public void setSslKeyStoreType(String sslKeyStoreType) {
		this.sslKeyStoreType = sslKeyStoreType;
	}


	public String getSslKeyFilePath() {
		return sslKeyFilePath;
	}


	public void setSslKeyFilePath(String sslKeyFilePath) {
		this.sslKeyFilePath = sslKeyFilePath;
	}


	public String getSslManagerPwd() {
		return sslManagerPwd;
	}


	public void setSslManagerPwd(String sslManagerPwd) {
		this.sslManagerPwd = sslManagerPwd;
	}


	public String getSslStorePwd() {
		return sslStorePwd;
	}


	public void setSslStorePwd(String sslStorePwd) {
		this.sslStorePwd = sslStorePwd;
	}


	public int getMaxMsgSize() {
		return maxMsgSize;
	}


	public void setMaxMsgSize(int maxMsgSize) {
		this.maxMsgSize = maxMsgSize;
	}

}
