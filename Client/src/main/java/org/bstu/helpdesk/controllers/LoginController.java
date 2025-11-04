package org.bstu.helpdesk.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bstu.helpdesk.network.ClientNetwork;
import java.io.IOException;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private ClientNetwork network;
    private String loginResponse = null; // Поле для хранения ответа сервера

    public void initialize() {
        network = new ClientNetwork();
        if (!network.connect()) {
            errorLabel.setText("Нет связи с сервером.");
            loginButton.setDisable(true);
        }
    }

    @FXML
    private void handleLogin() throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Логин и пароль не могут быть пустыми.");
            return;
        }

        String command = "LOGIN;" + login + ";" + password;

        // ============== ДИАГНОСТИКА - ЧАСТЬ 1 ==============
        System.out.println(">>> CLIENT: Отправляю команду: [" + command + "]");
        // =====================================================

        String response = network.sendCommandAndGetResponse(command);

        // ============== ДИАГНОСТИКА - ЧАСТЬ 2 ==============
        System.out.println(">>> CLIENT: Получен ответ: [" + response + "]");
        // =====================================================

        if (response != null && response.startsWith("SUCCESS_LOGIN")) {
            this.loginResponse = response; // Сохраняем успешный ответ
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();
        } else {
            errorLabel.setText("Неверный логин или пароль.");
            passwordField.clear();
        }
    }

    // Этот метод позволит главному классу получить ответ после закрытия окна
    public String getLoginResponse() {
        return loginResponse;
    }

    // Этот метод позволит получить доступ к сетевому соединению
    public ClientNetwork getNetwork() {
        return network;
    }
}