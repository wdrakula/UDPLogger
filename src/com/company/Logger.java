package com.company;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Logger implements Runnable{

    String logFile;
    DatagramSocket socket;
    JTextArea ta;
    Debugger debug = new Debugger(false);

    public Logger(String fn, int pn, JTextArea textArea) {
        logFile = fn;
        ta = textArea;
        try {
            socket = new DatagramSocket(pn);
            socket.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    public void stop() {
        running = false;
        resume();
    }

    public void pause() {
        paused = true;
        debug.log("pause\n");
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread
            debug.log("resume\n");
        }
    }


    @Override
    public void run() {

        while (running) {
            synchronized (pauseLock) {
                if (!running) {
                    break;
                }
                if (paused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException ex) {
                        break;
                    }
                    if (!running) {
                        break;
                    }
                }
            }
            debug.log("receiving...\n");

            try {
                byte[] buffer = new byte[256];
                DatagramPacket request = new DatagramPacket(buffer,buffer.length);
                try {
                    socket.receive(request);
                } catch (SocketTimeoutException e) {
                    debug.log("no data\n");
                    continue;
                }

                InetAddress clientAddress = request.getAddress();

                File file = new File(logFile);
                FileWriter fr;
                fr = new FileWriter(file, true);
                String s = new String(buffer,0,request.getLength(), StandardCharsets.UTF_8);
                fr.write("From:" + clientAddress + ">>" + s + "\n");
                ta.append("From:" + clientAddress + ">>" + s + "\n");
                while (ta.getLineCount() > 50) {
                    if (ta.getText().indexOf('\n') + 1 < ta.getDocument().getLength()) {
                        ta.setText(ta.getText().substring(ta.getText().indexOf('\n') + 1));
                    }
                }
                ta.setCaretPosition(ta.getDocument().getLength());

                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
