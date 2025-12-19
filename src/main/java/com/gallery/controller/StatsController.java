package com.gallery.controller;

import com.gallery.entity.Artist;
import com.gallery.entity.Exhibition;
import com.gallery.entity.Painting;
import com.gallery.service.ArtistService;
import com.gallery.service.ExhibitionService;
import com.gallery.service.PaintingService;
import com.gallery.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class StatsController {

    @FXML
    private Label totalPaintingsLabel, totalArtistsLabel, totalExhibitionsLabel, totalUsersLabel;
    @FXML
    private Label averageAgeLabel, averageExhibitionDurationLabel;
    @FXML
    private LineChart<String, Number> lineChartByYear;
    @FXML
    private BarChart<String, Number> barChartByArtist;
    @FXML
    private PieChart pieChartByGenre;

    private final PaintingService paintingService = new PaintingService();
    private final ArtistService artistService = new ArtistService();
    private final ExhibitionService exhibitionService = new ExhibitionService();
    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        lineChartByYear.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());
        barChartByArtist.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/application.css")).toExternalForm());
        List<Painting> paintings = paintingService.getAllPaintings();
        List<Artist> artists = artistService.getAllArtists();
        List<Exhibition> exhibitions = exhibitionService.getAllExhibitions();

        // показатели
        totalPaintingsLabel.setText("Всего картин: " + paintings.size());
        totalArtistsLabel.setText("Всего авторов: " + artists.size());
        totalExhibitionsLabel.setText("Всего выставок: " + exhibitions.size());
        totalUsersLabel.setText("Всего пользователей: " + userService.getAllUsers().size());

        // средний возраст картины
        double averageAge = paintings.stream()
            .filter(p -> p.getYear() != null)
            .mapToInt(p -> 2025 - p.getYear())
            .average()
            .orElse(0);
        averageAgeLabel.setText(String.format("Средний возраст картины: %.1f лет", averageAge));

        // средняя длительность выставки
        double averageDuration = exhibitions.stream()
            .filter(e -> e.getStartDate() != null && e.getEndDate() != null)
            .mapToLong(e -> ChronoUnit.DAYS.between(e.getStartDate(), e.getEndDate()))
            .average()
            .orElse(0);
        averageExhibitionDurationLabel.setText(String.format("Средняя длительность выставки: %.1f дней", averageDuration));

        // LineChart количество картин по годам
        Map<Integer, Long> paintingsByYear = paintings.stream()
            .filter(p -> p.getYear() != null)
            .collect(Collectors.groupingBy(Painting::getYear, Collectors.counting()));
        XYChart.Series<String, Number> seriesYear = new XYChart.Series<>();
        paintingsByYear.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> seriesYear.getData().add(new XYChart.Data<>(String.valueOf(entry.getKey()), entry.getValue())));
        lineChartByYear.getData().add(seriesYear);
        lineChartByYear.setLegendVisible(false);

        // BarChart количество картин по авторам
        Map<String, Long> paintingsByArtist = paintings.stream()
            .filter(p -> p.getArtist() != null)
            .collect(Collectors.groupingBy(p -> p.getArtist().getFullName(), Collectors.counting()));
        XYChart.Series<String, Number> seriesArtist = new XYChart.Series<>();
        paintingsByArtist.forEach((artist, count) -> seriesArtist.getData().add(new XYChart.Data<>(artist, count)));
        barChartByArtist.getData().add(seriesArtist);
        barChartByArtist.setLegendVisible(false);

        // PieChart распределение по жанрам
        Map<String, Long> paintingsByGenre = paintings.stream()
            .filter(p -> p.getGenre() != null)
            .collect(Collectors.groupingBy(Painting::getGenre, Collectors.counting()));
        pieChartByGenre.getData().clear();
        paintingsByGenre.forEach((genre, count) -> {
            PieChart.Data data = new PieChart.Data(genre + " (" + count + ")", count);
            pieChartByGenre.getData().add(data);
        });

        // Ось X
        CategoryAxis xAxisLine = (CategoryAxis) lineChartByYear.getXAxis();
        xAxisLine.setTickLabelFont(Font.font("System", FontWeight.BOLD, 12)); // подписи категорий

        // Ось Y
        NumberAxis yAxisLine = (NumberAxis) lineChartByYear.getYAxis();
        yAxisLine.setTickLabelFont(Font.font("System", FontWeight.BOLD, 12)); // числа
        CategoryAxis xAxisBar = (CategoryAxis) barChartByArtist.getXAxis();
        xAxisBar.setTickLabelFont(Font.font("System", FontWeight.BOLD, 12));
        NumberAxis yAxisBar = (NumberAxis) barChartByArtist.getYAxis();
        yAxisBar.setTickLabelFont(Font.font("System", FontWeight.BOLD, 12));

        // PieChart подписи на секторах
        pieChartByGenre.applyCss();
        pieChartByGenre.layout();
        pieChartByGenre.lookupAll(".chart-pie-label").forEach(node -> node.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;"));
    }
}
