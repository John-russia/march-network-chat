package ru.ivanov.march.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public String getUserName() {
        return userName;
    }

    private String userName;

    private static int usersCounter = 0;

    private void generateUserName() {
        usersCounter++;
        this.userName = "user" + usersCounter;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.generateUserName();
        new Thread(() -> {
            try {
                System.out.println("Подключился клиент");
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        if (msg.startsWith("/exit")) {
                            disconnect();
                            break;
                        }
                        if (msg.startsWith("/w")) {
                            String[] words = msg.split(" ");
                            server.directMessage(buildMessage(words), this, words[1]);
                        }
                        continue;
                    }
                    server.broadcastMessage(userName + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    private String buildMessage(String[] str) {
        String outString = "";
        for (int i = 2; i < str.length; i++) {
            outString += str[i] + " ";
        }
        return outString;
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
