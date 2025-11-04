package service;
import model.User;
import java.util.Optional;

public interface UserService {
    Optional<User> authenticate(String login, String password);
}