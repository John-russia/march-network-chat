package ru.ivanov.march.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AuthentificationService authentificationService;

    public AuthentificationService getAuthentificationService() {
        return authentificationService;
    }

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            this.authentificationService = new InDataBaseAuthentificationService();
            System.out.println("Запущен сервис аутентификации: " + authentificationService.getClass().getSimpleName());


            System.out.printf("Сервер запущен на порту: %d, ожидаем подключения клиентов\n", port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new ClientHandler(this, socket);
                } catch (Exception e) {
                    System.out.println("Возникла ошибка при обработке подключившегося клиента");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientHandler findByNick(String nickname) {
        ClientHandler result = null;
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickname)) {
                result = c;
                break;
            }
        }
        return result;
    }

    public void directMessage(String message, ClientHandler sender, String recipientName) {
        ClientHandler recipient = findByNick(recipientName);
        if (clients.contains(recipient)){
            recipient.sendMessage("From " + sender.getNickname() + ": " + message);
        } else {
            sender.sendMessage("Вы пробовали отправить сообщение несуществующему пользователю");
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        broadcastMessage("К чату присоединился " + clientHandler.getNickname());
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Из чата вышел " + clientHandler.getNickname());
    }

    public void kickUser(String nickname) {
        ClientHandler client = findByNick(nickname);
        client.sendMessage("Вас кикнули");
        client.disconnect();
    }

    public void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized boolean isNickNameBusy(String nickname) {
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }
}
