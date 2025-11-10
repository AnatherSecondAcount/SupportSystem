package dao;
import model.Category;
import model.Priority;
import java.util.List;

public interface DictionaryDao {
    List<Category> findAllCategories();
    List<Priority> findAllPriorities();
}