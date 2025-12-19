package com.gallery.controller;

import com.gallery.entity.User;
import com.gallery.service.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UsersController {

    @FXML
    private ScrollPane usersScrollPane;
    @FXML
    private FlowPane usersTilePane;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private TextField searchField;

    private final UserService userService = new UserService();
    private User currentUser;

    private List<User> allUsers;

    private static final Logger logger = Logger.getLogger(UsersController.class.getName());

    @FXML
    private void initialize() {
        // получение всех пользователей
        allUsers = userService.getAllUsers();

        // вычисление уникальных ролей из существующих пользователей
        List<String> roles = allUsers.stream()
            .map(User::getRole)
            .distinct()
            .toList();

        // заполнение ComboBox ролями
        roleComboBox.setItems(FXCollections.observableArrayList(roles));

        // привязка фильтра к ComboBox
        roleComboBox.setOnAction(e -> onSearch());

        // привязка фильтра к TextField
        searchField.setOnKeyReleased(e -> onSearch());

        usersTilePane.getChildren().clear();

        roleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                setText(empty || role == null ? "Выберите роль" : role);
            }
        });

        // загрузка пользователей
        loadUsers();
    }

    // метод для передачи текущего пользователя
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUsers(); // загрузка пользователей после установки currentUser
    }

    private void loadUsers() {
        if (currentUser == null) return;
        allUsers = userService.getAllUsers();
        updateUsersView(allUsers);
    }

    private void showConfirmDialog(User user, String newRole) {
        if (newRole.equals(user.getRole())) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Изменение роли пользователя");
        alert.setContentText("Выдать пользователю \"" + user.getUsername() + "\" роль " + newRole + "?");

        ButtonType confirm = new ButtonType("Подтвердить");
        ButtonType cancel = new ButtonType("Отмена", ButtonType.CANCEL.getButtonData());
        alert.getButtonTypes().setAll(confirm, cancel);

        alert.showAndWait().ifPresent(result -> {
            if (result == confirm) {
                try {
                    userService.updateRole(user, newRole, currentUser);
                } catch (SecurityException ex) {
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
                }
            }
            loadUsers();
        });
    }

    private void updateUsersView(List<User> users) {
        if (users.isEmpty()) {
            showEmptyMessage(searchField.getText());
        } else {
            // возврат TilePane в ScrollPane, чтобы снова показывать пользователей
            usersScrollPane.setContent(usersTilePane);
            usersTilePane.getChildren().clear();

            for (User user : users) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_row.fxml"));
                    HBox userRow = loader.load();

                    Label usernameLabel = (Label) loader.getNamespace().get("usernameLabel");
                    usernameLabel.setText(user.getUsername());

                    @SuppressWarnings("unchecked")
                    ComboBox<String> roleComboBox = (ComboBox<String>) loader.getNamespace().get("roleComboBox");

                    // кнопка удаления
                    Button deleteButton = (Button) loader.getNamespace().get("deleteButton");
                    deleteButton.setText("\u2716");

                    boolean isSelf = currentUser.getUsername().equalsIgnoreCase(user.getUsername());
                    boolean isMainAdmin = "admin".equalsIgnoreCase(user.getUsername());
                    boolean isCurrentUserAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
                    boolean isCurrentUserSuperAdmin = "admin".equalsIgnoreCase(currentUser.getUsername());

                    if (isSelf || isMainAdmin) {
                        deleteButton.setVisible(false);
                    } else {
                        deleteButton.setVisible(true);
                        deleteButton.setDisable(!isCurrentUserAdmin && !isCurrentUserSuperAdmin);
                    }

                    // обработчик кнопки удаления
                    deleteButton.setOnAction(e -> {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Подтверждение удаления");
                    confirmAlert.setHeaderText("Удаление пользователя");
                    confirmAlert.setContentText("Вы действительно хотите удалить пользователя \"" + user.getUsername() + "\"?");

                    DialogPane dialogPane = confirmAlert.getDialogPane();
                    dialogPane.lookup(".content.label").setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-alignment: center;");

                    // кнопки подтверждения и отмены
                    ButtonType delete = new ButtonType("Удалить", ButtonBar.ButtonData.LEFT);
                    ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.RIGHT);
                    confirmAlert.getButtonTypes().setAll(delete, cancel);

                    confirmAlert.showAndWait().ifPresent(result -> {
                        if (result == delete) {
                            try {
                                userService.deleteUser(user, currentUser);
                                loadUsers();
                            } catch (SecurityException ex) {
                                new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
                            }
                        }
                    });
                });

                    usernameLabel.setText(user.getUsername());
                    roleComboBox.setItems(FXCollections.observableArrayList("admin", "user"));
                    roleComboBox.setValue(user.getRole());

                    // блокировка смены роли
                    roleComboBox.setDisable(isSelf || isMainAdmin || !isCurrentUserAdmin);
                    roleComboBox.setOnAction(e -> {
                        String newRole = roleComboBox.getValue();
                        if (!newRole.equals(user.getRole())) {
                            showConfirmDialog(user, newRole);
                        }
                    });

                    userRow.setPrefWidth(200);
                    usersTilePane.getChildren().add(userRow);

                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Ошибка загрузки user_row.fxml", e);
                }
            }
        }
    }

    // поиск и фильтр
    @FXML
    private void onSearch() {
        if (allUsers == null) return;

        String query = searchField.getText().toLowerCase().trim();
        String selectedRole = roleComboBox.getValue();

        List<User> filtered = allUsers.stream()
            .filter(u -> (query.isEmpty() || u.getUsername().toLowerCase().contains(query)) &&
                (selectedRole == null || u.getRole().equalsIgnoreCase(selectedRole)))
            .toList();

        updateUsersView(filtered);
    }

    // сброс фильтров
    @FXML
    private void filterAll() {
        searchField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        if (allUsers != null) {
            updateUsersView(allUsers);
        }
    }

    private void showEmptyMessage(String query) {
        // Label
        Label label = new Label(query.isEmpty() ? "Ничего не найдено" : "По запросу \"" + query + "\" ничего не найдено");
        label.setStyle("-fx-font-size: 18px; -fx-text-fill: #666; -fx-font-weight: bold;");

        // StackPane для центрирования
        StackPane wrapper = new StackPane(label);
        wrapper.prefWidthProperty().bind(usersScrollPane.viewportBoundsProperty().map(bounds -> bounds.getWidth()));
        wrapper.prefHeightProperty().bind(usersScrollPane.viewportBoundsProperty().map(bounds -> bounds.getHeight()));
        StackPane.setAlignment(label, javafx.geometry.Pos.CENTER);

        // скрытие полосы прокрутки
        usersScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        usersScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Label в ScrollPane
        usersScrollPane.setContent(wrapper);
    }
}
