package org.bstu.helpdesk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bstu.helpdesk.controllers.LoginController;
import org.bstu.helpdesk.controllers.MainController;
import org.bstu.helpdesk.network.ClientNetwork;
import org.bstu.helpdesk.network.NetworkManager;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // --- Шаг 1: Показываем окно логина ---
            FXMLLoader loginLoader = new FXMLLoader();
            // Используем ЯВНОЕ указание класса App для поиска ресурса. Это надежнее.
            loginLoader.setLocation(App.class.getResource("/org/bstu/helpdesk/login-view.fxml"));

            // Проверяем, что ресурс был найден
            if (loginLoader.getLocation() == null) {
                throw new IOException("Не удалось найти FXML для окна входа: /org/bstu/helpdesk/login-view.fxml");
            }

            Parent loginRoot = loginLoader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Вход в систему");
            loginStage.setScene(new Scene(loginRoot));
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.showAndWait();

            // --- Шаг 2: Получаем результат от LoginController ---
            LoginController loginController = loginLoader.getController();
            String loginResponse = loginController.getLoginResponse();

            ClientNetwork network = loginController.getNetwork(); // Сохраняем ссылку на сеть

            // --- Шаг 3: Если логин успешен, показываем главное окно ---
            if (loginResponse != null && loginResponse.startsWith("SUCCESS_LOGIN")) {
                FXMLLoader mainLoader = new FXMLLoader();
                mainLoader.setLocation(App.class.getResource("/org/bstu/helpdesk/main-view.fxml"));

                if (mainLoader.getLocation() == null) {
                    throw new IOException("Не удалось найти FXML для главного окна: /org/bstu/helpdesk/main-view.fxml");
                }

                Parent mainRoot = mainLoader.load();

                MainController mainController = mainLoader.getController();
                // Передаем в MainController сетевое соединение и данные о пользователе
                mainController.initData(network, loginResponse);

                primaryStage.setTitle("Система учета заявок");
                primaryStage.setScene(new Scene(mainRoot, 800, 600));
                primaryStage.show();
            } else {
                System.out.println("Вход не выполнен. Завершение работы.");
            }

        } catch (IOException e) {
            System.err.println("Критическая ошибка FXML:");
            e.printStackTrace();
            // Можно добавить Alert для пользователя
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Критическая ошибка");
            alert.setHeaderText("Не удалось запустить приложение.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Этот метод автоматически вызывается JavaFX при закрытии приложения
    @Override
    public void stop() {
        System.out.println("Приложение закрывается. Отключаемся от сервера...");
        NetworkManager.getNetwork().disconnect();
    }
    public static void main(String[] args) {
        launch(args);
    }
}