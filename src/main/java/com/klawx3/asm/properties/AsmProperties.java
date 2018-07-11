package com.klawx3.asm.properties;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

public class AsmProperties {
    private static AsmProperties self;

    public enum PropertiesKeys {
        ARDUINO_PORT("arduino.port"),
        ARDUINO_PIN("arduino.pin"),
        MQTT_IP("mqtt.ip"),
        MQTT_PORT("mqtt.port"),
        MQTT_TURN_OFF_MESSAGE("mqtt.turn_off_message"),
        MQTT_TURN_ON_MESSAGE("mqtt.turn_on_message"),
        MQTT_TOPIC("mqtt.topic");

        public String value;

        PropertiesKeys(String value){
            this.value = value;
        }

    }

    public static final String FILE_NAME = "app.properties";
    private static final File file = new File(FILE_NAME);
    private Properties prop;
    private boolean firtTimeGenerated;

    public static AsmProperties getInstance(){
        if(self == null)
            self = new AsmProperties();
        return self;
    }

    private AsmProperties(){
        firtTimeGenerated = false;
        prop = new Properties();
        if(!file.isFile()) {
            createProperties();
            firtTimeGenerated = true;
        }else {
            loadProperties();
        }
    }

    public boolean isFirtTimeGenerated() {
        return firtTimeGenerated;
    }

    public Properties getProperties() {
        return prop;
    }

    private void createProperties() {
        try (FileOutputStream output = new FileOutputStream(FILE_NAME)) {
            Arrays.asList(PropertiesKeys.values()).forEach(p -> prop.setProperty(p.value,"null") );
            prop.store(output,"Example properties file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProperties() {
        try(FileInputStream input = new FileInputStream(FILE_NAME)){
            prop.load(input);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "AsmProperties{" +
                "prop=" + prop +
                '}';
    }
}
