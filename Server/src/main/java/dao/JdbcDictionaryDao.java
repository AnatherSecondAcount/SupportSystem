package dao;
import model.Category;
import model.Priority;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcDictionaryDao implements DictionaryDao {
    private final Connection connection = DatabaseConnector.getConnection();

    @Override
    public List<Category> findAllCategories() {
        List<Category> list = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM categories ORDER BY id");
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                list.add(category);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Priority> findAllPriorities() {
        List<Priority> list = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT * FROM priorities ORDER BY id");
            while (rs.next()) {
                Priority priority = new Priority();
                priority.setId(rs.getInt("id"));
                priority.setName(rs.getString("name"));
                list.add(priority);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}