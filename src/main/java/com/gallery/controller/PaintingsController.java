package com.gallery.controller;

import com.gallery.entity.Painting;
import com.gallery.service.PaintingService;
import com.gallery.util.TableCellFactoryUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaintingsController {
    private static final Logger log = LoggerFactory.getLogger(PaintingsController.class);

    @FXML
    private TableView<Painting> paintingsTable;
    @FXML
    private TableColumn<Painting, String> titleColumn;
    @FXML
    private TableColumn<Painting, String> imagesColumn;
    @FXML
    private TableColumn<Painting, String> genreColumn;
    @FXML
    private TableColumn<Painting, String> artistColumn;
    @FXML
    private TableColumn<Painting, Integer> yearColumn;
    @FXML
    private TableColumn<Painting, String> descriptionColumn;
    @FXML
    private TextField searchField;

    private final PaintingService paintingService = new PaintingService();

    private ObservableList<Painting> allPaintings;
    private FilteredList<Painting> filteredPaintings;

    @FXML
    private void initialize() {
        // на ширину экрана
        paintingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // колонки нельзя передвигать
        paintingsTable.getColumns().forEach(col -> col.setReorderable(false));

        // базовый список
        allPaintings = FXCollections.observableArrayList(paintingService.getAllPaintings());

        // фильтр
        filteredPaintings = new FilteredList<>(allPaintings, p -> true);

        // TableView работает с FilteredList
        paintingsTable.setItems(filteredPaintings);

        // Сортировка через SortedList
        SortedList<Painting> sortedPaintings = new SortedList<>(filteredPaintings);
        sortedPaintings.comparatorProperty().bind(paintingsTable.comparatorProperty());
        paintingsTable.setItems(sortedPaintings);

        paintingsTable.setPlaceholder(new javafx.scene.control.Label("Нет данных"));

        // настройка колонок
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // изображения
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
                        image = loadImage("/images/paintings/" + imageName);
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

        // автор SimpleStringProperty, чтобы отображалось имя
        artistColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getFullName() : "")
        );

        titleColumn.setCellFactory(TableCellFactoryUtil.wrappingCell());
        genreColumn.setCellFactory(TableCellFactoryUtil.wrappingCell());
        artistColumn.setCellFactory(TableCellFactoryUtil.wrappingCell());
        descriptionColumn.setCellFactory(TableCellFactoryUtil.wrappingCell());

        // загрузка картин
        loadPaintings();
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

    // поиск
    @FXML
    private void onSearch() {
        String q = searchField.getText().toLowerCase().trim();

        filteredPaintings.setPredicate(p -> {
            if (q.isEmpty()) return true;
            return p.getTitle().toLowerCase().contains(q)
                || p.getGenre().toLowerCase().contains(q)
                || (p.getArtist() != null &&
                p.getArtist().getFullName().toLowerCase().contains(q))
                || String.valueOf(p.getYear()).contains(q)
                || p.getDescription().toLowerCase().contains(q);
        });

        if (filteredPaintings.isEmpty()) {
            Label placeholderLabel = new Label("По запросу \"" + q + "\" ничего не найдено");
            placeholderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            paintingsTable.setPlaceholder(placeholderLabel);
        }
    }

    // очистка фильтра
    @FXML
    private void resetFilters() {
        searchField.clear();
        filteredPaintings.setPredicate(p -> true);
        paintingsTable.setPlaceholder(new Label("Нет данных"));
    }

    // обновление данных
    private void loadPaintings() {
        allPaintings.setAll(paintingService.getAllPaintings());
    }

    // CRUD
    @FXML
    private void deletePainting() {
        Painting selectedPainting = paintingsTable.getSelectionModel().getSelectedItem();

        if (selectedPainting != null) {
            // окно подтверждения
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы точно хотите удалить картину \"" + selectedPainting.getTitle() + "\"?");
            alert.setContentText("Это действие невозможно будет отменить.");

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.lookup(".content.label").setStyle("-fx-font-size: 14px; -fx-padding: 10 0 10 0; -fx-alignment: center;");

            // кнопки удалить и отмена
            ButtonType deleteButton = new ButtonType("Удалить", ButtonBar.ButtonData.LEFT);
            ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.RIGHT);
            alert.getButtonTypes().setAll(deleteButton, cancelButton);

            // ответ
            alert.showAndWait().ifPresent(response -> {
                if (response == deleteButton) {
                    paintingService.deletePainting(selectedPainting);
                    loadPaintings();
                }
            });
        } else {
            showAlert(AlertType.ERROR, "Ошибка", "Не выбрана картина", "Выберите картину для удаления");
        }
    }

    @FXML
    private void addPainting() {
        openPaintingDialog(null);
    }

    @FXML
    private void editPainting() {
        Painting selected = paintingsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openPaintingDialog(selected);
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не выбрана картина", "Выберите картину для редактирования");
        }
    }

    private void openPaintingDialog(Painting painting) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/painting_add_edit.fxml"));
            Parent page = loader.load();

            PaintingAddEditController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle(painting == null ? "Добавить картину" : "Изменить картину");

            Scene scene = new Scene(page);

            // CSS
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());

            stage.setScene(scene);

            controller.setDialogStage(stage);
            controller.setPainting(painting);

            stage.showAndWait();

            // обновление таблицы
            loadPaintings();

        } catch (Exception e) {
            log.error("Ошибка при открытии диалога добавления/редактирования картины", e);
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
