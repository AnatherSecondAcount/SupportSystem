package service;
import model.BaseUser;
import model.User;
import java.util.Optional;

public interface UserService {
    Optional<BaseUser> authenticate(String login, String password);
}