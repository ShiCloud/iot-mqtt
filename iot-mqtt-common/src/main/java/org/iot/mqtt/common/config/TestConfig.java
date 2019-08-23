package org.iot.mqtt.common.config;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class TestConfig extends AbstractConfig{
	
	public TestConfig(String configPath) {
		super(configPath);
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
			configurator.doConfigure(getLogBackXmlPath());
		} catch (JoranException e) {
			e.printStackTrace();
		}
	}

	String url = getProp("url");
	String username = getProp("username");
	String password = getProp("password");
	Short keepAlive = Short.valueOf(getProp("keepAlive"));
	Boolean retained = Boolean.valueOf(getProp("retained"));
	Integer reconnectAttemptsMax = Integer.valueOf(getProp("reconnectAttemptsMax"));
	Integer reconnectDelay = Integer.valueOf(getProp("reconnectDelay"));
	private String logBackXmlPath = getProp("logBackXmlPath");
	
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
