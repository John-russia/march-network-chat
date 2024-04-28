package ru.ivanov.march.chat.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InDataBaseAuthentificationService implements AuthentificationService{
    private class User {
        private int id;
        private String login;
        private String password;
        private String nickName;
        private List<String> roles = new ArrayList<>();

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public int getId() {
            return id;
        }

        public User(int id, String login, String password, String nickName) {
            this.id = id;
            this.login = login;
            this.password = password;
            this.nickName = nickName;
        }
    }

    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USERS_QUERY = "Select * from chat_users.users";
    private static final String USER_ROLES_QUERY = """
    select r.id as id, r.name as name from chat_users.user_to_role ur
    left join chat_users.roles r ON r.id = ur.role_id
    where ur.user_id = ?""";
    private static final String USERS_INSERT = "insert into chat_users.users (login, password, nickname) values (?, ?, ?);";
    private static final String USER_ROLE_INSERT = "insert into chat_users.user_to_role (user_id, role_id) values (?, 2);";

    public InDataBaseAuthentificationService() {
    }

    public void addUserWithUserRoleToDb(String login, String password, String nickname){
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, "postgres", "32105012")){
            try (PreparedStatement ps = connection.prepareStatement(USERS_INSERT)) {
                ps.setString(1, login);
                ps.setString(2, password);
                ps.setString(3, nickname);
                ps.executeUpdate();
            }
            int usrId = getIdByLogin(login);
            try (PreparedStatement ps = connection.prepareStatement(USER_ROLE_INSERT)) {
                ps.setInt(1, usrId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getIdByLogin(String login) {
        List<User> users = getUsersList();
        for (User u : users) {
            if (u.login.equals(login)) {
                return u.getId();
            }
        }
        return 0;
    }

    public List<User> getUsersList(){
        List<User> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, "postgres", "32105012")){
            try (Statement statement = connection.createStatement()) {
                try (ResultSet usersResultSet = statement.executeQuery(USERS_QUERY)){
                    while (usersResultSet.next()){
                        int id = usersResultSet.getInt("id");
                        String loginDb = usersResultSet.getString("login");
                        String passwordDb = usersResultSet.getString("password");
                        String nickName = usersResultSet.getString("nickname");
                        User user = new User(id, loginDb, passwordDb, nickName);
                        users.add(user);
                    }
                }
            }
            try(PreparedStatement ps = connection.prepareStatement(USER_ROLES_QUERY)){
                for (User user : users){
                    List<String> roles = new ArrayList<>();
                    ps.setInt(1, user.getId());
                    try (ResultSet usersResultSet = ps.executeQuery()){
                        while (usersResultSet.next()){
                            String role = usersResultSet.getString("name");
                            roles.add(role);
                        }
                        user.setRoles(roles);
                    }

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
        List<User> users = getUsersList();
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
        addUserWithUserRoleToDb(login, password, nickName);
        return true;
    }

    @Override
    public boolean isLoginAlreadyExists(String login) {
        List<User> users = getUsersList();
        for (User u : users) {
            if (u.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNickNameAlreadyExists(String nickName) {
        List<User> users = getUsersList();
        for (User u : users) {
            if (u.login.equals(nickName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAdmin(String nickName) {
        List<User> users = getUsersList();
        for (User u : users) {
            if (u.nickName.equals(nickName) && u.roles.contains("ADMIN")) {
                return true;
            }
        }
        return false;
    }
}
