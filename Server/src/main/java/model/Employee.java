package model;

public class Employee extends BaseUser {

    // Реализуем абстрактный метод родителя
    @Override
    public User.Role getRole() {
        return User.Role.EMPLOYEE;
    }
}