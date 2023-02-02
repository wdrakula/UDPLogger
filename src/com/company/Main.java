package com.company;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;


public class Main {

    private final DatagramSocket socket;
    static String logFile;

    public Main(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Syntax: UDPLogger <file> <port>");
            return;
        }
        logFile = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            Main server = new Main(port);
            //server.loadQuotesFromFile(quoteFile);
            server.service();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }

    }

    private void service() throws IOException {
        while (true) {
            byte[] buffer = new byte[256];
            DatagramPacket request = new DatagramPacket(buffer,buffer.length);
            socket.receive(request);

            InetAddress clientAddress = request.getAddress();
            //int clientPort = request.getPort();

            File file = new File(logFile);
            FileWriter fr = new FileWriter(file, true);
            String s = new String(buffer,0,request.getLength(), StandardCharsets.UTF_8);
            fr.write("From:" + clientAddress + ">>" + s + "\n");
            fr.close();

        }
    }
}
