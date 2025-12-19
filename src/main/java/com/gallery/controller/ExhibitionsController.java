package com.gallery.controller;

import com.gallery.entity.Exhibition;
import com.gallery.service.ExhibitionService;
import com.gallery.util.TableCellFactoryUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExhibitionsController {
    private static final Logger log = LoggerFactory.getLogger(ExhibitionsController.class);

    @FXML
    private TableView<Exhibition> exhibitionsTable;
    @FXML
    private TableColumn<Exhibition, String> colName;
    @FXML
    private TableColumn<Exhibition, String> colLocation;
    @FXML
    private TableColumn<Exhibition, String> colPaintings;
    @FXML
    private TableColumn<Exhibition, String> colStartDate;
    @FXML
    private TableColumn<Exhibition, String> colEndDate;
    @FXML
    private TableColumn<Exhibition, String> colDescription;
    @FXML
    private TextField searchField;

    private final ExhibitionService exhibitionService = new ExhibitionService();

    private ObservableList<Exhibition> allExhibitions;
    private FilteredList<Exhibition> filteredExhibitions;

    @FXML
    private void initialize() {
        // на ширину экрана
        exhibitionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // запрет на перемещение колонок
        setColumnsNoDrag(exhibitionsTable);

        // базовый список
        allExhibitions = FXCollections.observableArrayList(exhibitionService.getAllExhibitions());
        filteredExhibitions = new FilteredList<>(allExhibitions, e -> true);

        // TableView работает с FilteredList
        exhibitionsTable.setItems(filteredExhibitions);

        // сортировка по колонкам
        SortedList<Exhibition> sortedExhibitions = new SortedList<>(filteredExhibitions);
        sortedExhibitions.comparatorProperty().bind(exhibitionsTable.comparatorProperty());
        exhibitionsTable.setItems(sortedExhibitions);

        exhibitionsTable.setPlaceholder(new javafx.scene.control.Label("Нет данных"));

        // Настройка колонок
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colPaintings.setCellValueFactory(c -> {
            Exhibition ex = c.getValue();
            if (ex.getPaintingExhibitions() == null) {
                return new SimpleStringProperty("");
            }
            String titles = ex.getPaintingExhibitions().stream()
                .map(pe -> pe.getPainting().getTitle())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

            return new SimpleStringProperty(titles);
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        colStartDate.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getStartDate() != null ? c.getValue().getStartDate().format(formatter) : ""
            )
        );
        colEndDate.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getEndDate() != null ? c.getValue().getEndDate().format(formatter) : ""
            )
        );
        colDescription.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getDescription() != null ? c.getValue().getDescription() : "")
        );

        colName.setCellFactory(TableCellFactoryUtil.wrappingCell());
        colLocation.setCellFactory(TableCellFactoryUtil.wrappingCell());
        colPaintings.setCellFactory(TableCellFactoryUtil.wrappingCell());
        colDescription.setCellFactory(TableCellFactoryUtil.wrappingCell());

        // загрузка данных
        loadExhibitions();
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

        filteredExhibitions.setPredicate(exhibition -> {
            if (q.isEmpty()) return true;

            boolean matchesExhibition =
                (exhibition.getName() != null && exhibition.getName().toLowerCase().contains(q)) ||
                    (exhibition.getLocation() != null && exhibition.getLocation().toLowerCase().contains(q)) ||
                    (exhibition.getDescription() != null && exhibition.getDescription().toLowerCase().contains(q)) ||
                    (exhibition.getStartDate() != null && exhibition.getStartDate().toString().contains(q)) ||
                    (exhibition.getEndDate() != null && exhibition.getEndDate().toString().contains(q));

            boolean matchesPaintings =
                exhibition.getPaintingExhibitions() != null && exhibition.getPaintingExhibitions().stream()
                    .anyMatch(pe -> pe.getPainting() != null && pe.getPainting().getTitle() != null && pe.getPainting().getTitle().toLowerCase().contains(q));

            return matchesExhibition || matchesPaintings;
        });

        if (filteredExhibitions.isEmpty()) {
            Label placeholderLabel = new Label("По запросу \"" + q + "\" ничего не найдено");
            placeholderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            exhibitionsTable.setPlaceholder(placeholderLabel);
        }
    }

    // очистка фильтра
    @FXML
    private void resetFilters() {
        searchField.clear();
        filteredExhibitions.setPredicate(e -> true);
        exhibitionsTable.setPlaceholder(new Label("Нет данных"));
    }

    // обновление данных
    private void loadExhibitions() {
        allExhibitions.setAll(exhibitionService.getAllExhibitions());
    }

    // CRUD
    @FXML
    private void addExhibition() {
        openExhibitionDialog(null);
    }

    @FXML
    private void editExhibition() {
        Exhibition selected = exhibitionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openExhibitionDialog(selected);
        } else {
            showAlert(AlertType.ERROR, "Ошибка", "Выставка не выбрана", "Выберите выставку для редактирования");
        }
    }

    private void openExhibitionDialog(Exhibition exhibition) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Exhibition_Add_Edit.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(exhibition == null ? "Добавить выставку" : "Редактировать выставку");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(exhibitionsTable.getScene().getWindow());

            Scene scene = new Scene(page);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());
            dialogStage.setScene(scene);

            ExhibitionAddEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setExhibition(exhibition);

            dialogStage.showAndWait();

            loadExhibitions(); // обновить таблицу после закрытия
        } catch (Exception e) {
            log.error("Ошибка при открытии диалога добавления/редактирования выставки", e);
        }
    }

    @FXML
    private void deleteExhibition() {
        Exhibition selectedExhibition = exhibitionsTable.getSelectionModel().getSelectedItem();

        if (selectedExhibition != null) {
            // окно подтверждения
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Вы точно хотите удалить картину \"" + selectedExhibition.getName() + "\"?");
            alert.setContentText("Это действие невозможно будет отменить.");

            // кнопки удалить и отмена
            ButtonType deleteButton = new ButtonType("Удалить", ButtonBar.ButtonData.LEFT);
            ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.RIGHT);
            alert.getButtonTypes().setAll(deleteButton, cancelButton);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.lookup(".content.label").setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-alignment: center;");

            // ответ
            alert.showAndWait().ifPresent(response -> {
                if (response == deleteButton) {
                    exhibitionService.deleteExhibition(selectedExhibition);
                    loadExhibitions();
                }
            });
        } else {
            showAlert(AlertType.ERROR, "Ошибка", "Выставка не выбрана", "Выберите выставку для удаления");
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
