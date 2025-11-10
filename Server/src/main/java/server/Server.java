// Находится в файле: server/src/main/java/server/Server.java
package server;
import dao.*;
import service.*;

import service.TicketService;
import service.TicketServiceImpl;
import service.UserService;
import service.UserServiceImpl;
import service.CommentService;
import service.CommentServiceImpl;
import dao.DictionaryDao;
import dao.JdbcDictionaryDao;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 8888; // Порт, на котором будет работать сервер

    public void start() {
        // Создаем ВСЕ сервисы и DAO
        TicketService ticketService = new TicketServiceImpl();
        UserService userService = new UserServiceImpl();
        CommentService commentService = new CommentServiceImpl();
        DictionaryDao dictionaryDao = new JdbcDictionaryDao(); // <-- Создан

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен и слушает порт " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Подключился новый клиент: " + clientSocket.getInetAddress());

                // Передаем ВСЕ 4 зависимости в конструктор
                ClientHandler clientHandler = new ClientHandler(
                        clientSocket, ticketService, userService, commentService, dictionaryDao); // <-- Передан

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}