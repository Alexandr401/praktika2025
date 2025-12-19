package com.gallery.controller;

import com.gallery.entity.Artist;
import com.gallery.service.ArtistService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserArtistsController {
    private static final Logger log = LoggerFactory.getLogger(UserArtistsController.class);

    @FXML
    private GridPane artistsGrid;
    @FXML
    private ComboBox<Artist> artistComboBox;
    @FXML
    private ScrollPane scroll;
    @FXML
    private javafx.scene.control.TextField searchField;

    private final ArtistService artistService = new ArtistService();
    private final List<Node> allCards = new ArrayList<>();
    private final List<Artist> allArtists = new ArrayList<>();
    private final List<Node> displayedCards = new ArrayList<>();

    @FXML
    private void initialize() {
        scroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> layoutCards(displayedCards));
        scroll.setFitToWidth(true);

        loadArtists();

        // debounce для поиска
        PauseTransition pause = new PauseTransition(Duration.millis(250));
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            pause.setOnFinished(e -> updateFilteredArtists());
            pause.playFromStart();
        });

        artistComboBox.setOnAction(e -> updateFilteredArtists());

        // динамическое обновление ComboBox при открытии
        artistComboBox.setOnShowing(e -> {
            Artist currentArtist = artistComboBox.getSelectionModel().getSelectedItem();

            List<Artist> availableArtists = allArtists.stream()
                    .filter(a -> searchField.getText().isEmpty() || a.getFullName().toLowerCase().contains(searchField.getText().toLowerCase()))
                    .collect(Collectors.toList());

            artistComboBox.getItems().setAll(availableArtists);

            if (currentArtist != null && availableArtists.contains(currentArtist)) {
                artistComboBox.getSelectionModel().select(currentArtist);
            } else {
                artistComboBox.getSelectionModel().clearSelection();
            }
        });
    }

    private void loadArtists() {
        artistsGrid.getChildren().clear();
        allCards.clear();
        allArtists.clear();

        List<Artist> artists = artistService.getAllArtists();
        allArtists.addAll(artists);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Artist artist : artists) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user_artists_row.fxml"));
                VBox row = loader.load();

                Label fullNameLabel = (Label) row.lookup("#fullNameLabel");
                ImageView photoImageView = (ImageView) row.lookup("#photoImageView");
                Label datesLabel = (Label) row.lookup("#datesLabel");
                ScrollPane biographyScroll = (ScrollPane) row.lookup("#biographyScroll");
                Label biographyLabel = (Label) biographyScroll.getContent();

                fullNameLabel.setText(artist.getFullName());

                InputStream imageStream = getClass().getResourceAsStream("/images/artists/" + artist.getImage());
                Image image;
                if (imageStream != null) {
                    image = new Image(imageStream);
                } else {
                    InputStream defaultStream = getClass().getResourceAsStream("/images/no_image.png");
                    if (defaultStream != null) {
                        image = new Image(defaultStream);
                    } else {
                        image = null;
                        log.warn("Файл изображения артиста и default не найдены: {}", artist.getFullName());
                    }
                }
                photoImageView.setImage(image);

                String birth = artist.getBirthDate() != null ? artist.getBirthDate().format(formatter) : "";
                String death = artist.getDeathDate() != null ? artist.getDeathDate().format(formatter) : "Жив";
                datesLabel.setText(birth + " - " + death);

                biographyLabel.setText(artist.getBiography() != null ? artist.getBiography() : "");

                allCards.add(row);
            } catch (Exception e) {
                log.error("Ошибка при загрузке карточки артиста: {}", artist.getFullName(), e);
            }
        }

        displayedCards.clear();
        displayedCards.addAll(allCards);
        layoutCards(displayedCards);

        // ComboBox
        artistComboBox.getItems().setAll(allArtists);
        artistComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Artist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Выберите автора" : item.getFullName());
            }
        });
    }

    private void layoutCards(List<Node> cards) {
        if (cards.isEmpty()) {
            artistsGrid.getChildren().clear();
            return;
        }

        artistsGrid.getChildren().clear();
        double availableWidth = scroll.getViewportBounds().getWidth();

        int CARD_WIDTH = 500;
        int HGAP = 60;

        int columns = Math.max(1, (int) ((availableWidth + HGAP) / (CARD_WIDTH + HGAP)));

        for (int i = 0; i < cards.size(); i++) {
            Node card = cards.get(i);
            int col = i % columns;
            int row = i / columns;
            artistsGrid.add(card, col, row);
        }
    }

    private void updateFilteredArtists() {
        String query = searchField.getText().toLowerCase().trim();
        Artist selectedArtist = artistComboBox.getSelectionModel().getSelectedItem();

        displayedCards.clear();

        for (int i = 0; i < allCards.size(); i++) {
            VBox card = (VBox) allCards.get(i);
            Label fullNameLabel = (Label) card.lookup("#fullNameLabel");
            Artist artist = allArtists.get(i);

            boolean matchesQuery = fullNameLabel.getText().toLowerCase().contains(query);
            boolean matchesArtist = selectedArtist == null || selectedArtist.getId().equals(artist.getId());

            if (matchesQuery && matchesArtist) {
                displayedCards.add(card);
            }
        }

        if (displayedCards.isEmpty()) {
            showEmptyMessage(query);
        } else {
            layoutCards(displayedCards);
        }
    }

    private void showEmptyMessage(String query) {
        artistsGrid.getChildren().clear();
        Label label = new Label(query.isEmpty() ? "Ничего не найдено" : "По запросу \"" + query + "\" ничего не найдено");
        label.setStyle("-fx-font-size: 18px; -fx-text-fill: #666; -fx-padding: 0 0 90 0; -fx-font-weight: bold;");

        StackPane wrapper = new StackPane(label);
        wrapper.setPrefWidth(scroll.getViewportBounds().getWidth());
        wrapper.setPrefHeight(scroll.getViewportBounds().getHeight());
        StackPane.setAlignment(label, javafx.geometry.Pos.CENTER);

        displayedCards.add(wrapper);
        layoutCards(displayedCards);
    }

    @FXML
    private void filterAll() {
        searchField.clear();
        artistComboBox.getSelectionModel().clearSelection();
        displayedCards.clear();
        displayedCards.addAll(allCards);
        layoutCards(displayedCards);
    }
}
