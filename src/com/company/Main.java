package com.company;

import sun.misc.Signal;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;


public class Main extends JFrame {

    static String logFile;
    static int port;
    boolean isRunning = false;
    logger l = null;
    static JTextArea ta;

    public Main() {

        //JPanel p = new JPanel();
        JLabel workDir = new JLabel(System.getProperty("user.dir"));
        JLabel isR = new JLabel("Idle");

        JTextField logFileN = new JTextField("123.txt",16);
        JTextField portN =    new JTextField("3333", 6);

        workDir.setBounds(20,10,200, 40);
        logFileN.setBounds(20,60,200, 40);
        portN.setBounds(20,110,200, 40);

        ta = new JTextArea("...\n",4,30);
        JScrollPane scroll = new JScrollPane (ta,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBounds(20,160,400,150);

        JButton runStop=new JButton("Stop/Run");//create button
        runStop.setBounds(130,350,100, 40);
        isR.setBounds(240,350,100, 40);
        runStop.addActionListener(e -> {
            if (isRunning) {
                isRunning = false;
                isR.setText("Idle");
                l.pause();
            }
            else {
                isRunning = true;
                isR.setText("Running");

                if (l != null) {
                    l.resume();
                }
                else {
                    portN.setEnabled(false);
                    l = new logger(logFileN.getText(), Integer.parseInt(portN.getText()), ta);
                    Thread t = new Thread(l);
                    t.start();
                }
            }

        });


        add(runStop);//adding button on frame

        setSize(400,300);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setSize(400,300);
        //p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        add(workDir);
        add(logFileN);
        add(portN);
        add(scroll);
        add(runStop);
        add(isR);
        setVisible(true);

        setSize(400,500);
        setLayout(null);
        setVisible(true);

        Signal.handle(new Signal("INT"),  // SIGINT
                signal -> {
            System.out.println("Interrupted by Ctrl+C");
            l.stop();
                });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.out.println("Main window closed");
                l.stop();
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        if (args.length >= 2) {
            logFile = args[0];
            port = Integer.parseInt(args[1]);
        }

            new Main();

    }


    static class logger implements Runnable {
        String logFile;
        DatagramSocket socket;
        JTextArea ta;

        public logger(String fn, int pn, JTextArea textArea) {
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
            System.out.println("pause\n");
        }

        public void resume() {
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll(); // Unblocks thread
                System.out.println("resume\n");
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
                System.out.println("receiving...\n");

                try {
                    byte[] buffer = new byte[256];
                    DatagramPacket request = new DatagramPacket(buffer,buffer.length);
                    try {
                        socket.receive(request);
                    } catch (SocketTimeoutException e) {
                        System.out.println("no data\n");
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
                        ta.setText(ta.getText().substring(ta.getText().indexOf('\n')+1));
                    }

                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
