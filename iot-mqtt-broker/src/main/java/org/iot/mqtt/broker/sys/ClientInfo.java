package org.iot.mqtt.broker.sys;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.iot.mqtt.common.bean.Topic;

public class ClientInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	String clientId = null;
	Long send = 0L;
	Long receive = 0L;
	Long offline = 0L;
	Long flow = 0L;
	
	List<Topic> topics = new ArrayList<>();
	
	public ClientInfo(String clientId) {
		this.clientId = clientId;
	}

	public void addTopic(String topic,int qos) {
		topics.add(new Topic(topic, qos));
	}
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public List<Topic> getTopics() {
		return topics;
	}
	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public Long getSend() {
		return send;
	}

	public void setSend(Long send) {
		this.send = send;
	}

	public Long getReceive() {
		return receive;
	}

	public void setReceive(Long receive) {
		this.receive = receive;
	}

	public Long getOffline() {
		return offline;
	}

	public void setOffline(Long offline) {
		this.offline = offline;
	}

	public Long getFlow() {
		return flow;
	}

	public void setFlow(Long flow) {
		this.flow = flow;
	}

}
