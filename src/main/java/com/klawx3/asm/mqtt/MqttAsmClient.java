package com.klawx3.asm.mqtt;


import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MqttAsmClient implements MqttCallback {

    private static Logger logger = LoggerFactory.getLogger(MqttAsmClient.class);
    private static long MILLIS_TO_RECONNECT = 10 * 1000;

    private MqttClient client;
    private String ip;
    private int port;
    private String topic;

    private List<MqttAsmEventListener> listener;

    public MqttAsmClient(String ip, int port,String topic){
        this.ip = ip;
        this.port = port;
        this.topic = topic;
        try {
            client = new MqttClient("tcp://"+ip+":"+port,"asm_client");
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws MqttException {
        client.setCallback(this);
        client.connect();
        client.subscribe(topic);
        logger.info("Connected to MQTT server: {}:{} at topic '{}'",ip,port,topic);
    }

    public void addListener(MqttAsmEventListener l){
        if(listener == null)
            listener = new ArrayList<>();
        listener.add(l);
    }

    public void removeListener(MqttAsmEventListener l){
        listener.remove(l);
    }

    private void fireEvent(String topic, String message){
        listener.forEach(l -> l.messageArrive(topic, message));
    }

    @Override
    public void connectionLost(Throwable throwable) {
        logger.warn("Conecction lost", throwable);
        (new Thread(()->{
            while(!client.isConnected()){
                try {
                    Thread.sleep(MILLIS_TO_RECONNECT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Trying to reconnect on " + MILLIS_TO_RECONNECT + " ms");
                try {
                    connect();
                } catch (MqttException e) {
                    logger.warn("Can't connect");
                }
            }

        })).start();

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        fireEvent(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
