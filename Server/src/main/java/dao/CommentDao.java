package dao;

import model.Comment;
import java.util.List;

public interface CommentDao {
    void create(Comment comment);
    List<Comment> findByTicketId(long ticketId);
}