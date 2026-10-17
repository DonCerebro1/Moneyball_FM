package org.moneyballfm.moneyball_fm.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.ConfigLoader;
import org.moneyballfm.moneyball_fm.config.TacticConfig;
import org.moneyballfm.moneyball_fm.db.HistoryRepository;
import org.moneyballfm.moneyball_fm.model.*;
import org.moneyballfm.moneyball_fm.parser.FmExportParser;
import org.moneyballfm.moneyball_fm.service.*;
import org.moneyballfm.moneyball_fm.util.ViewInstallerService;

import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {

	private static final String ALL_CLUB_OPTION = "🏛️ Gesamter Verein (Kader-Übersicht)";

	@FXML private VBox rootPane;
	@FXML private TabPane mainTabPane;
	@FXML private Label statusLabel;
	@FXML private TextField txtMyClub;
	@FXML private TextField txtInGameSeason;
	@FXML private ComboBox<String> cmbActiveTactic;
	@FXML private Label lblImportTimestamp;

	// TAB 1: KPI-Header
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

	// TAB: Taktik-Matrix & Passung
	@FXML private Label lblMatrixBestTactic;
	@FXML private Label lblMatrixBestAvgScore;
	@FXML private Label lblMatrixRetrainCount;
	@FXML private TableView<TacticalMatrixRowModel> matrixTable;
	@FXML private TableColumn<TacticalMatrixRowModel, String> colMatrixPlayer;
	@FXML private TableColumn<TacticalMatrixRowModel, String> colMatrixNaturalPos;
	@FXML private TableColumn<TacticalMatrixRowModel, String> colMatrixCurrentRole;
	@FXML private TableColumn<TacticalMatrixRowModel, Double> colMatrixCurrentScore;
	@FXML private TableColumn<TacticalMatrixRowModel, String> colMatrixBestRole;
	@FXML private TableColumn<TacticalMatrixRowModel, Double> colMatrixBestScore;
	@FXML private TableColumn<TacticalMatrixRowModel, Double> colMatrixDelta;
	@FXML private TableColumn<TacticalMatrixRowModel, String> colMatrixAlternatives;
	@FXML private TableColumn<TacticalMatrixRowModel, String> colMatrixRecommendation;

	// TAB 5: Leihspieler
	@FXML private Label lblLoanReturnCount;
	@FXML private Label lblLoanExtendCount;
	@FXML private Label lblLoanSellCount;
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

	// TAB 6: Jugend
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

	// TAB 7: Scouting (inkl. Liga & Koeffizient)
	@FXML private Label lblScoutSignCount;
	@FXML private Label lblScoutBargainCount;
	@FXML private Label lblScoutRejectCount;
	@FXML private TableView<ScoutingPlayerModel> scoutTable;
	@FXML private TableColumn<ScoutingPlayerModel, String> colScoutName;
	@FXML private TableColumn<ScoutingPlayerModel, String> colScoutRole;
	@FXML private TableColumn<ScoutingPlayerModel, String> colScoutLeague;
	@FXML private TableColumn<ScoutingPlayerModel, Number> colScoutLeagueCoeff;
	@FXML private TableColumn<ScoutingPlayerModel, Integer> colScoutAge;
	@FXML private TableColumn<ScoutingPlayerModel, Long> colScoutAskingPrice;
	@FXML private TableColumn<ScoutingPlayerModel, Long> colScoutWageDemand;
	@FXML private TableColumn<ScoutingPlayerModel, Double> colScoutContractYears;
	@FXML private TableColumn<ScoutingPlayerModel, Integer> colScoutMinutes;
	@FXML private TableColumn<ScoutingPlayerModel, String> colScoutSampleQuality;
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

	// TAB 9: Registrierung
	@FXML private Label lblRegisteredCount;
	@FXML private Label lblU21ExemptCount;
	@FXML private Label lblRegistrationWages;
	@FXML private TableView<RegistrationRowModel> registrationTable;
	@FXML private TableColumn<RegistrationRowModel, Boolean> colRegActive;
	@FXML private TableColumn<RegistrationRowModel, String> colRegName;
	@FXML private TableColumn<RegistrationRowModel, String> colRegRole;
	@FXML private TableColumn<RegistrationRowModel, Integer> colRegAge;
	@FXML private TableColumn<RegistrationRowModel, Boolean> colRegU21;
	@FXML private TableColumn<RegistrationRowModel, Integer> colRegMinutes;
	@FXML private TableColumn<RegistrationRowModel, Double> colRegRating;
	@FXML private TableColumn<RegistrationRowModel, String> colRegTrend;
	@FXML private TableColumn<RegistrationRowModel, Long> colRegSalary;
	@FXML private TableColumn<RegistrationRowModel, String> colRegRecommendation;

	// TAB 10: Karriere-Historie & Verlauf
	@FXML private ComboBox<String> cmbHistoryPlayer;
	@FXML private Label lblHistorySnapshotsCount;
	@FXML private Label lblHistoryScoreTrend;
	@FXML private Label lblHistoryValueTrend;
	@FXML private TableView<PlayerHistoryEntry> historyTable;
	@FXML private TableColumn<PlayerHistoryEntry, String> colHistSeason;
	@FXML private TableColumn<PlayerHistoryEntry, String> colHistImportDate;
	@FXML private TableColumn<PlayerHistoryEntry, String> colHistRole;
	@FXML private TableColumn<PlayerHistoryEntry, Integer> colHistAge;
	@FXML private TableColumn<PlayerHistoryEntry, Long> colHistMarketValue;
	@FXML private TableColumn<PlayerHistoryEntry, Long> colHistSalary;
	@FXML private TableColumn<PlayerHistoryEntry, Integer> colHistMinutes;
	@FXML private TableColumn<PlayerHistoryEntry, Double> colHistScore;
	@FXML private TableColumn<PlayerHistoryEntry, String> colHistRec;

	private final ObservableList<PlayerRowModel> playerDataList = FXCollections.observableArrayList();
	private final ObservableList<PositionNeedModel> needsDataList = FXCollections.observableArrayList();
	private final ObservableList<DepthChartRowModel> depthChartDataList = FXCollections.observableArrayList();
	private final ObservableList<TacticalMatrixRowModel> matrixDataList = FXCollections.observableArrayList();
	private final ObservableList<LoanPlayerModel> outgoingLoanDataList = FXCollections.observableArrayList();
	private final ObservableList<LoanPlayerModel> incomingLoanDataList = FXCollections.observableArrayList();
	private final ObservableList<YouthPlayerModel> youthDataList = FXCollections.observableArrayList();
	private final ObservableList<ScoutingPlayerModel> scoutDataList = FXCollections.observableArrayList();
	private final ObservableList<ShadowSquadRowModel> shadowSquadDataList = FXCollections.observableArrayList();
	private final ObservableList<RegistrationRowModel> registrationDataList = FXCollections.observableArrayList();
	private final ObservableList<PlayerHistoryEntry> historyDataList = FXCollections.observableArrayList();

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
				if (newV != null) appConfig.setMyClubName(newV.trim());
			});
		}

		if (txtInGameSeason != null) {
			txtInGameSeason.setOnAction(e -> applyAndPersistSeason());
			txtInGameSeason.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
				if (!isNowFocused) applyAndPersistSeason();
			});
		}

		setupTacticSelector();
		setupAllTables();
		setupSalaryCalculator();
		setupHistoryTab();
		setupDragAndDrop();

		playerTable.setItems(playerDataList);
		playerTable.setEditable(true);
		needsTable.setItems(needsDataList);
		if (depthChartTable != null) depthChartTable.setItems(depthChartDataList);
		if (matrixTable != null) matrixTable.setItems(matrixDataList);
		if (outgoingLoanTable != null) outgoingLoanTable.setItems(outgoingLoanDataList);
		if (incomingLoanTable != null) incomingLoanTable.setItems(incomingLoanDataList);
		if (youthTable != null) youthTable.setItems(youthDataList);
		if (scoutTable != null) { scoutTable.setItems(scoutDataList); scoutTable.setEditable(true); }
		if (shadowSquadTable != null) { shadowSquadTable.setItems(shadowSquadDataList); shadowSquadTable.setEditable(true); }
		if (registrationTable != null) { registrationTable.setItems(registrationDataList); registrationTable.setEditable(true); }
		if (historyTable != null) historyTable.setItems(historyDataList);

		String activeTactic = appConfig.getActiveTactic() != null ? appConfig.getActiveTactic() : "4-3-3 Gegenpress";
		HistoryRepository.SnapshotMetadata meta = HistoryRepository.loadLatestSnapshot(
				playerDataList, outgoingLoanDataList, incomingLoanDataList, youthDataList, scoutDataList, activeTactic
		);

		if (meta != null && meta.inGameSeason() != null && !meta.inGameSeason().isBlank()) {
			if (txtInGameSeason != null) txtInGameSeason.setText(meta.inGameSeason());
		}

		if (!playerDataList.isEmpty() || !youthDataList.isEmpty() || !outgoingLoanDataList.isEmpty() || !scoutDataList.isEmpty()) {
			for (PlayerRowModel row : playerDataList) MoneyballCalculator.recalculate(row, appConfig);
			for (LoanPlayerModel l : outgoingLoanDataList) LoanAnalysisService.evaluate(l);
			for (LoanPlayerModel l : incomingLoanDataList) LoanAnalysisService.evaluate(l);
			for (YouthPlayerModel y : youthDataList) YouthAnalysisService.evaluate(y);
			for (ScoutingPlayerModel s : scoutDataList) ScoutingAnalysisService.evaluate(s, appConfig);

			String initialSeason = (txtInGameSeason != null && !txtInGameSeason.getText().isBlank())
					? txtInGameSeason.getText().trim() : "Winter 2037";
			recalculateAllPlayerContracts(initialSeason);

			refreshAll();
			if (statusLabel != null) statusLabel.setText("Stand aus H2 geladen (" + playerDataList.size() + " Kader, " + youthDataList.size() + " Talente, " + scoutDataList.size() + " Scouts).");
			if (lblImportTimestamp != null && meta != null && meta.importDate() != null) {
				lblImportTimestamp.setText("Import: " + meta.importDate() + " | " + initialSeason);
			}
		}

		refreshShadowSquad();
		refreshSquadKPIs();
	}

	private void applyAndPersistSeason() {
		if (txtInGameSeason == null) return;
		String season = txtInGameSeason.getText();
		if (season != null && !season.isBlank()) {
			String trimmedSeason = season.trim();
			HistoryRepository.updateLatestSnapshotSeason(trimmedSeason);

			recalculateAllPlayerContracts(trimmedSeason);

			if (statusLabel != null) statusLabel.setText("Saisonstand aktualisiert: " + trimmedSeason);
			if (lblImportTimestamp != null && lblImportTimestamp.getText().contains("|")) {
				String base = lblImportTimestamp.getText().substring(0, lblImportTimestamp.getText().indexOf("|") + 1);
				lblImportTimestamp.setText(base + " " + trimmedSeason);
			}
		}
	}

	private void recalculateAllPlayerContracts(String seasonText) {
		if (seasonText == null || seasonText.isBlank()) return;

		Matcher m = Pattern.compile("(\\d{4})").matcher(seasonText);
		int baseYear = 2037;
		if (m.find()) {
			baseYear = Integer.parseInt(m.group(1));
		}
		boolean isWinter = seasonText.toLowerCase().contains("winter");

		for (PlayerRowModel row : playerDataList) {
			Player raw = row.getRawPlayer();
			if (raw != null) {
				String rawEndDate = raw.getContractEndDate();
				if (rawEndDate != null && !rawEndDate.isBlank()) {
					double remaining = FmExportParser.calculateContractYears(rawEndDate, baseYear, isWinter);
					row.contractYearsProperty().set(remaining);
					raw.setContractYears(remaining);
				}
			}
		}

		refreshSquadNeeds();
		refreshSquadKPIs();
	}

	private void setupTacticSelector() {
		if (cmbActiveTactic == null || appConfig == null || appConfig.getTactics() == null) return;

		ObservableList<String> tacticNames = FXCollections.observableArrayList(
				appConfig.getTactics().stream()
						.map(TacticConfig::getName)
						.filter(name -> name != null && !name.isBlank())
						.toList()
		);

		cmbActiveTactic.setItems(tacticNames);

		String active = appConfig.getActiveTactic();
		if (active != null && tacticNames.contains(active)) {
			cmbActiveTactic.setValue(active);
		} else if (!tacticNames.isEmpty()) {
			cmbActiveTactic.setValue(tacticNames.get(0));
			appConfig.setActiveTactic(tacticNames.get(0));
		}

		cmbActiveTactic.valueProperty().addListener((obs, oldV, newV) -> {
			if (newV != null && !newV.equalsIgnoreCase(appConfig.getActiveTactic())) {
				switchActiveTactic(newV);
			}
		});
	}

	private void switchActiveTactic(String newTacticName) {
		String oldTactic = appConfig.getActiveTactic();

		if (oldTactic != null) {
			for (PlayerRowModel row : playerDataList) {
				String curRole = row.roleProperty().get();
				if (curRole != null && !curRole.isBlank()) {
					row.setRoleForTactic(oldTactic, curRole);
				}
			}
		}

		appConfig.setActiveTactic(newTacticName);

		setupAllTables();

		for (PlayerRowModel row : playerDataList) {
			String savedRole = row.getRoleForTactic(newTacticName);
			if (savedRole != null && !savedRole.isBlank()) {
				row.roleProperty().set(savedRole);
			} else {
				String curRole = row.roleProperty().get();
				row.setRoleForTactic(newTacticName, curRole);
			}
			MoneyballCalculator.recalculate(row, appConfig);
		}

		for (ScoutingPlayerModel s : scoutDataList) {
			ScoutingAnalysisService.evaluate(s, appConfig);
		}

		refreshAll();
		refreshShadowSquad();
		refreshSquadKPIs();

		HistoryRepository.updateLatestSnapshotRoles(playerDataList);

		if (statusLabel != null) {
			statusLabel.setText("Taktik gewechselt: " + newTacticName);
		}
	}

	private void onPlayerRoleEdited(PlayerRowModel row, String newRole) {
		String currentTactic = appConfig.getActiveTactic();
		row.setRoleForTactic(currentTactic, newRole);
		recalc(row);

		HistoryRepository.updateLatestSnapshotRoles(playerDataList);
	}

	private void setupAllTables() {
		Stage stage = (rootPane.getScene() != null) ? (Stage) rootPane.getScene().getWindow() : null;
		ObservableList<String> roles = getAvailableRoles();

		TableColumnBinder.setupPlayerTable(
				playerTable, colName, colRole, colOnLoan, colAge, colAgility, colPace, colStamina, colDetermination,
				colMarketValue, colFairMarketValue, colSalary, colMaxSalary, colMinutes, colStat1, colStat2, colStat3,
				colScore, colTier, colSalaryCheck, colQuality, roles, this::onPlayerRoleEdited, this::recalc, this::autoSaveToDatabase, stage
		);

		TableColumnBinder.setupScoutTable(
				scoutTable, colScoutName, colScoutRole, colScoutLeague, colScoutLeagueCoeff, colScoutAge, colScoutAskingPrice, colScoutWageDemand, colScoutContractYears,
				colScoutMinutes, colScoutSampleQuality, colScoutAgility, colScoutPace, colScoutStamina,
				colScoutStat1, colScoutStat2, colScoutStat3,
				colScoutScore, colScoutMaxFee, colScoutMaxWage, colScoutPriceCheck, colScoutAction, roles, this::recalcScout,
				this::autoSaveToDatabase, stage
		);

		TableColumnBinder.setupTacticalMatrixTable(
				matrixTable, colMatrixPlayer, colMatrixNaturalPos, colMatrixCurrentRole,
				colMatrixCurrentScore, colMatrixBestRole, colMatrixBestScore,
				colMatrixDelta, colMatrixAlternatives, colMatrixRecommendation
		);

		TableColumnBinder.setupLoanColumns(
				outgoingLoanTable, colOutLoanName, colOutLoanRole, colOutLoanAge, colOutLoanClub, colOutLoanGames,
				colOutLoanMinutes, colOutLoanRating, colOutLoanScore, colOutLoanClause, colOutLoanMarketValue,
				colOutLoanRecommendation, colOutLoanFuturePlan, roles, this::recalcLoan, this::autoSaveToDatabase, stage
		);

		TableColumnBinder.setupLoanColumns(
				incomingLoanTable, colInLoanName, colInLoanRole, colInLoanAge, colInLoanClub, colInLoanGames,
				colInLoanMinutes, colInLoanRating, colInLoanScore, colInLoanClause, colInLoanMarketValue,
				colInLoanRecommendation, colInLoanFuturePlan, roles, this::recalcLoan, this::autoSaveToDatabase, stage
		);

		TableColumnBinder.setupYouthTable(
				youthTable, colYouthName, colYouthRole, colYouthAge, colYouthPotentialStars, colYouthCurrentStars,
				colYouthAgility, colYouthPace, colYouthStamina, colYouthDetermination, colYouthStat1, colYouthStat2, colYouthStat3,
				colYouthScore, colYouthForecast, colYouthRecommendation, colYouthMaxSalary, roles, this::recalcYouth,
				this::autoSaveToDatabase, stage
		);

		TableColumnBinder.setupNeedsTable(
				needsTable, colNeedSlot, colNeedRole, colNeedCount, colNeedBestPlayer,
				colNeedBestScore, colNeedAvgScore, colNeedAvgAge, colNeedCritContracts,
				colNeedDiagnosis, colNeedRecommendation, colNeedTargetProfile
		);

		TableColumnBinder.setupDepthChartTable(
				depthChartTable, colDepthRole, colDepthRank, colDepthPlayerName,
				colDepthScore, colDepthTeamRole, colDepthAge, colDepthSalary
		);

		TableColumnBinder.setupShadowSquadTable(
				shadowSquadTable, colShadowRole, colShadowStarter, colShadowStarterSalary,
				colShadowStarterValue, colShadowC1Name, colShadowC1Fee, colShadowC1Salary,
				colShadowC2Name, colShadowC2Fee, colShadowC2Salary, colShadowNetGain, colShadowWageSavings
		);

		TableColumnBinder.setupRegistrationTable(
				registrationTable, colRegActive, colRegName, colRegRole, colRegAge,
				colRegU21, colRegMinutes, colRegRating, colRegTrend, colRegSalary,
				colRegRecommendation, this::refreshRegistrationKPIs
		);

		if (historyTable != null) {
			TableColumnBinder.setupHistoryTable(
					historyTable, colHistSeason, colHistImportDate, colHistRole,
					colHistAge, colHistMarketValue, colHistSalary, colHistMinutes,
					colHistScore, colHistRec
			);
		}
	}

	private void refreshAll() {
		refreshSquadNeeds();
		refreshTacticalMatrix();
		refreshLoanKPIs();
		refreshYouthKPIs();
		refreshScoutKPIs();
		refreshRegistrationData();
		refreshHistoryPlayerDropdown();
	}

	private void refreshTacticalMatrix() {
		if (matrixTable == null && lblMatrixBestTactic == null) return;

		List<TacticalMatrixRowModel> scanned = TacticalMatrixService.analyzeSquadMatrix(playerDataList, appConfig);
		matrixDataList.setAll(scanned);

		var tacticFits = TacticalMatrixService.evaluateTacticsFit(playerDataList, appConfig);
		if (!tacticFits.isEmpty()) {
			var bestTactic = tacticFits.get(0);
			if (lblMatrixBestTactic != null) lblMatrixBestTactic.setText(bestTactic.tacticName() + " (" + bestTactic.summary() + ")");
			if (lblMatrixBestAvgScore != null) lblMatrixBestAvgScore.setText(String.format(Locale.GERMANY, "%.1f", bestTactic.avgTop11Score()) + " Ø Top-11");
		} else {
			if (lblMatrixBestTactic != null) lblMatrixBestTactic.setText("—");
			if (lblMatrixBestAvgScore != null) lblMatrixBestAvgScore.setText("—");
		}

		long retrainCount = scanned.stream().filter(r -> r.recommendationProperty().get().contains("Umschulen")).count();
		if (lblMatrixRetrainCount != null) {
			lblMatrixRetrainCount.setText(retrainCount + " Spieler");
		}
	}

	private void setupSalaryCalculator() {
		if (cmbCalcPlayer != null) {
			cmbCalcPlayer.setItems(playerDataList);
			cmbCalcPlayer.setConverter(new StringConverter<>() {
				@Override public String toString(PlayerRowModel p) { return p == null ? "" : p.nameProperty().get() + " (" + p.roleProperty().get() + " | Score: " + p.moneyballScoreProperty().get() + ")"; }
				@Override public PlayerRowModel fromString(String s) { return null; }
			});
			cmbCalcPlayer.valueProperty().addListener((obs, o, n) -> {
				if (n != null) {
					if (txtCalcCustomName != null) txtCalcCustomName.setText(n.nameProperty().get());
					if (txtCalcScore != null) txtCalcScore.setText(String.valueOf(n.moneyballScoreProperty().get()));
					if (txtCalcDemand != null) txtCalcDemand.setText(String.valueOf(n.monthlySalaryProperty().get()));
					onCalculateSalary();
				}
			});
		}
		if (txtCalcScore != null) txtCalcScore.textProperty().addListener((o, ov, nv) -> onCalculateSalary());
		if (txtCalcDemand != null) txtCalcDemand.textProperty().addListener((o, ov, nv) -> onCalculateSalary());
	}

	private void setupHistoryTab() {
		if (cmbHistoryPlayer == null) return;
		cmbHistoryPlayer.valueProperty().addListener((obs, oldV, newV) -> {
			if (newV != null && !newV.isBlank()) {
				handleHistorySelection(newV);
			}
		});
	}

	private void refreshHistoryPlayerDropdown() {
		if (cmbHistoryPlayer == null) return;
		String curSelection = cmbHistoryPlayer.getValue();

		ObservableList<String> items = FXCollections.observableArrayList();
		items.add(ALL_CLUB_OPTION);

		List<String> playerNames = playerDataList.stream()
				.map(p -> p.nameProperty().get())
				.filter(n -> n != null && !n.isBlank())
				.distinct()
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.toList();

		items.addAll(playerNames);
		cmbHistoryPlayer.setItems(items);

		if (curSelection != null && items.contains(curSelection)) {
			cmbHistoryPlayer.setValue(curSelection);
			handleHistorySelection(curSelection);
		} else if (!items.isEmpty()) {
			cmbHistoryPlayer.setValue(ALL_CLUB_OPTION);
			loadClubHistory();
		} else {
			historyDataList.clear();
			resetHistoryKpis();
		}
	}

	private void handleHistorySelection(String selected) {
		if (ALL_CLUB_OPTION.equals(selected)) {
			loadClubHistory();
		} else {
			loadHistoryForPlayer(selected);
		}
	}

	private void loadClubHistory() {
		String club = (txtMyClub != null && !txtMyClub.getText().isBlank()) ? txtMyClub.getText().trim() : "Villarreal";
		List<ClubHistoryEntry> entries = HistoryRepository.getClubHistory(club);

		List<PlayerHistoryEntry> rows = entries.stream().map(c -> new PlayerHistoryEntry(
				c.getSnapshotDate(),
				c.getSeason(),
				"Kader: " + c.getPlayerCount() + " Spieler",
				(int) Math.round(c.getAvgAge()),
				c.getTotalMarketValue(),
				c.getTotalMonthlySalary(),
				0,
				Math.round(c.getAvgMoneyballScore() * 10.0) / 10.0,
				"Ø Alter: " + String.format(Locale.GERMANY, "%.1f", c.getAvgAge())
		)).toList();

		historyDataList.setAll(rows);

		if (lblHistorySnapshotsCount != null) {
			lblHistorySnapshotsCount.setText(entries.size() + (entries.size() == 1 ? " Saison-Stand" : " Saison-Stände"));
		}

		if (entries.size() >= 2) {
			ClubHistoryEntry first = entries.get(0);
			ClubHistoryEntry latest = entries.get(entries.size() - 1);

			double scoreDiff = Math.round((latest.getAvgMoneyballScore() - first.getAvgMoneyballScore()) * 10.0) / 10.0;
			long valueDiff = latest.getTotalMarketValue() - first.getTotalMarketValue();

			if (lblHistoryScoreTrend != null) {
				String sign = scoreDiff > 0.0 ? "+" : "";
				lblHistoryScoreTrend.setText(sign + String.format(Locale.GERMANY, "%.1f", scoreDiff) + " Ø Score");
				lblHistoryScoreTrend.setStyle(scoreDiff >= 0.0
						? "-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-font-size: 16px;"
						: "-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 16px;");
			}

			if (lblHistoryValueTrend != null) {
				String sign = valueDiff > 0 ? "+" : "";
				lblHistoryValueTrend.setText(sign + "€ " + euroFormat.format(valueDiff));
				lblHistoryValueTrend.setStyle(valueDiff >= 0
						? "-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-font-size: 16px;"
						: "-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 16px;");
			}
		} else if (entries.size() == 1) {
			if (lblHistoryScoreTrend != null) {
				lblHistoryScoreTrend.setText("Basisstand");
				lblHistoryScoreTrend.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 16px;");
			}
			if (lblHistoryValueTrend != null) {
				lblHistoryValueTrend.setText("Basisstand");
				lblHistoryValueTrend.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 16px;");
			}
		} else {
			resetHistoryKpis();
		}
	}

	private void loadHistoryForPlayer(String playerName) {
		if (playerName == null || playerName.isBlank()) {
			historyDataList.clear();
			resetHistoryKpis();
			return;
		}

		int currentAge = playerDataList.stream()
				.filter(p -> playerName.equalsIgnoreCase(p.nameProperty().get()))
				.mapToInt(p -> p.ageProperty().get())
				.findFirst()
				.orElse(-1);

		List<PlayerHistoryEntry> entries = HistoryRepository.getPlayerHistory(playerName, currentAge);
		historyDataList.setAll(entries);

		if (lblHistorySnapshotsCount != null) {
			lblHistorySnapshotsCount.setText(entries.size() + (entries.size() == 1 ? " Stand" : " Stände"));
		}

		if (entries.size() >= 2) {
			PlayerHistoryEntry first = entries.get(0);
			PlayerHistoryEntry latest = entries.get(entries.size() - 1);

			double scoreDiff = Math.round((latest.moneyballScore() - first.moneyballScore()) * 10.0) / 10.0;
			long valueDiff = latest.marketValue() - first.marketValue();

			if (lblHistoryScoreTrend != null) {
				String sign = scoreDiff > 0.0 ? "+" : "";
				lblHistoryScoreTrend.setText(sign + String.format(Locale.GERMANY, "%.1f", scoreDiff) + " Pkt.");
				lblHistoryScoreTrend.setStyle(scoreDiff >= 0.0
						? "-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-font-size: 16px;"
						: "-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 16px;");
			}

			if (lblHistoryValueTrend != null) {
				String sign = valueDiff > 0 ? "+" : "";
				lblHistoryValueTrend.setText(sign + "€ " + euroFormat.format(valueDiff));
				lblHistoryValueTrend.setStyle(valueDiff >= 0
						? "-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-font-size: 16px;"
						: "-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 16px;");
			}
		} else if (entries.size() == 1) {
			if (lblHistoryScoreTrend != null) {
				lblHistoryScoreTrend.setText("Basisstand");
				lblHistoryScoreTrend.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 16px;");
			}
			if (lblHistoryValueTrend != null) {
				lblHistoryValueTrend.setText("Basisstand");
				lblHistoryValueTrend.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 16px;");
			}
		} else {
			resetHistoryKpis();
		}
	}

	private void resetHistoryKpis() {
		if (lblHistorySnapshotsCount != null) lblHistorySnapshotsCount.setText("0 Stände");
		if (lblHistoryScoreTrend != null) {
			lblHistoryScoreTrend.setText("—");
			lblHistoryScoreTrend.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-font-size: 16px;");
		}
		if (lblHistoryValueTrend != null) {
			lblHistoryValueTrend.setText("—");
			lblHistoryValueTrend.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-font-size: 16px;");
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
				String clean = txtDemandClean(txtCalcDemand.getText());
				if (!clean.isEmpty()) demand = Long.parseLong(clean);
			}

			double af = 1.0;
			double contractYears = 2.0;

			if (cmbCalcPlayer != null && cmbCalcPlayer.getValue() != null) {
				PlayerRowModel selected = cmbCalcPlayer.getValue();
				af = selected.getAvailabilityFactor();
				contractYears = selected.contractYearsProperty().get();
			}

			var rec = SalaryCalculationService.calculateOffer(score, demand, af, contractYears, appConfig);

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

	private String txtDemandClean(String text) {
		return text.replaceAll("[^0-9]", "");
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

	@FXML
	public void onInstallFmViews() {
		ViewInstallerService.installViews(rootPane.getScene() != null ? rootPane.getScene().getWindow() : null);
	}

	private void autoSaveToDatabase() {}

	private void saveSquadSnapshotToDatabase() {
		String club = (txtMyClub != null && !txtMyClub.getText().isBlank()) ? txtMyClub.getText().trim() : "Villarreal";
		String season = (txtInGameSeason != null && !txtInGameSeason.getText().isBlank()) ? txtInGameSeason.getText().trim() : "Aktuell";
		HistoryRepository.saveSquadSnapshot(club, season, playerDataList, outgoingLoanDataList, incomingLoanDataList, youthDataList);
	}

	private void saveScoutingSnapshotToDatabase() {
		String club = (txtMyClub != null && !txtMyClub.getText().isBlank()) ? txtMyClub.getText().trim() : "Villarreal";
		String season = (txtInGameSeason != null && !txtInGameSeason.getText().isBlank()) ? txtInGameSeason.getText().trim() : "Aktuell";
		HistoryRepository.saveScoutingSnapshot(club, season, scoutDataList);
	}

	private void recalc(PlayerRowModel row) {
		MoneyballCalculator.recalculate(row, appConfig);
		refreshSquadNeeds();
		refreshTacticalMatrix();
		LoanAnalysisService.syncIncomingLoans(playerDataList, incomingLoanDataList);
		refreshShadowSquad();
		refreshSquadKPIs();
		refreshRegistrationData();
		autoSaveToDatabase();
	}

	private void recalcLoan(LoanPlayerModel player) {
		LoanAnalysisService.evaluate(player);
		refreshLoanKPIs();
		autoSaveToDatabase();
	}

	private void recalcYouth(YouthPlayerModel youth) {
		YouthAnalysisService.evaluate(youth);
		refreshYouthKPIs();
		autoSaveToDatabase();
	}

	private void recalcScout(ScoutingPlayerModel scout) {
		ScoutingAnalysisService.evaluate(scout, appConfig);
		refreshScoutKPIs();
		refreshShadowSquad();
		autoSaveToDatabase();
	}

	private void refreshLoanKPIs() {
		var kpis = LoanAnalysisService.calculateLoanKpis(outgoingLoanDataList, incomingLoanDataList);
		if (lblLoanReturnCount != null) lblLoanReturnCount.setText(kpis.returnCount() + " Spieler");
		if (lblLoanExtendCount != null) lblLoanExtendCount.setText(kpis.extendCount() + " Spieler");
		if (lblLoanSellCount != null) lblLoanSellCount.setText(kpis.sellCount() + " Spieler");
	}

	private void refreshYouthKPIs() {
		var kpis = YouthAnalysisService.calculateYouthKpis(youthDataList);
		if (lblYouthTopCount != null) lblYouthTopCount.setText(kpis.topCount() + " Spieler");
		if (lblYouthLoanCount != null) lblYouthLoanCount.setText(kpis.loanCount() + " Spieler");
		if (lblYouthDevelopCount != null) lblYouthDevelopCount.setText(kpis.developCount() + " Spieler");
		if (lblYouthSellCount != null) lblYouthSellCount.setText(kpis.sellCount() + " Spieler");
	}

	private void refreshScoutKPIs() {
		var kpis = ScoutingAnalysisService.calculateScoutKpis(scoutDataList);
		if (lblScoutSignCount != null) lblScoutSignCount.setText(kpis.signCount() + " Spieler");
		if (lblScoutBargainCount != null) lblScoutBargainCount.setText(kpis.bargainCount() + " Spieler");
		if (lblScoutRejectCount != null) lblScoutRejectCount.setText(kpis.rejectCount() + " Spieler");
	}

	private void refreshShadowSquad() {
		List<ShadowSquadRowModel> list = ShadowSquadService.generateShadowSquad(playerDataList, scoutDataList, appConfig);
		shadowSquadDataList.setAll(list);
		long totalProfit = shadowSquadDataList.stream().mapToLong(r -> r.netTransferGainProperty().get()).sum();
		long totalSavings = shadowSquadDataList.stream().mapToLong(r -> r.monthlyWageSavingsProperty().get()).sum();
		long covered = shadowSquadDataList.stream().filter(r -> {
			String c1 = r.candidate1NameProperty().get();
			return c1 != null && !c1.contains("Kein Scout") && !c1.contains("Unter Beobachtung");
		}).count();

		if (lblShadowTotalProfit != null) lblShadowTotalProfit.setText("€ " + euroFormat.format(totalProfit));
		if (lblShadowTotalSavings != null) lblShadowTotalSavings.setText("€ " + euroFormat.format(totalSavings) + " / Mon.");
		if (lblShadowCoveredSlots != null) lblShadowCoveredSlots.setText(covered + " / " + shadowSquadDataList.size());
	}

	private void refreshRegistrationData() {
		registrationDataList.clear();

		for (PlayerRowModel p : playerDataList) {
			RegistrationRowModel reg = new RegistrationRowModel(p);
			registrationDataList.add(reg);
		}

		for (YouthPlayerModel y : youthDataList) {
			boolean exists = registrationDataList.stream().anyMatch(r ->
					SquadImportService.normalizeSquadPlayerKey(r.nameProperty().get(), r.ageProperty().get())
							.equals(SquadImportService.normalizeSquadPlayerKey(y.getName(), y.ageProperty().get()))
			);

			if (!exists) {
				Player p = new Player(y.getName(), y.targetRoleProperty().get(), y.ageProperty().get(),
						0L, 0L, 1.0, y.agilityProperty().get(), y.paceProperty().get(), y.staminaProperty().get(),
						y.determinationProperty().get(), 0);
				RegistrationRowModel reg = new RegistrationRowModel(new PlayerRowModel(p, 0.0, 0.0, 0.0));
				reg.u21ExemptProperty().set(y.ageProperty().get() <= 21);
				reg.registeredProperty().set(false);
				reg.evaluate();
				registrationDataList.add(reg);
			}
		}

		long nonU21Count = registrationDataList.stream().filter(r -> !r.u21ExemptProperty().get()).count();
		if (nonU21Count <= 25) {
			registrationDataList.stream()
					.filter(r -> !r.u21ExemptProperty().get())
					.forEach(r -> r.registeredProperty().set(true));
		}

		refreshRegistrationKPIs();
	}

	private void refreshRegistrationKPIs() {
		long registeredSlots = registrationDataList.stream().filter(r -> r.registeredProperty().get()).count();
		long u21Count = registrationDataList.stream().filter(r -> r.u21ExemptProperty().get()).count();
		long totalRegWage = registrationDataList.stream().filter(r -> r.registeredProperty().get()).mapToLong(r -> r.salaryProperty().get()).sum();

		if (lblRegisteredCount != null) {
			lblRegisteredCount.setText(registeredSlots + " / 25 Slots");
			lblRegisteredCount.setStyle(registeredSlots > 25 ? "-fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-text-fill: #16a34a; -fx-font-weight: bold;" );
		}
		if (lblU21ExemptCount != null) lblU21ExemptCount.setText(u21Count + " U21 frei");
		if (lblRegistrationWages != null) lblRegistrationWages.setText("€ " + euroFormat.format(totalRegWage) + " / Mon.");
	}

	private void refreshSquadKPIs() {
		var kpis = SquadAnalysisService.calculateSquadKpis(playerDataList);
		if (lblSquadAvgAge != null) lblSquadAvgAge.setText(String.format(Locale.GERMANY, "%.1f", kpis.avgAge()));
		if (lblSquadAvgAthletics != null) lblSquadAvgAthletics.setText(String.format(Locale.GERMANY, "%.1f", kpis.avgAthletics()));
		if (lblSquadTotalWage != null) lblSquadTotalWage.setText("€ " + euroFormat.format(kpis.totalMonthlyWage()) + " / Mon.");
		if (lblSquadTotalMarketValue != null) lblSquadTotalMarketValue.setText("€ " + euroFormat.format(kpis.totalMarketValue()));
		if (lblSquadAvgMinutes != null) lblSquadAvgMinutes.setText(String.valueOf(kpis.avgMinutes()));
	}

	private void refreshSquadNeeds() {
		List<PositionNeedModel> needs = SquadAnalysisService.analyzeSquadNeeds(playerDataList, appConfig);
		needsDataList.setAll(needs);
		depthChartDataList.setAll(SquadAnalysisService.generateDepthChart(playerDataList, youthDataList, appConfig));

		var summary = SquadAnalysisService.summarizeNeeds(needs);
		if (lblCriticalNeeds != null) lblCriticalNeeds.setText(summary.criticalCount() + " Positionen");
		if (lblWarningNeeds != null) lblWarningNeeds.setText(summary.warningCount() + " Positionen");
		if (lblOptimalNeeds != null) lblOptimalNeeds.setText(summary.optimalCount() + " Positionen");
	}

	private long lastDropTimestamp = 0L;

	private void setupDragAndDrop() {
		rootPane.setOnDragOver(event -> {
			if (event.getGestureSource() != rootPane && event.getDragboard().hasFiles()) {
				event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}
			event.consume();
		});

		rootPane.setOnDragDropped(event -> {
			event.consume();
			long now = System.currentTimeMillis();
			if (now - lastDropTimestamp < 1500) { event.setDropCompleted(false); return; }
			lastDropTimestamp = now;

			Dragboard db = event.getDragboard();
			if (db.hasFiles()) {
				File droppedFile = db.getFiles().get(0);
				try {
					// 1. Spezieller Check: Ligen-Reputationsliste
					if (LeagueReputationImporter.isLeagueReputationFile(droppedFile)) {
						var result = LeagueReputationImporter.importFromFile(droppedFile);

						// Neu geladene Koeffizienten auf alle vorhandenen Scouts anwenden
						for (ScoutingPlayerModel s : scoutDataList) {
							ScoutingAnalysisService.evaluate(s, appConfig);
						}
						if (scoutTable != null) scoutTable.refresh();
						refreshScoutKPIs();
						refreshShadowSquad();

						statusLabel.setText(result.message());
						event.setDropCompleted(true);
						return;
					}

					// 2. Scouting-Datei (Tab Index 7: Scouting) -> ersetzt alte Scouts komplett
					if (mainTabPane != null && mainTabPane.getSelectionModel().getSelectedIndex() == 7) {
						scoutDataList.clear();
						var res = ScoutingAnalysisService.importScoutingFile(droppedFile, scoutDataList, getAvailableRoles(), appConfig);
						refreshScoutKPIs();
						refreshShadowSquad();
						saveScoutingSnapshotToDatabase();
						statusLabel.setText("Scouting-Liste ersetzt: " + res.message());
						event.setDropCompleted(true);
						return;
					}

					// 3. Vorbereitungs- / Testspiel-Datei
					if (FmExportParser.isPreseasonFile(droppedFile)) {
						int updated = SquadImportService.mergePreseasonStats(droppedFile, registrationDataList);
						refreshRegistrationKPIs();
						if (mainTabPane != null && mainTabPane.getTabs().size() >= 10) mainTabPane.getSelectionModel().select(9);
						statusLabel.setText("Testspiel-Stats gemerged: " + updated + " Spieler aktualisiert (aus " + droppedFile.getName() + ").");
						event.setDropCompleted(true);
						return;
					}

					// 4. Regulärer Kader-Import
					String clubName = (txtMyClub != null && !txtMyClub.getText().isBlank()) ? txtMyClub.getText() : null;
					var res = SquadImportService.importSquadFile(droppedFile, clubName, appConfig, playerDataList, outgoingLoanDataList, youthDataList);

					String currentSeason = (txtInGameSeason != null && !txtInGameSeason.getText().isBlank())
							? txtInGameSeason.getText().trim() : "Winter 2037";
					recalculateAllPlayerContracts(currentSeason);

					refreshAll();
					LoanAnalysisService.syncIncomingLoans(playerDataList, incomingLoanDataList);
					refreshShadowSquad();
					refreshSquadKPIs();
					saveSquadSnapshotToDatabase();

					if (lblImportTimestamp != null) {
						lblImportTimestamp.setText("Import: " + LocalDateTime.now().format(timeFormatter) + " | " + currentSeason);
					}
					statusLabel.setText(res.message());
					event.setDropCompleted(true);
				} catch (Exception e) {
					statusLabel.setText("Fehler beim Laden: " + e.getMessage());
					e.printStackTrace();
					event.setDropCompleted(false);
				}
			}
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
								String label = slot.getRoleLabel() != null && !slot.getRoleLabel().isBlank()
										? slot.getRoleLabel().trim() : null;
								String slotName = slot.getSlotName() != null && !slot.getSlotName().isBlank()
										? slot.getSlotName().trim() : null;

								if (label != null && !roles.contains(label)) roles.add(label);
								if (slotName != null && !roles.contains(slotName)) roles.add(slotName);
							}
						}
					});
		}

		for (TacticRole tr : TacticRole.values()) {
			String dName = tr.getDisplayName();
			if (!roles.contains(dName)) {
				roles.add(dName);
			}
		}

		return roles;
	}

	@FXML
	public void onAddPlayerManual() {
		Player p = new Player("Neuer Spieler", "Stoßstürmer (A)", 22, 1000000, 50000, 2.0, 12, 12, 12, 15, 1000);
		PlayerRowModel row = new PlayerRowModel(p, 0.5, 0.15, 0.45);
		MoneyballCalculator.recalculate(row, appConfig);
		playerDataList.add(row);
		refreshAll();
		LoanAnalysisService.syncIncomingLoans(playerDataList, incomingLoanDataList);
		refreshShadowSquad();
		refreshSquadKPIs();
		saveSquadSnapshotToDatabase();
		playerTable.scrollTo(row);
	}

	@FXML
	public void onAddLoanPlayerManual() {
		LoanPlayerModel p = new LoanPlayerModel(LoanPlayerModel.LoanType.OUTGOING, "Neuer Leihspieler", "Stoßstürmer (A)", 20, "Partnerverein (2. Liga)", 18, 1450, 7.12, 0, 0, 0.0, 2500000L, true);
		LoanAnalysisService.evaluate(p);
		outgoingLoanDataList.add(p);
		refreshLoanKPIs();
		autoSaveToDatabase();
		if (outgoingLoanTable != null) outgoingLoanTable.scrollTo(p);
	}

	@FXML
	public void onAddYouthPlayerManual() {
		YouthPlayerModel y = new YouthPlayerModel("Neues Talent", "Mitspielender Torwart", 17, 4.5, 2.0, 14, 14, 13, 15, 0.55, 0.22, 0.48);
		YouthAnalysisService.evaluate(y);
		youthDataList.add(y);
		refreshYouthKPIs();
		autoSaveToDatabase();
		if (youthTable != null) youthTable.scrollTo(y);
	}

	@FXML
	public void onAddScoutPlayerManual() {
		ScoutingPlayerModel s = new ScoutingPlayerModel("Neues Scouting-Ziel", "Stoßstürmer (A)", 21, 15000000L, 220000L, 1.0, 15, 14, 15, 0.58, 0.24, 0.54);
		ScoutingAnalysisService.evaluate(s, appConfig);
		scoutDataList.add(s);
		refreshScoutKPIs();
		refreshShadowSquad();
		saveScoutingSnapshotToDatabase();
		if (scoutTable != null) scoutTable.scrollTo(s);
	}

	@FXML
	public void onAddShadowPlayerManual() {
		ShadowSquadRowModel manualRow = new ShadowSquadRowModel("Stoßstürmer (A)", "Manueller Stammspieler", 500000L, 40000000L, "Manueller Kandidat 1", 12000000L, 180000L, "— Kein 2. Kandidat —", 0L, 0L, 28000000L, 320000L);
		shadowSquadDataList.add(manualRow);
		refreshShadowSquad();
		if (shadowSquadTable != null) shadowSquadTable.scrollTo(manualRow);
	}

	@FXML
	public void onClearList() {
		// Prüfen, ob wir im Scouting-Tab sind (Index 7)
		boolean isScoutingTabActive = (mainTabPane != null && mainTabPane.getSelectionModel().getSelectedIndex() == 7);

		if (isScoutingTabActive) {
			scoutDataList.clear();
			HistoryRepository.clearScoutingData();
			refreshScoutKPIs();
			refreshShadowSquad();

			if (lblScoutSignCount != null) lblScoutSignCount.setText("0 Spieler");
			if (lblScoutBargainCount != null) lblScoutBargainCount.setText("0 Spieler");
			if (lblScoutRejectCount != null) lblScoutRejectCount.setText("0 Spieler");

			statusLabel.setText("Scouting-Liste geleert (Kaderdaten blieben erhalten).");
			return;
		}

		// Standard: Gesamte Tabelle leeren
		playerDataList.clear();
		needsDataList.clear();
		depthChartDataList.clear();
		matrixDataList.clear();
		outgoingLoanDataList.clear();
		incomingLoanDataList.clear();
		youthDataList.clear();
		scoutDataList.clear();
		shadowSquadDataList.clear();
		registrationDataList.clear();
		historyDataList.clear();

		if (lblCriticalNeeds != null) lblCriticalNeeds.setText("0 Positionen");
		if (lblWarningNeeds != null) lblWarningNeeds.setText("0 Positionen");
		if (lblOptimalNeeds != null) lblOptimalNeeds.setText("0 Positionen");
		if (lblMatrixBestTactic != null) lblMatrixBestTactic.setText("—");
		if (lblMatrixBestAvgScore != null) lblMatrixBestAvgScore.setText("—");
		if (lblMatrixRetrainCount != null) lblMatrixRetrainCount.setText("0 Spieler");
		if (lblShadowTotalProfit != null) lblShadowTotalProfit.setText("€ 0");
		if (lblShadowTotalSavings != null) lblShadowTotalSavings.setText("€ 0 / Mon.");
		if (lblShadowCoveredSlots != null) lblShadowCoveredSlots.setText("0 / 0");
		if (lblRegisteredCount != null) lblRegisteredCount.setText("0 / 25 Slots");
		if (lblU21ExemptCount != null) lblU21ExemptCount.setText("0 U21 frei");
		if (lblRegistrationWages != null) lblRegistrationWages.setText("€ 0 / Mon.");
		if (lblImportTimestamp != null) lblImportTimestamp.setText("Stand: Noch kein Import");

		resetHistoryKpis();
		if (cmbHistoryPlayer != null) cmbHistoryPlayer.getItems().clear();

		refreshSquadKPIs();
		refreshLoanKPIs();
		refreshYouthKPIs();
		refreshScoutKPIs();
		onResetSalaryCalc();
		autoSaveToDatabase();
		statusLabel.setText("Tabelle geleert.");
	}
}