package org.iot.mqtt.test.utils;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectHandler implements RejectedExecutionHandler {
    private String taskName;
    private int maxBlockQueueSize;

    public RejectHandler(String taskName,int maxBlockQueueSize){
        this.taskName = taskName;
        this.maxBlockQueueSize = maxBlockQueueSize;
    };

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        throw new RejectedExecutionException("Task:" + taskName + ",maxBlockQueueSize:" + maxBlockQueueSize
                + ",Thread:" + r.toString());
    }
}
