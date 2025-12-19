package com.gallery.controller;

import com.gallery.entity.Artist;
import com.gallery.service.ArtistService;

// JavaFX
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Java IO / NIO
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtistAddEditController {
    private static final Logger log = LoggerFactory.getLogger(ArtistAddEditController.class);

    @FXML
    private TextField fullNameField;
    @FXML
    private ImageView previewImage;
    @FXML
    private Label imageNameLabel;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private DatePicker deathDatePicker;
    @FXML
    private TextArea biographyField;

    private Stage dialogStage;
    private Artist artist;
    private boolean isEdit = false;

    private final ArtistService artistService = new ArtistService();

    private String selectedImageName = null;

    @FXML
    private void initialize() {
        // Настройка маски для DatePicker
        setupDatePickerMask(birthDatePicker);
        setupDatePickerMask(deathDatePicker);
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

    private void showPreviewImage(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            previewImage.setImage(null);
            imageNameLabel.setText("");
            return;
        }

        Path imagePath = Paths.get("src/main/resources/images/artists/", imageName);

        if (!Files.exists(imagePath)) {
            System.err.println("Файл изображения не найден: " + imageName);
            previewImage.setImage(null);
            imageNameLabel.setText("");
            return;
        }

        try {
            Image img = new Image(Files.newInputStream(imagePath));
            previewImage.setImage(img);
            imageNameLabel.setText(imageName);
        } catch (IOException e) {
            log.error("Ошибка при загрузке изображения: {}", imageName, e);

            previewImage.setImage(null);
            imageNameLabel.setText("");
        }
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

            // только цифры
            String digits = text.replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                // убирать текст полностью
                change.setText("");
                change.setRange(0, change.getControlText().length());
                return change;
            }

            if (digits.length() > 8) digits = digits.substring(0, 8);

            // точки после дня и месяца
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                formatted.append(digits.charAt(i));
                if ((i == 1 || i == 3) && i != digits.length() - 1) formatted.append(".");
            }

            // текст целиком
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

    public void setArtist(Artist artist) {
        if (artist != null) {
            this.artist = artist;
            isEdit = true;

            fullNameField.setText(artist.getFullName());
            birthDatePicker.setValue(artist.getBirthDate());
            deathDatePicker.setValue(artist.getDeathDate());
            biographyField.setText(artist.getBiography());

            selectedImageName = artist.getImage();
            showPreviewImage(selectedImageName);
        } else {
            this.artist = new Artist();
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(dialogStage);
        if (file == null) return;

        try {
            Path targetDir = Paths.get("src/main/resources/images/artists/");
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(file.getName());
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            selectedImageName = file.getName();
            showPreviewImage(selectedImageName);

        } catch (IOException e) {
            showError("Ошибка копирования файла изображения.");
        }
    }

    @FXML
    private void handleSave() {
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty() || !fullName.matches("^[a-zA-Zа-яА-Я0-9 .,\\-]+$")) {
            showError("ФИО заполнено некорректно.");
            return;
        }

        // даты напрямую из DatePicker
        LocalDate birthDate = birthDatePicker.getValue();
        LocalDate deathDate = deathDatePicker.getValue();

        if (birthDate == null) {
            showError("Укажите дату рождения.");
            return;
        }

        if (birthDate.isAfter(LocalDate.now())) {
            showError("Дата рождения не может быть в будущем.");
            return;
        }

        if (deathDate != null && deathDate.isBefore(birthDate)) {
            showError("Дата смерти не может быть раньше даты рождения.");
            return;
        }

        if (deathDate != null && deathDate.isAfter(LocalDate.now())) {
            showError("Дата смерти не может быть в будущем.");
            return;
        }

        artist.setFullName(fullName);
        artist.setImage(selectedImageName);
        artist.setBirthDate(birthDate);
        artist.setDeathDate(deathDate);
        artist.setBiography(biographyField.getText());

        try {
            if (isEdit) artistService.updateArtist(artist);
            else artistService.addArtist(artist);
        } catch (Exception ex) {
            showError("Ошибка при сохранении автора: " + ex.getMessage());
            return;
        }

        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}
