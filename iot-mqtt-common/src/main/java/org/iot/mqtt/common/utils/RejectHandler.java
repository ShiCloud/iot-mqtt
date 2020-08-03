/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.iot.mqtt.common.utils;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
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
