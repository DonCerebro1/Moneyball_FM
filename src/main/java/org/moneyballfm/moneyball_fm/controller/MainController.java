package org.moneyballfm.moneyball_fm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.ConfigLoader;
import org.moneyballfm.moneyball_fm.model.Player;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.parser.FmExportParser;
import org.moneyballfm.moneyball_fm.service.MoneyballCalculator;

import java.io.File;
import java.util.List;

public class MainController {

	@FXML private VBox rootPane;
	@FXML private Label statusLabel;
	@FXML private TableView<PlayerRowModel> playerTable;

	@FXML private TableColumn<PlayerRowModel, String> colName;
	@FXML private TableColumn<PlayerRowModel, String> colRole;
	@FXML private TableColumn<PlayerRowModel, Integer> colAge;
	@FXML private TableColumn<PlayerRowModel, Long> colSalary;
	@FXML private TableColumn<PlayerRowModel, Integer> colMinutes;
	@FXML private TableColumn<PlayerRowModel, Double> colStat1;
	@FXML private TableColumn<PlayerRowModel, Double> colStat2;
	@FXML private TableColumn<PlayerRowModel, Double> colStat3;
	@FXML private TableColumn<PlayerRowModel, Double> colScore;
	@FXML private TableColumn<PlayerRowModel, String> colTier;
	@FXML private TableColumn<PlayerRowModel, String> colSalaryCheck;
	@FXML private TableColumn<PlayerRowModel, String> colQuality;

	private final ObservableList<PlayerRowModel> playerDataList = FXCollections.observableArrayList();
	private AppConfig appConfig;

	@FXML
	public void initialize() {
		appConfig = ConfigLoader.loadConfig();

		setupTableColumns();
		setupDragAndDrop();

		playerTable.setItems(playerDataList);
		playerTable.setEditable(true);
	}

	private void setupTableColumns() {
		colName.setCellValueFactory(cell -> cell.getValue().nameProperty());
		colName.setCellFactory(TextFieldTableCell.forTableColumn());
		colName.setOnEditCommit(e -> e.getRowValue().nameProperty().set(e.getNewValue()));

		colRole.setCellValueFactory(cell -> cell.getValue().roleProperty());
		colRole.setCellFactory(TextFieldTableCell.forTableColumn());
		colRole.setOnEditCommit(e -> {
			e.getRowValue().roleProperty().set(e.getNewValue());
			recalc(e.getRowValue());
		});

		colAge.setCellValueFactory(cell -> cell.getValue().ageProperty().asObject());
		colAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colAge.setOnEditCommit(e -> e.getRowValue().ageProperty().set(e.getNewValue()));

		colSalary.setCellValueFactory(cell -> cell.getValue().monthlySalaryProperty().asObject());
		colSalary.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
		colSalary.setOnEditCommit(e -> {
			e.getRowValue().monthlySalaryProperty().set(e.getNewValue());
			recalc(e.getRowValue());
		});

		colMinutes.setCellValueFactory(cell -> cell.getValue().minutesPlayedProperty().asObject());
		colMinutes.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colMinutes.setOnEditCommit(e -> {
			e.getRowValue().minutesPlayedProperty().set(e.getNewValue());
			recalc(e.getRowValue());
		});

		colStat1.setCellValueFactory(cell -> cell.getValue().statValue1Property().asObject());
		colStat1.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat1.setOnEditCommit(e -> {
			e.getRowValue().statValue1Property().set(e.getNewValue());
			recalc(e.getRowValue());
		});

		colStat2.setCellValueFactory(cell -> cell.getValue().statValue2Property().asObject());
		colStat2.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat2.setOnEditCommit(e -> {
			e.getRowValue().statValue2Property().set(e.getNewValue());
			recalc(e.getRowValue());
		});

		colStat3.setCellValueFactory(cell -> cell.getValue().statValue3Property().asObject());
		colStat3.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat3.setOnEditCommit(e -> {
			e.getRowValue().statValue3Property().set(e.getNewValue());
			recalc(e.getRowValue());
		});

		colScore.setCellValueFactory(cell -> cell.getValue().moneyballScoreProperty().asObject());
		colTier.setCellValueFactory(cell -> cell.getValue().salaryTierProperty());
		colSalaryCheck.setCellValueFactory(cell -> cell.getValue().salaryCheckProperty());
		colQuality.setCellValueFactory(cell -> cell.getValue().sampleQualityProperty());
	}

	private void recalc(PlayerRowModel row) {
		MoneyballCalculator.recalculate(row, appConfig);
	}

	private void setupDragAndDrop() {
		rootPane.setOnDragOver(event -> {
			if (event.getGestureSource() != rootPane && event.getDragboard().hasFiles()) {
				event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}
			event.consume();
		});

		rootPane.setOnDragDropped(event -> {
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasFiles()) {
				File droppedFile = db.getFiles().get(0);
				try {
					List<PlayerRowModel> rows = FmExportParser.parseFile(droppedFile);
					playerDataList.clear();
					for (PlayerRowModel row : rows) {
						MoneyballCalculator.recalculate(row, appConfig);
						playerDataList.add(row);
					}
					statusLabel.setText("Erfolgreich geladen: " + droppedFile.getName() + " (" + rows.size() + " Spieler)");
					success = true;
				} catch (Exception e) {
					statusLabel.setText("Fehler beim Laden: " + e.getMessage());
					e.printStackTrace();
				}
			}
			event.setDropCompleted(success);
			event.consume();
		});
	}

	@FXML
	public void onAddPlayerManual() {
		Player p = new Player("Neuer Spieler", "Stoßstürmer (A)", 22, 1000000, 50000, 2.0, 12, 12, 12, 1000);
		PlayerRowModel row = new PlayerRowModel(p, 0.5, 0.15, 0.45);
		MoneyballCalculator.recalculate(row, appConfig);
		playerDataList.add(row);
		playerTable.scrollTo(row);
	}

	@FXML
	public void onClearList() {
		playerDataList.clear();
		statusLabel.setText("Tabelle geleert.");
	}
}