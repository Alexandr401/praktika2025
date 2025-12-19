package com.gallery.controller;

import com.gallery.entity.Painting;
import com.gallery.entity.Artist;
import com.gallery.service.PaintingService;
import com.gallery.service.ArtistService;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

import javafx.scene.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.shape.Rectangle;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPaintingsController {
    private static final Logger log = LoggerFactory.getLogger(UserPaintingsController.class);

    @FXML
    private GridPane paintingsListGrid;
    @FXML
    private ScrollPane scroll;
    @FXML
    private ComboBox<String> genreComboBox;
    @FXML
    private ComboBox<Artist> artistComboBox;
    @FXML
    private TextField searchField;

    private List<Painting> allPaintings = new ArrayList<>();

    // хранение полного списка элементов
    private List<String> allGenres = new ArrayList<>();
    private List<Artist> allArtists = new ArrayList<>();

    private final PaintingService paintingService = new PaintingService();
    private final ArtistService artistService = new ArtistService();

    // все карточки отдельно
    private final List<Node> items = new ArrayList<>();

    // кэш всех карточек
    private final List<Node> allCards = new ArrayList<>();

    @FXML
    private void initialize() {
        scroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> layoutCards());

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);

        allPaintings = paintingService.getAllPaintings();
        loadPaintings(allPaintings);

        allGenres = allPaintings.stream()
            .map(Painting::getGenre)
            .filter(g -> g != null && !g.isEmpty())
            .distinct()
            .collect(Collectors.toList());

        allArtists = artistService.getAllArtists();

        // установка подсказок
        genreComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Выберите жанр" : item);
            }
        });

        artistComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Выберите автора" : item.getFullName());
            }
        });

        // показываем все
        genreComboBox.getItems().setAll(allGenres);
        artistComboBox.getItems().setAll(allArtists);

        // обработчики фильтрации
        PauseTransition pause = new PauseTransition(Duration.millis(250));

        searchField.textProperty().addListener((obs, o, n) -> {
            pause.setOnFinished(e -> updateFilteredPaintings());
            pause.playFromStart();
        });

        genreComboBox.setOnAction(e -> updateFilteredPaintings());
        artistComboBox.setOnAction(e -> updateFilteredPaintings());

        // Динамическое обновление ComboBox при открытии
        genreComboBox.setOnShowing(e -> {
            String currentGenre = genreComboBox.getSelectionModel().getSelectedItem();
            Artist selectedArtist = artistComboBox.getSelectionModel().getSelectedItem();

            List<String> availableGenres = allGenres.stream()
                .filter(g -> selectedArtist == null || allPaintings.stream()
                    .anyMatch(p -> g.equals(p.getGenre()) &&
                        p.getArtist() != null &&
                        p.getArtist().getId().equals(selectedArtist.getId())))
                .collect(Collectors.toList());

            genreComboBox.getItems().setAll(availableGenres);

            // сохранение текущего выбора
            if (currentGenre != null && availableGenres.contains(currentGenre)) {
                genreComboBox.getSelectionModel().select(currentGenre);
            } else {
                genreComboBox.getSelectionModel().clearSelection();
            }
        });

        artistComboBox.setOnShowing(e -> {
            Artist currentArtist = artistComboBox.getSelectionModel().getSelectedItem();
            String selectedGenre = genreComboBox.getSelectionModel().getSelectedItem();

            List<Artist> availableArtists = allArtists.stream()
                .filter(a -> selectedGenre == null || allPaintings.stream()
                    .anyMatch(p -> p.getGenre() != null &&
                        p.getGenre().equals(selectedGenre) &&
                        p.getArtist() != null &&
                        p.getArtist().getId().equals(a.getId())))
                .collect(Collectors.toList());

            artistComboBox.getItems().setAll(availableArtists);

            // сохранение текущего выбора
            if (currentArtist != null && availableArtists.contains(currentArtist)) {
                artistComboBox.getSelectionModel().select(currentArtist);
            } else {
                artistComboBox.getSelectionModel().clearSelection();
            }
        });
    }

    // загрузка карточек
    private void loadPaintings(List<Painting> paintings) {
        items.clear();
        allCards.clear(); // очищение кэша при первом запуске
        paintingsListGrid.getChildren().clear();

        for (Painting p : paintings) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_painting_row.fxml"));
                HBox row = loader.load();

                ImageView imageView = (ImageView) row.lookup("#imageView");
                Label titleText = (Label) row.lookup("#titleText");
                Text artistText = (Text) row.lookup("#artistText");
                Text yearText = (Text) row.lookup("#yearText");
                Text genreText = (Text) row.lookup("#genreText");
                ScrollPane descriptionScroll = (ScrollPane) row.lookup("#descriptionScroll");
                Label descriptionText = (Label) descriptionScroll.getContent();
                descriptionText.setText(p.getDescription() != null ? p.getDescription() : "");

                //  заполнение данными
                try {
                    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/paintings/" + p.getImage())));
                    imageView.setImage(image);
                } catch (Exception e) {
                    imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/no_image.png"))));
                }

                // скругление улов у картинок
                Rectangle clip = new Rectangle();
                clip.setArcWidth(32);  // радиус
                clip.setArcHeight(32);
                clip.widthProperty().bind(imageView.fitWidthProperty());
                clip.heightProperty().bind(imageView.fitHeightProperty());
                imageView.setClip(clip);

                // текст
                titleText.setText(p.getTitle());
                artistText.setText(p.getArtist() != null ? p.getArtist().getFullName() : "");
                yearText.setText(p.getYear() != null ? String.valueOf(p.getYear()) : "");
                genreText.setText(p.getGenre() != null ? p.getGenre() : "");
                descriptionText.setText(p.getDescription() != null ? p.getDescription() : "");

                // обработка клика по ImageView
                imageView.setOnMouseClicked(event -> {
                    Image img = imageView.getImage();
                    if (img != null) {
                        showFullImage(img, p.getTitle());
                    }
                });

                items.add(row);
                allCards.add(row); // сохранение карточек

            } catch (Exception e) {
                log.error("Ошибка при загрузке карточки картины: {}", p.getTitle(), e);
            }
        }
        layoutCards();
    }

    private void layoutCards() {
        if (items.isEmpty()) return;

        paintingsListGrid.getChildren().clear();
        double availableWidth = scroll.getViewportBounds().getWidth();

        // константы карточек
        int CARD_WIDTH = 750;
        int HGAP = 60;

        // количество столбцов по ширине
        int columns = Math.max(1, (int) ((availableWidth + HGAP) / (CARD_WIDTH + HGAP)));

        for (int i = 0; i < items.size(); i++) {
            Node card = items.get(i);

            int col = i % columns;
            int row = i / columns;

            paintingsListGrid.add(card, col, row);
        }
    }

    @FXML
    private void filterAll() {
        // сброс всех фильтров и поиска
        searchField.clear();
        genreComboBox.getSelectionModel().clearSelection();
        artistComboBox.getSelectionModel().clearSelection();

        items.clear();
        items.addAll(allCards); // все карточки
        layoutCards();
    }

    private void updateFilteredPaintings() {
        String q = searchField.getText().toLowerCase().trim();
        String selectedGenre = genreComboBox.getSelectionModel().getSelectedItem();
        Artist selectedArtist = artistComboBox.getSelectionModel().getSelectedItem();
        items.clear(); // очистка текущего списка

        // фильтр картин по текущим выбранным фильтрам
        for (int i = 0; i < allPaintings.size(); i++) {
            Painting p = allPaintings.get(i);
            boolean matches = (q.isEmpty() || p.getTitle().toLowerCase().contains(q)
                || (p.getGenre() != null && p.getGenre().toLowerCase().contains(q))
                || (p.getArtist() != null && p.getArtist().getFullName().toLowerCase().contains(q))
                || (p.getYear() != null && String.valueOf(p.getYear()).contains(q)))
                && (selectedGenre == null || selectedGenre.equals(p.getGenre())) && (selectedArtist == null ||
                (p.getArtist() != null && p.getArtist().getId().equals(selectedArtist.getId())));
            if (matches) {
                items.add(allCards.get(i)); // готовая карточка
            }
        }
        if (items.isEmpty()) {
            showEmptyMessage(q);
        } else {
            layoutCards(); // layout без пересоздания
        }
    }

    private void showEmptyMessage(String query) {
        items.clear();
        paintingsListGrid.getChildren().clear();

        Label label = new Label(query.isEmpty() ? "Ничего не найдено" : "По запросу \"" + query + "\" ничего не найдено");
        label.setStyle("-fx-font-size: 18px; -fx-text-fill: #666; -fx-padding: 0 0 100 0; -fx-font-weight: bold;" );

        // Label в StackPane для центрирования
        StackPane wrapper = new StackPane(label);
        wrapper.setPrefWidth(scroll.getViewportBounds().getWidth()); // всю ширину scroll
        wrapper.setPrefHeight(scroll.getViewportBounds().getHeight()); // всю высоту scroll
        StackPane.setAlignment(label, javafx.geometry.Pos.CENTER); // централизация Label внутри StackPane

        items.add(wrapper);
        layoutCards();
    }

    // полноразмерное изображение
    private void showFullImage(Image image, String title) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);

        // ImageView для изображения
        ImageView fullImageView = new ImageView(image);
        fullImageView.setPreserveRatio(true);
        fullImageView.setFitWidth(1200);
        fullImageView.setFitHeight(800);

        // StackPane для центрирования картинки внутри ScrollPane
        StackPane imageContainer = new StackPane(fullImageView);
        imageContainer.setAlignment(Pos.CENTER); // всегда по центру

        // ScrollPane для прокрутки при увеличении
        ScrollPane scrollPane = new ScrollPane(imageContainer);
        scrollPane.setPannable(false);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: black;");

        // название картины снизу поверх изображения
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 10;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        // Label для отображения текущего масштаба
        Label scaleLabel = new Label("Масштаб: 1.00");
        scaleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5;");

        // текст на картину
        StackPane stack = new StackPane(scrollPane, titleLabel, scaleLabel);
        StackPane.setAlignment(titleLabel, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(scaleLabel, Pos.TOP_RIGHT);

        Scene scene = new Scene(stack, 1200, 800);
        stage.setScene(scene);

        // масштабирование шаг 0.25
        final double[] scale = {1.0};
        stack.setOnScroll(event -> {
            double delta = event.getDeltaY() > 0 ? 0.25 : -0.25;
            double newScale = scale[0] + delta;
            newScale = Math.max(1.0, Math.min(5.0, newScale));
            scale[0] = newScale;

            fullImageView.setScaleX(scale[0]);
            fullImageView.setScaleY(scale[0]);

            // курсор MOVE масштаб >= 1.5
            if (scale[0] >= 1.5) {
                fullImageView.setCursor(Cursor.MOVE);
            } else {
                fullImageView.setCursor(Cursor.DEFAULT);
                fullImageView.setTranslateX(0);
                fullImageView.setTranslateY(0);
            }

            // текст масштаба
            scaleLabel.setText(String.format("Масштаб: %.2f", newScale));

            event.consume();
        });

        // перетаскивание изображения если масштаб >= 1.5
        final double[] mouseAnchorX = new double[1];
        final double[] mouseAnchorY = new double[1];
        final double[] translateX = new double[1];
        final double[] translateY = new double[1];

        fullImageView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && scale[0] >= 1.5) {
                mouseAnchorX[0] = event.getSceneX();
                mouseAnchorY[0] = event.getSceneY();
                translateX[0] = fullImageView.getTranslateX();
                translateY[0] = fullImageView.getTranslateY();
            }
        });

        fullImageView.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown() && scale[0] >= 1.5) {
                double deltaX = event.getSceneX() - mouseAnchorX[0];
                double deltaY = event.getSceneY() - mouseAnchorY[0];
                fullImageView.setTranslateX(translateX[0] + deltaX);
                fullImageView.setTranslateY(translateY[0] + deltaY);
            }
        });

        stage.setMaximized(true);
        stage.showAndWait();
    }
}
