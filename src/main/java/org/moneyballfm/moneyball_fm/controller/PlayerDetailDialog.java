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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.moneyballfm.moneyball_fm.db.HistoryRepository;
import org.moneyballfm.moneyball_fm.model.LoanPlayerModel;
import org.moneyballfm.moneyball_fm.model.PlayerHistoryEntry;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.ScoutingPlayerModel;
import org.moneyballfm.moneyball_fm.model.YouthPlayerModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PlayerDetailDialog {

	private static final NumberFormat EURO_FORMAT = NumberFormat.getNumberInstance(Locale.GERMANY);

	public static void show(PlayerRowModel player, Stage ownerStage) {
		if (player == null) return;
		showGeneric(player.nameProperty().get(), player.roleProperty().get(), player.ageProperty().get(), ownerStage);
	}

	public static void show(LoanPlayerModel player, Stage ownerStage) {
		if (player == null) return;
		showGeneric(player.getPlayerName(), player.targetRoleProperty().get(), player.ageProperty().get(), ownerStage);
	}

	public static void show(YouthPlayerModel player, Stage ownerStage) {
		if (player == null) return;
		showGeneric(player.getName(), player.targetRoleProperty().get(), player.ageProperty().get(), ownerStage);
	}

	// Scouting-Kandidaten mit Verhandlungskennzahlen und Scouting-Historie
	public static void show(ScoutingPlayerModel scout, Stage ownerStage) {
		if (scout == null) return;

		Stage dialog = new Stage();
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(ownerStage);

		String name = scout.nameProperty().get();
		int age = scout.ageProperty().get();
		String role = scout.targetRoleProperty().get();
		dialog.setTitle("Scouting-Profil: " + name + " (" + age + " J.)");

		VBox root = new VBox(15);
		root.setPadding(new Insets(18));
		root.setPrefSize(820, 640);

		// 1. Header
		String club = scout.clubProperty().get() != null ? " • " + scout.clubProperty().get() : "";
		Label title = new Label(name + " (" + role + ") • " + age + " Jahre" + club);
		title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

		// 2. Finanz- & Scouting-KPI-Box
		GridPane kpiGrid = new GridPane();
		kpiGrid.setHgap(20);
		kpiGrid.setVgap(8);
		kpiGrid.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12px; -fx-border-color: #e2e8f0; -fx-border-radius: 6px; -fx-background-radius: 6px;");

		Label lblScore = new Label("Scouting-Score: " + scout.scoutingScoreProperty().get() + " / 100");
		lblScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb;");

		String askingStr = scout.askingPriceProperty().get() >= 900_000_000L ? "Unverkäuflich" : "€ " + EURO_FORMAT.format(scout.askingPriceProperty().get());
		Label lblPrice = new Label("Forderung: " + askingStr + " (Max: € " + EURO_FORMAT.format(scout.maxTransferFeeProperty().get()) + ")");

		Label lblWage = new Label("Gehaltsforderung: € " + EURO_FORMAT.format(scout.wageDemandProperty().get()) + " / Mon. (Max: € " + EURO_FORMAT.format(scout.maxWageProperty().get()) + ")");
		Label lblVerdict = new Label("Urteil: " + scout.priceCheckProperty().get() + " | " + scout.transferActionProperty().get());
		lblVerdict.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");

		kpiGrid.add(lblScore, 0, 0);
		kpiGrid.add(lblVerdict, 1, 0);
		kpiGrid.add(lblPrice, 0, 1);
		kpiGrid.add(lblWage, 1, 1);

		// 3. Scouting-Historie mit Alterstoleranz laden
		List<PlayerHistoryEntry> history = HistoryRepository.getScoutingHistory(name, age);

		// LineChart
		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Snapshot / Datum");
		NumberAxis yAxis = new NumberAxis(0, 100, 10);
		yAxis.setLabel("Moneyball-Score");

		LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
		lineChart.setTitle("Entwicklung: Performance-Score");
		lineChart.setLegendVisible(false);
		lineChart.setPrefHeight(230);

		XYChart.Series<String, Number> series = new XYChart.Series<>();
		for (PlayerHistoryEntry entry : history) {
			series.getData().add(new XYChart.Data<>(entry.snapshotDateProperty().get(), entry.moneyballScoreProperty().get()));
		}

		if (history.isEmpty()) {
			series.getData().add(new XYChart.Data<>("Aktuell (Scout)", scout.scoutingScoreProperty().get()));
		}
		lineChart.getData().add(series);

		// Tabelle
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

		TableColumn<PlayerHistoryEntry, String> colVal = new TableColumn<>("Ablöse / Wert");
		colVal.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty("€ " + EURO_FORMAT.format(c.getValue().marketValueProperty().get())));
		colVal.setPrefWidth(120);

		TableColumn<PlayerHistoryEntry, String> colRec = new TableColumn<>("Einstufung");
		colRec.setCellValueFactory(c -> c.getValue().recommendationProperty());
		colRec.setPrefWidth(170);

		table.getColumns().addAll(colDate, colRole, colAge, colScore, colVal, colRec);
		VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

		root.getChildren().addAll(title, kpiGrid, lineChart, table);

		Scene scene = new Scene(root);
		dialog.setScene(scene);
		dialog.show();
	}

	public static void showGeneric(String playerName, String role, Stage ownerStage) {
		showGeneric(playerName, role, -1, ownerStage);
	}

	public static void showGeneric(String playerName, String role, int age, Stage ownerStage) {
		if (playerName == null || playerName.isBlank()) return;

		Stage dialog = new Stage();
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(ownerStage);
		dialog.setTitle("Spieler-Historie: " + playerName + (age > 0 ? " (" + age + " J.)" : ""));

		// Historie mit Alterstoleranz abfragen (+/- 2 Jahre)
		List<PlayerHistoryEntry> history = HistoryRepository.getPlayerHistory(playerName, age);

		VBox root = new VBox(15);
		root.setPadding(new Insets(15));
		root.setPrefSize(800, 600);

		// 1. Header-Info
		String rolePart = (role != null && !role.isBlank()) ? " (" + role + ")" : "";
		String agePart = (age > 0) ? " • " + age + " Jahre" : "";
		Label title = new Label(playerName + rolePart + agePart);
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