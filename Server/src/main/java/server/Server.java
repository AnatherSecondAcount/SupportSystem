// Находится в файле: server/src/main/java/server/Server.java
package server;

import service.TicketService;
import service.TicketServiceImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 8888; // Порт, на котором будет работать сервер

    public void start() {
        // Создаем сервис, который будет обрабатывать бизнес-логику
        TicketService ticketService = new TicketServiceImpl();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен и слушает порт " + PORT);

            // Бесконечный цикл для ожидания клиентов
            while (true) {
                // accept() блокирует выполнение, пока не подключится новый клиент
                Socket clientSocket = serverSocket.accept();
                System.out.println("Подключился новый клиент: " + clientSocket.getInetAddress());

                // Для каждого клиента создаем новый поток-обработчик
                // и передаем ему сокет и сервис
                ClientHandler clientHandler = new ClientHandler(clientSocket, ticketService);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}