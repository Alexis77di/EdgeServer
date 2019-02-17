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
import java.util.*;

public class MqttSubscriber implements MqttCallback {

    final static String backhaulIp = "127.0.0.1";
    private final List<Record> data;
    private final Map<String, Info> info;


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
        info = new HashMap<>();
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

    public static double distance(String l1, String l2) {
        double lat1 = Double.parseDouble(l1.split(",")[0]);
        double lat2 = Double.parseDouble(l2.split(",")[0]);
        double lon1 = Double.parseDouble(l1.split(",")[1]);
        double lon2 = Double.parseDouble(l2.split(",")[1]);
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000;

    }

    public void messageArrived(String topic, MqttMessage message) {
        System.out.println("topic: " + topic);
        ManagerThread mng_thread = new ManagerThread(new String(message.getPayload()), data, info);
        Thread tmp_thread = new Thread(mng_thread);
        tmp_thread.start();
    }

    class Info {
        String location;
        int count;
    }

    private class ManagerThread implements Runnable {

        String[] arr;
        List<Record> data;
        Map<String, Info> info;


        ManagerThread(String topic, List<Record> d, Map<String, Info> i) {
            arr = topic.split("/");
            info = i;
            data = d;
        }

        @Override
        public void run() {
            if (arr.length == 6) {
                String mac = arr[0];
                String accelero = arr[1];
                String location = arr[2];
                String csv = arr[3];
                String file = arr[5];
                try {

                    //status = WebSocketClient.danger(mac, accelero, location, EyesClosed(csv), 2469, backhaulIp);
                    Info i;
                    if (info.containsKey(mac)) {
                        i = info.get(mac);
                    } else {
                        System.out.println("New mac: " + mac);
                        i = new Info();
                        info.put(mac, i);
                    }
                    if (EyesClosed(csv)) {
                        i.count++;
                        if (file.contains("Opened")) {
                            System.err.println("Eyes predicted closed but were open");
                        }
                    } else {
                        i.count = 0;
                        if (file.contains("Closed")) {
                            System.err.println("Eyes predicted opened but were close");
                        }
                    }
                    i.location = location;
                    System.out.println("info: " + location + " " + i.count);

                    if (i.count >= 3) {
                        MqttPublisher publisher = new MqttPublisher(mac);
                        try {

                            publisher.alarm();

                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        System.out.println("alarm: " + mac);

                        for (Map.Entry<String, Info> entry : info.entrySet()) {
                            if (mac.equals(entry.getKey())) {
                                continue;
                            }
                            System.out.println("danger: " + entry.getKey());
                            String mac1 = entry.getKey();
                            Info info1 = entry.getValue();
                            if (distance(info1.location, location) < 500) {
                                MqttPublisher publisher2 = new MqttPublisher(mac1);

                                try {
                                    publisher2.flash();

                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        boolean EyesClosed(String csv) {
            Reader in = new StringReader(csv);
            return kNN(data, Entropy.calculate(in), 3);
        }


    }

}


