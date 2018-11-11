package com.example.alexisapp;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Date;


public class MqttSubscriberSample implements MqttCallback {
    public static void main(String[] args) {
        String topic = "MQTT Examples";
        int qos = 2;
        String broker = "tcp://localhost:1883";
        String clientId = "JavaSampleSubscriber";
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            //Connect client to MQTT Broker
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            //Set callback
            MqttSubscriberSample main = new MqttSubscriberSample();
            sampleClient.setCallback(main);

            System.out.println("Connecting to broker:" + broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");

            System.out.println("Subscribing to topic \"" + topic + "\" qos " + qos);
            sampleClient.subscribe(topic, qos);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }


    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost!" + cause);
        System.exit(1);
    }

    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void messageArrived(String topic, MqttMessage message) {
        String time = new Date().toString();
        System.out.println("Time:\t" + time + "Topic\t" + topic + "Message:\t" + new String(message.getPayload()) + " Qos:\t" + message.getQos());
    }
}
