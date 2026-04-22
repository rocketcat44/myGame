package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class HelloWorldSwing {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 6082;

    private JFrame frame;
    private JButton rockButton;
    private JButton paperButton;
    private JButton scissorsButton;
    private JLabel statusLabel;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new HelloWorldSwing().createAndShowGUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void createAndShowGUI() throws IOException {
        frame = new JFrame("Rock Paper Scissors");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new BorderLayout());

        statusLabel = new JLabel("Waiting for another player", SwingConstants.CENTER);
        frame.add(statusLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        rockButton = new JButton("Rock");
        paperButton = new JButton("Paper");
        scissorsButton = new JButton("Scissors");

        ActionListener choiceListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String choice = e.getActionCommand().toLowerCase();
                sendChoice(choice);
                disableButtons();
                statusLabel.setText("Waiting for another player");
            }
        };

        rockButton.addActionListener(choiceListener);
        paperButton.addActionListener(choiceListener);
        scissorsButton.addActionListener(choiceListener);

        buttonPanel.add(rockButton);
        buttonPanel.add(paperButton);
        buttonPanel.add(scissorsButton);

        frame.add(buttonPanel, BorderLayout.CENTER);

        frame.setVisible(true);

        connectToServer();
        enableButtons(); // Start with buttons enabled
        new Thread(this::listenForResults).start();
    }

    private void connectToServer() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void sendChoice(String choice) {
        out.println(choice);
    }

    private void listenForResults() {
        try {
            String result;
            while ((result = in.readLine()) != null) {
                final String finalResult = result;
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(finalResult);
                    enableButtons();
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disableButtons() {
        rockButton.setEnabled(false);
        paperButton.setEnabled(false);
        scissorsButton.setEnabled(false);
    }

    private void enableButtons() {
        rockButton.setEnabled(true);
        paperButton.setEnabled(true);
        scissorsButton.setEnabled(true);
    }
}
