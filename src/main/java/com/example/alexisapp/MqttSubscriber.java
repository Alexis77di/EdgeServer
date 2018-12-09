package com.example.alexisapp;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSubscriber implements MqttCallback {

    private static MqttPublisher publisher;

    //String macAddress;
    public static void main(String[] args) {

        String topic = "#";
        int qos = 2;
        String broker = "tcp://localhost:1883";
        String clientId1 = "Μyclientid2";
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttAsyncClient sampleClient = new MqttAsyncClient(broker, clientId1, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.setCallback(new MqttSubscriber());
            System.out.println("Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            Thread.sleep(1000);
            sampleClient.subscribe(topic, qos);
            System.out.println("Subscribed");
        } catch (Exception me) {
            if (me instanceof MqttException) {
                System.out.println("reason " + ((MqttException) me).getReasonCode());
            }
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
        System.out.println("topic: " + topic);
        System.out.println("message: " + new String(message.getPayload()));
        ManagerThread mng_thread = new ManagerThread(new String(message.getPayload()));
        Thread tmp_thread = new Thread(mng_thread);
        tmp_thread.start();
    }

    private class ManagerThread implements Runnable {

        String[] arr;

        public ManagerThread(String topic) {

            arr = topic.split("/");


        }

        @Override
        public void run() {
            int cntr = arr.length;
            if (cntr == 4) {
                String macAddress = arr[0];
                String sensor_Value = arr[1];
                String latitude = arr[2];
                String longtitude = arr[3];


                String[] accelero_values = sensor_Value.split(",");
                publisher = new MqttPublisher();
                publisher.main(macAddress, "alarm");

            }

        }

    }
}

