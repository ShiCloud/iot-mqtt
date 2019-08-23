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

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			logger.error("please set config path");
			System.exit(-1);
		}
		MqttSendTest handler = new MqttSendTest();
		TestConfig properties = new TestConfig(args[0]);
		handler.init(properties, null, "MqttSendTest", false);
		logger.info("MqttSendTest testConn inited");
		handler.test(handler);
		System.exit(0);
	}
	
	public void test(MqttSendTest handler) throws Exception {

		int poolSize = 1000;
		ForkJoinPool forkJoinPool = new ForkJoinPool(poolSize);
		ForkJoinTask<?>[] list = new ForkJoinTask[poolSize];
		for (int i = 0; i < poolSize; i++) {
			ForkJoinTask<?> fork = forkJoinPool.submit(new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 2000; i++) {
						try {
							Thread.sleep(10);
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
		System.out.println("total time "+(System.currentTimeMillis() - start));
	}
}
