package org.iot.mqtt.test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.fusesource.mqtt.client.QoS;
import org.iot.mqtt.common.config.TestConfig;
import org.iot.mqtt.handler.MqttBaseHandler;
import org.iot.mqtt.test.support.TestSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shifeng on 2018/10/31
 *
 */
public class MqttMutiClientSendTest extends MqttBaseHandler {
	private static Logger logger = LoggerFactory.getLogger(MqttMutiClientSendTest.class);

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			logger.error("please set config path");
			System.exit(-1);
		}
		if(args.length > 1) {
			TestSize.setClientSize(Integer.valueOf(args[1]));
		}
		if(args.length > 2) {
			TestSize.setThreadSize(Integer.valueOf(args[2]));
		}
		if(args.length > 3) {
			TestSize.setMsgNums(Integer.valueOf(args[3]));
		}
		if(args.length > 4) {
			TestSize.setSleepTimes(Integer.valueOf(args[4]));
		}
		TestConfig properties = new TestConfig(args[0]);
		ForkJoinPool forkJoinPool = new ForkJoinPool(TestSize.getClientSize());
		ForkJoinTask<?>[] list = new ForkJoinTask[TestSize.getClientSize()];
		for (int i = 0; i < TestSize.getClientSize(); i++) {
			int index = i;
			ForkJoinTask<?> fork = forkJoinPool.submit(new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						createClientTest(properties,"client"+index);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}));
			list[i] = fork;
		}
		for (int i = 0; i < TestSize.getClientSize(); i++) {
			list[i].join();
		}
		System.exit(0);
	}
	
	public static void createClientTest(TestConfig properties,String clientId) throws Exception {
		MqttMutiClientSendTest handler = new MqttMutiClientSendTest();
		String clientName = clientId+"Send";
		handler.init(properties, null, clientName, false);
		logger.info(clientName+" testConn inited");
		ForkJoinPool forkJoinPool = new ForkJoinPool(TestSize.getThreadSize());
		ForkJoinTask<?>[] list = new ForkJoinTask[TestSize.getThreadSize()];
		for (int i = 0; i < TestSize.getThreadSize(); i++) {
			ForkJoinTask<?> fork = forkJoinPool.submit(new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < TestSize.getMsgNums(); i++) {
						try {
							Thread.sleep(TestSize.getSleepTimes());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						handler.send(clientId+"QOS" + (i % 3), (clientName + "::" + i).getBytes(),
								QoS.values()[(i % 3)], false);
					}
				}
			}));
			list[i] = fork;
		}
		long start = System.currentTimeMillis();
		for (int i = 0; i < TestSize.getThreadSize(); i++) {
			list[i].join();
		}
		logger.info(clientName + " total time "+(System.currentTimeMillis() - start));
	}
}
