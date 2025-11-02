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
                // Поток для чтения данных от клиента
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // Поток для отправки данных клиенту
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
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