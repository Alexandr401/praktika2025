package com.gallery.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.InputStream;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // FXML
        URL fxmlUrl = getClass().getResource("/views/login.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML файл /views/login.fxml не найден");
        }

        Parent root = FXMLLoader.load(fxmlUrl);
        primaryStage.setTitle("GalleryApp");

        // Создание сцены
        Scene scene = new Scene(root);

        // Подключение CSS
        URL cssUrl = getClass().getResource("/application.css");
        if (cssUrl == null) {
            throw new IllegalStateException("CSS файл /application.css не найден");
        }
        scene.getStylesheets().add(cssUrl.toExternalForm());

        // Размер окна
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());

        // минимальный размер окна
        primaryStage.setMinWidth(850);
        primaryStage.setMinHeight(690);

        // Установка сцены
        primaryStage.setScene(scene);

        // иконка
        InputStream iconStream = getClass().getResourceAsStream("/icon.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        } else {
            System.out.println("Иконка не найдена: /icon.png");
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        System.setProperty("prism.verbose", "true"); //  отладка JavaFX
        launch(args);
    }
}



























