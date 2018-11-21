package com.example.alexisapp;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static java.lang.Thread.sleep;


public class MqttPublishSample {
    private static final String WARNING = "warning";
    private static final String ERROR = "error";
    private final int qos;
    private final String topic;
    private final MqttClient sampleClient;

    public MqttPublishSample(int qos, String topic) throws MqttException {
        this.qos = qos;
        this.topic = topic;
        MemoryPersistence persistence = new MemoryPersistence();
        String broker = "tcp://192.168.1.2:1883";
        String clientId = "JavaSample";
        this.sampleClient = new MqttClient(broker, clientId, persistence);
    }

    public static void main(String[] args) {
        try {
            MqttPublishSample publisher = new MqttPublishSample(2, "MQTT Examples");

            publisher.connect();
            publisher.SendCommand(WARNING);
            sleep(1000);
            publisher.SendCommand(ERROR);

            publisher.disconnect();
            System.exit(0);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void SendCommand(String type) throws MqttException {

        System.out.println("Publishing message: " + type);

        MqttMessage message = new MqttMessage(type.getBytes());
        message.setQos(qos);
        sampleClient.publish(topic, message);
        System.out.println("Message published");
    }

    public void connect() throws MqttException {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        System.out.println("Connecting to broker");
        sampleClient.connect(connOpts);
        System.out.println("Connected");
    }

    public void disconnect() throws MqttException {
        sampleClient.disconnect();
        System.out.println("Disconnected");
    }
}

