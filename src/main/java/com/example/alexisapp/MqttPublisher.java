package com.example.alexisapp;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

class MqttPublisher {
    private String topic;

    MqttPublisher(String topic) {
        this.topic = topic;
    }

    void alarm() throws MqttException {

        int qos = 2;
        String broker = "tcp://localhost:1883";
        String clientId = "Μyclientid";
        MemoryPersistence persistence = new MemoryPersistence();

        MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        System.out.println("Connecting to broker: " + broker);
        sampleClient.connect(connOpts);
        System.out.println("Connected");
        System.out.println("Publishing alarm");
        MqttMessage message = new MqttMessage("alarm".getBytes());
        message.setQos(qos);
        sampleClient.publish(topic, message);
        System.out.println("Message published");
    }

    void flash() throws MqttException {

        int qos = 2;
        String broker = "tcp://localhost:1883";
        String clientId = "Μyclientid";
        MemoryPersistence persistence = new MemoryPersistence();

        MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        System.out.println("Connecting to broker: " + broker);
        sampleClient.connect(connOpts);
        System.out.println("Connected");
        System.out.println("Publishing alarm");
        MqttMessage message = new MqttMessage("flash".getBytes());
        message.setQos(qos);
        sampleClient.publish(topic, message);
        System.out.println("Message published");
    }
}
