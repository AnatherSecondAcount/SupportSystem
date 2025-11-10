package org.bstu.helpdesk.network; // Проверьте, что имя пакета правильное

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

public class ClientNetwork {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    public boolean isConnected = false;

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            isConnected = true;
            return true;
        } catch (IOException e) {
            isConnected = false;
            return false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    // Этот метод подходит для команд, которые возвращают ОДНУ строку ответа
    public String sendCommandAndGetResponse(String command) throws IOException {
        if (!isConnected) {
            throw new IOException("Сервер не подключен");
        }
        writer.println(command);
        return reader.readLine();
    }

    public List<String> getAllTickets() throws IOException {
        if (!isConnected) {
            throw new IOException("Сервер не подключен");
        }

        writer.println("GET_ALL_TICKETS");

        List<String> ticketLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("END_OF_LIST")) {
                break;
            }
            ticketLines.add(line);
        }
        return ticketLines;
    }

    public void disconnect() {
        if (isConnected) {
            try {
                if (socket != null) socket.close();
                isConnected = false;
            } catch (IOException e) {
                // Игнорируем ошибки при закрытии
            }
        }
    }

    public List<String> getCommentsForTicket(long ticketId) throws IOException {
        if (!isConnected) {
            throw new IOException("Сервер не подключен");
        }

        writer.println("GET_COMMENTS;" + ticketId);

        List<String> commentLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("END_OF_LIST")) {
                break;
            }
            commentLines.add(line);
        }
        return commentLines;
    }

    public List<String> getCategories() throws IOException {
        if (!isConnected) {
            throw new IOException("Сервер не подключен");
        }

        writer.println("GET_CATEGORIES");

        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("END_OF_LIST")) {
                break;
            }
            lines.add(line);
        }
        return lines;
    }

    public List<String> getPriorities() throws IOException {
        if (!isConnected) {
            throw new IOException("Сервер не подключен");
        }

        writer.println("GET_PRIORITIES");

        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("END_OF_LIST")) {
                break;
            }
            lines.add(line);
        }
        return lines;
    }
}