// Находится в файле: client/src/main/java/org/bstu/helpdesk/App.java
package org.bstu.helpdesk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

// Имя класса теперь App
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Путь к FXML теперь соответствует вашей структуре пакетов
        Parent root = FXMLLoader.load(getClass().getResource("/org/bstu/helpdesk/main-view.fxml"));

        primaryStage.setTitle("Система учета заявок");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}