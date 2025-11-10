package service;

import dao.JdbcUserDao;
import dao.UserDao;
import model.BaseUser;
import model.User;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserDao userDao = new JdbcUserDao();

    @Override
    public Optional<BaseUser> authenticate(String login, String password) {
        Optional<BaseUser> userOpt = userDao.findByLogin(login);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt;
        }
        return Optional.empty();
    }
}