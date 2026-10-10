package org.moneyballfm.moneyball_fm.controller;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.moneyballfm.moneyball_fm.db.HistoryRepository;
import org.moneyballfm.moneyball_fm.model.PlayerHistoryEntry;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PlayerDetailDialog {

	private static final NumberFormat EURO_FORMAT = NumberFormat.getNumberInstance(Locale.GERMANY);

	public static void show(PlayerRowModel player, Stage ownerStage) {
		if (player == null) return;

		Stage dialog = new Stage();
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(ownerStage);
		dialog.setTitle("Spieler-Historie: " + player.nameProperty().get());

		List<PlayerHistoryEntry> history = HistoryRepository.getPlayerHistory(player.nameProperty().get());

		VBox root = new VBox(15);
		root.setPadding(new Insets(15));
		root.setPrefSize(800, 600);

		// 1. Header-Info
		Label title = new Label(player.nameProperty().get() + " (" + player.roleProperty().get() + ")");
		title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

		// 2. LineChart für Moneyball-Score
		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Snapshot / Datum");

		NumberAxis yAxis = new NumberAxis(0, 100, 10);
		yAxis.setLabel("Moneyball-Score");

		LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
		lineChart.setTitle("Entwicklung: Moneyball-Score");
		lineChart.setLegendVisible(false);
		lineChart.setPrefHeight(250);

		XYChart.Series<String, Number> series = new XYChart.Series<>();
		for (PlayerHistoryEntry entry : history) {
			series.getData().add(new XYChart.Data<>(entry.snapshotDateProperty().get(), entry.moneyballScoreProperty().get()));
		}
		lineChart.getData().add(series);

		// 3. Verlaufs-Tabelle
		TableView<PlayerHistoryEntry> table = new TableView<>();
		table.setItems(FXCollections.observableArrayList(history));

		TableColumn<PlayerHistoryEntry, String> colDate = new TableColumn<>("Datum");
		colDate.setCellValueFactory(c -> c.getValue().snapshotDateProperty());
		colDate.setPrefWidth(120);

		TableColumn<PlayerHistoryEntry, String> colRole = new TableColumn<>("Rolle");
		colRole.setCellValueFactory(c -> c.getValue().roleProperty());
		colRole.setPrefWidth(120);

		TableColumn<PlayerHistoryEntry, Integer> colAge = new TableColumn<>("Alter");
		colAge.setCellValueFactory(c -> c.getValue().ageProperty().asObject());
		colAge.setPrefWidth(50);

		TableColumn<PlayerHistoryEntry, Double> colScore = new TableColumn<>("Score");
		colScore.setCellValueFactory(c -> c.getValue().moneyballScoreProperty().asObject());
		colScore.setPrefWidth(65);

		TableColumn<PlayerHistoryEntry, String> colVal = new TableColumn<>("Marktwert");
		colVal.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty("€ " + EURO_FORMAT.format(c.getValue().marketValueProperty().get())));
		colVal.setPrefWidth(110);

		TableColumn<PlayerHistoryEntry, Integer> colMin = new TableColumn<>("Minuten");
		colMin.setCellValueFactory(c -> c.getValue().minutesPlayedProperty().asObject());
		colMin.setPrefWidth(70);

		TableColumn<PlayerHistoryEntry, String> colRec = new TableColumn<>("Status / Gehalt");
		colRec.setCellValueFactory(c -> c.getValue().recommendationProperty());
		colRec.setPrefWidth(150);

		table.getColumns().addAll(colDate, colRole, colAge, colScore, colVal, colMin, colRec);
		VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

		root.getChildren().addAll(title, lineChart, table);

		Scene scene = new Scene(root);
		dialog.setScene(scene);
		dialog.show();
	}
}