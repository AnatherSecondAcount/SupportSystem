package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell; // Импорт для ListCell
import network.ClientNetwork; // Импорт сетевого клиента
import model.Ticket; // Импорт нашей модели Ticket

import java.io.IOException;
import java.util.List;

public class MainController {

    // Поля, связанные с элементами в FXML (должны совпадать fx:id)
    @FXML
    private ListView<String> ticketsListView; // Список, куда будем выводить заявки

    @FXML
    private Button refreshButton; // Ссылка на кнопку

    private ClientNetwork network; // Наш сетевой клиент

    // Этот метод вызывается при загрузке FXML
    @FXML
    public void initialize() {
        // 1. Настраиваем отображение элементов в ListView
        // Каждая заявка будет выводиться в виде "ID; Заголовок; Статус"
        ticketsListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    // Разделяем строку, полученную с сервера
                    String[] parts = item.split(";");
                    if (parts.length >= 3) {
                        setText("ID: " + parts[0] + " | " + parts[1] + " [" + parts[2] + "]");
                    } else {
                        setText(item); // Если формат не соответствует, выводим как есть
                    }
                }
            }
        });

        // 2. Создаем и подключаем сетевого клиента
        network = new ClientNetwork();
        if (!network.connect()) {
            // Если подключиться не удалось, выводим ошибку в список
            ObservableList<String> errorList = FXCollections.observableArrayList("Не удалось подключиться к серверу.");
            ticketsListView.setItems(errorList);
            // Кнопка обновления становится неактивной
            refreshButton.setDisable(true);
        } else {
            // Если подключились, сразу грузим заявки
            loadTickets();
        }
    }

    // Метод, который вызывается при нажатии кнопки "Обновить список"
    @FXML
    private void loadTickets() {
        try {
            List<String> ticketLines = network.getAllTickets(); // Запрашиваем все заявки с сервера

            if (ticketLines.isEmpty()) {
                // Если заявок нет, отображаем сообщение
                ticketsListView.setItems(FXCollections.observableArrayList("Заявок нет."));
            } else {
                // Преобразуем список строк от сервера в ObservableList для ListView
                ObservableList<String> observableTickets = FXCollections.observableArrayList(ticketLines);
                ticketsListView.setItems(observableTickets);
            }
        } catch (IOException e) {
            // Если произошла ошибка, отображаем сообщение об ошибке
            ObservableList<String> errorList = FXCollections.observableArrayList("Ошибка загрузки заявок: " + e.getMessage());
            ticketsListView.setItems(errorList);
            refreshButton.setDisable(true); // Блокируем кнопку при ошибке
        }
    }
}