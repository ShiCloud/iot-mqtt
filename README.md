# iot-mqtt
 Release 1.0
本项目完全借鉴jmqtt项目https://github.com/Cicizz/jmqtt  
目前只是对部分代码进行了梳理重构。去除了集群部分。  
后期会加入原创的集群的实现。  
现在已对mqtt3.1.1协议有完整的实现。
本项目默认采用rocksdb做消息的持久化，所以即使项目故障推出，也不会丢失数据，恢复项目后可以自动重连发送。
运行方法： 
1.maven build 出相应jar包，放入release/bin(或者加QQ群682036712，有编译好的jar)。
2.cd release/bin目录 执行相应方法 ，相关配置在config目录下。
  2.1 server启动broker，默认1883端口，用户名admin密码123456。
  2.2 subscribe_test启动接受服务，默认监听/QOS0,/QOS1,/QOS2,三个主题。
  2.2 send_test启动发送服务，向/QOS0,/QOS1,/QOS2三个主题,并发1000个线程发送消息,共200万个消息。
  2.3 subscribe_sys启动监控服务，默认监听$SYS/主题.并显示当前所有client信息，包含每个client 接受消息数，发送消息数，关注的主题，等信息。
