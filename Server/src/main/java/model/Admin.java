package model;

public class Admin extends BaseUser {

    // Реализуем абстрактный метод родителя
    @Override
    public User.Role getRole() {
        return User.Role.ADMIN;
    }
}