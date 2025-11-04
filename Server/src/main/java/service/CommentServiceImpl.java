package service;

import dao.CommentDao;
import dao.JdbcCommentDao;
import model.Comment;
import java.time.LocalDateTime;
import java.util.List;

public class CommentServiceImpl implements CommentService {
    private final CommentDao commentDao = new JdbcCommentDao();

    @Override
    public Comment addComment(long ticketId, long authorId, String text) {
        Comment newComment = new Comment();
        newComment.setTicketId(ticketId);
        newComment.setAuthorId(authorId);
        newComment.setTextContent(text);
        newComment.setCreatedAt(LocalDateTime.now());

        commentDao.create(newComment);
        return newComment;
    }

    @Override
    public List<Comment> getCommentsForTicket(long ticketId) {
        return commentDao.findByTicketId(ticketId);
    }
}