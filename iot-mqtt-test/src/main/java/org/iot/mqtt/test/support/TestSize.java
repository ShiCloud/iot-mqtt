package org.iot.mqtt.test.support;

public class TestSize {
	private static int clientSize = 10;
	private static int threadSize = 3;
	private static int msgNums = 1000;
	private static int sleepTimes = 100;
	
	public static int getClientSize() {
		return clientSize;
	}
	public static void setClientSize(int clientSize) {
		TestSize.clientSize = clientSize;
	}
	public static int getThreadSize() {
		return threadSize;
	}
	public static void setThreadSize(int threadSize) {
		TestSize.threadSize = threadSize;
	}
	public static int getMsgNums() {
		return msgNums;
	}
	public static void setMsgNums(int msgNums) {
		TestSize.msgNums = msgNums;
	}
	public static int getSleepTimes() {
		return sleepTimes;
	}
	public static void setSleepTimes(int sleepTimes) {
		TestSize.sleepTimes = sleepTimes;
	}
	
	
}
