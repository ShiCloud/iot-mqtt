package org.iot.mqtt.broker.session;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectManager {

    private  Map<String, ClientSession> clientCache = new ConcurrentHashMap<>();

    public Map<String, ClientSession> getClientCache() {
		return clientCache;
	}
    
	public Collection<ClientSession> getAllClient(){
        return this.clientCache.values();
    }

	public ClientSession getClient(String clientId){
        return this.clientCache.get(clientId);
    }

    public ClientSession putClient(String clientId,ClientSession clientSession){
        return this.clientCache.put(clientId,clientSession);
    }

    public boolean containClient(String clientId){
        return this.clientCache.containsKey(clientId);
    }

    public ClientSession removeClient(String clientId){
        return this.clientCache.remove(clientId);
    }
}
