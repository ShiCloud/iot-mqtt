package org.iot.mqtt.store;

/**
 * 保存会话信息
 */
public interface SessionStore {

    /**
     * 是否包含该会话信�?
     */
    boolean containSession(String clientId);

    /**
     * 设置session状�??
     */
    Object setSession(String clientId,Object obj);

    /**
     * 获取上次的会话信�?,cleansession=false时，�?要重新加�?
     * 若为true：表示该客户端在线，�?要挤掉之前连接的客户�?
     * 若为空：表示从未连接�?
     * 若为字符串：该字符串表示上次离线时间
     */
    Object getLastSession(String clientId);

    /**
     * 清除会话信息
     */
    boolean clearSession(String clientId);
}
