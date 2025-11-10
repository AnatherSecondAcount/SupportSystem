package dao;
import model.BaseUser;
import java.util.Optional;

public interface UserDao {
    Optional<BaseUser> findByLogin(String login);
}