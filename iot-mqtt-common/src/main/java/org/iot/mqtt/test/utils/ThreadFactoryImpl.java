package org.iot.mqtt.test.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryImpl implements ThreadFactory {

    private AtomicInteger counter = new AtomicInteger(0);
    private String threadName;

    public ThreadFactoryImpl(String taskName){
        this.threadName = taskName + " thread";
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r,threadName + "_" + this.counter.incrementAndGet());
    }
}
