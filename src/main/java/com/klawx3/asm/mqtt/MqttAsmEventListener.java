package com.klawx3.asm.mqtt;

public interface MqttAsmEventListener {
    void messageArrive(String topic,String message);
}
