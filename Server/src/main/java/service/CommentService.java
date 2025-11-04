package service;

import model.Comment;
import java.util.List;

public interface CommentService {
    Comment addComment(long ticketId, long authorId, String text);
    List<Comment> getCommentsForTicket(long ticketId);
}