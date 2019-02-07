package com.example.alexisapp;

import featureSelectionMetricsPackage.Entropy;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MqttSubscriber implements MqttCallback {

    final static String backhaulIp = "127.0.0.1";
    private final List<Record> data;

    static boolean kNN(List<Record> train, List<Double> record, int k) {
        int closed = 0;
        int opened = 0;
        double wClosed = 0;
        double wOpened = 0;
        train.sort(Comparator.comparingDouble(value -> value.EuclideanDistance(record)));
        for (int i = 0; i < k; i++) {
            Record r = train.get(i);
            if (r.EyesClosed) {
                closed++;
                wClosed += 1 / r.EuclideanDistance(record);
            } else {
                opened++;
                wOpened += 1 / r.EuclideanDistance(record);

            }
        }
        return closed * wClosed > opened * wOpened;
    }

    public MqttSubscriber(List<Record> d) {
        data = d;
    }

    public static void main(String[] args) {
        WebSocketClient.receive(backhaulIp, 15123);
        Reader in = null;
        List<Record> data = new ArrayList<>();
        try {
            in = new FileReader("copy.csv");
            CSVParser records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                List<Double> d = new ArrayList<>();
                Iterator<String> iterator = record.iterator();
                boolean b = iterator.next().contains("EyesClosed");
                while (iterator.hasNext()) {
                    String s = iterator.next();
                    d.add(Double.parseDouble(s));
                }
                Record r = new Record();
                r.EyesClosed = b;
                r.Vector = d;
                data.add(r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Record r : data) {
            System.out.println(r.EyesClosed + " " + kNN(new ArrayList<>(data), (r.Vector), 3));
        }

        String topic = "#";
        int qos = 2;
        String broker = "tcp://localhost:1883";
        String clientId1 = "Μyclientid2";
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttClient sampleClient = new MqttClient(broker, clientId1, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.setCallback(new MqttSubscriber(data));
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
        ManagerThread mng_thread = new ManagerThread(new String(message.getPayload()), data);
        Thread tmp_thread = new Thread(mng_thread);
        tmp_thread.start();
    }

    private class ManagerThread implements Runnable {

        String[] arr;
        List<Record> data;

        ManagerThread(String topic, List<Record> d) {
            arr = topic.split("/");
            data = d;
        }

        @Override
        public void run() {
            if (arr.length == 4) {
                String mac = arr[0];
                String accelero = arr[1];
                String location = arr[2];
                String csv = arr[3];
                int status = 0;
                try {
                    status = WebSocketClient.danger(mac, accelero, location, EyesClosed(csv), 15123, backhaulIp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (status == 1) {
                    MqttPublisher publisher = new MqttPublisher(mac);
                    try {

                        publisher.alarm();

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                } else if (status == 2) {
                    MqttPublisher publisher = new MqttPublisher(mac);

                    try {
                        publisher.flash();

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        boolean EyesClosed(String csv) {
            Reader in = new StringReader(csv);
            return kNN(data, Entropy.calculate(in), 3);
        }


    }


}


