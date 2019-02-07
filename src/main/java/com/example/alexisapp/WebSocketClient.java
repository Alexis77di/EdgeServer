package com.example.alexisapp;

import java.io.*;
import java.net.ServerSocket;
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


    public static int danger(String mac, String accelero, String location, boolean eyesClosed, int port, String host) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = serverSocket.accept();
        System.out.println("Accepted connection : " + socket);
        byte[] bytearray = String.format("%s/%s/%s/%d", mac, accelero, location, eyesClosed ? 1 : 0).getBytes();
        OutputStream os = socket.getOutputStream();
        System.out.println("Sending Files...");
        os.write(bytearray);
        os.flush();
        socket.close();
        System.out.println("File transfer complete");
        socket = new Socket(host, port);
        InputStream is = socket.getInputStream();
        int status = is.read();
        socket.close();
        return status;
    }
}
