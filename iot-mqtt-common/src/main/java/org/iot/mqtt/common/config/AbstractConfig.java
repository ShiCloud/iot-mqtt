package org.iot.mqtt.common.config;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AbstractConfig {
	
	private Properties p = new Properties();
	
	AbstractConfig(String configPath) {
        InputStream is = null;
        try {
            is = new BufferedInputStream (new FileInputStream(configPath));;
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	public String getProp(String key) {
        return p.getProperty(key);
    }
}
