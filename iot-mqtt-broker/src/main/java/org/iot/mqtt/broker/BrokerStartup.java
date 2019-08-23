package org.iot.mqtt.broker;

import org.iot.mqtt.common.config.MqttConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

public class BrokerStartup {
	
	private static final Logger log = LoggerFactory.getLogger(BrokerStartup.class);
	
	public static void main(String[] args) throws Exception{
		
		if(args.length == 0) {
			log.error("please set config path");
			System.exit(-1);
		}
		
		MqttConfig mqttConfig = new MqttConfig(args[0]);
		
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure(mqttConfig.getLogBackXmlPath());
		
		
        BrokerService brokerService = new BrokerService(mqttConfig);
        brokerService.start();
        
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
            	brokerService.shutdown();
            }
        }));
        
        log.info("[Server] -> broker startup success");
	}
}
