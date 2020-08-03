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
package org.iot.mqtt.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.iot.mqtt.common.utils.SnowFlake;
import org.iot.mqtt.test.support.BaseTest;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class OtherTest extends BaseTest {

	public static AtomicInteger WCOUNT = new AtomicInteger(1);
	public static AtomicInteger RCOUNT = new AtomicInteger(1);
	private static SnowFlake snowFlake = new SnowFlake(1, 1);
	public static long generateMessageId(long id){
        long messageId = Math.abs( id % 0x000000007fffffffL);
        if(messageId == 0){
            return 1;
        }
        return messageId;
    }
	public static void main(String[] args) throws Exception {
		
		for (int i = 0; i < 100; i++) {
			long nextId = snowFlake.nextId();
			System.out.println(nextId);
			nextId = generateMessageId(nextId);
			System.out.println(nextId);
		}
		
	}
}
