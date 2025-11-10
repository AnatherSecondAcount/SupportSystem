// Убедитесь, что имя пакета правильное. Например, package dao;
package dao;

import model.Ticket; // Проверьте этот импорт. Путь к Ticket должен быть верным.

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// "implements TicketDao" означает, что этот класс ОБЯЗАН иметь все 5 методов из интерфейса
public class JdbcTicketDao implements TicketDao {

    private final Connection connection;

    public JdbcTicketDao() {
        this.connection = DatabaseConnector.getConnection();
    }

    // Аннотация @Override помогает отлавливать такие ошибки.
    // Она говорит "я ПЕРЕОПРЕДЕЛЯЮ метод из интерфейса".
    // Если метод не найден в интерфейсе, будет ошибка.

    @Override
    public void create(Ticket ticket) {
        String sql = "INSERT INTO tickets (title, description, status, created_by_user_id, created_at, category_id, priority_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ticket.getTitle());
            statement.setString(2, ticket.getDescription());
            statement.setString(3, ticket.getStatus().name());
            statement.setLong(4, ticket.getCreatedByUserId());
            statement.setTimestamp(5, Timestamp.valueOf(ticket.getCreatedAt()));
            statement.setObject(6, ticket.getCategoryId());
            statement.setObject(7, ticket.getPriorityId());


            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                ticket.setId(rs.getLong("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Ticket> findById(long id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Ticket ticket = mapRowToTicket(rs);
                return Optional.of(ticket);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Ticket> findAll() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets ORDER BY id";
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                tickets.add(mapRowToTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    @Override
    public void update(Ticket ticket) {
        String sql = "UPDATE tickets SET title = ?, description = ?, status = ?, category_id = ?, priority_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ticket.getTitle());
            statement.setString(2, ticket.getDescription());
            statement.setString(3, ticket.getStatus().name());
            statement.setObject(4, ticket.getCategoryId());
            statement.setObject(5, ticket.getPriorityId());

            statement.setLong(6, ticket.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM tickets WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Ticket mapRowToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setTitle(rs.getString("title"));
        ticket.setDescription(rs.getString("description"));
        ticket.setStatus(Ticket.Status.valueOf(rs.getString("status")));
        ticket.setCreatedByUserId(rs.getLong("created_by_user_id"));
        ticket.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        ticket.setCategoryId((Integer) rs.getObject("category_id"));
        ticket.setPriorityId((Integer) rs.getObject("priority_id"));
        return ticket;
    }
}