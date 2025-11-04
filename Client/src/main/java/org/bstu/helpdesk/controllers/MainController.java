package org.bstu.helpdesk.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bstu.helpdesk.network.ClientNetwork;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML
    private ListView<String> ticketsListView;
    @FXML
    private Button refreshButton;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button createButton;
    @FXML
    private Label userInfoLabel;

    private ClientNetwork network;
    //private String currentUserInfo;
    private long currentUserId;
    private String currentUserLogin;
    private String currentUserRole;

    @FXML
    public void initialize() {

        ticketsListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    String[] parts = item.split(";");
                    if (parts.length >= 3) {
                        setText("ID: " + parts[0] + " | " + parts[1] + " [" + parts[2] + "]");
                    } else {
                        setText(item);
                    }
                }
            }
        });

        ticketsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Проверяем, что это двойной клик
                String selectedItem = ticketsListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && !selectedItem.contains("...")) {
                    handleTicketSelection(selectedItem);
                }
            }
        });

        network = new ClientNetwork();
        if (!network.connect()) {
            showAlert("Ошибка подключения", "Не удалось подключиться к серверу.");
            ticketsListView.setItems(FXCollections.observableArrayList("Сервер недоступен."));
            refreshButton.setDisable(true);
            createButton.setDisable(true); // Также блокируем новую кнопку
        } else {
            loadTickets();
        }
    }

    public void initData(ClientNetwork network, String loginResponse) {
        this.network = network;

        // Разбираем ответ сервера "SUCCESS_LOGIN;ID;ЛОГИН;РОЛЬ"
        String[] parts = loginResponse.split(";", 4);
        this.currentUserId = Long.parseLong(parts[1]);
        this.currentUserLogin = parts[2];
        this.currentUserRole = parts[3];

        // Устанавливаем текст в новую метку
        userInfoLabel.setText("Вы вошли как: " + currentUserLogin + " [" + currentUserRole + "]");

        // Загружаем заявки
        loadTickets();

        // --- Логика доступа по ролям ---
        // Если пользователь не администратор, запрещаем удаление
        // (мы пока не делали кнопки в окне деталей, но это задел на будущее)
        // if (!currentUserRole.equals("ADMIN")) {
        //     // ... здесь можно будет скрывать/блокировать кнопки
        // }
    }

    @FXML
    void loadTickets() {
        try {
            List<String> ticketLines = network.getAllTickets();
            if (ticketLines.isEmpty()) {
                ticketsListView.setItems(FXCollections.observableArrayList("Заявок нет."));
            } else {
                ObservableList<String> observableTickets = FXCollections.observableArrayList(ticketLines);
                ticketsListView.setItems(observableTickets);
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка загрузки заявок: " + e.getMessage());
            refreshButton.setDisable(true);
        }
    }

    // =================== НОВЫЙ МЕТОД ===================
    @FXML
    private void createTicket() throws IOException {
        String title = titleField.getText();
        String description = descriptionArea.getText();

        // Простая проверка, что поля не пустые
        if (title.isBlank() || description.isBlank()) {
            showAlert("Ошибка ввода", "Заголовок и описание не могут быть пустыми.");
            return;
        }

        // Формируем команду для сервера
        String command = "CREATE_TICKET;" + title + ";" + description;

        // Отправляем команду на сервер
        String response = network.sendCommandAndGetResponse(command);
        String[] responseParts = response.split(";", 2);

        if (responseParts[0].equals("SUCCESS")) {
            showAlert("Успех", "Заявка успешно создана!");
            // Очищаем поля ввода
            titleField.clear();
            descriptionArea.clear();
            // Обновляем список заявок, чтобы увидеть новую
            loadTickets();
        } else {
            showAlert("Ошибка сервера", "Не удалось создать заявку: " + responseParts[1]);
        }
    }

    // Вспомогательный метод для показа диалоговых окон
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // === НОВЫЙ МЕТОД ДЛЯ ОБРАБОТКИ ВЫБОРА ЗАЯВКИ ===
    private void handleTicketSelection(String selectedItem) {
        // selectedItem - это СЫРАЯ строка от сервера, например: "3;хочу пиццу;OPEN"
        try {
            // 1. Просто разделяем строку по точке с запятой и берем первый элемент (ID)
            String idString = selectedItem.split(";", 2)[0];
            long ticketId = Long.parseLong(idString);

            // 2. Вся остальная логика остается прежней
            String response = network.sendCommandAndGetResponse("GET_TICKET_BY_ID;" + ticketId);

            if (response != null && !response.startsWith("ERROR")) {
                String[] parts = response.split(";", 4);
                if(parts.length == 4) {
                    showTicketDetailWindow(parts[0], parts[1], parts[2], parts[3]);
                } else {
                    showAlert("Ошибка данных", "Сервер вернул некорректные данные о заявке.");
                }
            } else {
                showAlert("Ошибка", "Не удалось загрузить детали заявки. Ответ сервера: " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Произошла ошибка при обработке данных.");
        }
    }

    // === НОВЫЙ МЕТОД ДЛЯ ОТКРЫТИЯ НОВОГО ОКНА ===
    private void showTicketDetailWindow(String id, String title, String status, String description) throws IOException {
        // Создаем загрузчик для нового FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/bstu/helpdesk/ticket-detail-view.fxml"));
        Parent root = loader.load();

        // Получаем контроллер этого FXML
        TicketDetailController controller = loader.getController();
        // Вызываем его метод для установки данных
        controller.initData(Long.parseLong(id), title, status, description, this.network, this, this.currentUserRole);

        // Создаем новую сцену и окно (Stage)
        Stage stage = new Stage();
        stage.setTitle("Детали заявки ID: " + id);
        stage.setScene(new Scene(root));

        // Делаем окно модальным (блокирует основное окно)
        stage.initModality(Modality.APPLICATION_MODAL);

        // Показываем окно и ждем, пока его не закроют
        stage.showAndWait();
    }
}