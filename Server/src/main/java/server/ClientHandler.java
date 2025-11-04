// Находится в файле: server/src/main/java/server/ClientHandler.java
package server;

import model.Ticket;
import model.User;
import service.TicketService;
import service.UserService;
import java.util.Optional;
import model.Comment;
import service.CommentService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.nio.charset.StandardCharsets;

// Runnable позволяет запускать этот класс в отдельном потоке
public class ClientHandler implements Runnable {

    // === КОД ДЛЯ ОТЛАДКИ ===
    private static int counter = 0; // Общий счетчик для всех ClientHandler'ов
    private final int handlerId;      // Уникальный ID для этого конкретного объекта

    private final Socket clientSocket;
    private final TicketService ticketService;
    private UserService userService;
    private final CommentService commentService;
    private User currentUser = null;

    public ClientHandler(Socket socket, TicketService ticketService, UserService userService, CommentService commentService) {
        // === КОД ДЛЯ ОТЛАДКИ ===
        this.handlerId = ++counter; // Увеличиваем счетчик и присваиваем ID
        System.out.println(">>> SERVER: Создан ClientHandler с ID = " + this.handlerId);

        this.clientSocket = socket;
        this.ticketService = ticketService;
        this.userService = userService;
        this.commentService = commentService;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8)
        ) {
            String clientCommand;
            // Читаем команды от клиента, пока он не отключится
            while ((clientCommand = reader.readLine()) != null) {
                // === КОД ДЛЯ ОТЛАДКИ ===
                System.out.println(">>> [" + this.handlerId + "] Получена команда: " + clientCommand);
                // ======================

                System.out.println("Получена команда от клиента: " + clientCommand);

                // --- ПРОСТОЙ ПРОТОКОЛ: "КОМАНДА;АРГУМЕНТ1;АРГУМЕНТ2" ---
                String[] parts = clientCommand.split(";", 2);
                String command = parts[0];
                String args = (parts.length > 1) ? parts[1] : "";

                switch (command) {
                    case "LOGIN":
                        String[] loginData = args.split(";", 2);
                        if (loginData.length == 2) {
                            String login = loginData[0];
                            String password = loginData[1];
                            Optional<User> userOpt = userService.authenticate(login, password);

                            if (userOpt.isPresent()) {
                                this.currentUser = userOpt.get(); // 1. Присваиваем
                                System.out.println(">>> [" + this.handlerId + "] User logged in: " + this.currentUser.getLogin());

                                // 2. Используем
                                String response = "SUCCESS_LOGIN;" + this.currentUser.getId() + ";" + this.currentUser.getLogin() + ";" + this.currentUser.getRole().name();
                                writer.println(response);
                            } else {
                                System.err.println("!!! [" + this.handlerId + "] Authentication failed for user: " + login);
                                writer.println("ERROR;Invalid login or password");
                            }
                        } else {
                            writer.println("ERROR;Invalid arguments for LOGIN");
                        }
                        break;

                    case "GET_ALL_TICKETS":
                        List<Ticket> tickets = ticketService.getAllTickets();
                        // Сериализуем (превращаем в строку) список заявок
                        for (Ticket ticket : tickets) {
                            writer.println(ticket.getId() + ";" + ticket.getTitle() + ";" + ticket.getStatus());
                        }
                        // Отправляем специальную строку-маркер конца передачи
                        writer.println("END_OF_LIST");
                        break;

                    case "GET_TICKET_BY_ID":
                        try {
                            long id = Long.parseLong(args);
                            Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
                            if(ticketOpt.isPresent()) {
                                Ticket t = ticketOpt.get();
                                writer.println(t.getId() + ";" + t.getTitle() + ";" + t.getStatus() + ";" + t.getDescription());
                            } else {
                                writer.println("ERROR;Заявка не найдена");
                            }
                        } catch (NumberFormatException e) {
                            writer.println("ERROR;Неверный формат ID");
                        }
                        break;

                    case "CREATE_TICKET":
                        String[] ticketData = args.split(";", 3); // Теперь 3 части
                        if (ticketData.length == 3) {
                            String title = ticketData[0];
                            String description = ticketData[1];
                            long creatorId = Long.parseLong(ticketData[2]); // <-- Получаем ID из команды
                            Ticket createdTicket = ticketService.createTicket(title, description, creatorId);
                            writer.println("SUCCESS;Ticket created with ID: " + createdTicket.getId());
                        } else {
                            // Если клиент прислал команду в неверном формате
                            writer.println("ERROR;Invalid arguments for CREATE_TICKET");
                        }
                        break;

                    case "GET_COMMENTS":
                        try {
                            long ticketId = Long.parseLong(args);
                            List<Comment> comments = commentService.getCommentsForTicket(ticketId);
                            for (Comment c : comments) {
                                writer.println(c.getAuthorLogin() + ";" + c.getCreatedAt() + ";" + c.getTextContent());
                            }
                            writer.println("END_OF_LIST");
                        } catch (NumberFormatException e) {
                            writer.println("ERROR;Invalid ticket ID");
                        }
                        break;

                    case "ADD_COMMENT":
                        // === КОД ДЛЯ ОТЛАДКИ ===
                        if(this.currentUser == null){
                            System.err.println("!!! [" + this.handlerId + "] ОШИБКА: Попытка добавить комментарий, но currentUser is null!");
                        } else {
                            System.out.println(">>> [" + this.handlerId + "] Пользователь, добавляющий комментарий: " + this.currentUser.getLogin());
                        }
                        // ======================

                        String[] commentData = args.split(";", 2);
                        if (commentData.length == 2) {

                            try {
                                System.out.println(">>> SERVER: Начал обработку ADD_COMMENT...");
                                long ticketId = Long.parseLong(commentData[0]);
                                String text = commentData[1];

                                System.out.println(">>> SERVER: Вызов commentService.addComment для ticketId=" + ticketId + ", authorId=" + currentUser.getId());

                                commentService.addComment(ticketId, currentUser.getId(), text);

                                System.out.println(">>> SERVER: Успешно добавил комментарий. Отправляю SUCCESS.");
                                writer.println("SUCCESS;Comment added");

                            } catch (Throwable t) { // <-- ИЗМЕНЕНО НА Throwable t
                                System.err.println("!!! КРИТИЧЕСКАЯ ОШИБКА/ERROR ВНУТРИ ADD_COMMENT HANDLER !!!");
                                t.printStackTrace(); // <-- Распечатаем Throwable
                            }

                        } else {
                            writer.println("ERROR;Invalid arguments for ADD_COMMENT");
                        }
                        break;

                    case "UPDATE_STATUS":
                        // args содержит "ID;НОВЫЙ_СТАТУС"
                        String[] updateData = args.split(";", 2);
                        if (updateData.length == 2) {
                            try {
                                long ticketId = Long.parseLong(updateData[0]);
                                Ticket.Status newStatus = Ticket.Status.valueOf(updateData[1]); // Превращаем строку в enum

                                // Вызываем сервисный слой
                                ticketService.updateTicketStatus(ticketId, newStatus);
                                writer.println("SUCCESS;Status updated successfully");

                            } catch (NumberFormatException e) {
                                writer.println("ERROR;Invalid ticket ID format");
                            } catch (IllegalArgumentException e) {
                                // Эта ошибка возникнет, если valueOf() не найдет такой статус
                                writer.println("ERROR;Invalid status value");
                            }
                        } else {
                            writer.println("ERROR;Invalid arguments for UPDATE_STATUS");
                        }
                        break;
                    case "DELETE_TICKET":
                        // args содержит ID
                        try {
                            long ticketId = Long.parseLong(args);
                            ticketService.deleteTicket(ticketId); // Вызываем сервисный слой
                            writer.println("SUCCESS;Ticket deleted successfully");
                        } catch (NumberFormatException e) {
                            writer.println("ERROR;Invalid ticket ID format");
                        }
                        break;

                    // TODO: Добавить обработку других команд (CREATE, UPDATE, DELETE)

                    default:
                        writer.println("ERROR;Неизвестная команда");
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при общении с клиентом: " + e.getMessage());
        } finally {
            System.out.println("Клиент " + clientSocket.getInetAddress() + " отключился.");
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}