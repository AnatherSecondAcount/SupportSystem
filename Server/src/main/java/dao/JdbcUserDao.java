package dao;

import model.Admin;
import model.BaseUser;
import model.Employee;
import model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
    private final Connection connection = DatabaseConnector.getConnection();

    @Override
    public Optional<BaseUser> findByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                // Создаем фабрику объектов в зависимости от роли
                User.Role role = User.Role.valueOf(rs.getString("role"));
                BaseUser user;

                if (role == User.Role.ADMIN) {
                    user = new Admin();
                } else {
                    user = new Employee();
                }

                user.setId(rs.getLong("id"));
                user.setLogin(rs.getString("login"));
                user.setPassword(rs.getString("password"));

                return Optional.of(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}