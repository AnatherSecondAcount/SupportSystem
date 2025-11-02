package network; // Проверьте пакет, чтобы соответствовал структуре

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public class ClientNetwork {
    private static final String SERVER_HOST = "localhost"; // Адрес сервера
    private static final int SERVER_PORT = 8888;        // Порт сервера

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean isConnected = false;

    // Метод для подключения к серверу
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;
            System.out.println("Подключено к серверу " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (ConnectException e) {
            System.err.println("Не удалось подключиться к серверу: Сервер не запущен или недоступен.");
            isConnected = false;
            return false;
        } catch (IOException e) {
            System.err.println("Ошибка сетевого соединения: " + e.getMessage());
            isConnected = false;
            return false;
        }
    }

    // Метод для отправки команды серверу
    public String sendCommand(String command) {
        if (!isConnected) {
            return "ERROR;Сервер не подключен";
        }

        try {
            writer.println(command); // Отправляем команду

            // Читаем ответ от сервера
            // Здесь нужно продумать, как получать ответ.
            // Если сервер отправит несколько строк (например, список заявок), 
            // нам нужен будет механизм ожидания конца передачи.
            // Для начала, попробуем прочитать одну строку:
            String response = reader.readLine();

            // TODO: В реальном приложении здесь будет более сложная логика
            // для обработки многострочных ответов (например, списка).
            // Сейчас считаем, что сервер отвечает одной строкой (или ERROR;)

            if (response == null) {
                return "ERROR;Сервер неожиданно закрыл соединение";
            }

            System.out.println("Получен ответ: " + response);
            return response;

        } catch (IOException e) {
            System.err.println("Ошибка при отправке команды или чтении ответа: " + e.getMessage());
            disconnect(); // Разрываем соединение при ошибке
            return "ERROR;Ошибка связи с сервером";
        }
    }

    // --- МЕТОД ДЛЯ ПОЛУЧЕНИЯ СПИСКА ЗАЯВОК ---
    public List<String> getAllTickets() throws IOException {
        if (!isConnected) {
            throw new IOException("Сервер не подключен");
        }

        writer.println("GET_ALL_TICKETS");

        List<String> ticketLines = new ArrayList<>();
        String line;
        // Читаем строки, пока не получим маркер конца списка "END_OF_LIST"
        while ((line = reader.readLine()) != null) {
            if (line.equals("END_OF_LIST")) {
                break;
            }
            ticketLines.add(line);
        }

        if (ticketLines.isEmpty()) {
            // Проверяем, если не получили ничего, возможно, это ошибка на сервере
            // Или просто заявок нет. Попробуем прочитать возможную ошибку, если сервер вернет.
            if (reader.ready()) { // Если есть что читать, это может быть сообщение об ошибке
                String errorMessage = reader.readLine();
                if (errorMessage != null && errorMessage.startsWith("ERROR")) {
                    throw new IOException("Сервер вернул ошибку: " + errorMessage);
                }
            }
            // Если списка нет и ошибки тоже, возможно, заявок просто нет
        }

        return ticketLines;
    }


    // Метод для отключения
    public void disconnect() {
        if (isConnected) {
            try {
                if (writer != null) writer.close();
                if (reader != null) reader.close();
                if (socket != null) socket.close();
                isConnected = false;
                System.out.println("Отключено от сервера.");
            } catch (IOException e) {
                System.err.println("Ошибка при закрытии сетевых ресурсов: " + e.getMessage());
            }
        }
    }
}