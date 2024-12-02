package org.example;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Client-Server Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        JButton serverButton = new JButton("Start Server");
        JButton clientButton = new JButton("Start Client");

        // Generate and save key pair
        generateAndSaveKeyPair();

        serverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    try {
                        Server.main(new String[]{});
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        clientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    try {
                        Client.main(new String[]{});
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        JPanel panel = new JPanel();
        panel.add(serverButton);
        panel.add(clientButton);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private static void generateAndSaveKeyPair() {
        try {
            KeyPair keyPair = KeyGenerator.generateRSAKeyPair();
            KeyUtil.saveKeyPair(keyPair, "keypair.ser");
            System.out.println("Key pair generated and saved.");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error generating key pair: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error saving key pair: " + e.getMessage());
        }
    }
}