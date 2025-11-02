// Находится в файле: server/src/main/java/service/TicketService.java
package service;

import model.Ticket; // Проверьте правильность импорта
import java.util.List;
import java.util.Optional;

public interface TicketService {
    Ticket createTicket(String title, String description, long creatorId);
    Optional<Ticket> getTicketById(long id);
    List<Ticket> getAllTickets();
    Ticket updateTicketStatus(long ticketId, Ticket.Status newStatus);
    void deleteTicket(long ticketId);
}