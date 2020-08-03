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
package org.iot.mqtt.broker.acl.impl;

import org.iot.mqtt.broker.acl.ConnectPermission;
import org.iot.mqtt.common.config.MqttConfig;


/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class DefaultConnectPermission implements ConnectPermission {
	
	MqttConfig mqttConfig;
	
	public DefaultConnectPermission(MqttConfig mqttConfig) {
		this.mqttConfig  = mqttConfig;
	}

	@Override
    public boolean clientIdVerfy(String clientId) {
        return true;
    }

    @Override
    public boolean onBlacklist(String remoteAddr, String clientId) {
        return false;
    }

    @Override
    public boolean authentication(String clientId, String userName, byte[] password) {
    	if(mqttConfig.getUsername().equals(userName) && 
    			mqttConfig.getPassword().equals(new String(password))) {
    		return true;
    	}
    	return false;
    }

    @Override
    public boolean verifyHeartbeatTime(String clientId, int time) {
        return true;
    }
}
