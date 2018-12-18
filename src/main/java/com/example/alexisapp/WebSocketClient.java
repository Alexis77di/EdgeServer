package com.example.alexisapp;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

class WebSocketClient {
    static void receive(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream("copy.csv");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int count;
            byte[] buffer = new byte[8192]; // or 4096, or more
            while ((count = is.read(buffer)) > 0) {
                bos.write(buffer, 0, count);
            }
            bos.flush();
            bos.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
