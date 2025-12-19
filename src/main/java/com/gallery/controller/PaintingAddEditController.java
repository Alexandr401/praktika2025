package com.gallery.controller;

import com.gallery.entity.Artist;
import com.gallery.entity.Painting;
import com.gallery.service.ArtistService;
import com.gallery.service.PaintingService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Year;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaintingAddEditController {
    private static final Logger log = LoggerFactory.getLogger(PaintingAddEditController.class);

    @FXML
    private TextField titleField;
    @FXML
    private TextField genreField;
    @FXML
    private ComboBox<Artist> artistComboBox;
    @FXML
    private TextField yearField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ImageView previewImage;
    @FXML
    private Label imageNameLabel;

    private Stage dialogStage;
    private Painting painting;

    private final PaintingService paintingService = new PaintingService();
    private final ArtistService artistService = new ArtistService();

    private String selectedImageName = null;

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
    private void initialize() {
        setupArtistComboBox();

        TextFormatter<String> yearFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,4}")) { // 0-4 цифры
                return change;
            }
            return null;
        });
        yearField.setTextFormatter(yearFormatter);
    }

    private void setupArtistComboBox() {
        List<Artist> artists = artistService.getAllArtists();
        ObservableList<Artist> artistList = FXCollections.observableArrayList(artists);
        FilteredList<Artist> filteredArtists = new FilteredList<>(artistList, p -> true);

        artistComboBox.setItems(filteredArtists);
        artistComboBox.setEditable(true);

        artistComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Artist artist) {
                return artist != null ? artist.getFullName() : "";
            }

            @Override
            public Artist fromString(String string) {
                return artistList.stream()
                    .filter(a -> a.getFullName().equals(string))
                    .findFirst()
                    .orElse(null);
            }
        });
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setPainting(Painting painting) {
        this.painting = painting;
        if (painting != null) {
            titleField.setText(painting.getTitle());
            artistComboBox.setValue(painting.getArtist());
            yearField.setText(painting.getYear() != null ? painting.getYear().toString() : "");
            descriptionField.setText(painting.getDescription());
            genreField.setText(painting.getGenre());

            selectedImageName = painting.getImage();
            showPreviewImage(selectedImageName);
        }
    }

    private void showPreviewImage(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            previewImage.setImage(null);
            imageNameLabel.setText("");
            return;
        }

        Path imagePath = Paths.get("src/main/resources/images/paintings/", imageName);

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

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(dialogStage);

        if (file == null) return;

        try {
            Path targetDir = Paths.get("src/main/resources/images/paintings/");
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
        // изображения
        String title = titleField.getText().trim();
        if (title.isEmpty() || !title.matches("^[a-zA-Zа-яА-Я0-9 .,\\-]+$")) {
            showError("Название заполнено некорректно.");
            return;
        }

        // автор
        if (artistComboBox.getValue() == null) {
            showError("Выберите автора.");
            return;
        }

        // год
        int year;
        if (!yearField.getText().trim().isEmpty()) {
            try {
                year = Integer.parseInt(yearField.getText().trim());
                int current = Year.now().getValue();
                if (year <= 0 || year > current) {
                    showError("Год указан неверно.");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Год должен быть числом.");
                return;
            }
        } else {
            showError("Укажите год создания картины.");
            return;
        }

        // описание
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            showError("Описание не может быть пустым.");
            return;
        }

        // проверка изображения
        if (selectedImageName == null || selectedImageName.isEmpty()) {
            showError("Выберите изображение картины.");
            return;
        }

        // жанр
        String genre = genreField.getText().trim();
        if (genre.isEmpty() || !genre.matches("^[a-zA-Zа-яА-Я0-9 .,\\-]+$")) {
            showError("Жанр заполнен некорректно.");
            return;
        }

        // заполнение объекта
        if (painting == null) painting = new Painting();

        painting.setTitle(title);
        painting.setGenre(genre);
        painting.setArtist(artistComboBox.getValue());
        painting.setYear(year);
        painting.setDescription(description);
        painting.setImage(selectedImageName);

        // сохранение
        try {
            paintingService.savePainting(painting);
        } catch (Exception ex) {
            showError("Ошибка при сохранении картины: " + ex.getMessage());
            return;
        }

        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}
