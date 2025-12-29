package com.gallery.controller;

import com.gallery.entity.Artist;
import com.gallery.service.ArtistService;
import com.gallery.util.TableCellFactoryUtil;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtistsController {
    private static final Logger log = LoggerFactory.getLogger(ArtistsController.class);

    @FXML
    private TableView<Artist> artistsTable;
    @FXML
    private TableColumn<Artist, String> nameColumn;
    @FXML
    private TableColumn<Artist, String> imagesColumn;
    @FXML
    private TableColumn<Artist, LocalDate> birthDateColumn;
    @FXML
    private TableColumn<Artist, LocalDate> deathDateColumn;
    @FXML
    private TableColumn<Artist, String> biographyColumn;

    @FXML
    private TextField searchField;

    private final ArtistService artistService = new ArtistService();

    private ObservableList<Artist> allArtists;
    private FilteredList<Artist> filteredArtists;

    @FXML
    private void initialize() {
        // на ширину экрана
        artistsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // запрет на перемещение колонок
        setColumnsNoDrag(artistsTable);

        // базовый список
        allArtists = FXCollections.observableArrayList(artistService.getAllArtists());
        filteredArtists = new FilteredList<>(allArtists, a -> true);

        // TableView работает с FilteredList
        artistsTable.setItems(filteredArtists);

        // сортировка по колонкам
        SortedList<Artist> sortedArtists = new SortedList<>(filteredArtists);
        sortedArtists.comparatorProperty().bind(artistsTable.comparatorProperty());

        artistsTable.setItems(sortedArtists);
        artistsTable.setPlaceholder(new Label("Нет данных"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        // настройка колонок
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        // фото
        imagesColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        imagesColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String imageName, boolean empty) {
                super.updateItem(imageName, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Image image;

                try {
                    if (imageName == null || imageName.isEmpty()) {
                        image = loadImage("/images/no_image.png");
                    } else {
                        image = loadImage("/images/artists/" + imageName);
                    }

                    imageView.setImage(image);
                    setGraphic(imageView);

                } catch (Exception e) {
                    log.error("Ошибка при загрузке изображения: {}", imageName, e);
                    imageView.setImage(loadImage("/images/no_image.png"));
                    setGraphic(imageView);
                }
            }
        });

        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        deathDateColumn.setCellValueFactory(new PropertyValueFactory<>("deathDate"));
        biographyColumn.setCellValueFactory(new PropertyValueFactory<>("biography"));

        // настройка отображения даты рождения
        birthDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? "" : item.format(formatter));
            }
        });

        // настройка отображения даты смерти
        deathDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : (item == null ? "Жив" : item.format(formatter)));
            }
        });

        nameColumn.setCellFactory(TableCellFactoryUtil.wrappingCell());
        biographyColumn.setCellFactory(TableCellFactoryUtil.wrappingCell());

        // загрузка авторов
        loadArtists();
    }

    private Image loadImage(String path) {
        var stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            log.warn("Ресурс не найден: {}", path);
            stream = getClass().getResourceAsStream("/images/no_image.png");
        }
        assert stream != null;
        return new Image(stream);
    }

    // рекурсивный метод для блокировки перетаскивания колонок
    private void setColumnsNoDrag(TableView<?> table) {
        table.getColumns().forEach(this::disableReorderRecursively);
    }

    private void disableReorderRecursively(TableColumn<?, ?> column) {
        column.setReorderable(false);
        if (!column.getColumns().isEmpty()) {
            column.getColumns().forEach(this::disableReorderRecursively);
        }
    }

    // поиск
    @FXML
    private void onSearch() {
        String q = searchField.getText().toLowerCase().trim();

        filteredArtists.setPredicate(artist -> {
            if (q.isEmpty()) return true;
            return (artist.getFullName() != null && artist.getFullName().toLowerCase().contains(q))
                || (artist.getBiography() != null && artist.getBiography().toLowerCase().contains(q))
                || (artist.getBirthDate() != null && artist.getBirthDate().toString().contains(q))
                || (artist.getDeathDate() != null && artist.getDeathDate().toString().contains(q));
        });

        if (filteredArtists.isEmpty()) {
            Label placeholderLabel = new Label("По запросу \"" + q + "\" ничего не найдено");
            placeholderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            artistsTable.setPlaceholder(placeholderLabel);
        }
    }

    // очистка фильтра
    @FXML
    private void resetFilters() {
        // сброс поиска
        searchField.clear();

        // все элементы
        filteredArtists.setPredicate(a -> true);

        artistsTable.setPlaceholder(new Label("Нет данных"));
    }

    //  обновление данных
    private void loadArtists() {
        allArtists.setAll(artistService.getAllArtists());
    }

    // CRUD
    @FXML
    private void deleteArtist() {
        Artist selectedArtist = artistsTable.getSelectionModel().getSelectedItem();

        if (selectedArtist != null) {
            // окно подтверждения
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы точно хотите удалить картину \"" + selectedArtist.getFullName() + "\"?");
            alert.setContentText("Это действие невозможно будет отменить.");

            // кнопки Удалить и Отмена
            ButtonType deleteButton = new ButtonType("Удалить", ButtonBar.ButtonData.LEFT);
            ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.RIGHT);
            alert.getButtonTypes().setAll(deleteButton, cancelButton);

            ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
            if (buttonBar != null) {
                buttonBar.setStyle("-fx-alignment: center;");
            }

            // ответ
            alert.showAndWait().ifPresent(response -> {
                if (response == deleteButton) {
                    artistService.deleteArtist(selectedArtist);
                    loadArtists();
                }
            });
        } else {
            showAlert(AlertType.ERROR, "Ошибка", "Не выбран автор", "Выберите автора для удаления");
        }
    }

    @FXML
    private void addArtist() {
        openArtistDialog(null);
    }

    @FXML
    private void editArtist() {
        Artist selected = artistsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openArtistDialog(selected);
        } else {
            showAlert(AlertType.ERROR, "Ошибка", "Не выбран автор", "Выберите автора для редактирования");
        }
    }

    private void openArtistDialog(Artist artist) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/artist_add_edit.fxml"));
            Parent page = loader.load();

            ArtistAddEditController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(artist == null ? "Добавить автора" : "Редактировать автора");

            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(artistsTable.getScene().getWindow());

            Scene scene = new Scene(page);

            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());
            dialogStage.setScene(scene);

            controller.setDialogStage(dialogStage);
            controller.setArtist(artist);

            dialogStage.showAndWait();
            loadArtists(); // обновление таблицы после закрытия диалога
        } catch (Exception e) {
            log.error("Ошибка при открытии диалога добавления/редактирования автора", e);
        }
    }

    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
