package org.moneyballfm.moneyball_fm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.ConfigLoader;
import org.moneyballfm.moneyball_fm.db.HistoryRepository;
import org.moneyballfm.moneyball_fm.model.DepthChartRowModel;
import org.moneyballfm.moneyball_fm.model.LoanPlayerModel;
import org.moneyballfm.moneyball_fm.model.Player;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.PositionNeedModel;
import org.moneyballfm.moneyball_fm.model.ScoutingPlayerModel;
import org.moneyballfm.moneyball_fm.model.ShadowSquadRowModel;
import org.moneyballfm.moneyball_fm.model.YouthPlayerModel;
import org.moneyballfm.moneyball_fm.parser.FmExportParser;
import org.moneyballfm.moneyball_fm.service.LoanAnalysisService;
import org.moneyballfm.moneyball_fm.service.MoneyballCalculator;
import org.moneyballfm.moneyball_fm.service.SalaryCalculationService;
import org.moneyballfm.moneyball_fm.service.ScoutingAnalysisService;
import org.moneyballfm.moneyball_fm.service.ShadowSquadService;
import org.moneyballfm.moneyball_fm.service.SquadAnalysisService;
import org.moneyballfm.moneyball_fm.service.YouthAnalysisService;

import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainController {

	@FXML private VBox rootPane;
	@FXML private TabPane mainTabPane;
	@FXML private Label statusLabel;
	@FXML private TextField txtMyClub;

	// Zeitstempel-Label oben rechts
	@FXML private Label lblImportTimestamp;

	// TAB 1: KPI-Header (Kader-Analyse)
	@FXML private Label lblSquadAvgAge;
	@FXML private Label lblSquadAvgAthletics;
	@FXML private Label lblSquadTotalWage;
	@FXML private Label lblSquadTotalMarketValue;
	@FXML private Label lblSquadAvgMinutes;

	// TAB 1: Spieler-Tabelle
	@FXML private TableView<PlayerRowModel> playerTable;
	@FXML private TableColumn<PlayerRowModel, String> colName;
	@FXML private TableColumn<PlayerRowModel, String> colRole;
	@FXML private TableColumn<PlayerRowModel, Boolean> colOnLoan;
	@FXML private TableColumn<PlayerRowModel, Integer> colAge;

	@FXML private TableColumn<PlayerRowModel, Integer> colAgility;
	@FXML private TableColumn<PlayerRowModel, Integer> colPace;
	@FXML private TableColumn<PlayerRowModel, Integer> colStamina;
	@FXML private TableColumn<PlayerRowModel, Integer> colDetermination;

	@FXML private TableColumn<PlayerRowModel, Long> colMarketValue;
	@FXML private TableColumn<PlayerRowModel, Long> colFairMarketValue;
	@FXML private TableColumn<PlayerRowModel, Long> colSalary;
	@FXML private TableColumn<PlayerRowModel, Long> colMaxSalary;

	@FXML private TableColumn<PlayerRowModel, Integer> colMinutes;
	@FXML private TableColumn<PlayerRowModel, Double> colStat1;
	@FXML private TableColumn<PlayerRowModel, Double> colStat2;
	@FXML private TableColumn<PlayerRowModel, Double> colStat3;
	@FXML private TableColumn<PlayerRowModel, Double> colScore;
	@FXML private TableColumn<PlayerRowModel, String> colTier;
	@FXML private TableColumn<PlayerRowModel, String> colSalaryCheck;
	@FXML private TableColumn<PlayerRowModel, String> colQuality;

	// TAB 2: Positions-Bedarf
	@FXML private Label lblCriticalNeeds;
	@FXML private Label lblWarningNeeds;
	@FXML private Label lblOptimalNeeds;

	@FXML private TableView<PositionNeedModel> needsTable;
	@FXML private TableColumn<PositionNeedModel, String> colNeedSlot;
	@FXML private TableColumn<PositionNeedModel, String> colNeedRole;
	@FXML private TableColumn<PositionNeedModel, Integer> colNeedCount;
	@FXML private TableColumn<PositionNeedModel, String> colNeedBestPlayer;
	@FXML private TableColumn<PositionNeedModel, Double> colNeedBestScore;
	@FXML private TableColumn<PositionNeedModel, Double> colNeedAvgScore;
	@FXML private TableColumn<PositionNeedModel, Double> colNeedAvgAge;
	@FXML private TableColumn<PositionNeedModel, Integer> colNeedCritContracts;
	@FXML private TableColumn<PositionNeedModel, String> colNeedDiagnosis;
	@FXML private TableColumn<PositionNeedModel, String> colNeedRecommendation;
	@FXML private TableColumn<PositionNeedModel, String> colNeedTargetProfile;

	// TAB 3: Gehalts-Kalkulator
	@FXML private ComboBox<PlayerRowModel> cmbCalcPlayer;
	@FXML private TextField txtCalcCustomName;
	@FXML private TextField txtCalcScore;
	@FXML private TextField txtCalcDemand;
	@FXML private VBox boxCalcAssessment;
	@FXML private Label lblCalcAssessment;
	@FXML private Label lblCalcTier;
	@FXML private Label lblCalcStartingOffer;
	@FXML private Label lblCalcTargetSalary;
	@FXML private Label lblCalcMaxSalary;

	// TAB 4: Tiefenchart & Kader-Hierarchie
	@FXML private TableView<DepthChartRowModel> depthChartTable;
	@FXML private TableColumn<DepthChartRowModel, String> colDepthRole;
	@FXML private TableColumn<DepthChartRowModel, Integer> colDepthRank;
	@FXML private TableColumn<DepthChartRowModel, String> colDepthPlayerName;
	@FXML private TableColumn<DepthChartRowModel, String> colDepthScore;
	@FXML private TableColumn<DepthChartRowModel, String> colDepthTeamRole;
	@FXML private TableColumn<DepthChartRowModel, String> colDepthAge;
	@FXML private TableColumn<DepthChartRowModel, String> colDepthSalary;

	// TAB 5: Leihspieler-Entscheidung (Aufgeteilt)
	@FXML private Label lblLoanReturnCount;
	@FXML private Label lblLoanExtendCount;
	@FXML private Label lblLoanSellCount;

	// Tabelle 1: Eigene Verliehene (Outgoing)
	@FXML private TableView<LoanPlayerModel> outgoingLoanTable;
	@FXML private TableColumn<LoanPlayerModel, String> colOutLoanName;
	@FXML private TableColumn<LoanPlayerModel, String> colOutLoanRole;
	@FXML private TableColumn<LoanPlayerModel, Integer> colOutLoanAge;
	@FXML private TableColumn<LoanPlayerModel, String> colOutLoanClub;
	@FXML private TableColumn<LoanPlayerModel, Integer> colOutLoanGames;
	@FXML private TableColumn<LoanPlayerModel, Integer> colOutLoanMinutes;
	@FXML private TableColumn<LoanPlayerModel, Double> colOutLoanRating;
	@FXML private TableColumn<LoanPlayerModel, Double> colOutLoanScore;
	@FXML private TableColumn<LoanPlayerModel, Boolean> colOutLoanClause;
	@FXML private TableColumn<LoanPlayerModel, Long> colOutLoanMarketValue;
	@FXML private TableColumn<LoanPlayerModel, String> colOutLoanRecommendation;
	@FXML private TableColumn<LoanPlayerModel, String> colOutLoanFuturePlan;

	// Tabelle 2: Von anderen Geliehene (Incoming)
	@FXML private TableView<LoanPlayerModel> incomingLoanTable;
	@FXML private TableColumn<LoanPlayerModel, String> colInLoanName;
	@FXML private TableColumn<LoanPlayerModel, String> colInLoanRole;
	@FXML private TableColumn<LoanPlayerModel, Integer> colInLoanAge;
	@FXML private TableColumn<LoanPlayerModel, String> colInLoanClub;
	@FXML private TableColumn<LoanPlayerModel, Integer> colInLoanGames;
	@FXML private TableColumn<LoanPlayerModel, Integer> colInLoanMinutes;
	@FXML private TableColumn<LoanPlayerModel, Double> colInLoanRating;
	@FXML private TableColumn<LoanPlayerModel, Double> colInLoanScore;
	@FXML private TableColumn<LoanPlayerModel, Boolean> colInLoanClause;
	@FXML private TableColumn<LoanPlayerModel, Long> colInLoanMarketValue;
	@FXML private TableColumn<LoanPlayerModel, String> colInLoanRecommendation;
	@FXML private TableColumn<LoanPlayerModel, String> colInLoanFuturePlan;

	// TAB 6: Jugend & Talent-Pipeline
	@FXML private Label lblYouthTopCount;
	@FXML private Label lblYouthLoanCount;
	@FXML private Label lblYouthDevelopCount;
	@FXML private Label lblYouthSellCount;

	@FXML private TableView<YouthPlayerModel> youthTable;
	@FXML private TableColumn<YouthPlayerModel, String> colYouthName;
	@FXML private TableColumn<YouthPlayerModel, String> colYouthRole;
	@FXML private TableColumn<YouthPlayerModel, Integer> colYouthAge;
	@FXML private TableColumn<YouthPlayerModel, Double> colYouthPotentialStars;
	@FXML private TableColumn<YouthPlayerModel, Double> colYouthCurrentStars;
	@FXML private TableColumn<YouthPlayerModel, Integer> colYouthAgility;
	@FXML private TableColumn<YouthPlayerModel, Integer> colYouthPace;
	@FXML private TableColumn<YouthPlayerModel, Integer> colYouthStamina;
	@FXML private TableColumn<YouthPlayerModel, Integer> colYouthDetermination;
	@FXML private TableColumn<YouthPlayerModel, Double> colYouthStat1;
	@FXML private TableColumn<YouthPlayerModel, Double> colYouthStat2;
	@FXML private TableColumn<YouthPlayerModel, Double> colYouthStat3;
	@FXML private TableColumn<YouthPlayerModel, Double> colYouthScore;
	@FXML private TableColumn<YouthPlayerModel, String> colYouthForecast;
	@FXML private TableColumn<YouthPlayerModel, String> colYouthRecommendation;
	@FXML private TableColumn<YouthPlayerModel, Long> colYouthMaxSalary;

	// TAB 7: Moneyball Scouting
	@FXML private Label lblScoutSignCount;
	@FXML private Label lblScoutBargainCount;
	@FXML private Label lblScoutRejectCount;

	@FXML private TableView<ScoutingPlayerModel> scoutTable;
	@FXML private TableColumn<ScoutingPlayerModel, String> colScoutName;
	@FXML private TableColumn<ScoutingPlayerModel, String> colScoutRole;
	@FXML private TableColumn<ScoutingPlayerModel, Integer> colScoutAge;
	@FXML private TableColumn<ScoutingPlayerModel, Long> colScoutAskingPrice;
	@FXML private TableColumn<ScoutingPlayerModel, Long> colScoutWageDemand;
	@FXML private TableColumn<ScoutingPlayerModel, Double> colScoutContractYears;
	@FXML private TableColumn<ScoutingPlayerModel, Integer> colScoutAgility;
	@FXML private TableColumn<ScoutingPlayerModel, Integer> colScoutPace;
	@FXML private TableColumn<ScoutingPlayerModel, Integer> colScoutStamina;
	@FXML private TableColumn<ScoutingPlayerModel, Double> colScoutStat1;
	@FXML private TableColumn<ScoutingPlayerModel, Double> colScoutStat2;
	@FXML private TableColumn<ScoutingPlayerModel, Double> colScoutStat3;
	@FXML private TableColumn<ScoutingPlayerModel, Double> colScoutScore;
	@FXML private TableColumn<ScoutingPlayerModel, Long> colScoutMaxFee;
	@FXML private TableColumn<ScoutingPlayerModel, Long> colScoutMaxWage;
	@FXML private TableColumn<ScoutingPlayerModel, String> colScoutPriceCheck;
	@FXML private TableColumn<ScoutingPlayerModel, String> colScoutAction;

	// TAB 8: Schattenkader
	@FXML private Label lblShadowTotalProfit;
	@FXML private Label lblShadowTotalSavings;
	@FXML private Label lblShadowCoveredSlots;

	@FXML private TableView<ShadowSquadRowModel> shadowSquadTable;
	@FXML private TableColumn<ShadowSquadRowModel, String> colShadowRole;
	@FXML private TableColumn<ShadowSquadRowModel, String> colShadowStarter;
	@FXML private TableColumn<ShadowSquadRowModel, Long> colShadowStarterSalary;
	@FXML private TableColumn<ShadowSquadRowModel, Long> colShadowStarterValue;
	@FXML private TableColumn<ShadowSquadRowModel, String> colShadowC1Name;
	@FXML private TableColumn<ShadowSquadRowModel, Long> colShadowC1Fee;
	@FXML private TableColumn<ShadowSquadRowModel, Long> colShadowC1Salary;
	@FXML private TableColumn<ShadowSquadRowModel, String> colShadowC2Name;
	@FXML private TableColumn<ShadowSquadRowModel, Long> colShadowC2Fee;
	@FXML private TableColumn<ShadowSquadRowModel, Long> colShadowC2Salary;
	@FXML private TableColumn<ShadowSquadRowModel, Long> colShadowNetGain;
	@FXML private TableColumn<ShadowSquadRowModel, Long> colShadowWageSavings;

	private final ObservableList<PlayerRowModel> playerDataList = FXCollections.observableArrayList();
	private final ObservableList<PositionNeedModel> needsDataList = FXCollections.observableArrayList();
	private final ObservableList<DepthChartRowModel> depthChartDataList = FXCollections.observableArrayList();
	private final ObservableList<LoanPlayerModel> outgoingLoanDataList = FXCollections.observableArrayList();
	private final ObservableList<LoanPlayerModel> incomingLoanDataList = FXCollections.observableArrayList();
	private final ObservableList<YouthPlayerModel> youthDataList = FXCollections.observableArrayList();
	private final ObservableList<ScoutingPlayerModel> scoutDataList = FXCollections.observableArrayList();
	private final ObservableList<ShadowSquadRowModel> shadowSquadDataList = FXCollections.observableArrayList();

	private AppConfig appConfig;
	private final NumberFormat euroFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	@FXML
	public void initialize() {
		appConfig = ConfigLoader.loadConfig();

		if (txtMyClub != null) {
			String defaultClub = appConfig.getMyClubName() != null ? appConfig.getMyClubName() : "Villarreal";
			txtMyClub.setText(defaultClub);
			txtMyClub.textProperty().addListener((obs, oldV, newV) -> {
				if (newV != null) {
					appConfig.setMyClubName(newV.trim());
				}
			});
		}

		setupPlayerTableColumns();
		setupDepthChartTableColumns();
		setupNeedsTableColumns();
		setupSalaryCalculator();
		setupLoanTableColumns();
		setupYouthTableColumns();
		setupScoutTableColumns();
		setupShadowSquadTableColumns();
		setupDragAndDrop();

		playerTable.setItems(playerDataList);
		playerTable.setEditable(true);

		needsTable.setItems(needsDataList);
		if (depthChartTable != null) depthChartTable.setItems(depthChartDataList);
		if (outgoingLoanTable != null) outgoingLoanTable.setItems(outgoingLoanDataList);
		if (incomingLoanTable != null) incomingLoanTable.setItems(incomingLoanDataList);
		if (youthTable != null) youthTable.setItems(youthDataList);
		if (scoutTable != null) scoutTable.setItems(scoutDataList);
		if (shadowSquadTable != null) {
			shadowSquadTable.setItems(shadowSquadDataList);
			shadowSquadTable.setEditable(true);
		}

		// Zuletzt gespeicherten Stand direkt aus H2 laden (inkl. Scouting)
		String lastImportDate = HistoryRepository.loadLatestSnapshot(
				playerDataList,
				outgoingLoanDataList,
				incomingLoanDataList,
				youthDataList,
				scoutDataList
		);

		if (!playerDataList.isEmpty() || !youthDataList.isEmpty() || !outgoingLoanDataList.isEmpty() || !scoutDataList.isEmpty()) {
			// 1. A-Kader durchrechnen
			for (PlayerRowModel row : playerDataList) {
				MoneyballCalculator.recalculate(row, appConfig);
			}

			// 2. Leihspieler durchrechnen
			for (LoanPlayerModel l : outgoingLoanDataList) {
				LoanAnalysisService.evaluate(l);
			}
			for (LoanPlayerModel l : incomingLoanDataList) {
				LoanAnalysisService.evaluate(l);
			}

			// 3. Talente durchrechnen (Prognose & Handlungsempfehlung wiederherstellen)
			for (YouthPlayerModel y : youthDataList) {
				YouthAnalysisService.evaluate(y);
			}

			// 4. Scouting-Kandidaten durchrechnen
			for (ScoutingPlayerModel s : scoutDataList) {
				ScoutingAnalysisService.evaluate(s, appConfig);
			}

			refreshSquadNeeds();
			refreshLoanKPIs();
			refreshYouthKPIs();
			refreshScoutKPIs();

			if (statusLabel != null) {
				statusLabel.setText("Stand aus H2 geladen (" + playerDataList.size() + " Kader, " + youthDataList.size() + " Talente, " + scoutDataList.size() + " Scouts).");
			}

			if (lblImportTimestamp != null && lastImportDate != null) {
				lblImportTimestamp.setText("Importiert am: " + lastImportDate);
			}
		}

		refreshShadowSquad();
		refreshSquadKPIs();
	}

	private void autoSaveToDatabase() {
		String club = (txtMyClub != null && !txtMyClub.getText().isBlank()) ? txtMyClub.getText().trim() : "Villarreal";
		HistoryRepository.persistCurrentState(
				club,
				playerDataList,
				outgoingLoanDataList,
				incomingLoanDataList,
				youthDataList,
				scoutDataList
		);
	}

	private void setupDepthChartTableColumns() {
		if (depthChartTable == null) return;
		colDepthRole.setCellValueFactory(cell -> cell.getValue().tacticalRoleProperty());
		colDepthRank.setCellValueFactory(cell -> cell.getValue().rankProperty().asObject());
		colDepthPlayerName.setCellValueFactory(cell -> cell.getValue().playerNameProperty());
		colDepthScore.setCellValueFactory(cell -> cell.getValue().moneyballScoreProperty());
		colDepthTeamRole.setCellValueFactory(cell -> cell.getValue().teamRoleProperty());
		colDepthAge.setCellValueFactory(cell -> cell.getValue().ageProperty());
		colDepthSalary.setCellValueFactory(cell -> cell.getValue().monthlySalaryProperty());
	}

	private void setupSalaryCalculator() {
		if (cmbCalcPlayer != null) {
			cmbCalcPlayer.setItems(playerDataList);
			cmbCalcPlayer.setConverter(new StringConverter<>() {
				@Override
				public String toString(PlayerRowModel player) {
					if (player == null) return "";
					return player.nameProperty().get() + " (" + player.roleProperty().get() + " | Score: " + player.moneyballScoreProperty().get() + ")";
				}

				@Override
				public PlayerRowModel fromString(String string) {
					return null;
				}
			});

			cmbCalcPlayer.valueProperty().addListener((obs, oldVal, newVal) -> {
				if (newVal != null) {
					if (txtCalcCustomName != null) txtCalcCustomName.setText(newVal.nameProperty().get());
					if (txtCalcScore != null) txtCalcScore.setText(String.valueOf(newVal.moneyballScoreProperty().get()));
					if (txtCalcDemand != null) txtCalcDemand.setText(String.valueOf(newVal.monthlySalaryProperty().get()));
					onCalculateSalary();
				}
			});
		}

		if (txtCalcScore != null) {
			txtCalcScore.textProperty().addListener((obs, oldV, newV) -> onCalculateSalary());
		}
		if (txtCalcDemand != null) {
			txtCalcDemand.textProperty().addListener((obs, oldV, newV) -> onCalculateSalary());
		}
	}

	@FXML
	public void onCalculateSalary() {
		try {
			double score = 0.0;
			if (txtCalcScore != null && !txtCalcScore.getText().isBlank()) {
				score = Double.parseDouble(txtCalcScore.getText().trim().replace(",", "."));
			}

			long demand = 0L;
			if (txtCalcDemand != null && !txtCalcDemand.getText().isBlank()) {
				String clean = txtCalcDemand.getText().replaceAll("[^0-9]", "");
				if (!clean.isEmpty()) demand = Long.parseLong(clean);
			}

			SalaryCalculationService.SalaryOfferRecommendation rec =
					SalaryCalculationService.calculateOffer(score, demand, appConfig);

			if (lblCalcTier != null) lblCalcTier.setText(rec.tierId());
			if (lblCalcStartingOffer != null) lblCalcStartingOffer.setText("€ " + euroFormat.format(rec.suggestedStartingOffer()) + " / Mon.");
			if (lblCalcTargetSalary != null) lblCalcTargetSalary.setText("€ " + euroFormat.format(rec.fairTargetSalary()) + " / Mon.");
			if (lblCalcMaxSalary != null) lblCalcMaxSalary.setText("€ " + euroFormat.format(rec.maxWalkAwaySalary()) + " / Mon.");

			if (lblCalcAssessment != null) {
				lblCalcAssessment.setText(rec.assessment());
				lblCalcAssessment.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + rec.statusColorHex() + ";");
			}
		} catch (Exception ignored) {}
	}

	@FXML
	public void onResetSalaryCalc() {
		if (cmbCalcPlayer != null) cmbCalcPlayer.setValue(null);
		if (txtCalcCustomName != null) txtCalcCustomName.clear();
		if (txtCalcScore != null) txtCalcScore.clear();
		if (txtCalcDemand != null) txtCalcDemand.clear();
		if (lblCalcTier != null) lblCalcTier.setText("-");
		if (lblCalcStartingOffer != null) lblCalcStartingOffer.setText("€ 0 / Mon.");
		if (lblCalcTargetSalary != null) lblCalcTargetSalary.setText("€ 0 / Mon.");
		if (lblCalcMaxSalary != null) lblCalcMaxSalary.setText("€ 0 / Mon.");
		if (lblCalcAssessment != null) {
			lblCalcAssessment.setText("Bereit für Verhandlung");
			lblCalcAssessment.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");
		}
	}

	private void setupPlayerTableColumns() {
		colName.setCellValueFactory(cell -> cell.getValue().nameProperty());
		colName.setCellFactory(TextFieldTableCell.forTableColumn());
		colName.setOnEditCommit(e -> {
			e.getRowValue().nameProperty().set(e.getNewValue());
			autoSaveToDatabase();
		});

		colRole.setCellValueFactory(cell -> cell.getValue().roleProperty());
		colRole.setCellFactory(ComboBoxTableCell.forTableColumn(getAvailableRoles()));
		colRole.setOnEditCommit(e -> {
			e.getRowValue().roleProperty().set(e.getNewValue());
			recalc(e.getRowValue());
		});

		if (colOnLoan != null) {
			colOnLoan.setCellValueFactory(cell -> cell.getValue().onLoanProperty());
			colOnLoan.setCellFactory(ComboBoxTableCell.forTableColumn(true, false));
			colOnLoan.setOnEditCommit(e -> {
				e.getRowValue().onLoanProperty().set(e.getNewValue());
				syncIncomingLoans();
				refreshSquadKPIs();
				autoSaveToDatabase();
			});
		}

		colAge.setCellValueFactory(cell -> cell.getValue().ageProperty().asObject());
		colAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colAge.setOnEditCommit(e -> {
			e.getRowValue().ageProperty().set(e.getNewValue());
			recalc(e.getRowValue());
		});

		colAgility.setCellValueFactory(cell -> cell.getValue().agilityProperty().asObject());
		colAgility.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colAgility.setOnEditCommit(e -> {
			e.getRowValue().agilityProperty().set(e.getNewValue());
			refreshSquadKPIs();
			autoSaveToDatabase();
		});

		colPace.setCellValueFactory(cell -> cell.getValue().paceProperty().asObject());
		colPace.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colPace.setOnEditCommit(e -> {
			e.getRowValue().paceProperty().set(e.getNewValue());
			refreshSquadKPIs();
			autoSaveToDatabase();
		});

		colStamina.setCellValueFactory(cell -> cell.getValue().staminaProperty().asObject());
		colStamina.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colStamina.setOnEditCommit(e -> {
			e.getRowValue().staminaProperty().set(e.getNewValue());
			refreshSquadKPIs();
			autoSaveToDatabase();
		});

		if (colDetermination != null) {
			colDetermination.setCellValueFactory(cell -> cell.getValue().determinationProperty().asObject());
			colDetermination.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			colDetermination.setOnEditCommit(e -> {
				e.getRowValue().determinationProperty().set(e.getNewValue());
				autoSaveToDatabase();
			});
		}

		StringConverter<Long> euroStringConverter = createEuroConverter();

		if (colMarketValue != null) {
			colMarketValue.setCellValueFactory(cell -> cell.getValue().marketValueProperty().asObject());
			colMarketValue.setCellFactory(TextFieldTableCell.forTableColumn(euroStringConverter));
			colMarketValue.setOnEditCommit(e -> {
				e.getRowValue().marketValueProperty().set(e.getNewValue());
				recalc(e.getRowValue());
			});
		}

		if (colFairMarketValue != null) {
			colFairMarketValue.setCellValueFactory(cell -> cell.getValue().fairMarketValueProperty().asObject());
			colFairMarketValue.setCellFactory(TextFieldTableCell.forTableColumn(euroStringConverter));
			colFairMarketValue.setEditable(false);
		}

		if (colSalary != null) {
			colSalary.setCellValueFactory(cell -> cell.getValue().monthlySalaryProperty().asObject());
			colSalary.setCellFactory(TextFieldTableCell.forTableColumn(euroStringConverter));
			colSalary.setOnEditCommit(e -> {
				e.getRowValue().monthlySalaryProperty().set(e.getNewValue());
				recalc(e.getRowValue());
			});
		}

		if (colMaxSalary != null) {
			colMaxSalary.setCellValueFactory(cell -> cell.getValue().maxSalaryProperty().asObject());
			colMaxSalary.setCellFactory(TextFieldTableCell.forTableColumn(euroStringConverter));
			colMaxSalary.setEditable(false);
		}

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

	private void setupNeedsTableColumns() {
		colNeedSlot.setCellValueFactory(cell -> cell.getValue().slotNameProperty());
		colNeedRole.setCellValueFactory(cell -> cell.getValue().roleLabelProperty());
		colNeedCount.setCellValueFactory(cell -> cell.getValue().playerCountProperty().asObject());
		colNeedBestPlayer.setCellValueFactory(cell -> cell.getValue().bestPlayerNameProperty());
		colNeedBestScore.setCellValueFactory(cell -> cell.getValue().bestScoreProperty().asObject());
		colNeedAvgScore.setCellValueFactory(cell -> cell.getValue().averageScoreProperty().asObject());
		colNeedAvgAge.setCellValueFactory(cell -> cell.getValue().averageAgeProperty().asObject());
		colNeedCritContracts.setCellValueFactory(cell -> cell.getValue().criticalContractsProperty().asObject());
		colNeedDiagnosis.setCellValueFactory(cell -> cell.getValue().diagnosisProperty());
		colNeedRecommendation.setCellValueFactory(cell -> cell.getValue().recommendationProperty());
		colNeedTargetProfile.setCellValueFactory(cell -> cell.getValue().targetProfileProperty());
	}

	private void setupLoanTableColumns() {
		StringConverter<Long> euroConv = createEuroConverter();

		if (outgoingLoanTable != null) {
			bindLoanColumns(
					colOutLoanName, colOutLoanRole, colOutLoanAge, colOutLoanClub,
					colOutLoanGames, colOutLoanMinutes, colOutLoanRating, colOutLoanScore,
					colOutLoanClause, colOutLoanMarketValue, colOutLoanRecommendation, colOutLoanFuturePlan,
					euroConv
			);
		}

		if (incomingLoanTable != null) {
			bindLoanColumns(
					colInLoanName, colInLoanRole, colInLoanAge, colInLoanClub,
					colInLoanGames, colInLoanMinutes, colInLoanRating, colInLoanScore,
					colInLoanClause, colInLoanMarketValue, colInLoanRecommendation, colInLoanFuturePlan,
					euroConv
			);
		}
	}

	private void bindLoanColumns(
			TableColumn<LoanPlayerModel, String> colName,
			TableColumn<LoanPlayerModel, String> colRole,
			TableColumn<LoanPlayerModel, Integer> colAge,
			TableColumn<LoanPlayerModel, String> colClub,
			TableColumn<LoanPlayerModel, Integer> colGames,
			TableColumn<LoanPlayerModel, Integer> colMinutes,
			TableColumn<LoanPlayerModel, Double> colRating,
			TableColumn<LoanPlayerModel, Double> colScore,
			TableColumn<LoanPlayerModel, Boolean> colClause,
			TableColumn<LoanPlayerModel, Long> colMarketValue,
			TableColumn<LoanPlayerModel, String> colRecommendation,
			TableColumn<LoanPlayerModel, String> colFuturePlan,
			StringConverter<Long> euroConv) {

		if (colName == null) return;

		colName.setCellValueFactory(cell -> cell.getValue().playerNameProperty());
		colName.setCellFactory(TextFieldTableCell.forTableColumn());
		colName.setOnEditCommit(e -> {
			e.getRowValue().playerNameProperty().set(e.getNewValue());
			autoSaveToDatabase();
		});

		if (colRole != null) {
			colRole.setCellValueFactory(cell -> cell.getValue().targetRoleProperty());
			colRole.setCellFactory(ComboBoxTableCell.forTableColumn(getAvailableRoles()));
			colRole.setOnEditCommit(e -> {
				e.getRowValue().targetRoleProperty().set(e.getNewValue());
				autoSaveToDatabase();
			});
		}

		if (colAge != null) {
			colAge.setCellValueFactory(cell -> cell.getValue().ageProperty().asObject());
			colAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			colAge.setOnEditCommit(e -> {
				e.getRowValue().ageProperty().set(e.getNewValue());
				recalcLoan(e.getRowValue());
			});
		}

		if (colClub != null) {
			colClub.setCellValueFactory(cell -> cell.getValue().clubAndLeagueProperty());
			colClub.setCellFactory(TextFieldTableCell.forTableColumn());
			colClub.setOnEditCommit(e -> {
				e.getRowValue().clubAndLeagueProperty().set(e.getNewValue());
				autoSaveToDatabase();
			});
		}

		if (colGames != null) {
			colGames.setCellValueFactory(cell -> cell.getValue().gamesPlayedProperty().asObject());
			colGames.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			colGames.setOnEditCommit(e -> {
				e.getRowValue().gamesPlayedProperty().set(e.getNewValue());
				recalcLoan(e.getRowValue());
			});
		}

		if (colMinutes != null) {
			colMinutes.setCellValueFactory(cell -> cell.getValue().minutesPlayedProperty().asObject());
			colMinutes.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			colMinutes.setOnEditCommit(e -> {
				e.getRowValue().minutesPlayedProperty().set(e.getNewValue());
				recalcLoan(e.getRowValue());
			});
		}

		if (colRating != null) {
			colRating.setCellValueFactory(cell -> cell.getValue().averageRatingProperty().asObject());
			colRating.setCellFactory(col -> new TableCell<>() {
				private final TextField textField = new TextField();

				{
					textField.setOnAction(e -> commitEditHelper());
					textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
						if (!isFocused && isEditing()) commitEditHelper();
					});
				}

				private void commitEditHelper() {
					try {
						double val = Double.parseDouble(textField.getText().trim().replace(",", "."));
						commitEdit(val);
					} catch (Exception ex) {
						cancelEdit();
					}
				}

				@Override
				public void startEdit() {
					LoanPlayerModel row = getTableRow() != null ? getTableRow().getItem() : null;
					if (row != null && row.getLoanType() == LoanPlayerModel.LoanType.INCOMING) return;
					super.startEdit();
					textField.setText(getItem() != null && getItem() > 0 ? String.valueOf(getItem()) : "");
					setText(null);
					setGraphic(textField);
					textField.selectAll();
					textField.requestFocus();
				}

				@Override
				public void cancelEdit() {
					super.cancelEdit();
					setGraphic(null);
					updateText(getItem());
				}

				@Override
				protected void updateItem(Double rating, boolean empty) {
					super.updateItem(rating, empty);
					if (empty) {
						setText(null);
						setGraphic(null);
					} else if (isEditing()) {
						setText(null);
						setGraphic(textField);
					} else {
						setGraphic(null);
						updateText(rating);
					}
				}

				private void updateText(Double rating) {
					LoanPlayerModel row = getTableRow() != null ? getTableRow().getItem() : null;
					if (row != null && row.getLoanType() == LoanPlayerModel.LoanType.INCOMING) {
						setText("—");
					} else {
						setText(rating != null && rating > 0 ? String.format(Locale.GERMANY, "%.2f", rating) : "—");
					}
				}
			});
			colRating.setOnEditCommit(e -> {
				e.getRowValue().averageRatingProperty().set(e.getNewValue());
				recalcLoan(e.getRowValue());
			});
		}

		if (colScore != null) {
			colScore.setCellValueFactory(cell -> cell.getValue().moneyballScoreProperty().asObject());
			colScore.setEditable(false);
		}

		if (colClause != null) {
			colClause.setCellValueFactory(cell -> cell.getValue().clauseFulfilledProperty());
			colClause.setCellFactory(ComboBoxTableCell.forTableColumn(true, false));
			colClause.setOnEditCommit(e -> {
				e.getRowValue().clauseFulfilledProperty().set(e.getNewValue());
				recalcLoan(e.getRowValue());
			});
		}

		if (colMarketValue != null) {
			colMarketValue.setCellValueFactory(cell -> cell.getValue().marketValueProperty().asObject());
			colMarketValue.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
			colMarketValue.setOnEditCommit(e -> {
				e.getRowValue().marketValueProperty().set(e.getNewValue());
				autoSaveToDatabase();
			});
		}

		if (colRecommendation != null) {
			colRecommendation.setCellValueFactory(cell -> cell.getValue().recommendationProperty());
		}

		if (colFuturePlan != null) {
			colFuturePlan.setCellValueFactory(cell -> cell.getValue().futurePlanProperty());
		}
	}

	private void recalcLoan(LoanPlayerModel player) {
		LoanAnalysisService.evaluate(player);
		refreshLoanKPIs();
		autoSaveToDatabase();
	}

	private void refreshLoanKPIs() {
		long returnCount = 0;
		long extendCount = 0;
		long sellCount = 0;

		for (LoanPlayerModel l : outgoingLoanDataList) {
			String rec = l.recommendationProperty().get();
			if (rec != null) {
				if (rec.contains("KADER-RÜCKKEHR") || rec.contains("KAUFOPTION")) returnCount++;
				if (rec.contains("WEITER VERLEIHEN") || rec.contains("LEIHE VERLÄNGERN")) extendCount++;
				if (rec.contains("VERKAUFEN") || rec.contains("LEIH-ENDE")) sellCount++;
			}
		}
		for (LoanPlayerModel l : incomingLoanDataList) {
			String rec = l.recommendationProperty().get();
			if (rec != null) {
				if (rec.contains("KADER-RÜCKKEHR") || rec.contains("KAUFOPTION")) returnCount++;
				if (rec.contains("WEITER VERLEIHEN") || rec.contains("LEIHE VERLÄNGERN")) extendCount++;
				if (rec.contains("VERKAUFEN") || rec.contains("LEIH-ENDE")) sellCount++;
			}
		}

		if (lblLoanReturnCount != null) lblLoanReturnCount.setText(returnCount + " Spieler");
		if (lblLoanExtendCount != null) lblLoanExtendCount.setText(extendCount + " Spieler");
		if (lblLoanSellCount != null) lblLoanSellCount.setText(sellCount + " Spieler");
	}

	@FXML
	public void onAddLoanPlayerManual() {
		LoanPlayerModel p = new LoanPlayerModel(
				LoanPlayerModel.LoanType.OUTGOING,
				"Neuer Leihspieler",
				"Stoßstürmer (A)",
				20,
				"Partnerverein (2. Liga)",
				18,
				1450,
				7.12,
				0.0,
				2500000L,
				true
		);
		LoanAnalysisService.evaluate(p);
		outgoingLoanDataList.add(p);
		refreshLoanKPIs();
		autoSaveToDatabase();
		if (outgoingLoanTable != null) outgoingLoanTable.scrollTo(p);
	}

	private void syncIncomingLoans() {
		incomingLoanDataList.clear();

		for (PlayerRowModel p : playerDataList) {
			if (p.isOnLoan()) {
				LoanPlayerModel incoming = new LoanPlayerModel(
						LoanPlayerModel.LoanType.INCOMING,
						p.nameProperty().get(),
						p.roleProperty().get(),
						p.ageProperty().get(),
						"Eigener Verein (Leihgabe)",
						p.minutesPlayedProperty().get() > 0 ? (p.minutesPlayedProperty().get() / 80) : 0,
						p.minutesPlayedProperty().get(),
						0.0,
						p.moneyballScoreProperty().get(),
						p.marketValueProperty().get(),
						true
				);
				LoanAnalysisService.evaluate(incoming);
				incomingLoanDataList.add(incoming);
			}
		}
		refreshLoanKPIs();
	}

	// --- TAB 6: JUGEND & TALENT-PIPELINE SETUP ---
	private void setupYouthTableColumns() {
		if (youthTable == null) return;

		ObservableList<Double> starOptions = FXCollections.observableArrayList(
				0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0
		);

		StringConverter<Double> starConverter = new StringConverter<>() {
			@Override
			public String toString(Double val) {
				return (val == null || val <= 0.0) ? "—" : String.format(Locale.GERMANY, "%.1f ★", val);
			}

			@Override
			public Double fromString(String str) {
				if (str == null || str.isBlank() || str.equals("—")) return 0.0;
				String clean = str.replace("★", "").trim().replace(",", ".");
				try { return Double.parseDouble(clean); } catch (Exception e) { return 0.0; }
			}
		};

		colYouthName.setCellValueFactory(cell -> cell.getValue().nameProperty());
		colYouthName.setCellFactory(TextFieldTableCell.forTableColumn());
		colYouthName.setOnEditCommit(e -> {
			e.getRowValue().nameProperty().set(e.getNewValue());
			autoSaveToDatabase();
		});

		colYouthRole.setCellValueFactory(cell -> cell.getValue().targetRoleProperty());
		colYouthRole.setCellFactory(ComboBoxTableCell.forTableColumn(getAvailableRoles()));
		colYouthRole.setOnEditCommit(e -> {
			e.getRowValue().targetRoleProperty().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthAge.setCellValueFactory(cell -> cell.getValue().ageProperty().asObject());
		colYouthAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colYouthAge.setOnEditCommit(e -> {
			e.getRowValue().ageProperty().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthPotentialStars.setCellValueFactory(cell -> cell.getValue().potentialStarsProperty().asObject());
		colYouthPotentialStars.setCellFactory(ComboBoxTableCell.forTableColumn(starConverter, starOptions));
		colYouthPotentialStars.setOnEditCommit(e -> {
			e.getRowValue().potentialStarsProperty().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthCurrentStars.setCellValueFactory(cell -> cell.getValue().currentStarsProperty().asObject());
		colYouthCurrentStars.setCellFactory(ComboBoxTableCell.forTableColumn(starConverter, starOptions));
		colYouthCurrentStars.setOnEditCommit(e -> {
			e.getRowValue().currentStarsProperty().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthAgility.setCellValueFactory(cell -> cell.getValue().agilityProperty().asObject());
		colYouthAgility.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colYouthAgility.setOnEditCommit(e -> {
			e.getRowValue().agilityProperty().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthPace.setCellValueFactory(cell -> cell.getValue().paceProperty().asObject());
		colYouthPace.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colYouthPace.setOnEditCommit(e -> {
			e.getRowValue().paceProperty().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthStamina.setCellValueFactory(cell -> cell.getValue().staminaProperty().asObject());
		colYouthStamina.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colYouthStamina.setOnEditCommit(e -> {
			e.getRowValue().staminaProperty().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthDetermination.setCellValueFactory(cell -> cell.getValue().determinationProperty().asObject());
		colYouthDetermination.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colYouthDetermination.setOnEditCommit(e -> {
			e.getRowValue().determinationProperty().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthStat1.setCellValueFactory(cell -> cell.getValue().statValue1Property().asObject());
		colYouthStat1.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colYouthStat1.setOnEditCommit(e -> {
			e.getRowValue().statValue1Property().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthStat2.setCellValueFactory(cell -> cell.getValue().statValue2Property().asObject());
		colYouthStat2.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colYouthStat2.setOnEditCommit(e -> {
			e.getRowValue().statValue2Property().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthStat3.setCellValueFactory(cell -> cell.getValue().statValue3Property().asObject());
		colYouthStat3.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colYouthStat3.setOnEditCommit(e -> {
			e.getRowValue().statValue3Property().set(e.getNewValue());
			recalcYouth(e.getRowValue());
		});

		colYouthScore.setCellValueFactory(cell -> cell.getValue().talentScoreProperty().asObject());
		colYouthScore.setEditable(false);

		colYouthForecast.setCellValueFactory(cell -> cell.getValue().forecastFirstTeamProperty());
		colYouthRecommendation.setCellValueFactory(cell -> cell.getValue().pipelineRecommendationProperty());

		StringConverter<Long> euroConv = createEuroConverter();
		colYouthMaxSalary.setCellValueFactory(cell -> cell.getValue().maxYouthSalaryProperty().asObject());
		colYouthMaxSalary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colYouthMaxSalary.setEditable(false);
	}

	private void recalcYouth(YouthPlayerModel youth) {
		YouthAnalysisService.evaluate(youth);
		refreshYouthKPIs();
		autoSaveToDatabase();
	}

	private void refreshYouthKPIs() {
		long topCount = youthDataList.stream()
				.filter(y -> y.pipelineRecommendationProperty().get().contains("1. MANNSCHAFT") || y.pipelineRecommendationProperty().get().contains("TOP-TALENT"))
				.count();

		long loanCount = youthDataList.stream()
				.filter(y -> y.pipelineRecommendationProperty().get().contains("VERLEIHEN"))
				.count();

		long developCount = youthDataList.stream()
				.filter(y -> y.pipelineRecommendationProperty().get().contains("HALTEN") || y.pipelineRecommendationProperty().get().contains("ENTWICKELN"))
				.count();

		long sellCount = youthDataList.stream()
				.filter(y -> y.pipelineRecommendationProperty().get().contains("VERKAUF"))
				.count();

		if (lblYouthTopCount != null) lblYouthTopCount.setText(topCount + " Spieler");
		if (lblYouthLoanCount != null) lblYouthLoanCount.setText(loanCount + " Spieler");
		if (lblYouthDevelopCount != null) lblYouthDevelopCount.setText(developCount + " Spieler");
		if (lblYouthSellCount != null) lblYouthSellCount.setText(sellCount + " Spieler");
	}

	@FXML
	public void onAddYouthPlayerManual() {
		YouthPlayerModel y = new YouthPlayerModel(
				"Neues Talent",
				"Stoßstürmer (A)",
				17,
				4.5,
				2.0,
				14,
				14,
				13,
				15,
				0.55,
				0.22,
				0.48
		);
		YouthAnalysisService.evaluate(y);
		youthDataList.add(y);
		refreshYouthKPIs();
		autoSaveToDatabase();
		if (youthTable != null) youthTable.scrollTo(y);
	}

	// --- TAB 7: MONEYBALL SCOUTING SETUP ---
	private void setupScoutTableColumns() {
		if (scoutTable == null) return;

		StringConverter<Long> euroConv = createEuroConverter();

		colScoutName.setCellValueFactory(cell -> cell.getValue().nameProperty());
		colScoutName.setCellFactory(TextFieldTableCell.forTableColumn());
		colScoutName.setOnEditCommit(e -> {
			e.getRowValue().nameProperty().set(e.getNewValue());
			autoSaveToDatabase();
		});

		colScoutRole.setCellValueFactory(cell -> cell.getValue().targetRoleProperty());
		colScoutRole.setCellFactory(ComboBoxTableCell.forTableColumn(getAvailableRoles()));
		colScoutRole.setOnEditCommit(e -> {
			e.getRowValue().targetRoleProperty().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutAge.setCellValueFactory(cell -> cell.getValue().ageProperty().asObject());
		colScoutAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colScoutAge.setOnEditCommit(e -> {
			e.getRowValue().ageProperty().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutAskingPrice.setCellValueFactory(cell -> cell.getValue().askingPriceProperty().asObject());
		colScoutAskingPrice.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colScoutAskingPrice.setOnEditCommit(e -> {
			e.getRowValue().askingPriceProperty().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutWageDemand.setCellValueFactory(cell -> cell.getValue().wageDemandProperty().asObject());
		colScoutWageDemand.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colScoutWageDemand.setOnEditCommit(e -> {
			e.getRowValue().wageDemandProperty().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutContractYears.setCellValueFactory(cell -> cell.getValue().contractYearsProperty().asObject());
		colScoutContractYears.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colScoutContractYears.setOnEditCommit(e -> {
			e.getRowValue().contractYearsProperty().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutAgility.setCellValueFactory(cell -> cell.getValue().agilityProperty().asObject());
		colScoutAgility.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colScoutAgility.setOnEditCommit(e -> {
			e.getRowValue().agilityProperty().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutPace.setCellValueFactory(cell -> cell.getValue().paceProperty().asObject());
		colScoutPace.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colScoutPace.setOnEditCommit(e -> {
			e.getRowValue().paceProperty().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutStamina.setCellValueFactory(cell -> cell.getValue().staminaProperty().asObject());
		colScoutStamina.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colScoutStamina.setOnEditCommit(e -> {
			e.getRowValue().staminaProperty().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutStat1.setCellValueFactory(cell -> cell.getValue().statValue1Property().asObject());
		colScoutStat1.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colScoutStat1.setOnEditCommit(e -> {
			e.getRowValue().statValue1Property().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutStat2.setCellValueFactory(cell -> cell.getValue().statValue2Property().asObject());
		colScoutStat2.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colScoutStat2.setOnEditCommit(e -> {
			e.getRowValue().statValue2Property().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutStat3.setCellValueFactory(cell -> cell.getValue().statValue3Property().asObject());
		colScoutStat3.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colScoutStat3.setOnEditCommit(e -> {
			e.getRowValue().statValue3Property().set(e.getNewValue());
			recalcScout(e.getRowValue());
		});

		colScoutScore.setCellValueFactory(cell -> cell.getValue().scoutingScoreProperty().asObject());
		colScoutScore.setEditable(false);

		colScoutMaxFee.setCellValueFactory(cell -> cell.getValue().maxTransferFeeProperty().asObject());
		colScoutMaxFee.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colScoutMaxFee.setEditable(false);

		colScoutMaxWage.setCellValueFactory(cell -> cell.getValue().maxWageProperty().asObject());
		colScoutMaxWage.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colScoutMaxWage.setEditable(false);

		colScoutPriceCheck.setCellValueFactory(cell -> cell.getValue().priceCheckProperty());
		colScoutAction.setCellValueFactory(cell -> cell.getValue().transferActionProperty());
	}

	private void recalcScout(ScoutingPlayerModel scout) {
		ScoutingAnalysisService.evaluate(scout, appConfig);
		refreshScoutKPIs();
		refreshShadowSquad();
		autoSaveToDatabase();
	}

	private void refreshScoutKPIs() {
		long signCount = scoutDataList.stream()
				.filter(s -> s.transferActionProperty().get() != null && s.transferActionProperty().get().contains("SOFORT VERPFLICHTEN"))
				.count();

		long bargainCount = scoutDataList.stream()
				.filter(s -> s.priceCheckProperty().get() != null && s.priceCheckProperty().get().contains("TOP-SCHNÄPPCHEN"))
				.count();

		long rejectCount = scoutDataList.stream()
				.filter(s -> s.transferActionProperty().get() != null && (s.transferActionProperty().get().contains("ABLEHNEN") || s.transferActionProperty().get().contains("ZU HOCH")))
				.count();

		if (lblScoutSignCount != null) lblScoutSignCount.setText(signCount + " Spieler");
		if (lblScoutBargainCount != null) lblScoutBargainCount.setText(bargainCount + " Spieler");
		if (lblScoutRejectCount != null) lblScoutRejectCount.setText(rejectCount + " Spieler");
	}

	@FXML
	public void onAddScoutPlayerManual() {
		ScoutingPlayerModel s = new ScoutingPlayerModel(
				"Neues Scouting-Ziel",
				"Stoßstürmer (A)",
				21,
				15000000L,
				220000L,
				1.0,
				15,
				14,
				15,
				0.58,
				0.24,
				0.54
		);
		ScoutingAnalysisService.evaluate(s, appConfig);
		scoutDataList.add(s);
		refreshScoutKPIs();
		refreshShadowSquad();
		autoSaveToDatabase();
		if (scoutTable != null) scoutTable.scrollTo(s);
	}

	// --- TAB 8: SCHATTENKADER SETUP ---
	private void setupShadowSquadTableColumns() {
		if (shadowSquadTable == null) return;
		StringConverter<Long> euroConv = createEuroConverter();

		colShadowRole.setCellValueFactory(cell -> cell.getValue().tacticalRoleProperty());
		colShadowStarter.setCellValueFactory(cell -> cell.getValue().starterNameProperty());

		colShadowStarterSalary.setCellValueFactory(cell -> cell.getValue().starterSalaryProperty().asObject());
		colShadowStarterSalary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));

		colShadowStarterValue.setCellValueFactory(cell -> cell.getValue().starterMarketValueProperty().asObject());
		colShadowStarterValue.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));

		colShadowC1Name.setCellValueFactory(cell -> cell.getValue().candidate1NameProperty());
		colShadowC1Fee.setCellValueFactory(cell -> cell.getValue().candidate1FeeProperty().asObject());
		colShadowC1Fee.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colShadowC1Salary.setCellValueFactory(cell -> cell.getValue().candidate1SalaryProperty().asObject());
		colShadowC1Salary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));

		colShadowC2Name.setCellValueFactory(cell -> cell.getValue().candidate2NameProperty());
		colShadowC2Fee.setCellValueFactory(cell -> cell.getValue().candidate2FeeProperty().asObject());
		colShadowC2Fee.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colShadowC2Salary.setCellValueFactory(cell -> cell.getValue().candidate2SalaryProperty().asObject());
		colShadowC2Salary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));

		colShadowNetGain.setCellValueFactory(cell -> cell.getValue().netTransferGainProperty().asObject());
		colShadowNetGain.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));

		colShadowWageSavings.setCellValueFactory(cell -> cell.getValue().monthlyWageSavingsProperty().asObject());
		colShadowWageSavings.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
	}

	private void refreshShadowSquad() {
		List<ShadowSquadRowModel> list = ShadowSquadService.generateShadowSquad(playerDataList, scoutDataList, appConfig);
		shadowSquadDataList.setAll(list);
		refreshShadowKPIsLive();
	}

	private void refreshShadowKPIsLive() {
		long totalProfit = shadowSquadDataList.stream().mapToLong(r -> r.netTransferGainProperty().get()).sum();
		long totalSavings = shadowSquadDataList.stream().mapToLong(r -> r.monthlyWageSavingsProperty().get()).sum();
		long covered = shadowSquadDataList.stream().filter(r -> {
			String c1 = r.candidate1NameProperty().get();
			return c1 != null && !c1.contains("Kein Scout");
		}).count();

		if (lblShadowTotalProfit != null) lblShadowTotalProfit.setText("€ " + euroFormat.format(totalProfit));
		if (lblShadowTotalSavings != null) lblShadowTotalSavings.setText("€ " + euroFormat.format(totalSavings) + " / Mon.");
		if (lblShadowCoveredSlots != null) lblShadowCoveredSlots.setText(covered + " / " + shadowSquadDataList.size());
	}

	@FXML
	public void onAddShadowPlayerManual() {
		ShadowSquadRowModel manualRow = new ShadowSquadRowModel(
				"Stoßstürmer (A)",
				"Manueller Stammspieler",
				500000L,
				40000000L,
				"Manueller Kandidat 1",
				12000000L,
				180000L,
				"— Kein 2. Kandidat —",
				0L,
				0L,
				28000000L,
				320000L
		);
		shadowSquadDataList.add(manualRow);
		refreshShadowKPIsLive();
		if (shadowSquadTable != null) {
			shadowSquadTable.scrollTo(manualRow);
		}
	}

	// --- TAB 1: KADER-KPI BERECHNUNG ---
	private void refreshSquadKPIs() {
		if (playerDataList.isEmpty()) {
			if (lblSquadAvgAge != null) lblSquadAvgAge.setText("0,0");
			if (lblSquadAvgAthletics != null) lblSquadAvgAthletics.setText("0,0");
			if (lblSquadTotalWage != null) lblSquadTotalWage.setText("€ 0 / Mon.");
			if (lblSquadTotalMarketValue != null) lblSquadTotalMarketValue.setText("€ 0");
			if (lblSquadAvgMinutes != null) lblSquadAvgMinutes.setText("0");
			return;
		}

		double avgAge = playerDataList.stream().mapToInt(p -> p.ageProperty().get()).average().orElse(0.0);

		double avgAthletics = playerDataList.stream()
				.mapToDouble(p -> (p.agilityProperty().get() + p.paceProperty().get() + p.staminaProperty().get()) / 3.0)
				.average().orElse(0.0);

		long totalWage = playerDataList.stream().mapToLong(p -> p.monthlySalaryProperty().get()).sum();
		long totalMarketValue = playerDataList.stream().mapToLong(p -> p.marketValueProperty().get()).sum();
		double avgMinutes = playerDataList.stream().mapToInt(p -> p.minutesPlayedProperty().get()).average().orElse(0.0);

		if (lblSquadAvgAge != null) lblSquadAvgAge.setText(String.format(Locale.GERMANY, "%.1f", avgAge));
		if (lblSquadAvgAthletics != null) lblSquadAvgAthletics.setText(String.format(Locale.GERMANY, "%.1f", avgAthletics));
		if (lblSquadTotalWage != null) lblSquadTotalWage.setText("€ " + euroFormat.format(totalWage) + " / Mon.");
		if (lblSquadTotalMarketValue != null) lblSquadTotalMarketValue.setText("€ " + euroFormat.format(totalMarketValue));
		if (lblSquadAvgMinutes != null) lblSquadAvgMinutes.setText(String.valueOf((int) Math.round(avgMinutes)));
	}

	private void recalc(PlayerRowModel row) {
		MoneyballCalculator.recalculate(row, appConfig);
		refreshSquadNeeds();
		syncIncomingLoans();
		refreshShadowSquad();
		refreshSquadKPIs();
		autoSaveToDatabase();
	}

	private void refreshSquadNeeds() {
		List<PositionNeedModel> needs = SquadAnalysisService.analyzeSquadNeeds(playerDataList, appConfig);
		needsDataList.setAll(needs);

		depthChartDataList.setAll(SquadAnalysisService.generateDepthChart(playerDataList, youthDataList, appConfig));

		long criticalCount = needs.stream()
				.filter(n -> {
					String d = n.diagnosisProperty().get();
					return d != null && (d.startsWith("🚨") || d.startsWith("🔴"));
				})
				.count();

		long warningCount = needs.stream()
				.filter(n -> {
					String d = n.diagnosisProperty().get();
					return d != null && (d.startsWith("⚠️") || d.startsWith("⏳") || d.startsWith("👴") || d.startsWith("🟡"));
				})
				.count();

		long optimalCount = needs.stream()
				.filter(n -> {
					String d = n.diagnosisProperty().get();
					return d != null && d.startsWith("🟢");
				})
				.count();

		if (lblCriticalNeeds != null) lblCriticalNeeds.setText(criticalCount + " Positionen");
		if (lblWarningNeeds != null) lblWarningNeeds.setText(warningCount + " Positionen");
		if (lblOptimalNeeds != null) lblOptimalNeeds.setText(optimalCount + " Positionen");
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
					outgoingLoanDataList.clear();
					incomingLoanDataList.clear();
					youthDataList.clear();

					String myClub = (txtMyClub != null && !txtMyClub.getText().isBlank())
							? txtMyClub.getText().trim().toLowerCase()
							: (appConfig.getMyClubName() != null ? appConfig.getMyClubName().toLowerCase() : "villarreal");

					String clubPrefix = myClub.substring(0, Math.min(3, myClub.length()));

					for (PlayerRowModel row : rows) {
						String club = row.currentClubProperty().get() != null
								? row.currentClubProperty().get().trim().toLowerCase() : "";
						String art = row.contractTypeProperty().get() != null
								? row.contractTypeProperty().get().trim().toLowerCase() : "";

						boolean isOutgoingLoan = !club.isBlank() && !club.startsWith(clubPrefix);
						boolean isIncomingLoan = art.contains("leihe");

						boolean isReserveOrYouthTeam = club.startsWith(clubPrefix) && (
								club.contains(" b") || club.contains(" c") ||
										club.contains("u19") || club.contains("u21") ||
										club.contains("u18") || club.contains("u23") ||
										club.contains("juvenil")
						);

						if (isOutgoingLoan) {
							LoanPlayerModel outgoing = new LoanPlayerModel(
									LoanPlayerModel.LoanType.OUTGOING,
									row.nameProperty().get(),
									row.roleProperty().get(),
									row.ageProperty().get(),
									row.currentClubProperty().get(),
									row.minutesPlayedProperty().get() > 0 ? (row.minutesPlayedProperty().get() / 80) : 0,
									row.minutesPlayedProperty().get(),
									7.00,
									0.0,
									row.marketValueProperty().get(),
									true
							);
							LoanAnalysisService.evaluate(outgoing);
							outgoingLoanDataList.add(outgoing);
						} else if (isReserveOrYouthTeam) {
							YouthPlayerModel youth = new YouthPlayerModel(
									row.nameProperty().get(),
									row.roleProperty().get(),
									row.ageProperty().get(),
									4.0,
									2.0,
									row.agilityProperty().get(),
									row.paceProperty().get(),
									row.staminaProperty().get(),
									row.determinationProperty().get(),
									row.statValue1Property().get(),
									row.statValue2Property().get(),
									row.statValue3Property().get()
							);
							YouthAnalysisService.evaluate(youth);
							youthDataList.add(youth);
						} else {
							row.onLoanProperty().set(isIncomingLoan);
							MoneyballCalculator.recalculate(row, appConfig);
							playerDataList.add(row);
						}
					}

					refreshSquadNeeds();
					syncIncomingLoans();
					refreshYouthKPIs();
					refreshLoanKPIs();
					refreshShadowSquad();
					refreshSquadKPIs();

					// Dauerhaft in H2-Datenbank archivieren
					HistoryRepository.saveSquadSnapshot(
							myClub,
							"Aktuell",
							droppedFile.getName(),
							playerDataList,
							outgoingLoanDataList,
							incomingLoanDataList,
							youthDataList
					);

					// Zeitstempel oben rechts aktualisieren
					if (lblImportTimestamp != null) {
						lblImportTimestamp.setText("Importiert am: " + LocalDateTime.now().format(timeFormatter));
					}

					statusLabel.setText("Geladen & archiviert: " + droppedFile.getName() + " (" + playerDataList.size() + " Kader, " + outgoingLoanDataList.size() + " verliehen, " + youthDataList.size() + " Talente)");
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

	private ObservableList<String> getAvailableRoles() {
		ObservableList<String> roles = FXCollections.observableArrayList();
		if (appConfig != null && appConfig.getTactics() != null) {
			appConfig.getTactics().stream()
					.filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(appConfig.getActiveTactic()))
					.findFirst()
					.ifPresent(active -> {
						if (active.getSlots() != null) {
							for (var slot : active.getSlots()) {
								if (slot.getRoleLabel() != null && !roles.contains(slot.getRoleLabel())) {
									roles.add(slot.getRoleLabel());
								} else if (slot.getSlotName() != null && !roles.contains(slot.getSlotName())) {
									roles.add(slot.getSlotName());
								}
							}
						}
					});
		}
		return roles;
	}

	private StringConverter<Long> createEuroConverter() {
		return new StringConverter<>() {
			@Override
			public String toString(Long value) {
				if (value == null || value <= 0) return "€ 0";
				return "€ " + euroFormat.format(value);
			}

			@Override
			public Long fromString(String string) {
				if (string == null || string.isBlank()) return 0L;
				String clean = string.replaceAll("[^0-9]", "");
				return clean.isEmpty() ? 0L : Long.parseLong(clean);
			}
		};
	}

	@FXML
	public void onAddPlayerManual() {
		Player p = new Player("Neuer Spieler", "Stoßstürmer (A)", 22, 1000000, 50000, 2.0, 12, 12, 12, 15, 1000);
		PlayerRowModel row = new PlayerRowModel(p, 0.5, 0.15, 0.45);
		MoneyballCalculator.recalculate(row, appConfig);
		playerDataList.add(row);
		refreshSquadNeeds();
		syncIncomingLoans();
		refreshShadowSquad();
		refreshSquadKPIs();
		autoSaveToDatabase();
		playerTable.scrollTo(row);
	}

	@FXML
	public void onClearList() {
		playerDataList.clear();
		needsDataList.clear();
		depthChartDataList.clear();
		outgoingLoanDataList.clear();
		incomingLoanDataList.clear();
		youthDataList.clear();
		scoutDataList.clear();
		shadowSquadDataList.clear();

		if (lblCriticalNeeds != null) lblCriticalNeeds.setText("0 Positionen");
		if (lblWarningNeeds != null) lblWarningNeeds.setText("0 Positionen");
		if (lblOptimalNeeds != null) lblOptimalNeeds.setText("0 Positionen");
		if (lblShadowTotalProfit != null) lblShadowTotalProfit.setText("€ 0");
		if (lblShadowTotalSavings != null) lblShadowTotalSavings.setText("€ 0 / Mon.");
		if (lblShadowCoveredSlots != null) lblShadowCoveredSlots.setText("0 / 0");

		if (lblImportTimestamp != null) {
			lblImportTimestamp.setText("Stand: Noch kein Import");
		}

		refreshSquadKPIs();
		refreshLoanKPIs();
		refreshYouthKPIs();
		refreshScoutKPIs();
		onResetSalaryCalc();
		autoSaveToDatabase();
		statusLabel.setText("Tabelle geleert.");
	}
}