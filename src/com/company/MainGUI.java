package com.company;

import sun.misc.Signal;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainGUI extends JFrame{

    private JTextField logFileN;
    private JTextField portN;
    private JTextArea textArea1;
    private JButton runStop;
    private JLabel WorkDir;
    private JPanel mainPanel;

    private boolean isRunning = false;
    static Logger logger;
    static Debugger debug = new Debugger(false);


    public MainGUI() {
        WorkDir.setText(System.getProperty("user.dir"));
        runStop.addActionListener(e -> {
            if (isRunning) {
                isRunning = false;
                runStop.setText("Idle ... Press to run");
                if (logger != null) logger.pause();
            } else {
                isRunning = true;
                runStop.setText("Running ... Press to stop");
                if (logger != null) logger.resume();
                else {
                    portN.setEnabled(false);
                    logFileN.setEnabled(false);
                    logger = new Logger(logFileN.getText(), Integer.parseInt(portN.getText()), textArea1);
                    Thread t = new Thread(logger);
                    t.start();

                }
            }

        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("UDPLogger for Arduino projects");
        frame.setContentPane(new MainGUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                debug.log("Main window closed");
                if (logger != null) logger.stop();
                System.exit(0);
            }
        });

        Signal.handle(new Signal("INT"),  // SIGINT
                signal -> {
                    debug.log("Interrupted by Ctrl+C");
                    if (logger != null) logger.stop();
                });

    }
}
