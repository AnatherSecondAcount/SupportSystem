// Находится в файле: server/src/main/java/server/ClientHandler.java
package server;

import model.Ticket;
import service.TicketService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;

// Runnable позволяет запускать этот класс в отдельном потоке
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final TicketService ticketService;

    public ClientHandler(Socket socket, TicketService ticketService) {
        this.clientSocket = socket;
        this.ticketService = ticketService;
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