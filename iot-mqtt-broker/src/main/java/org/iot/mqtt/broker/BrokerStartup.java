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
package org.iot.mqtt.broker;

import org.iot.mqtt.common.bean.StoreType;
import org.iot.mqtt.common.config.MqttConfig;
import org.iot.mqtt.common.config.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class BrokerStartup {
	
	private static final Logger log = LoggerFactory.getLogger(BrokerStartup.class);
	
	public static void main(String[] args) throws Exception{
		if(args.length == 0) {
			log.error("please set config path");
			System.exit(-1);
		}
		String configPath = args[0];
		start(configPath);
	}

	public static void start(String configPath) throws JoranException {
		
		MqttConfig mqttConfig = Yaml.readConfig(configPath,MqttConfig.class);
        mqttConfig.setConfigPath(configPath);
        
        if(mqttConfig.getStoreType()!=StoreType.RHEAKV) {
	        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	        JoranConfigurator configurator = new JoranConfigurator();
	        configurator.setContext(lc);
	        lc.reset();
	        configurator.doConfigure(mqttConfig.getLogBackXmlPath());
        }
		
        BrokerService brokerService = new BrokerService(mqttConfig);
        brokerService.start();
        
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
            	brokerService.shutdown();
            }
        }));
	}
}
