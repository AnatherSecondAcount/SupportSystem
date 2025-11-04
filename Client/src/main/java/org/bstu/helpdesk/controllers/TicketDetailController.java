package org.bstu.helpdesk.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Ticket;
import org.bstu.helpdesk.network.ClientNetwork;
import org.bstu.helpdesk.network.NetworkManager;

import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TicketDetailController {

    @FXML private Label idLabel;
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button markInProgressButton;
    @FXML private Button markAsClosedButton;
    @FXML private Button deleteButton;
    @FXML private ListView<String> commentsListView;
    @FXML private TextField newCommentField;
    @FXML private Button addCommentButton;

    // Поля для хранения нужных данных и объектов
    private long ticketId;
    private ClientNetwork network;
    private MainController mainController;

    @FXML
    public void initialize() {
        // Настраиваем красивое отображение для ячеек списка комментариев
        commentsListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // ============= НОВАЯ ПРОВЕРКА =============
                    if (!item.contains(";")) {
                        // Если это служебное сообщение, просто выводим его.
                        setText(item);
                        setWrapText(false);
                        return; // Выходим, чтобы не выполнять остальной код.
                    }
                    // ===========================================

                    // Остальной код для разбора и форматирования выполняется,
                    // только если мы уверены, что это строка с данными.
                    String[] parts = item.split(";", 3);
                    String author = parts[0];
                    LocalDateTime dateTime = LocalDateTime.parse(parts[1]);
                    String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    String text = parts[2];

                    setText(author + " (" + formattedDateTime + "):\n" + text);
                    setWrapText(true);
                }
            }
        });
    }

    // Новый метод для передачи всех нужных данных из MainController
    public void initData(long ticketId, String title, String status, String description,
                         ClientNetwork network, MainController mainController, String userRole) {
        this.ticketId = ticketId;
        this.network = NetworkManager.getNetwork();
        this.mainController = mainController;

        idLabel.setText(String.valueOf(ticketId));
        titleLabel.setText(title);
        statusLabel.setText(status);
        descriptionLabel.setText(description);

        // Загружаем комментарии сразу при открытии окна
        loadComments();

        if (!userRole.equals("ADMIN")) {
            deleteButton.setVisible(false); // Делаем кнопку невидимой
            deleteButton.setManaged(false); // "Схлопываем" пространство, которое она занимала
        }

        // Блокируем кнопки в зависимости от текущего статуса
        if (Ticket.Status.valueOf(status) == Ticket.Status.OPEN) {
            markAsClosedButton.setDisable(true);
        } else if (Ticket.Status.valueOf(status) == Ticket.Status.IN_PROGRESS) {
            markInProgressButton.setDisable(true);
        } else if (Ticket.Status.valueOf(status) == Ticket.Status.CLOSED) {
            markInProgressButton.setDisable(true);
            markAsClosedButton.setDisable(true);
        }

        // --- Логика доступа по ролям ---
        if (!userRole.equals("ADMIN")) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
    }
    private void loadComments() {
        try {
            List<String> commentLines = network.getCommentsForTicket(ticketId); // Новый метод в ClientNetwork
            if(commentLines.isEmpty()){
                commentsListView.setItems(FXCollections.observableArrayList("Комментариев пока нет."));
            } else {
                ObservableList<String> observableComments = FXCollections.observableArrayList(commentLines);
                commentsListView.setItems(observableComments);
            }
        } catch (IOException e) {
            commentsListView.setItems(FXCollections.observableArrayList("Ошибка загрузки комментариев."));
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddComment() {
        String commentText = newCommentField.getText();
        if (commentText.isBlank()) {
            return;
        }

        try {
            String command = "ADD_COMMENT;" + ticketId + ";" + commentText;
            String response = network.sendCommandAndGetResponse(command);

            if (response != null && response.startsWith("SUCCESS")) {
                newCommentField.clear();
                loadComments(); // Перезагружаем список, чтобы увидеть новый комментарий
            } else {
                showAlert("Ошибка", "Не удалось добавить комментарий: " + response);
            }
        } catch (IOException e) {
            showAlert("Ошибка сети", "Потеряно соединение с сервером.");
        }
    }
    @FXML
    private void onMarkInProgress() {
        updateStatus(Ticket.Status.IN_PROGRESS);
    }

    @FXML
    private void onMarkAsClosed() {
        updateStatus(Ticket.Status.CLOSED);
    }

    private void updateStatus(Ticket.Status newStatus) {
        try {
            String command = "UPDATE_STATUS;" + ticketId + ";" + newStatus.name();
            String response = network.sendCommandAndGetResponse(command);
            if (response != null && response.startsWith("SUCCESS")) {
                showAlert("Успех", "Статус заявки обновлен.");
                // Вызываем метод в MainController для обновления главного списка
                mainController.loadTickets();
                // Закрываем текущее окно
                closeWindow();
            } else {
                showAlert("Ошибка", "Не удалось обновить статус: " + response);
            }
        } catch (IOException e) {
            showAlert("Ошибка сети", "Потеряно соединение с сервером.");
        }
    }

    @FXML
    private void onDelete() {
        // Задаем пользователю подтверждающий вопрос
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Вы уверены, что хотите удалить заявку ID: " + ticketId + "?");
        alert.setContentText("Это действие необратимо.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Если пользователь нажал "OK", отправляем команду
            try {
                String command = "DELETE_TICKET;" + ticketId;
                String response = network.sendCommandAndGetResponse(command);
                if (response != null && response.startsWith("SUCCESS")) {
                    showAlert("Успех", "Заявка успешно удалена.");
                    mainController.loadTickets(); // Обновляем главный список
                    closeWindow(); // Закрываем окно деталей
                } else {
                    showAlert("Ошибка", "Не удалось удалить заявку: " + response);
                }
            } catch (IOException e) {
                showAlert("Ошибка сети", "Потеряно соединение с сервером.");
            }
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}