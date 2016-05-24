/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webuseragent.helpers;

import com.webuseragent.main.App;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Clive
 */
public class ConfigHelper {
    
    private ConfigHelper() {
        this.properties = new Properties();
        try {
            InputStream stream  = App.class.getResourceAsStream("/config.properties");             
            properties.load(stream);
            
            //properties.load(new FileInputStream("/config.properties"));
            //InputStream stream  = App.class.getResourceAsStream("/WebUserAgentGoogleStory.json"); 
            //theString = IOUtils.toString(stream); 
            
        } catch (IOException ex) {
            
        }    }
    
    private Properties properties;
    
    public static ConfigHelper getInstance() {
        return ConfigHelperHolder.INSTANCE;
    }

    /**
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    private static class ConfigHelperHolder {        
            private static final ConfigHelper INSTANCE = new ConfigHelper();
    }
    
}
