// Находится в файле: server/src/main/java/server/ClientHandler.java
package server;

import model.Ticket;
import model.User;
import service.TicketService;
import service.UserService;
import java.util.Optional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.nio.charset.StandardCharsets;

// Runnable позволяет запускать этот класс в отдельном потоке
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final TicketService ticketService;
    private UserService userService;

    public ClientHandler(Socket socket, TicketService ticketService, UserService userService) {
        //System.out.println(">>> SERVER: Создание ClientHandler...");

        this.clientSocket = socket;
        //System.out.println(">>> SERVER: clientSocket установлен: " + (this.clientSocket != null));

        this.ticketService = ticketService;
        //System.out.println(">>> SERVER: ticketService установлен: " + (this.ticketService != null));

        // Здесь самое интересное
        this.userService = userService;
        //System.out.println(">>> SERVER: userService ПОЛУЧЕН как: " + (userService != null));
        //System.out.println(">>> SERVER: this.userService СОХРАНЕН как: " + (this.userService != null));

        //System.out.println(">>> SERVER: ClientHandler создан.");
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
                System.out.println("Получена команда от клиента: " + clientCommand);

                // --- ПРОСТОЙ ПРОТОКОЛ: "КОМАНДА;АРГУМЕНТ1;АРГУМЕНТ2" ---
                String[] parts = clientCommand.split(";", 2);
                String command = parts[0];
                String args = (parts.length > 1) ? parts[1] : "";

                switch (command) {
                    case "LOGIN":
                        try { // <-- Добавляем try
                            System.out.println(">>> SERVER: Начал обработку LOGIN...");
                            String[] loginData = args.split(";", 2);

                            if (loginData.length == 2) {
                                String login = loginData[0];
                                String password = loginData[1];
                                System.out.println(">>> SERVER: Пытаюсь аутентифицировать пользователя: " + login);

                                Optional<User> userOpt = userService.authenticate(login, password);

                                if (userOpt.isPresent()) {
                                    User user = userOpt.get();
                                    String response = "SUCCESS_LOGIN;" + user.getId() + ";" + user.getLogin() + ";" + user.getRole().name();
                                    System.out.println(">>> SERVER: Успешно. Отправляю ответ: " + response);
                                    writer.println(response);
                                } else {
                                    System.out.println(">>> SERVER: Неуспешно. Неверный логин или пароль.");
                                    writer.println("ERROR;Invalid login or password");
                                }
                            } else {
                                System.out.println(">>> SERVER: Ошибка формата команды.");
                                writer.println("ERROR;Invalid arguments for LOGIN");
                            }
                        } catch (Exception e) { // <-- Ловим любую ошибку
                            System.err.println("!!! КРИТИЧЕСКАЯ ОШИБКА ВНУТРИ LOGIN HANDLER !!!");
                            e.printStackTrace(); // <-- Распечатываем полный стектрейс ошибки
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
                        // Аргументы 'args' содержат "Заголовок;Описание"
                        String[] ticketData = args.split(";", 2);
                        if (ticketData.length == 2) {
                            String title = ticketData[0];
                            String description = ticketData[1];

                            // Вызываем наш сервисный слой для создания заявки.
                            // Поскольку у нас пока нет системы пользователей,
                            // мы временно "хардкодим" ID создателя = 1L.
                            Ticket createdTicket = ticketService.createTicket(title, description, 1L);

                            // Отправляем клиенту подтверждение с ID новой заявки
                            writer.println("SUCCESS;Ticket created with ID: " + createdTicket.getId());
                        } else {
                            // Если клиент прислал команду в неверном формате
                            writer.println("ERROR;Invalid arguments for CREATE_TICKET");
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