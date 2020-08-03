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
package org.iot.mqtt.store;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public interface StorePrefix {

	String SPLIT = ":";
	
	String CLIENT_SESSION = "cs"+SPLIT;
	
	String CLIENT_RESEND = "cr"+SPLIT;
	
	String MESSAGE = "m"+SPLIT;
	
	String MATCH_SUBSCRIPTION = "mss"+SPLIT;
	
	String TOPIC_CLIENT_SUBSCRIPTION = "tcs"+SPLIT;
	
	String SESSION = "se"+SPLIT;

    String REC_FLOW_MESSAGE = "rf"+SPLIT;

    String SEND_FLOW_MESSAGE = "sf"+SPLIT;

    String OFFLINE_MESSAGE = "o"+SPLIT;

    String RETAIN_MESSAGE = "rs"+SPLIT;

    String SUBSCRIPTION = "ss"+SPLIT;

    String WILL_MESSAGE = "w"+SPLIT;
}
