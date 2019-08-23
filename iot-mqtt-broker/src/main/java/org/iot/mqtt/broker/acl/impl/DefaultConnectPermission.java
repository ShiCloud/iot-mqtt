package org.iot.mqtt.broker.acl.impl;

import org.iot.mqtt.broker.acl.ConnectPermission;
import org.iot.mqtt.common.config.MqttConfig;


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
    public boolean verfyHeartbeatTime(String clientId, int time) {
        return true;
    }
}
