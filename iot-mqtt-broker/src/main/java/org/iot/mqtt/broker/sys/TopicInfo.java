package org.iot.mqtt.broker.sys;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TopicInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	Integer receivedTotal = 0;
	Integer sendTotal = 0;
	List<String> clientIds = new ArrayList<>();
	
	public Integer getReceivedTotal() {
		return receivedTotal;
	}
	public void setReceivedTotal(Integer receivedTotal) {
		this.receivedTotal = receivedTotal;
	}
	public Integer getSendTotal() {
		return sendTotal;
	}
	public void setSendTotal(Integer sendTotal) {
		this.sendTotal = sendTotal;
	}
	public List<String> getClientIds() {
		return clientIds;
	}
	public void setClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
	}
	
	
	
}
