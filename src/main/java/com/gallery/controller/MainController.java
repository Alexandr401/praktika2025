package com.gallery.controller;

import com.gallery.entity.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController implements MainControllerBase {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML
    private BorderPane rootPane;
    @FXML
    private Label userLabel;
    @FXML
    private Button btnPaintings;
    @FXML
    private Button btnArtists;
    @FXML
    private Button btnExhibitions;
    @FXML
    private Button btnUsers;
    @FXML
    private Button btnStats;
    @FXML
    private Button btnAbout;

    private Button activeButton;

    // текущий пользователь
    private User currentUser;

    private void setCurrentUser(User user) {  // для внутреннего использования
        this.currentUser = user;
        if (userLabel != null) {
            userLabel.setText("Привет, " + user.getUsername());
        }
    }

    @Override
    public void setUser(User user) {
        setCurrentUser(user); // внутренний метод
    }

    // инициализация контроллера
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            if (currentUser != null && userLabel != null) {
                userLabel.setText("Привет, " + currentUser.getUsername());
            }
        });
    }

    // экран с картинами
    @FXML
    private void openPaintings() {
        loadCenter("/views/paintings.fxml");
        setActiveButton(btnPaintings);
    }

    // экран с авторами
    @FXML
    private void openArtists() {
        loadCenter("/views/artists.fxml");
        setActiveButton(btnArtists);
    }

    // экран выставок
    @FXML
    private void openExhibitions() {
        loadCenter("/views/exhibitions.fxml");
        setActiveButton(btnExhibitions);
    }

    // экран управления пользователями
    @FXML
    private void openUsers() {
        loadCenter("/views/users.fxml");
        setActiveButton(btnUsers);
    }

    // экран статистики
    @FXML
    private void openStats() {
        loadCenter("/views/stats.fxml");
        setActiveButton(btnStats);
    }

    // экран Об авторе
    @FXML
    private void openAbout() {
        loadCenter("/views/about.fxml");
        setActiveButton(btnAbout);
    }

    // метод для загрузки FXML в center BorderPane с передачей текущего пользователя
    private void loadCenter(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();

            // Передача currentUser для UsersController через интерфейс
            Object controller = loader.getController();
            if (controller instanceof UsersController usersController) {
                usersController.setCurrentUser(currentUser);
            }

            view.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());
            rootPane.setCenter(view);
        } catch (Exception e) {
            log.error("Ошибка загрузки центра окна: {}", path, e);
        }
    }

    // выход из приложения/логин
    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Ошибка при выходе и загрузке окна логина", e);
        }
    }

    // меняет активную кнопку
    private void setActiveButton(Button button) {
        if (activeButton != null) {
            // убрать подсветку с предыдущей кнопки
            activeButton.getStyleClass().remove("active");
        }
        activeButton = button; // новая активная кнопка
        if (activeButton != null) {
            if (!activeButton.getStyleClass().contains("active")) {
                activeButton.getStyleClass().add("active");
            }
        }
    }

}
