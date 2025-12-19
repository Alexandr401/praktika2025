package com.gallery.controller;

import com.gallery.entity.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainUserController implements MainControllerBase {
    private static final Logger log = LoggerFactory.getLogger(MainUserController.class);

    @FXML
    private Button btnUserPaintings;
    @FXML
    private Button btnUserArtist;
    @FXML
    private Button btnUserExhibitions;
    @FXML
    private Button btnUserAbout;

    private Button activeButton;

    @FXML
    private BorderPane rootPane;
    @FXML
    private Label userLabel;

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

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            if (userLabel != null) {
                if (currentUser != null) {
                    userLabel.setText("Привет, " + currentUser.getUsername());
                } else {
                    userLabel.setText("Добро пожаловать!");
                }
            }
        });
    }

    @FXML
    private void openUserPaintings() {
        loadCenter("/views/user_paintings.fxml");
        setActiveButton(btnUserPaintings);
    }

    @FXML
    private void openUserArtists() {
        loadCenter("/views/user_artists.fxml");
        setActiveButton(btnUserArtist);
    }

    @FXML
    private void openUserExhibitions() {
        loadCenter("/views/user_exhibitions.fxml");
        setActiveButton(btnUserExhibitions);
    }

    @FXML
    private void openUserAbout() {
        loadCenter("/views/about.fxml");
        setActiveButton(btnUserAbout);
    }

    // метод для загрузки новых экранов
    private void loadCenter(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();

            // передача текущего пользователя контроллеру
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("setUser", User.class).invoke(controller, currentUser);
                } catch (NoSuchMethodException ignored) {}
            }

            view.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());
            rootPane.setCenter(view);
        } catch (Exception e) {
            log.error("Ошибка загрузки экрана: {}", path, e);
        }
    }

    @FXML
    private void logout() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/login.fxml")));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Ошибка при выходе и загрузке окна логина", e);
        }
    }

    // менять активную кнопку
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
