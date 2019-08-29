# iot-mqtt
# Release 1.0
本项目完全基于jmqtt项目https://github.com/Cicizz/jmqtt 修改。 
目前只是对部分代码进行了梳理重构。去除了集群部分。 
修正了一些bug。并加入简单的$SYS/实现 
后期会加入原创的集群的实现。  
现在已对mqtt3.1.1协议有完整的实现。
本项目默认采用rocksdb做消息的持久化，所以即使项目故障推出，也不会丢失数据，恢复项目后可以自动重连发送。
运行方法： 
- maven build 出相应jar包，放入release/bin(或者加QQ群682036712，有编译好的jar)。
- cd release/bin目录 执行相应方法 ，相关配置在config目录下。
  1. server启动broker，默认1883端口，用户名admin密码123456。
  2. subscribe_test启动接受服务，默认监听/QOS0,/QOS1,/QOS2,三个主题。
  3. send_test启动发送服务，向/QOS0,/QOS1,/QOS2三个主题,并发1000个线程发送消息。
  4. subscribe_sys启动监控服务，默认监听$SYS/主题.并显示当前所有client信息，包含每个client 接受消息数，发送消息数，关注的主题，等信息。
  
## 性能测试 
## 阿里ECS 16核 64G内存 1T硬盘 未作任何优化
### client id --- 1000个  
### 每client id topic --- 5个
### 每个topic发送 ---  10000条
### 每条数据时间间隔   --- 1000ms 
### 总数据量 ---  1000x5x10000 = 5千万个          
### 发送耗时 --- 10286秒
### 接收耗时 --- 10320秒
![平均耗时](https://github.com/ShiCloud/iot-mqtt/blob/master/pic/%E5%8F%91%E9%80%81%E6%97%B6%E9%97%B4.png) 
                                                
![资源占用](https://github.com/ShiCloud/iot-mqtt/blob/master/pic/5%E5%8D%83%E4%B8%87.png) 
  
  
![欢迎大家一起交流](https://github.com/ShiCloud/iot-mqtt/blob/master/pic/iot-mqtt%E7%BE%A4%E8%81%8A%E4%BA%8C%E7%BB%B4%E7%A0%81.png)
