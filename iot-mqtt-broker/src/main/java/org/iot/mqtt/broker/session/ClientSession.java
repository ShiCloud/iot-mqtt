package org.iot.mqtt.broker.session;

import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ClientSession {

    private String clientId;
    private boolean cleanSession;
    private transient ChannelHandlerContext ctx;
    
    private transient AtomicInteger msgId = new AtomicInteger(1);

    private transient AtomicLong sendIdCounter = new AtomicLong(0);
    private transient AtomicLong receiveIdCounter = new AtomicLong(0);

    public ClientSession(){}

    public ClientSession(String clientId, boolean cleanSession){
        this.clientId = clientId;
        this.cleanSession = cleanSession;
    }

    public ClientSession(String clientId, boolean cleanSession,ChannelHandlerContext ctx){
        this.clientId = clientId;
        this.cleanSession = cleanSession;
        this.ctx = ctx;
    }

    public String getClientId() {
        return clientId;
    }


    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }
    
    public Long getSendIdCounter(){
        return sendIdCounter.get();
    }
    
    public void addSendIdCounter(){
        sendIdCounter.incrementAndGet();
    }
    
    public Long getReceiveIdCounter(){
        return receiveIdCounter.get();
    }
    
    public void addReceiveIdCounter(){
        receiveIdCounter.incrementAndGet();
    }

    public int generateMessageId(){
        int messageId = msgId.getAndIncrement();
        messageId = Math.abs( messageId % 0xFFFF);
        if(messageId == 0){
            return generateMessageId();
        }
        return messageId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientSession that = (ClientSession) o;
        return Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }
}
