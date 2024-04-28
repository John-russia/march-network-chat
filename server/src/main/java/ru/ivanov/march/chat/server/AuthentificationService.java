package ru.ivanov.march.chat.server;

public interface AuthentificationService {
    String getNickNameByLoginAndPassword(String login, String password);

    boolean register(String login, String password, String nickName);

    boolean isLoginAlreadyExists(String login);

    boolean isNickNameAlreadyExists(String nickName);
    boolean isAdmin(String nickName);
}
