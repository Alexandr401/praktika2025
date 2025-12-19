package com.gallery.controller;

import com.gallery.entity.Exhibition;
import com.gallery.entity.Painting;
import com.gallery.entity.PaintingExhibition;
import com.gallery.service.ExhibitionService;
import com.gallery.service.PaintingService;
import com.gallery.service.PaintingExhibitionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExhibitionAddEditController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea descriptionField;

    @FXML
    private TableView<Painting> availableTable;
    @FXML
    private TableColumn<Painting, String> availTitleCol;
    @FXML
    private TableColumn<Painting, String> availArtistCol;
    @FXML
    private TableView<Painting> selectedTable;
    @FXML
    private TableColumn<Painting, String> selTitleCol;
    @FXML
    private TableColumn<Painting, String> selArtistCol;

    private final PaintingService paintingService = new PaintingService();
    private final PaintingExhibitionService paintingExhibitionService = new PaintingExhibitionService();
    private final ExhibitionService exhibitionService = new ExhibitionService();

    private Stage dialogStage;
    private Exhibition exhibition;
    private Integer currentExhibitionId = null;

    private final ObservableList<Painting> availableList = FXCollections.observableArrayList();
    private final ObservableList<Painting> selectedList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        availTitleCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getTitle()));
        availArtistCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
            p.getValue().getArtist() != null ? p.getValue().getArtist().getFullName() : ""
        ));

        selTitleCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getTitle()));
        selArtistCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
            p.getValue().getArtist() != null ? p.getValue().getArtist().getFullName() : ""
        ));

        availableTable.setItems(availableList);
        selectedTable.setItems(selectedList);

        // маска даты
        setupDatePickerMask(startDatePicker);
        setupDatePickerMask(endDatePicker);

        availableTable.setPlaceholder(new Label("Нет доступных картин"));
        selectedTable.setPlaceholder(new Label("Нет выбранных картин"));

        startDatePicker.valueProperty().addListener((obs, oldV, newV) -> loadAvailablePaintings());
        endDatePicker.valueProperty().addListener((obs, oldV, newV) -> loadAvailablePaintings());
    }

    private void setupDatePickerMask(DatePicker datePicker) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        datePicker.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(formatter) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.trim().isEmpty()) return null;
                try {
                    return LocalDate.parse(string, formatter);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        TextField editor = datePicker.getEditor();

        editor.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();

            // убрать все кроме цифр
            String digits = text.replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                // убрать текст полностью
                change.setText("");
                change.setRange(0, change.getControlText().length());
                return change;
            }

            if (digits.length() > 8) digits = digits.substring(0, 8);

            // добавить точки после дня и месяца
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                formatted.append(digits.charAt(i));
                if ((i == 1 || i == 3) && i != digits.length() - 1) formatted.append(".");
            }

            // новый текст целиком
            change.setText(formatted.toString());
            change.setRange(0, change.getControlText().length());
            change.selectRange(formatted.length(), formatted.length());

            return change;
        }));

        // поставить дату при потере фокуса
        editor.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                try {
                    LocalDate date = LocalDate.parse(editor.getText(), formatter);
                    datePicker.setValue(date);
                } catch (Exception ignored) { }
            }
        });
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setExhibition(Exhibition exhibition) {
        this.exhibition = exhibition != null ? exhibition : new Exhibition();

        if (exhibition != null) {
            currentExhibitionId = exhibition.getId();

            nameField.setText(exhibition.getName());
            locationField.setText(exhibition.getLocation());
            startDatePicker.setValue(exhibition.getStartDate());
            endDatePicker.setValue(exhibition.getEndDate());
            descriptionField.setText(exhibition.getDescription());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            if (startDatePicker.getValue() != null) {
                startDatePicker.getEditor().setText(startDatePicker.getValue().format(formatter));
            }

            if (endDatePicker.getValue() != null){
                endDatePicker.getEditor().setText(endDatePicker.getValue().format(formatter));
            }

            if (exhibition.getPaintingExhibitions() != null) {
                List<Painting> selected = exhibition.getPaintingExhibitions().stream()
                    .map(PaintingExhibition::getPainting)
                    .filter(Objects::nonNull)
                    .toList();
                selectedList.addAll(selected);
            }
        }

        loadAvailablePaintings();
    }

    // получение LocalDate из DatePicker с маской или выбора даты
    private LocalDate getDate(DatePicker picker) {
        // фактическое значение DatePicker
        LocalDate value = picker.getValue();
        if (value != null) return value;

        try {
            String text = picker.getEditor().getText();
            if (text == null || text.isEmpty()) return null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return LocalDate.parse(text, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    private void loadAvailablePaintings() {
        availableList.clear();

        LocalDate start = getDate(startDatePicker);
        LocalDate end = getDate(endDatePicker);

        if (start == null || end == null) {
            availableTable.setPlaceholder(new Label("Сначала выберите даты"));
            return;
        }

        List<Painting> allPaintings = paintingService.getAllPaintings();
        Set<Long> busy = paintingExhibitionService.findBusyPaintingIdsBetween(start, end, currentExhibitionId);

        for (Painting p : allPaintings) {
            boolean alreadySelected = selectedList.stream().anyMatch(x -> Objects.equals(x.getId(), p.getId()));
            if (!alreadySelected) {
                availableList.add(p);
            }
        }

        availableTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Painting item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setDisable(false);
                    setStyle("");
                } else {
                    boolean isBusy = busy.contains(item.getId());
                    setDisable(isBusy);
                    setStyle(isBusy ? "-fx-opacity: 0.4;" : "");
                }
            }
        });
    }

    @FXML
    private void handleAddSelected() {
        Painting sel = availableTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        LocalDate start = getDate(startDatePicker);
        LocalDate end = getDate(endDatePicker);
        boolean busy = paintingExhibitionService.isPaintingBusy(
            sel.getId(), start, end, currentExhibitionId
        );
        if (busy) {
            showError("Картина занята в эти даты.");
            return;
        }
        availableList.remove(sel);
        selectedList.add(sel);
    }

    @FXML
    private void handleRemoveSelected() {
        Painting sel = selectedTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        selectedList.remove(sel);
        availableList.add(sel);
    }

    @FXML
    private void handleSave() {

        String name = nameField.getText().trim();
        if (name.isEmpty() || !name.matches("^[a-zA-Zа-яА-Я0-9 .,\\-]+$")) {
            showError("Название выставки заполнено некорректно.");
            return;
        }

        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            showError("Укажите место проведения выставки.");
            return;
        }

        LocalDate start = getDate(startDatePicker);
        LocalDate end = getDate(endDatePicker);

        if (start == null || end == null || end.isBefore(start)) {
            showError("Укажите корректный диапазон дат.");
            return;
        }

        if (selectedList.isEmpty()) {
            showError("Добавьте картину в выставку.");
            return;
        }

        exhibition.setName(name);
        exhibition.setLocation(locationField.getText().trim());
        exhibition.setStartDate(start);
        exhibition.setEndDate(end);
        exhibition.setDescription(descriptionField.getText());

        try {
            Exhibition saved = exhibitionService.save(exhibition);

            paintingExhibitionService.deleteByExhibitionId(saved.getId());

            for (Painting p : selectedList) {
                PaintingExhibition pe = new PaintingExhibition();
                pe.setPainting(p);
                pe.setExhibition(saved);
                pe.setStartDate(start);
                pe.setEndDate(end);

                paintingExhibitionService.addPaintingExhibition(pe);
            }
        } catch (Exception e) {
            showError("Ошибка сохранения выставки: " + e.getMessage());
            return;
        }

        dialogStage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);

        Label contentLabel = new Label(message);
        contentLabel.setStyle("-fx-font-size: 14px;");
        contentLabel.setMaxWidth(400);
        contentLabel.setAlignment(javafx.geometry.Pos.CENTER);

        alert.getDialogPane().setContent(contentLabel);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}
