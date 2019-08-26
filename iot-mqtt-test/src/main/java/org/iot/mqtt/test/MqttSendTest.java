package org.iot.mqtt.test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.fusesource.mqtt.client.QoS;
import org.iot.mqtt.common.config.TestConfig;
import org.iot.mqtt.handler.MqttBaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shifeng on 2018/10/31
 *
 */
public class MqttSendTest extends MqttBaseHandler {
	private static Logger logger = LoggerFactory.getLogger(MqttSendTest.class);

	private static int poolSize = 1000;
	private static int msgNums = 1000;
	private static int sleepTimes = 100;
	
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			logger.error("please set config path");
			System.exit(-1);
		}
		if(args.length > 1) {
			poolSize = Integer.valueOf(args[1]);
		}
		if(args.length > 2) {
			msgNums = Integer.valueOf(args[2]);
		}
		if(args.length > 3) {
			sleepTimes = Integer.valueOf(args[3]);
		}
		MqttSendTest handler = new MqttSendTest();
		TestConfig properties = new TestConfig(args[0]);
		handler.init(properties, null, "MqttSendTest", false);
		logger.info("MqttSendTest testConn inited");
		handler.test(handler);
		System.exit(0);
	}
	
	public void test(MqttSendTest handler) throws Exception {

		
		ForkJoinPool forkJoinPool = new ForkJoinPool(poolSize);
		ForkJoinTask<?>[] list = new ForkJoinTask[poolSize];
		for (int i = 0; i < poolSize; i++) {
			ForkJoinTask<?> fork = forkJoinPool.submit(new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < msgNums; i++) {
						try {
							Thread.sleep(sleepTimes);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						handler.send("/QOS" + (i % 3), (Thread.currentThread().getName() + "::" + i).getBytes(),
								QoS.values()[(i % 3)], false);
					}
				}
			}));
			list[i] = fork;
		}
		long start = System.currentTimeMillis();
		for (int i = 0; i < poolSize; i++) {
			list[i].join();
		}
		logger.info("total time "+(System.currentTimeMillis() - start));
	}
}
