package org.bstu.helpdesk.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Ticket;
import org.bstu.helpdesk.network.ClientNetwork;
import java.io.IOException;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class TicketDetailController {

    @FXML private Label idLabel;
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button markInProgressButton;
    @FXML private Button markAsClosedButton;
    @FXML private Button deleteButton;

    // Поля для хранения нужных данных и объектов
    private long ticketId;
    private ClientNetwork network;
    private MainController mainController;

    // Новый метод для передачи всех нужных данных из MainController
    public void initData(long ticketId, String title, String status, String description,
                         ClientNetwork network, MainController mainController, String userRole) {
        this.ticketId = ticketId;
        this.network = network;
        this.mainController = mainController;

        idLabel.setText(String.valueOf(ticketId));
        titleLabel.setText(title);
        statusLabel.setText(status);
        descriptionLabel.setText(description);

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