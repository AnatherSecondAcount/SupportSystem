// Находится в файле: server/src/main/java/service/TicketServiceImpl.java
package service;

import dao.JdbcTicketDao;
import dao.TicketDao;
import model.Ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TicketServiceImpl implements TicketService {

    private final TicketDao ticketDao;

    // В конструкторе мы создаем наш DAO.
    // В будущем здесь можно будет использовать Dependency Injection.
    public TicketServiceImpl() {
        this.ticketDao = new JdbcTicketDao();
    }

    @Override
    public Ticket createTicket(String title, String description, long creatorId, int categoryId, int priorityId)  {
        Ticket newTicket = new Ticket();
        newTicket.setTitle(title);
        newTicket.setDescription(description);
        newTicket.setCreatedByUserId(creatorId);

        // === Бизнес-логика ===
        // 1. Устанавливаем начальный статус по умолчанию
        newTicket.setStatus(Ticket.Status.OPEN);
        // 2. Устанавливаем текущее время создания
        newTicket.setCreatedAt(LocalDateTime.now());

        // --- Устанавливаем ID из параметров ---
        newTicket.setCategoryId(categoryId);
        newTicket.setPriorityId(priorityId);

        // === ВРЕМЕННО УСТАНАВЛИВАЕМ ЗНАЧЕНИЯ ПО УМОЛЧАНИЮ ===
        newTicket.setCategoryId(2); // ID для 'SOFTWARE'
        newTicket.setPriorityId(1); // ID для 'LOW'

        ticketDao.create(newTicket);
        return newTicket;
    }

    @Override
    public Optional<Ticket> getTicketById(long id) {
        // Здесь логика простая - просто передаем вызов в DAO
        return ticketDao.findById(id);
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketDao.findAll();
    }

    @Override
    public Ticket updateTicketStatus(long ticketId, Ticket.Status newStatus) {
        Optional<Ticket> ticketOpt = ticketDao.findById(ticketId);

        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            // === Бизнес-логика ===
            // Например, можно добавить проверку: нельзя закрыть заявку, если она не была в работе.
            ticket.setStatus(newStatus);
            ticketDao.update(ticket);
            return ticket;
        } else {
            // В будущем здесь лучше выбрасывать свое исключение, например, TicketNotFoundException
            throw new RuntimeException("Заявка с ID " + ticketId + " не найдена.");
        }
    }

    @Override
    public void deleteTicket(long ticketId) {
        ticketDao.delete(ticketId);
    }
}