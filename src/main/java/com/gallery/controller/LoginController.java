package com.gallery.controller;

import com.gallery.entity.User;
import com.gallery.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private Label errorLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        // создания admin и user с хэшированными паролями
        createDefaultUsers();
    }

    private void createDefaultUsers() {
        // admin
        User admin = userService.findByUsername("admin");
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setRole("admin");
            admin.setPassword("admin"); // пароль
            userService.save(admin);    // <-- save() хэширует

            System.out.println("Создан админ с хэшированным паролем");
        }

        // user
        User user = userService.findByUsername("user");
        if (user == null) {
            user = new User();
            user.setUsername("user");
            user.setRole("user");
            user.setPassword("user");
            userService.save(user);

            System.out.println("Создан пользователь с хэшированным паролем");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // проверка на пустые поля
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Заполните все поля");
            errorLabel.setVisible(true);
            return;
        }

        // проверка пользователя через сервис
        User user = userService.authenticate(username, password);
        if (user == null) {
            errorLabel.setText("Неверный логин или пароль");
            errorLabel.setVisible(true);
            return;
        }

        // успешно убрать ошибку
        errorLabel.setVisible(false);
        // открыть основное окно
        openMainWindow(user);
    }

    private void openMainWindow(User user) {
        try {
            String fxmlPath;
            if ("admin".equalsIgnoreCase(user.getRole())) {
                fxmlPath = "/views/main.fxml";
            } else if ("user".equalsIgnoreCase(user.getRole())) {
                fxmlPath = "/views/main_user.fxml";
            } else {
                System.out.println("Неизвестная роль: " + user.getRole());
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // получение контроллер нового окна
            Object controller = loader.getController();
            if (controller instanceof MainControllerBase mainController) {
                mainController.setUser(user); // передача текущего пользователя
            }

            // получение Stage и установка сцены
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            log.error("Ошибка при открытии основного окна", e);
        }
    }

    @FXML
    private void openRegister() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/register.fxml")));
            Stage stage = (Stage) usernameField.getScene().getWindow();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Ошибка при открытии окна регистрации", e);
        }
    }
}
