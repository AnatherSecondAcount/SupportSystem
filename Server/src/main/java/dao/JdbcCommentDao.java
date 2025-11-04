package dao;

import model.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCommentDao implements CommentDao {
    private final Connection connection = DatabaseConnector.getConnection();

    @Override
    public void create(Comment comment) {
        String sql = "INSERT INTO comments (ticket_id, author_id, text_content, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, comment.getTicketId());
            statement.setLong(2, comment.getAuthorId());
            statement.setString(3, comment.getTextContent());
            statement.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Comment> findByTicketId(long ticketId) {
        List<Comment> comments = new ArrayList<>();
        // SQL-запрос с JOIN для получения логина автора из таблицы users
        String sql = "SELECT c.*, u.login AS author_login FROM comments c " +
                "JOIN users u ON c.author_id = u.id " +
                "WHERE c.ticket_id = ? ORDER BY c.created_at ASC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ticketId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getLong("id"));
                comment.setTicketId(rs.getLong("ticket_id"));
                comment.setAuthorId(rs.getLong("author_id"));
                comment.setTextContent(rs.getString("text_content"));
                comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                comment.setAuthorLogin(rs.getString("author_login")); // Получаем из JOIN
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }
}