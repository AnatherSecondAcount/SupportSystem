package dao;
import model.User;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByLogin(String login);
}