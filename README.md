





# Release 2.0

- [x] ### 分布式集群架构，基于Raft协议

- [x] ### 完整支持 MQTT V3.1.1  协议规范

- [x] ### 完整的实现了QOS0,  QOS1,  QOS2消息

- [x] ### 完全开放源码，基于 Apache Version 2.0 开源协议

# 架构图

 https://www.yuque.com/cloudshi/ms000x/apuehg 

 # 启动三个节点

```java
	public static String[] serverPath = new String[] {
			"src/main/resources/cluster/server1.yaml",
			"src/main/resources/cluster/server2.yaml",
			"src/main/resources/cluster/server3.yaml" };
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < serverPath.length; i++) {
			final int num = i;
			new Thread(new Runnable(){  
	            public void run(){  
	            	try {
						BrokerStartup.start(serverPath[num]);
					} catch (JoranException e) {
						e.printStackTrace();
					}
	            }}).start();  
		}
	}
```

# server配置文件说明

```yaml
serverName: server1 #服务名称，必须配置且不同
username: admin #mqtt broker用户名
password: 123456 #mqtt broker密码
tcpPort: 18081 #mqtt broker端口号
logBackXmlPath: src/main/resources/logback-test.xml #log配置文件路径
performanceConfig: #优化配置
  selectorThreadNum: 3
  ioThreadNum: 8
  tcpBackLog: 1024
  tcpNoDelay: false
  tcpReuseAddr: true
  tcpKeepAlive: false
  tcpSndBuf: 65536
  tcpRcvBuf: 65536
  useEpoll: false
  pooledByteBufAllocatorEnable: false
  popSize: 100 #单节点从分布式存储查询消息数量
  popInterval: 1000 # 单节点发送消息到客户端时间间隔
  clientSize: 10000 # 最大单节点连接客户端数量
  queueSize: 50000 # 线程池队列数量
# 0 rocksdb 1 RheaKV
storeType: 1
rocksDbPath: store/rocksdb1/ #必须配置且每个server不同
datacenterId: 1 #用于生成分布式唯一id的雪花算法配置
machineId: 1 #用于生成分布式唯一id的雪花算法配置
#分布式相关配置
clusterName: rhea_mqtt #必须配置且每个server相同
placementDriverOptions:
  fake: true
  # 路由表
  regionRouteTableOptionsList:
     #raft协议节点存储路由配置
    - { regionId: -1, nodeOptions: { timerPoolSize: 1, rpcProcessorThreadPoolSize: 4 } }    
storeEngineOptions:
  rocksDBOptions:
    dbPath: store/rhea_db/ #必须配置且每个server可以相同
  raftDataPath: store/rhea_raft/ #必须配置且每个server可以相同
  serverAddress:
    ip: 127.0.0.1
    port: 18181 #raft协议节点端口
  regionEngineOptionsList:
     #raft协议节点存储分区配置
    - { regionId: -1, nodeOptions: { timerPoolSize: 1, rpcProcessorThreadPoolSize: 4 } }
  leastKeysOnSplit: 10
initialServerList: 127.0.0.1:18181,127.0.0.1:18182,127.0.0.1:18183 #raft协议节点列表
```

欢迎大家一起 交流qq群
https://cdn.nlark.com/yuque/0/2020/png/1624173/1593050500510-b2821135-c8e0-4969-8417-97ab50bcc65f.png

