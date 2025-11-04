package org.bstu.helpdesk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bstu.helpdesk.controllers.LoginController;
import org.bstu.helpdesk.controllers.MainController;
import org.bstu.helpdesk.network.ClientNetwork;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) { // Убираем throws IOException, так как обработаем внутри
        try {
            // --- Шаг 1: Показываем окно логина ---
            FXMLLoader loginLoader = new FXMLLoader();
            // Устанавливаем путь к FXML. Используем полный путь от корня classpath.
            loginLoader.setLocation(App.class.getResource("/org/bstu/helpdesk/login-view.fxml"));

            Parent loginRoot = loginLoader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Вход в систему");
            loginStage.setScene(new Scene(loginRoot));
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.showAndWait();

            // --- Шаг 2: Получаем результат от LoginController ---
            LoginController loginController = loginLoader.getController();
            String loginResponse = loginController.getLoginResponse();

            // --- Шаг 3: Если логин успешен, показываем главное окно ---
            if (loginResponse != null && loginResponse.startsWith("SUCCESS_LOGIN")) {
                FXMLLoader mainLoader = new FXMLLoader();
                mainLoader.setLocation(App.class.getResource("/org/bstu/helpdesk/main-view.fxml"));

                Parent mainRoot = mainLoader.load();

                MainController mainController = mainLoader.getController();
                mainController.initData(loginController.getNetwork(), loginResponse);

                primaryStage.setTitle("Система учета заявок");
                primaryStage.setScene(new Scene(mainRoot, 800, 600));
                primaryStage.show();
            } else {
                System.out.println("Вход не выполнен. Завершение работы.");
            }

        } catch (IOException e) {
            // !!! ЭТОТ БЛОК ПОКАЖЕТ НАМ НАСТОЯЩУЮ ОШИБКУ !!!
            System.err.println("Критическая ошибка: не удалось загрузить FXML. Проверьте путь и синтаксис файла.");
            e.printStackTrace(); // Печатаем полный стектрейс исключения
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}