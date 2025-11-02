// Убедитесь, что имя пакета правильное.
package dao;

import model.Ticket;
import java.util.Optional;
import java.util.List;

// Внутри интерфейса не должно быть никаких дубликатов!
public interface TicketDao {
    void create(Ticket ticket);
    Optional<Ticket> findById(long id);
    List<Ticket> findAll();
    void update(Ticket ticket);
    void delete(long id);
}