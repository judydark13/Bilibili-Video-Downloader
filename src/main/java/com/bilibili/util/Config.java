package com.bilibili.util;

import java.io.*;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();
    
    static {
        loadConfig();
    }
    
    public static void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Bilibili Downloader Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getSESSDATA() {
        return properties.getProperty("SESSDATA", "");
    }
    
    public static void setSESSDATA(String sessdata) {
        properties.setProperty("SESSDATA", sessdata);
        saveConfig();
    }
} 