package com.klawx3.asm;


import com.klawx3.asm.mqtt.MqttAsmClient;
import com.klawx3.asm.mqtt.MqttAsmEventListener;
import com.klawx3.asm.properties.AsmProperties;
import com.klawx3.asm.properties.AsmProperties.PropertiesKeys;
import org.firmata4j.IODevice;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.PinEventListener;
import org.firmata4j.firmata.FirmataDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class App implements MqttAsmEventListener {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    private AsmProperties prop;
    private IODevice arduino;
    private MqttAsmClient mqtt;
    private Pin relayPin;

    private final static long HIGH = 1;
    private final static long LOW = 0;

    public App(){

        prop = AsmProperties.getInstance();
        if(prop.isFirtTimeGenerated()){
            logger.info(String.format("File '%s' generated, please set all parameters", AsmProperties.FILE_NAME));
            System.exit(0);
        }
        arduino = new FirmataDevice(p(PropertiesKeys.ARDUINO_PORT));
        try{
            arduino.start();
            arduino.ensureInitializationIsDone();
            logger.info("Connected to arduino on " + p(PropertiesKeys.ARDUINO_PORT));
        } catch (IOException e) {
            logger.error("Probably error: Device '"+ p(PropertiesKeys.ARDUINO_PORT) + "' not founded - " +
                    e.getLocalizedMessage(), e);
            System.exit(1);
        } catch (InterruptedException e) {
            logger.error(e.getLocalizedMessage(), e);
            System.exit(1);
        }
        int pin = -1;
        try{
            pin = Integer.parseInt(p(PropertiesKeys.ARDUINO_PIN));
        }catch (NumberFormatException e){
            logger.error("Arduino pin must be a numeric value", e);
            System.exit(1);
        }
        relayPin = arduino.getPin(pin);
        try {
            relayPin.setMode(Pin.Mode.OUTPUT);
        } catch (IOException e) {
            logger.error("no deberia caer aca", e);
        }
        int port = -1;
        try{
            port = Integer.parseInt(p(PropertiesKeys.MQTT_PORT));
        }catch (NumberFormatException e){
            logger.error("MQTT Port must be a numeric value", e);
            System.exit(1);
        }

        mqtt = new MqttAsmClient(p(PropertiesKeys.MQTT_IP),port,p(PropertiesKeys.MQTT_TOPIC));
        mqtt.addListener(this);

    }

    private String p(PropertiesKeys key){
        return prop.getProperties().getProperty(key.value);
    }

    public static void main( String[] args ) {
        new App();
    }

    @Override
    public void messageArrive(String topic, String message) {
        logger.info("{} {}", topic, message);
        if(message.equalsIgnoreCase("on")){
            try {
                relayPin.setValue(HIGH);
            } catch (IOException e) {
                logger.error("Arduino error", e);
            }
        }else if(message.equalsIgnoreCase("off")){
            try {
                relayPin.setValue(LOW);
            } catch (IOException e) {
                logger.error("Arduino error", e);
            }
        }
    }
}
