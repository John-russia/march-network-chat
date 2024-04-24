package ru.ivanov.march.chat.server;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthentificationService implements AuthentificationService {
    private class User {
        private String login;
        private String password;
        private String nickName;
        private String role;

        public User(String login, String password, String nickName, String role) {
            this.login = login;
            this.password = password;
            this.nickName = nickName;
            this.role = role;
        }
    }

    private List<User> users;

    public InMemoryAuthentificationService() {
        this.users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            this.users.add(new User("login" + i, "pass" + i, "nick" + i, "USER"));
        }
        this.users.add(new User("admin1", "passadm1", "admin1", "ADMIN"));
    }

    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.nickName;
            }
        }
        return null;
    }

    @Override
    public boolean register(String login, String password, String nickName) {
        if (isLoginAlreadyExists(login)) {
            return false;
        }
        if (isNickNameAlreadyExists(nickName)) {
            return false;
        }
        users.add(new User(login, password, nickName, "USER"));
        return true;
    }

    @Override
    public boolean isLoginAlreadyExists(String login) {
        for (User u : users) {
            if (u.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNickNameAlreadyExists(String nickName) {
        for (User u : users) {
            if (u.nickName.equals(nickName)) {
                return true;
            }
        }
        return false;
    }
}
