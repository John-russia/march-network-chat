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
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                System.out.println("Подключился клиент");
                if (tryToAuthenticate()) {
                    communicate();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    private void communicate() throws IOException {
        while (true) {
            String msg = in.readUTF();
            if (msg.startsWith("/")) {
                if (msg.startsWith("/exit")) {
                    break;
                }
                if (msg.startsWith("/w")) {
                    String[] words = msg.split(" ");
                    server.directMessage(buildMessage(words), this, words[1]);
                    continue;
                }
                if (msg.startsWith("/kick ")) {
                    String[] tokens = msg.split(" ");
                    if (!server.getAuthentificationService().isAdmin(this.nickname)){
                        sendMessage("Вы не являетесь администратором и не можете кикнуть пользователя");
                        continue;
                    }
                    if (tokens.length != 2) {
                        sendMessage("Некорректный формат запроса, формат команды: /kick username");
                        continue;
                    }
                    String nickname = tokens[1];
                    if (!server.isNickNameBusy(nickname)) {
                        sendMessage("Пользователь с указанным логином не онлайн");
                        continue;
                    } else {
                        server.kickUser(nickname);
                    }
                }
                continue;
            }
            server.broadcastMessage(nickname + ": " + msg);
        }
    }

    private boolean tryToAuthenticate() throws IOException {
        while (true) {
            String msg = in.readUTF();
            if (msg.startsWith("/auth ")) {
                String[] tokens = msg.split(" ");
                if (tokens.length != 3) {
                    sendMessage("Некорректный формат запроса");
                }
                String login = tokens[1];
                String password = tokens[2];

                String nickname = server.getAuthentificationService().getNickNameByLoginAndPassword(login, password);
                if (nickname == null) {
                    sendMessage("Неправильный логин/пароль");
                    continue;
                }
                if (server.isNickNameBusy(nickname)) {
                    sendMessage("Указанная учётка занята, попробуйте позднее");
                    continue;
                }
                this.nickname = nickname;
                server.subscribe(this);
                sendMessage("Вы успешно авторизовались, nickname: " + nickname);
                return true;
            } else if (msg.startsWith("/register ")) {
                String[] tokens = msg.split(" ");
                if (tokens.length != 4) {
                    sendMessage("Некорректный формат запроса");
                }
                String login = tokens[1];
                String password = tokens[2];
                String nickname = tokens[3];
                if (server.getAuthentificationService().isLoginAlreadyExists(login)) {
                    sendMessage("Указанный логин уже занят");
                    continue;
                }
                if (server.getAuthentificationService().isNickNameAlreadyExists(nickname)) {
                    sendMessage("Указанный nickname уже занят");
                    continue;
                }
                if (!server.getAuthentificationService().register(login, nickname, password)) {
                    sendMessage("Не удалось зарегистрироваться");
                    continue;
                }
                this.nickname = nickname;
                server.subscribe(this);
                sendMessage("Вы успешно зарегистрировались, nickname: " + nickname + ". Добро пожаловать!");
                return true;
            } else if (msg.equals("/exit")) {
                return false;
            } else {
                sendMessage("Необходимо авторизоваться (/auth login password) или зарегистрироваться (/register login password nickname)");
            }
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildMessage(String[] str) {
        String outString = "";
        for (int i = 2; i < str.length; i++) {
            outString += str[i] + " ";
        }
        return outString;
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
