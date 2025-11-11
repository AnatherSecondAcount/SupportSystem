package org.bstu.helpdesk.controllers;
import org.bstu.helpdesk.App;
import org.bstu.helpdesk.models.DictionaryItem;

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
import org.bstu.helpdesk.network.NetworkManager;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private ListView<String> ticketsListView;
    @FXML private Button refreshButton;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private Button createButton;
    @FXML private Label userInfoLabel;
    @FXML private ComboBox<DictionaryItem> categoryComboBox;
    @FXML private ComboBox<DictionaryItem> priorityComboBox;
    @FXML private Button logoutButton;

    private ClientNetwork network;
    //private String currentUserInfo;
    private long currentUserId;
    private String currentUserLogin;
    private String currentUserRole;
    private App app;

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
                        String id = parts[0];
                        String title = parts[1];
                        String status = parts[2];
                        // === ПЕРЕВОДИМ СТАТУС ===
                        String localizedStatus = switch (status) {
                            case "OPEN" -> "Открыта";
                            case "IN_PROGRESS" -> "В работе";
                            case "CLOSED" -> "Закрыта";
                            default -> status;
                        };
                        setText("ID: " + id + " | " + title + " [" + localizedStatus + "]");
                    } else {
                        setText(item);
                    }
                }
            }
        });

        ticketsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = ticketsListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && !selectedItem.contains("...")) {
                    handleTicketSelection(selectedItem);
                }
            }
        });
    }

    public void initData(App app, ClientNetwork network, String loginResponse) {
        this.app = app;
        this.network = NetworkManager.getNetwork();

        // Разбираем ответ сервера "SUCCESS_LOGIN;ID;ЛОГИН;РОЛЬ"
        String[] parts = loginResponse.split(";", 4);
        this.currentUserId = Long.parseLong(parts[1]);
        this.currentUserLogin = parts[2];
        this.currentUserRole = parts[3];

        // Устанавливаем текст в новую метку
        userInfoLabel.setText("Вы вошли как: " + currentUserLogin + " [" + currentUserRole + "]");

        // Загружаем заявки
        loadTickets();
        // Загружаем справочники
        loadDictionaries();
    }

    public long getCurrentUserId() {
        return currentUserId;
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

    private void loadDictionaries() {
        try {
            // Загрузка и обработка категорий
            List<String> categoriesData = network.getCategories();
            ObservableList<DictionaryItem> categories = FXCollections.observableArrayList();
            for (String line : categoriesData) {
                String[] parts = line.split(";", 2);
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];

                // Объявляем переменную ЗДЕСЬ
                String localizedName = switch (name) {
                    case "HARDWARE" -> "Оборудование";
                    case "SOFTWARE" -> "Программное обеспечение";
                    case "NETWORK" -> "Сеть";
                    default -> name;
                };
                categories.add(new DictionaryItem(id, localizedName));
            }
            categoryComboBox.setItems(categories);
            if (!categories.isEmpty()) { categoryComboBox.getSelectionModel().selectFirst(); }

            // Загрузка и обработка приоритетов
            List<String> prioritiesData = network.getPriorities();
            ObservableList<DictionaryItem> priorities = FXCollections.observableArrayList();
            for (String line : prioritiesData) {
                String[] parts = line.split(";", 2);
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];

                // И объявляем еще одну, независимую переменную, ЗДЕСЬ
                String localizedName = switch (name) {
                    case "LOW" -> "Низкий";
                    case "MEDIUM" -> "Средний";
                    case "HIGH" -> "Высокий";
                    default -> name;
                };
                priorities.add(new DictionaryItem(id, localizedName));
            }
            priorityComboBox.setItems(priorities);
            if (!priorities.isEmpty()) { priorityComboBox.getSelectionModel().selectFirst(); }

        } catch (IOException e) {
            showAlert("Ошибка загрузки", "Не удалось загрузить справочники категорий и приоритетов.");
            e.printStackTrace();
        }
    }

    @FXML
    private void createTicket() throws IOException {
        String title = titleField.getText();
        String description = descriptionArea.getText();

        if (title.isBlank() || description.isBlank()) {
            showAlert("Ошибка ввода", "Заголовок и описание не могут быть пустыми.");
            return;
        }

        DictionaryItem selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        DictionaryItem selectedPriority = priorityComboBox.getSelectionModel().getSelectedItem();

        if (selectedCategory == null || selectedPriority == null) {
            showAlert("Ошибка ввода", "Пожалуйста, выберите категорию и приоритет.");
            return;
        }

        int categoryId = selectedCategory.getId();
        int priorityId = selectedPriority.getId();

        String command = "CREATE_TICKET;" + title + ";" + description + ";" + currentUserId + ";" + categoryId + ";" + priorityId;
        String response = network.sendCommandAndGetResponse(command);

        if (response != null && response.startsWith("SUCCESS")) {
            showAlert("Успех", "Заявка успешно создана!");
            titleField.clear();
            descriptionArea.clear();
            loadTickets();
        } else {
            showAlert("Ошибка сервера", "Не удалось создать заявку: " + response);
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

    @FXML
    private void handleLogout() {
        // Закрываем текущее окно
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();

        // Отключаемся от сервера
        network.disconnect();

        // Вызываем метод в App, который перезапустит логин
        app.showLoginScreen();
    }


}