package com.qantas.selenium.core.configuration;/*
package com.dplvs.selenium.core.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

*/
/**
 * Configuration handler. This Class should handle run configuration and global properties.
 * Configuration handling:
 * <ol>
 * <li>Look for the property key in testConfig map, if key is present, return the value</li>
 * <li>Look for the property key in system properties - return value of this property if key
 * present
 * </li>
 * <li>If no System Property is found - value is provided from configuration files
 * (config_default.yml and config.yml). Values provided in config.yml, are overriding values from
 * config_default.yml</li>
 * </ol>
 *//*

public class Configuration {

    public static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_CONFIG_FILE_NAME = "config.yml";
    private static Map<String, String> defaultConfig;
    private static Map<String, String> testConfig = new HashMap<>();


    private Configuration() {}

    private static Map<String, String> readConfiguration() {
        if (defaultConfig == null) {
            Yaml yaml = new Yaml();

            try {
                defaultConfig = (Map<String, String>) yaml.load(new FileInputStream(new File(
                        DEFAULT_CONFIG_FILE_NAME)));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(String.format("CANNOT FIND DEFAULT CONFIG FILE : %s",
                        DEFAULT_CONFIG_FILE_NAME
                ), e);
            }

            // Implement another config file here
        }

        return defaultConfig;
    }

    public static String getPropertyFromFile(String propertyName) {
        return "null".equals(String.valueOf(readConfiguration().get(propertyName))) ? null
                : String.valueOf(
                readConfiguration()
                        .get(
                                propertyName));
    }

    private static String getProp(String propertyName) {
        if (testConfig.get(propertyName) == null) {
            return System.getProperty(propertyName) != null ? System.getProperty(propertyName)
                    : getPropertyFromFile(propertyName);
        } else {
            return testConfig.get(propertyName);
        }
    }

    public static String getEnv() {
        return getProp("environment");
    }


    public static String getEnvType(String env) {
        if (env.toUpperCase().contains("SIT")) return "SIT";
        if (env.toUpperCase().contains("UAT")) return "UAT";
        if (env.toUpperCase().contains("DEV")) return "DEV";
        if (env.toUpperCase().contains("PROD")) return "PROD";

        return null;
    }

    public static void main(String[] args) throws Exception{

        System.out.println(getProp("environment"));
    }
}*/
