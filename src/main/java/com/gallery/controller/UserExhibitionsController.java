package com.gallery.controller;

import com.gallery.entity.Exhibition;
import com.gallery.service.ExhibitionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserExhibitionsController {
    private static final Logger log = LoggerFactory.getLogger(UserExhibitionsController.class);

    @FXML
    private GridPane exhibitionsList;
    @FXML
    private ComboBox<String> titleComboBox;
    @FXML
    private ComboBox<String> genreComboBox;
    @FXML
    private ComboBox<String> artistComboBox;
    @FXML
    private ComboBox<String> paintingComboBox;
    @FXML
    private TextField searchField;

    private final ExhibitionService exhibitionService = new ExhibitionService();

    private List<Exhibition> allExhibitions = new ArrayList<>();
    private List<String> allTitles = new ArrayList<>();
    private List<String> allGenres = new ArrayList<>();
    private List<String> allArtists = new ArrayList<>();
    private List<String> allPaintings = new ArrayList<>();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    private ScrollPane scroll;

    private final List<Node> items = new ArrayList<>();

    @FXML
    private void initialize() {
        scroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> layoutCards());

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);

        allExhibitions = exhibitionService.getAllExhibitions();

        // загрузка уникальных значений для всех фильтров
        loadAllFilterValues(allExhibitions);

        // все выставки
        displayExhibitions(allExhibitions);

        // установка подсказок
        titleComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Выберите название" : item);
            }
        });

        genreComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Выберите жанр" : item);
            }
        });

        artistComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Выберите автора" : item);
            }
        });

        paintingComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Выберите картину" : item);
            }
        });

        // обработчики фильтров
        searchField.textProperty().addListener((obs, oldV, newV) -> updateFilteredExhibitions());
        titleComboBox.setOnAction(e -> updateFilteredExhibitions());
        genreComboBox.setOnAction(e -> updateFilteredExhibitions());
        artistComboBox.setOnAction(e -> updateFilteredExhibitions());
        paintingComboBox.setOnAction(e -> updateFilteredExhibitions());

        // динамическое обновление ComboBox при открытии
        titleComboBox.setOnShowing(e -> {
            String currentTitle = titleComboBox.getSelectionModel().getSelectedItem();

            List<String> availableTitles = allTitles.stream()
                .filter(t -> genreComboBox.getSelectionModel().getSelectedItem() == null || allExhibitions.stream()
                    .anyMatch(ex -> ex.getName().equals(t) && ex.getPaintingExhibitions().stream()
                        .anyMatch(pe -> pe.getPainting().getGenre()
                            .equals(genreComboBox.getSelectionModel().getSelectedItem()))
                    )
                )
                .collect(Collectors.toList());

            titleComboBox.getItems().setAll(availableTitles);

            if (currentTitle != null && availableTitles.contains(currentTitle)) {
                titleComboBox.getSelectionModel().select(currentTitle);
            } else {
                titleComboBox.getSelectionModel().clearSelection();
            }
        });

        genreComboBox.setOnShowing(e -> {
            String currentGenre = genreComboBox.getSelectionModel().getSelectedItem();
            String selectedTitle = titleComboBox.getSelectionModel().getSelectedItem();

            List<String> availableGenres = allGenres.stream()
                .filter(g -> selectedTitle == null || allExhibitions.stream()
                    .anyMatch(ex -> ex.getName().equals(selectedTitle) && ex.getPaintingExhibitions().stream()
                        .anyMatch(pe -> pe.getPainting().getGenre().equals(g))
                    )
                )
                .collect(Collectors.toList());

            genreComboBox.getItems().setAll(availableGenres);

            if (currentGenre != null && availableGenres.contains(currentGenre)) {
                genreComboBox.getSelectionModel().select(currentGenre);
            } else {
                genreComboBox.getSelectionModel().clearSelection();
            }
        });

        artistComboBox.setOnShowing(e -> {
            String currentArtist = artistComboBox.getSelectionModel().getSelectedItem();
            String selectedTitle = titleComboBox.getSelectionModel().getSelectedItem();
            String selectedGenre = genreComboBox.getSelectionModel().getSelectedItem();

            List<String> availableArtists = allArtists.stream()
                .filter(a -> (selectedTitle == null || allExhibitions.stream()
                    .anyMatch(ex -> ex.getName().equals(selectedTitle)
                        && ex.getPaintingExhibitions().stream()
                        .anyMatch(pe -> pe.getPainting().getArtist().getFullName().equals(a))
                    ))
                    && (selectedGenre == null || allExhibitions.stream()
                    .anyMatch(ex -> ex.getPaintingExhibitions().stream()
                        .anyMatch(pe -> pe.getPainting().getGenre().equals(selectedGenre)
                            && pe.getPainting().getArtist().getFullName().equals(a))
                    ))
                )
                .collect(Collectors.toList());

            artistComboBox.getItems().setAll(availableArtists);

            if (currentArtist != null && availableArtists.contains(currentArtist)) {
                artistComboBox.getSelectionModel().select(currentArtist);
            } else {
                artistComboBox.getSelectionModel().clearSelection();
            }
        });

        paintingComboBox.setOnShowing(e -> {
            String currentPainting = paintingComboBox.getSelectionModel().getSelectedItem();
            String selectedTitle = titleComboBox.getSelectionModel().getSelectedItem();
            String selectedGenre = genreComboBox.getSelectionModel().getSelectedItem();
            String selectedArtist = artistComboBox.getSelectionModel().getSelectedItem();

            List<String> availablePaintings = allPaintings.stream()
                .filter(p -> allExhibitions.stream()
                    .anyMatch(ex -> (selectedTitle == null || ex.getName().equals(selectedTitle))
                        && ex.getPaintingExhibitions().stream()
                        .anyMatch(pe -> pe.getPainting().getTitle().equals(p)
                            && (selectedGenre == null || pe.getPainting().getGenre().equals(selectedGenre))
                            && (selectedArtist == null || pe.getPainting().getArtist().getFullName().equals(selectedArtist))
                        )
                    )
                )
                .collect(Collectors.toList());

            paintingComboBox.getItems().setAll(availablePaintings);

            if (currentPainting != null && availablePaintings.contains(currentPainting)) {
                paintingComboBox.getSelectionModel().select(currentPainting);
            } else {
                paintingComboBox.getSelectionModel().clearSelection();
            }
        });
    }

    private void loadAllFilterValues(List<Exhibition> exhibitions) {
        allTitles = exhibitions.stream()
            .map(Exhibition::getName)
            .distinct()
            .collect(Collectors.toList());

        allGenres = exhibitions.stream()
            .flatMap(e -> e.getPaintingExhibitions().stream())
            .map(pe -> pe.getPainting().getGenre())
            .distinct()
            .collect(Collectors.toList());

        allArtists = exhibitions.stream()
            .flatMap(e -> e.getPaintingExhibitions().stream())
            .map(pe -> pe.getPainting().getArtist().getFullName())
            .distinct()
            .collect(Collectors.toList());

        allPaintings = exhibitions.stream()
            .flatMap(e -> e.getPaintingExhibitions().stream())
            .map(pe -> pe.getPainting().getTitle())
            .distinct()
            .collect(Collectors.toList());
    }

    private void updateFilteredExhibitions() {
        String q = searchField.getText().toLowerCase().trim();
        String selectedTitle = titleComboBox.getSelectionModel().getSelectedItem();
        String selectedGenre = genreComboBox.getSelectionModel().getSelectedItem();
        String selectedArtist = artistComboBox.getSelectionModel().getSelectedItem();
        String selectedPainting = paintingComboBox.getSelectionModel().getSelectedItem();

        List<Exhibition> filtered = allExhibitions.stream().filter(e -> {
            boolean matches = q.isEmpty() ||
                e.getName().toLowerCase().contains(q) ||
                e.getPaintingExhibitions().stream().anyMatch(pe ->
                    (pe.getPainting().getGenre() != null && pe.getPainting().getGenre().toLowerCase().contains(q)) ||
                        (pe.getPainting().getArtist() != null && pe.getPainting().getArtist().getFullName().toLowerCase().contains(q)) ||
                        (pe.getPainting().getTitle() != null && pe.getPainting().getTitle().toLowerCase().contains(q)) ||
                        e.getStartDate().toString().contains(q) ||
                        e.getEndDate().toString().contains(q)
                );

            if (selectedTitle != null) matches &= e.getName().equals(selectedTitle);
            if (selectedGenre != null) matches &= e.getPaintingExhibitions().stream().anyMatch(pe -> selectedGenre.equals(pe.getPainting().getGenre()));
            if (selectedArtist != null) matches &= e.getPaintingExhibitions().stream().anyMatch(pe -> pe.getPainting().getArtist().getFullName().equals(selectedArtist));
            if (selectedPainting != null) matches &= e.getPaintingExhibitions().stream().anyMatch(pe -> selectedPainting.equals(pe.getPainting().getTitle()));
            return matches;
        }).collect(Collectors.toList());

        if (filtered.isEmpty()) {
            showEmptyMessage(q);
        } else {
            displayExhibitions(filtered);
        }
    }

    @FXML
    private void filterAll() {
        searchField.clear();

        titleComboBox.setValue(null);
        genreComboBox.setValue(null);
        artistComboBox.setValue(null);
        paintingComboBox.setValue(null);

        displayExhibitions(allExhibitions);
    }

    private void displayExhibitions(List<Exhibition> exhibitions) {
        items.clear();
        exhibitionsList.getChildren().clear();

        for (Exhibition e : exhibitions) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_exhibition_row.fxml"));
                VBox row = loader.load();

                Label titleText = (Label) row.lookup("#titleText");
                Text datesText = (Text) row.lookup("#datesText");
                Label genresText = (Label) row.lookup("#genresText");

                ScrollPane descriptionScroll = (ScrollPane) row.lookup("#descriptionText");
                Label descriptionText = (Label) descriptionScroll.getContent();

                Label artistsText = (Label) row.lookup("#artistsText");
                Label paintingsText = (Label) row.lookup("#paintingsText");

                titleText.setText(e.getName());
                datesText.setText("С " + e.getStartDate().format(formatter) + " До " + e.getEndDate().format(formatter));

                genresText.setText(
                    e.getPaintingExhibitions().stream()
                        .map(pe -> pe.getPainting().getGenre())
                        .distinct()
                        .collect(Collectors.joining(", "))
                );

                descriptionText.setText(e.getDescription() != null ? e.getDescription() : "");

                artistsText.setText(
                    e.getPaintingExhibitions().stream()
                        .map(pe -> pe.getPainting().getArtist().getFullName())
                        .distinct()
                        .collect(Collectors.joining(", "))
                );

                paintingsText.setText(
                    e.getPaintingExhibitions().stream()
                        .map(pe -> pe.getPainting().getTitle())
                        .distinct()
                        .collect(Collectors.joining(", "))
                );

                items.add(row);

            } catch (Exception ex) {
                log.error("Ошибка при загрузке карточки выставки: {}", e.getName(), ex);
            }
        }

        layoutCards();
    }

    private void layoutCards() {
        if (items.isEmpty()) return;

        exhibitionsList.getChildren().clear();
        double availableWidth = scroll.getViewportBounds().getWidth();

        int CARD_WIDTH = 750; // ширина карточки выставки
        int HGAP = 60;

        int columns = Math.max(1,
            (int) ((availableWidth + HGAP) / (CARD_WIDTH + HGAP))
        );

        for (int i = 0; i < items.size(); i++) {
            Node card = items.get(i);

            int col = i % columns;
            int row = i / columns;

            exhibitionsList.add(card, col, row);
        }
    }

    private void showEmptyMessage(String query) {
        items.clear();
        exhibitionsList.getChildren().clear();

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
}
