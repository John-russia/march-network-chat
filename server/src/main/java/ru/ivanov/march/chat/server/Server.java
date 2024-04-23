package ru.ivanov.march.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.printf("Сервер запущен на порту: %d, ожидаем подключения клиентов\n", port);
            while (true) {
                Socket socket = serverSocket.accept();
                subscribe(new ClientHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public ClientHandler findByName(String name){
        ClientHandler result = null;
        for (ClientHandler c : clients) {
            if (c.getUserName().equals(name)){
                result = c;
                break;
            }
        }
        return result;
    }

    public void directMessage(String message, ClientHandler sender, String recipientName) {
        ClientHandler recipient = findByName(recipientName);
        if (clients.contains(recipient)){
            recipient.sendMessage("From " + sender.getUserName() + ": " + message);
        } else {
            sender.sendMessage("Вы пробовали отправить сообщение несуществующему пользователю");
        }
    }

}
