package org.iot.mqtt.broker.sys;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SysInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	Integer clientNum = 0;
	
	List<ClientInfo> clientInfos = new ArrayList<>();
	
	public void addClientInfo(ClientInfo clientInfo) {
		clientInfos.add(clientInfo);
		clientNum++;
	}
	
	public Integer getClientNum() {
		return clientNum;
	}

	public void setClientNum(Integer clientNum) {
		this.clientNum = clientNum;
	}

	public List<ClientInfo> getClientInfos() {
		return clientInfos;
	}


	public void setClientInfos(List<ClientInfo> clientInfos) {
		this.clientInfos = clientInfos;
	}

}
