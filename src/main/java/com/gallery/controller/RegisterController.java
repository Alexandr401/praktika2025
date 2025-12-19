package com.gallery.controller;

import com.gallery.entity.User;
import com.gallery.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {
    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField passwordField2;
    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String pass1 = passwordField.getText().trim();
        String pass2 = passwordField2.getText().trim();

        if (username.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            showError("Заполните все поля");
            return;
        }

        if (!pass1.equals(pass2)) {
            showError("Пароли не совпадают");
            return;
        }

        if (userService.findByUsername(username) != null) {
            showError("Пользователь уже существует");
            return;
        }

        // создание нового пользователя
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(pass1);
        newUser.setRole("user");   // автоматически роль
        userService.save(newUser);

        // аутентификация сразу после регистрации
        User authenticatedUser = userService.authenticate(username, pass1);
        if (authenticatedUser != null) {
            openMainWindow(authenticatedUser);
        } else {
            showError("Ошибка авторизации после регистрации");
        }
    }

    private void showError(String text) {
        errorLabel.setText(text);
        errorLabel.setVisible(true);
    }

    private void openMainWindow(User user) {
        try {
            String fxmlPath = "/views/main_user.fxml"; // для обычного пользователя
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // передача пользователя в контроллер
            Object controller = loader.getController();
            if (controller instanceof MainControllerBase mainController) {
                mainController.setUser(user);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());

            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Ошибка при открытии главного окна после регистрации", e);
        }
    }

    @FXML
    private void returnToLogin() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/login.fxml")));
            Stage stage = (Stage) usernameField.getScene().getWindow();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Ошибка при возврате на Login", e);
        }
    }

}
