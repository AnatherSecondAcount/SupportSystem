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
import org.bstu.helpdesk.network.NetworkManager;

import java.io.IOException;

public class App extends Application {

    private Stage primaryStage; // Храним ссылку на главное окно
    private ClientNetwork network;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.network = NetworkManager.getNetwork();

        // Сразу показываем экран входа
        showLoginScreen();
    }

    public void showLoginScreen() {
        try {
            // Закрываем главное окно, если оно было открыто
            primaryStage.close();

            FXMLLoader loginLoader = new FXMLLoader(App.class.getResource("login-view.fxml"));
            Parent loginRoot = loginLoader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Вход в систему");
            loginStage.setScene(new Scene(loginRoot));
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.showAndWait();

            LoginController loginController = loginLoader.getController();
            String loginResponse = loginController.getLoginResponse();

            if (loginResponse != null && loginResponse.startsWith("SUCCESS_LOGIN")) {
                // Если логин успешен, показываем главное окно
                showMainScreen(loginResponse);
            } else {
                System.out.println("Вход не выполнен. Завершение работы.");
                // Можно просто ничего не делать, приложение закроется само
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMainScreen(String loginResponse) throws IOException {
        FXMLLoader mainLoader = new FXMLLoader(App.class.getResource("main-view.fxml"));
        Parent mainRoot = mainLoader.load();

        MainController mainController = mainLoader.getController();
        // Передаем ссылку на сам App.java
        mainController.initData(this, network, loginResponse);

        primaryStage.setTitle("Система учета заявок");
        primaryStage.setScene(new Scene(mainRoot, 800, 600));
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.out.println("Приложение закрывается. Отключаемся от сервера...");
        if (network != null && network.isConnected()) {
            network.disconnect();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}