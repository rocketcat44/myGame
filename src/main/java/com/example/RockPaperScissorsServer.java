package com.example;

import java.io.*;
import java.net.*;
import java.util.*;

public class RockPaperScissorsServer {
    private static final int PORT = 6082;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static Map<ClientHandler, String> choices = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (clients.size() < 2) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                System.out.println("Client connected. Total clients: " + clients.size());
            }
            System.out.println("Two clients connected. Game can start.");
            sendToAll("ready");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendToAll(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.out != null) {
                    client.out.println(message);
                }
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received from client: " + message);
                    synchronized (choices) {
                        choices.put(this, message);
                        if (choices.size() == 2) {
                            determineWinner();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.remove(this);
                choices.remove(this);
            }
        }

        private void determineWinner() {
            ClientHandler client1 = clients.get(0);
            ClientHandler client2 = clients.get(1);
            String choice1 = choices.get(client1);
            String choice2 = choices.get(client2);

            String result1 = getResult(choice1, choice2);
            String result2 = getResult(choice2, choice1);

            client1.out.println(result1);
            client2.out.println(result2);

            choices.clear();
        }

        private String getResult(String myChoice, String opponentChoice) {
            if (myChoice.equals(opponentChoice)) {
                return "tie";
            }
            if ((myChoice.equals("rock") && opponentChoice.equals("scissors")) ||
                (myChoice.equals("paper") && opponentChoice.equals("rock")) ||
                (myChoice.equals("scissors") && opponentChoice.equals("paper"))) {
                return "you won";
            } else {
                return "opponent won";
            }
        }
    }
}