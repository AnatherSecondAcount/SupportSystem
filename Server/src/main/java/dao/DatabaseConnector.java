package dao;

// Находится в файле: server/src/main/java/com/helpdesk/dao/DatabaseConnector.java

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    // --- НАСТРОЙКИ ПОДКЛЮЧЕНИЯ ---
    // Вам нужно создать базу данных 'helpdesk_db' вручную
    private static final String URL = "jdbc:postgresql://localhost:5432/helpdesk_db";
    private static final String USER = "postgres"; // Ваше имя пользователя для PostgreSQL
    private static final String PASSWORD = "admin"; // Ваш пароль

    private static Connection connection = null;

    // Приватный конструктор, чтобы нельзя было создать экземпляр класса
    private DatabaseConnector() { }

    public static Connection getConnection() {
        if (connection == null) {
            synchronized (DatabaseConnector.class) {
                if (connection == null) {
                    try {
                        connection = DriverManager.getConnection(URL, USER, PASSWORD);
                        System.out.println("Соединение с базой данных установлено.");
                    } catch (SQLException e) {
                        // Лучше использовать логгер, но для курсового проекта этого достаточно
                        System.err.println("Не удалось установить соединение с БД!");
                        e.printStackTrace();
                    }
                }
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Соединение с базой данных закрыто.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
