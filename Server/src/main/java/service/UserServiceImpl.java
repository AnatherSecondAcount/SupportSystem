package service;

import dao.JdbcUserDao;
import dao.UserDao;
import model.User;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserDao userDao = new JdbcUserDao();

    @Override
    public Optional<User> authenticate(String login, String password) {
        Optional<User> userOpt = userDao.findByLogin(login);
        // Проверяем, найден ли пользователь и совпадает ли пароль
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt;
        }
        return Optional.empty(); // Возвращаем пустой Optional, если аутентификация не удалась
    }
}