package org.iot.mqtt.common.config;

public class MqttConfig extends AbstractConfig {
	
    
    public MqttConfig(String configPath) {
    	super(configPath);
	}
	
    private String version = "1.0.0";

    private int pollThreadNum = Runtime.getRuntime().availableProcessors() * 2;
    
    private String username = getProp("username");
    private String password = getProp("password");
    
    
    private String logBackXmlPath = getProp("logBackXmlPath");
	private int selectorThreadNum = Integer.valueOf(getProp("selectorThreadNum"));
	private int ioThreadNum = Integer.valueOf(getProp("ioThreadNum"));
    private int tcpBackLog = Integer.valueOf(getProp("tcpBackLog"));
    private boolean tcpNoDelay = Boolean.valueOf(getProp("tcpNoDelay"));
    private boolean tcpReuseAddr = Boolean.valueOf(getProp("tcpReuseAddr"));
    private boolean tcpKeepAlive = Boolean.valueOf(getProp("tcpKeepAlive"));
    private int tcpSndBuf = Integer.valueOf(getProp("tcpSndBuf"));
    private int tcpRcvBuf = Integer.valueOf(getProp("tcpRcvBuf"));
    private boolean useEpoll = Boolean.valueOf(getProp("useEpoll"));
    private boolean pooledByteBufAllocatorEnable = Boolean.valueOf(getProp("pooledByteBufAllocatorEnable"));
    
    /**
     * store type default 1.in memory 2.rocksdb 
     */
    int storeType = Integer.valueOf(getProp("storeType"));

    /* rocksdb store configuration start */
    private String rocksDbPath = getProp("rocksDbPath");
    private int maxBackgroundFlushes = 10;
    private int maxBackgroundCompactions = 10;
    private int maxOpenFiles = 2048;
    private int maxSubcompactions = 10;
    private int baseBackGroundCompactions = 10;
    private int useFixedLengthPrefixExtractor = 10;
    private int writeBufferSize = 128;
    private int maxWriteBufferNumber = 10;
    private int level0SlowdownWritesTrigger = 30;
    private int level0StopWritesTrigger = 50;
    private int maxBytesForLevelBase = 512;
    private int targetFileSizeBase = 128;
    private int delayedWriteRate = 64;
    
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

	public String getLogBackXmlPath() {
		return logBackXmlPath;
	}

	public void setLogBackXmlPath(String logBackXmlPath) {
		this.logBackXmlPath = logBackXmlPath;
	}

	/* rocksdb store configuration end */
    public String getRocksDbPath() {
        return rocksDbPath;
    }

    public void setRocksDbPath(String rocksDbPath) {
        this.rocksDbPath = rocksDbPath;
    }

    public int getMaxBackgroundFlushes() {
        return maxBackgroundFlushes;
    }

    public void setMaxBackgroundFlushes(int maxBackgroundFlushes) {
        this.maxBackgroundFlushes = maxBackgroundFlushes;
    }

    public int getMaxBackgroundCompactions() {
        return maxBackgroundCompactions;
    }

    public void setMaxBackgroundCompactions(int maxBackgroundCompactions) {
        this.maxBackgroundCompactions = maxBackgroundCompactions;
    }

    public int getMaxOpenFiles() {
        return maxOpenFiles;
    }

    public void setMaxOpenFiles(int maxOpenFiles) {
        this.maxOpenFiles = maxOpenFiles;
    }

    public int getMaxSubcompactions() {
        return maxSubcompactions;
    }

    public void setMaxSubcompactions(int maxSubcompactions) {
        this.maxSubcompactions = maxSubcompactions;
    }

    public int getBaseBackGroundCompactions() {
        return baseBackGroundCompactions;
    }

    public void setBaseBackGroundCompactions(int baseBackGroundCompactions) {
        this.baseBackGroundCompactions = baseBackGroundCompactions;
    }

    public int getUseFixedLengthPrefixExtractor() {
        return useFixedLengthPrefixExtractor;
    }

    public void setUseFixedLengthPrefixExtractor(int useFixedLengthPrefixExtractor) {
        this.useFixedLengthPrefixExtractor = useFixedLengthPrefixExtractor;
    }

    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public int getMaxWriteBufferNumber() {
        return maxWriteBufferNumber;
    }

    public void setMaxWriteBufferNumber(int maxWriteBufferNumber) {
        this.maxWriteBufferNumber = maxWriteBufferNumber;
    }

    public int getLevel0SlowdownWritesTrigger() {
        return level0SlowdownWritesTrigger;
    }

    public void setLevel0SlowdownWritesTrigger(int level0SlowdownWritesTrigger) {
        this.level0SlowdownWritesTrigger = level0SlowdownWritesTrigger;
    }

    public int getLevel0StopWritesTrigger() {
        return level0StopWritesTrigger;
    }

    public void setLevel0StopWritesTrigger(int level0StopWritesTrigger) {
        this.level0StopWritesTrigger = level0StopWritesTrigger;
    }

    public int getMaxBytesForLevelBase() {
        return maxBytesForLevelBase;
    }

    public void setMaxBytesForLevelBase(int maxBytesForLevelBase) {
        this.maxBytesForLevelBase = maxBytesForLevelBase;
    }

    public int getTargetFileSizeBase() {
        return targetFileSizeBase;
    }

    public void setTargetFileSizeBase(int targetFileSizeBase) {
        this.targetFileSizeBase = targetFileSizeBase;
    }

    public int getDelayedWriteRate() {
        return delayedWriteRate;
    }

    public void setDelayedWriteRate(int delayedWriteRate) {
        this.delayedWriteRate = delayedWriteRate;
    }
    
    
    /**
     * tcp port default 1883
     */
    private boolean startTcp = true;
    private int tcpPort = Integer.valueOf(getProp("tcpPort"));

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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getPollThreadNum() {
		return pollThreadNum;
	}

	public void setPollThreadNum(int pollThreadNum) {
		this.pollThreadNum = pollThreadNum;
	}

	public int getSelectorThreadNum() {
		return selectorThreadNum;
	}

	public void setSelectorThreadNum(int selectorThreadNum) {
		this.selectorThreadNum = selectorThreadNum;
	}

	public int getIoThreadNum() {
		return ioThreadNum;
	}

	public void setIoThreadNum(int ioThreadNum) {
		this.ioThreadNum = ioThreadNum;
	}

	public int getTcpBackLog() {
		return tcpBackLog;
	}

	public void setTcpBackLog(int tcpBackLog) {
		this.tcpBackLog = tcpBackLog;
	}

	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	public void setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}

	public boolean isTcpReuseAddr() {
		return tcpReuseAddr;
	}

	public void setTcpReuseAddr(boolean tcpReuseAddr) {
		this.tcpReuseAddr = tcpReuseAddr;
	}

	public boolean isTcpKeepAlive() {
		return tcpKeepAlive;
	}

	public void setTcpKeepAlive(boolean tcpKeepAlive) {
		this.tcpKeepAlive = tcpKeepAlive;
	}

	public int getTcpSndBuf() {
		return tcpSndBuf;
	}

	public void setTcpSndBuf(int tcpSndBuf) {
		this.tcpSndBuf = tcpSndBuf;
	}

	public int getTcpRcvBuf() {
		return tcpRcvBuf;
	}

	public void setTcpRcvBuf(int tcpRcvBuf) {
		this.tcpRcvBuf = tcpRcvBuf;
	}

	public boolean isUseEpoll() {
		return useEpoll;
	}

	public void setUseEpoll(boolean useEpoll) {
		this.useEpoll = useEpoll;
	}

	public boolean isPooledByteBufAllocatorEnable() {
		return pooledByteBufAllocatorEnable;
	}

	public void setPooledByteBufAllocatorEnable(boolean pooledByteBufAllocatorEnable) {
		this.pooledByteBufAllocatorEnable = pooledByteBufAllocatorEnable;
	}

	public boolean isStartTcp() {
		return startTcp;
	}

	public void setStartTcp(boolean startTcp) {
		this.startTcp = startTcp;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

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

	public int getStoreType() {
		return storeType;
	}

	public void setStoreType(int storeType) {
		this.storeType = storeType;
	}
	
	
}
