package model;

public class User {
    private long id;
    private String login;
    private String password;
    private Role role;

    public enum Role {
        ADMIN,      // Администратор
        EMPLOYEE    // Обычный сотрудник
    }

    // Пустой конструктор (часто бывает полезен)
    public User() {
    }

    // Конструктор со всеми полями
    public User(long id, String login, String password, Role role) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
    }

    // --- Геттеры и Сеттеры ---

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Дополнительный полезный метод toString() для отладки
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", role=" + role +
                '}';
    }
}