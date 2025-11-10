// Server/src/main/java/model/BaseUser.java
package model;

public abstract class BaseUser {
    protected long id;
    protected String login;
    protected String password;

    // Абстрактный метод, который ОБЯЗАНЫ реализовать все наследники.
    // Это и есть полиморфизм в действии.
    public abstract User.Role getRole();

    // Общие геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}