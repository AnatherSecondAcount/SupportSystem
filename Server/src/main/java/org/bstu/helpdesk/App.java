package org.bstu.helpdesk;

import server.Server;

public class App {
    public static void main(String[] args) {
        // Создаем экземпляр нашего сервера
        Server server = new Server();
        // Запускаем его
        server.start();
    }
}