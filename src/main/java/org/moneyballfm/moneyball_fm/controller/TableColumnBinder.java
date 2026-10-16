package org.moneyballfm.moneyball_fm.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.moneyballfm.moneyball_fm.model.*;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TableColumnBinder {

	private static final NumberFormat EURO_FORMAT = NumberFormat.getNumberInstance(Locale.GERMANY);

	public static StringConverter<Long> createEuroConverter() {
		return new StringConverter<>() {
			@Override
			public String toString(Long value) {
				if (value == null || value == 0) return "€ 0";
				if (value >= 900_000_000L) return "Unverkäuflich";
				if (value < 0) return "- € " + EURO_FORMAT.format(Math.abs(value));
				return "€ " + EURO_FORMAT.format(value);
			}

			@Override
			public Long fromString(String string) {
				if (string == null || string.isBlank()) return 0L;
				if (string.toLowerCase().contains("unverkäuflich")) return 999_000_000L;
				boolean negative = string.contains("-");
				String clean = string.replaceAll("[^0-9]", "");
				if (clean.isEmpty()) return 0L;
				long val = Long.parseLong(clean);
				return negative ? -val : val;
			}
		};
	}

	public static void setupPlayerTable(
			TableView<PlayerRowModel> table,
			TableColumn<PlayerRowModel, String> colName,
			TableColumn<PlayerRowModel, String> colRole,
			TableColumn<PlayerRowModel, Boolean> colOnLoan,
			TableColumn<PlayerRowModel, Integer> colAge,
			TableColumn<PlayerRowModel, Integer> colAgility,
			TableColumn<PlayerRowModel, Integer> colPace,
			TableColumn<PlayerRowModel, Integer> colStamina,
			TableColumn<PlayerRowModel, Integer> colDetermination,
			TableColumn<PlayerRowModel, Long> colMarketValue,
			TableColumn<PlayerRowModel, Long> colFairMarketValue,
			TableColumn<PlayerRowModel, Long> colSalary,
			TableColumn<PlayerRowModel, Long> colMaxSalary,
			TableColumn<PlayerRowModel, Integer> colMinutes,
			TableColumn<PlayerRowModel, Double> colStat1,
			TableColumn<PlayerRowModel, Double> colStat2,
			TableColumn<PlayerRowModel, Double> colStat3,
			TableColumn<PlayerRowModel, Double> colScore,
			TableColumn<PlayerRowModel, String> colTier,
			TableColumn<PlayerRowModel, String> colSalaryCheck,
			TableColumn<PlayerRowModel, String> colQuality,
			ObservableList<String> availableRoles,
			BiConsumer<PlayerRowModel, String> onRoleChanged,
			Consumer<PlayerRowModel> onRecalc,
			Runnable onSave,
			Stage stage) {

		StringConverter<Long> euroConv = createEuroConverter();

		colName.setCellValueFactory(c -> c.getValue().nameProperty());
		colName.setCellFactory(TextFieldTableCell.forTableColumn());
		colName.setOnEditCommit(e -> {
			e.getRowValue().nameProperty().set(e.getNewValue());
			onSave.run();
		});

		colRole.setCellValueFactory(c -> c.getValue().roleProperty());
		colRole.setCellFactory(ComboBoxTableCell.forTableColumn(availableRoles));
		colRole.setOnEditCommit(e -> {
			String selectedRole = e.getNewValue();
			if (selectedRole != null && !selectedRole.isBlank()) {
				onRoleChanged.accept(e.getRowValue(), selectedRole);
			}
		});

		if (colOnLoan != null) {
			colOnLoan.setCellValueFactory(c -> c.getValue().onLoanProperty());
			colOnLoan.setCellFactory(ComboBoxTableCell.forTableColumn(true, false));
			colOnLoan.setOnEditCommit(e -> {
				e.getRowValue().onLoanProperty().set(e.getNewValue());
				onRecalc.accept(e.getRowValue());
			});
		}

		colAge.setCellValueFactory(c -> c.getValue().ageProperty().asObject());
		colAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colAge.setOnEditCommit(e -> {
			e.getRowValue().ageProperty().set(e.getNewValue());
			onRecalc.accept(e.getRowValue());
		});

		colAgility.setCellValueFactory(c -> c.getValue().agilityProperty().asObject());
		colAgility.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colAgility.setOnEditCommit(e -> { e.getRowValue().agilityProperty().set(e.getNewValue()); onSave.run(); });

		colPace.setCellValueFactory(c -> c.getValue().paceProperty().asObject());
		colPace.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colPace.setOnEditCommit(e -> { e.getRowValue().paceProperty().set(e.getNewValue()); onSave.run(); });

		colStamina.setCellValueFactory(c -> c.getValue().staminaProperty().asObject());
		colStamina.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colStamina.setOnEditCommit(e -> { e.getRowValue().staminaProperty().set(e.getNewValue()); onSave.run(); });

		if (colDetermination != null) {
			colDetermination.setCellValueFactory(c -> c.getValue().determinationProperty().asObject());
			colDetermination.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			colDetermination.setOnEditCommit(e -> { e.getRowValue().determinationProperty().set(e.getNewValue()); onSave.run(); });
		}

		if (colMarketValue != null) {
			colMarketValue.setCellValueFactory(c -> c.getValue().marketValueProperty().asObject());
			colMarketValue.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
			colMarketValue.setOnEditCommit(e -> { e.getRowValue().marketValueProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });
		}
		if (colFairMarketValue != null) {
			colFairMarketValue.setCellValueFactory(c -> c.getValue().fairMarketValueProperty().asObject());
			colFairMarketValue.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
			colFairMarketValue.setEditable(false);
		}
		if (colSalary != null) {
			colSalary.setCellValueFactory(c -> c.getValue().monthlySalaryProperty().asObject());
			colSalary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
			colSalary.setOnEditCommit(e -> { e.getRowValue().monthlySalaryProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });
		}
		if (colMaxSalary != null) {
			colMaxSalary.setCellValueFactory(c -> c.getValue().maxSalaryProperty().asObject());
			colMaxSalary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
			colMaxSalary.setEditable(false);
		}

		colMinutes.setCellValueFactory(c -> c.getValue().minutesPlayedProperty().asObject());
		colMinutes.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colMinutes.setOnEditCommit(e -> { e.getRowValue().minutesPlayedProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colStat1.setCellValueFactory(c -> c.getValue().statValue1Property().asObject());
		colStat1.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat1.setOnEditCommit(e -> { e.getRowValue().statValue1Property().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colStat2.setCellValueFactory(c -> c.getValue().statValue2Property().asObject());
		colStat2.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat2.setOnEditCommit(e -> { e.getRowValue().statValue2Property().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colStat3.setCellValueFactory(c -> c.getValue().statValue3Property().asObject());
		colStat3.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat3.setOnEditCommit(e -> { e.getRowValue().statValue3Property().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colScore.setCellValueFactory(c -> c.getValue().moneyballScoreProperty().asObject());
		colTier.setCellValueFactory(c -> c.getValue().salaryTierProperty());
		colSalaryCheck.setCellValueFactory(c -> c.getValue().salaryCheckProperty());
		colQuality.setCellValueFactory(c -> c.getValue().sampleQualityProperty());

		table.setRowFactory(tv -> {
			TableRow<PlayerRowModel> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty()) {
					PlayerDetailDialog.show(row.getItem(), stage);
				}
			});
			return row;
		});
	}

	public static void setupScoutTable(
			TableView<ScoutingPlayerModel> table,
			TableColumn<ScoutingPlayerModel, String> colName,
			TableColumn<ScoutingPlayerModel, String> colRole,
			TableColumn<ScoutingPlayerModel, Integer> colAge,
			TableColumn<ScoutingPlayerModel, Long> colPrice,
			TableColumn<ScoutingPlayerModel, Long> colWage,
			TableColumn<ScoutingPlayerModel, Double> colYears,
			TableColumn<ScoutingPlayerModel, Integer> colMinutes,
			TableColumn<ScoutingPlayerModel, String> colQuality,
			TableColumn<ScoutingPlayerModel, Integer> colAgility,
			TableColumn<ScoutingPlayerModel, Integer> colPace,
			TableColumn<ScoutingPlayerModel, Integer> colStamina,
			TableColumn<ScoutingPlayerModel, Double> colStat1,
			TableColumn<ScoutingPlayerModel, Double> colStat2,
			TableColumn<ScoutingPlayerModel, Double> colStat3,
			TableColumn<ScoutingPlayerModel, Double> colScore,
			TableColumn<ScoutingPlayerModel, Long> colMaxFee,
			TableColumn<ScoutingPlayerModel, Long> colMaxWage,
			TableColumn<ScoutingPlayerModel, String> colPriceCheck,
			TableColumn<ScoutingPlayerModel, String> colAction,
			ObservableList<String> availableRoles,
			Consumer<ScoutingPlayerModel> onRecalc,
			Runnable onSave,
			Stage stage) {

		StringConverter<Long> euroConv = createEuroConverter();

		colName.setCellValueFactory(c -> c.getValue().nameProperty());
		colName.setCellFactory(TextFieldTableCell.forTableColumn());
		colName.setOnEditCommit(e -> { e.getRowValue().nameProperty().set(e.getNewValue()); onSave.run(); });

		colRole.setCellValueFactory(c -> c.getValue().targetRoleProperty());
		colRole.setCellFactory(ComboBoxTableCell.forTableColumn(availableRoles));
		colRole.setOnEditCommit(e -> {
			String newRole = e.getNewValue();
			if (newRole != null && !newRole.isBlank()) {
				e.getRowValue().targetRoleProperty().set(newRole);
				TacticRole tr = TacticRole.fromDisplayName(newRole);
				if (tr == null) tr = TacticRole.fromPosition(newRole);
				applyScoutingStatsForRole(e.getRowValue(), tr, newRole);
				onRecalc.accept(e.getRowValue());
			}
		});

		colAge.setCellValueFactory(c -> c.getValue().ageProperty().asObject());
		colAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colAge.setOnEditCommit(e -> { e.getRowValue().ageProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colPrice.setCellValueFactory(c -> c.getValue().askingPriceProperty().asObject());
		colPrice.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colPrice.setOnEditCommit(e -> { e.getRowValue().askingPriceProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colWage.setCellValueFactory(c -> c.getValue().wageDemandProperty().asObject());
		colWage.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colWage.setOnEditCommit(e -> { e.getRowValue().wageDemandProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colYears.setCellValueFactory(c -> c.getValue().contractYearsProperty().asObject());
		colYears.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colYears.setOnEditCommit(e -> { e.getRowValue().contractYearsProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		if (colMinutes != null) {
			colMinutes.setCellValueFactory(c -> c.getValue().minutesProperty().asObject());
			colMinutes.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			colMinutes.setOnEditCommit(e -> { e.getRowValue().minutesProperty().set(e.getNewValue() != null ? e.getNewValue() : 0); onRecalc.accept(e.getRowValue()); });
		}

		if (colQuality != null) {
			colQuality.setCellValueFactory(c -> {
				int mins = c.getValue().minutesProperty().get();
				double s1 = c.getValue().statValue1Property().get();
				double s2 = c.getValue().statValue2Property().get();
				double s3 = c.getValue().statValue3Property().get();
				if ((s1 == 0.0 && s2 == 0.0 && s3 == 0.0) && mins == 0) return new SimpleStringProperty("⚪ Keine Daten (0 Min.)");
				if (mins < 450) return new SimpleStringProperty("🔴 Zu gering (< 5 Spiele)");
				if (mins < 900) return new SimpleStringProperty("🟡 Mäßig (5–10 Spiele)");
				return new SimpleStringProperty("🟢 Solide (10+ Spiele)");
			});
			colQuality.setEditable(false);
		}

		colAgility.setCellValueFactory(c -> c.getValue().agilityProperty().asObject());
		colAgility.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colAgility.setOnEditCommit(e -> { e.getRowValue().agilityProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colPace.setCellValueFactory(c -> c.getValue().paceProperty().asObject());
		colPace.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colPace.setOnEditCommit(e -> { e.getRowValue().paceProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colStamina.setCellValueFactory(c -> c.getValue().staminaProperty().asObject());
		colStamina.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colStamina.setOnEditCommit(e -> { e.getRowValue().staminaProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colStat1.setCellValueFactory(c -> c.getValue().statValue1Property().asObject());
		colStat1.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat1.setOnEditCommit(e -> {
			double v = e.getNewValue() != null ? e.getNewValue() : 0.0;
			e.getRowValue().statValue1Property().set(v);
			syncManualStatToRaw(e.getRowValue(), 1, v);
			onRecalc.accept(e.getRowValue());
		});

		colStat2.setCellValueFactory(c -> c.getValue().statValue2Property().asObject());
		colStat2.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat2.setOnEditCommit(e -> {
			double v = e.getNewValue() != null ? e.getNewValue() : 0.0;
			e.getRowValue().statValue2Property().set(v);
			syncManualStatToRaw(e.getRowValue(), 2, v);
			onRecalc.accept(e.getRowValue());
		});

		colStat3.setCellValueFactory(c -> c.getValue().statValue3Property().asObject());
		colStat3.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat3.setOnEditCommit(e -> {
			double v = e.getNewValue() != null ? e.getNewValue() : 0.0;
			e.getRowValue().statValue3Property().set(v);
			syncManualStatToRaw(e.getRowValue(), 3, v);
			onRecalc.accept(e.getRowValue());
		});

		colScore.setCellValueFactory(c -> c.getValue().scoutingScoreProperty().asObject());
		colScore.setEditable(false);

		colMaxFee.setCellValueFactory(c -> c.getValue().maxTransferFeeProperty().asObject());
		colMaxFee.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colMaxFee.setEditable(false);

		colMaxWage.setCellValueFactory(c -> c.getValue().maxWageProperty().asObject());
		colMaxWage.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colMaxWage.setEditable(false);

		colPriceCheck.setCellValueFactory(c -> c.getValue().priceCheckProperty());
		colAction.setCellValueFactory(c -> c.getValue().transferActionProperty());

		table.setRowFactory(tv -> {
			TableRow<ScoutingPlayerModel> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty()) {
					PlayerDetailDialog.show(row.getItem(), stage);
				}
			});
			return row;
		});
	}

	public static void setupLoanColumns(
			TableView<LoanPlayerModel> table,
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
			ObservableList<String> availableRoles,
			Consumer<LoanPlayerModel> onRecalc,
			Runnable onSave,
			Stage stage) {

		if (table == null || colName == null) return;
		StringConverter<Long> euroConv = createEuroConverter();

		colName.setCellValueFactory(c -> c.getValue().playerNameProperty());
		colName.setCellFactory(TextFieldTableCell.forTableColumn());
		colName.setOnEditCommit(e -> { e.getRowValue().playerNameProperty().set(e.getNewValue()); onSave.run(); });

		if (colRole != null) {
			colRole.setCellValueFactory(c -> c.getValue().targetRoleProperty());
			colRole.setCellFactory(ComboBoxTableCell.forTableColumn(availableRoles));
			colRole.setOnEditCommit(e -> { e.getRowValue().targetRoleProperty().set(e.getNewValue()); onSave.run(); });
		}
		if (colAge != null) {
			colAge.setCellValueFactory(c -> c.getValue().ageProperty().asObject());
			colAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			colAge.setOnEditCommit(e -> { e.getRowValue().ageProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });
		}
		if (colClub != null) {
			colClub.setCellValueFactory(c -> c.getValue().clubAndLeagueProperty());
			colClub.setCellFactory(TextFieldTableCell.forTableColumn());
			colClub.setOnEditCommit(e -> { e.getRowValue().clubAndLeagueProperty().set(e.getNewValue()); onSave.run(); });
		}
		if (colGames != null) {
			colGames.setCellValueFactory(c -> c.getValue().gamesPlayedProperty().asObject());
			colGames.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			colGames.setOnEditCommit(e -> { e.getRowValue().gamesPlayedProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });
		}
		if (colMinutes != null) {
			colMinutes.setCellValueFactory(c -> c.getValue().minutesPlayedProperty().asObject());
			colMinutes.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
			colMinutes.setOnEditCommit(e -> { e.getRowValue().minutesPlayedProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });
		}

		if (colRating != null) {
			colRating.setCellValueFactory(c -> c.getValue().averageRatingProperty().asObject());
			colRating.setCellFactory(col -> new TableCell<>() {
				private final TextField textField = new TextField();
				{
					textField.setOnAction(ev -> commitHelper());
					textField.focusedProperty().addListener((o, was, is) -> { if (!is && isEditing()) commitHelper(); });
				}
				private void commitHelper() {
					try { commitEdit(Double.parseDouble(textField.getText().trim().replace(",", "."))); }
					catch (Exception ex) { cancelEdit(); }
				}
				@Override public void startEdit() {
					LoanPlayerModel r = getTableRow() != null ? getTableRow().getItem() : null;
					if (r != null && r.getLoanType() == LoanPlayerModel.LoanType.INCOMING) return;
					super.startEdit();
					textField.setText(getItem() != null && getItem() > 0 ? String.valueOf(getItem()) : "");
					setText(null); setGraphic(textField); textField.selectAll(); textField.requestFocus();
				}
				@Override public void cancelEdit() { super.cancelEdit(); setGraphic(null); updateText(getItem()); }
				@Override protected void updateItem(Double val, boolean empty) {
					super.updateItem(val, empty);
					if (empty) { setText(null); setGraphic(null); }
					else if (isEditing()) { setText(null); setGraphic(textField); }
					else { setGraphic(null); updateText(val); }
				}
				private void updateText(Double val) {
					LoanPlayerModel r = getTableRow() != null ? getTableRow().getItem() : null;
					if (r != null && r.getLoanType() == LoanPlayerModel.LoanType.INCOMING) setText("—");
					else setText(val != null && val > 0 ? String.format(Locale.GERMANY, "%.2f", val) : "—");
				}
			});
			colRating.setOnEditCommit(e -> { e.getRowValue().averageRatingProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });
		}

		if (colScore != null) {
			colScore.setCellValueFactory(c -> c.getValue().moneyballScoreProperty().asObject());
			colScore.setEditable(false);
		}
		if (colClause != null) {
			colClause.setCellValueFactory(c -> c.getValue().clauseFulfilledProperty());
			colClause.setCellFactory(ComboBoxTableCell.forTableColumn(true, false));
			colClause.setOnEditCommit(e -> { e.getRowValue().clauseFulfilledProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });
		}
		if (colMarketValue != null) {
			colMarketValue.setCellValueFactory(c -> c.getValue().marketValueProperty().asObject());
			colMarketValue.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
			colMarketValue.setOnEditCommit(e -> { e.getRowValue().marketValueProperty().set(e.getNewValue()); onSave.run(); });
		}
		if (colRecommendation != null) colRecommendation.setCellValueFactory(c -> c.getValue().recommendationProperty());
		if (colFuturePlan != null) colFuturePlan.setCellValueFactory(c -> c.getValue().futurePlanProperty());

		table.setRowFactory(tv -> {
			TableRow<LoanPlayerModel> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty()) {
					PlayerDetailDialog.show(row.getItem(), stage);
				}
			});
			return row;
		});
	}

	public static void setupYouthTable(
			TableView<YouthPlayerModel> table,
			TableColumn<YouthPlayerModel, String> colName,
			TableColumn<YouthPlayerModel, String> colRole,
			TableColumn<YouthPlayerModel, Integer> colAge,
			TableColumn<YouthPlayerModel, Double> colPotStars,
			TableColumn<YouthPlayerModel, Double> colCurStars,
			TableColumn<YouthPlayerModel, Integer> colAgility,
			TableColumn<YouthPlayerModel, Integer> colPace,
			TableColumn<YouthPlayerModel, Integer> colStamina,
			TableColumn<YouthPlayerModel, Integer> colDeterm,
			TableColumn<YouthPlayerModel, Double> colStat1,
			TableColumn<YouthPlayerModel, Double> colStat2,
			TableColumn<YouthPlayerModel, Double> colStat3,
			TableColumn<YouthPlayerModel, Double> colScore,
			TableColumn<YouthPlayerModel, String> colForecast,
			TableColumn<YouthPlayerModel, String> colRecommendation,
			TableColumn<YouthPlayerModel, Long> colMaxSalary,
			ObservableList<String> availableRoles,
			Consumer<YouthPlayerModel> onRecalc,
			Runnable onSave,
			Stage stage) {

		if (table == null) return;
		ObservableList<Double> starOptions = FXCollections.observableArrayList(0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0);
		StringConverter<Double> starConverter = new StringConverter<>() {
			@Override public String toString(Double v) { return (v == null || v <= 0.0) ? "—" : String.format(Locale.GERMANY, "%.1f ★", v); }
			@Override public Double fromString(String s) {
				if (s == null || s.isBlank() || s.equals("—")) return 0.0;
				try { return Double.parseDouble(s.replace("★", "").trim().replace(",", ".")); } catch (Exception e) { return 0.0; }
			}
		};

		colName.setCellValueFactory(c -> c.getValue().nameProperty());
		colName.setCellFactory(TextFieldTableCell.forTableColumn());
		colName.setOnEditCommit(e -> { e.getRowValue().nameProperty().set(e.getNewValue()); onSave.run(); });

		colRole.setCellValueFactory(c -> c.getValue().targetRoleProperty());
		colRole.setCellFactory(ComboBoxTableCell.forTableColumn(availableRoles));
		colRole.setOnEditCommit(e -> { e.getRowValue().targetRoleProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colAge.setCellValueFactory(c -> c.getValue().ageProperty().asObject());
		colAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colAge.setOnEditCommit(e -> { e.getRowValue().ageProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colPotStars.setCellValueFactory(c -> c.getValue().potentialStarsProperty().asObject());
		colPotStars.setCellFactory(ComboBoxTableCell.forTableColumn(starConverter, starOptions));
		colPotStars.setOnEditCommit(e -> { e.getRowValue().potentialStarsProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colCurStars.setCellValueFactory(c -> c.getValue().currentStarsProperty().asObject());
		colCurStars.setCellFactory(ComboBoxTableCell.forTableColumn(starConverter, starOptions));
		colCurStars.setOnEditCommit(e -> { e.getRowValue().currentStarsProperty().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colAgility.setCellValueFactory(c -> c.getValue().agilityProperty().asObject());
		colAgility.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colAgility.setOnEditCommit(e -> { e.getRowValue().agilityProperty().set(e.getNewValue()); onSave.run(); });

		colPace.setCellValueFactory(c -> c.getValue().paceProperty().asObject());
		colPace.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colPace.setOnEditCommit(e -> { e.getRowValue().paceProperty().set(e.getNewValue()); onSave.run(); });

		colStamina.setCellValueFactory(c -> c.getValue().staminaProperty().asObject());
		colStamina.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colStamina.setOnEditCommit(e -> { e.getRowValue().staminaProperty().set(e.getNewValue()); onSave.run(); });

		colDeterm.setCellValueFactory(c -> c.getValue().determinationProperty().asObject());
		colDeterm.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colDeterm.setOnEditCommit(e -> { e.getRowValue().determinationProperty().set(e.getNewValue()); onSave.run(); });

		colStat1.setCellValueFactory(c -> c.getValue().statValue1Property().asObject());
		colStat1.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat1.setOnEditCommit(e -> { e.getRowValue().statValue1Property().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colStat2.setCellValueFactory(c -> c.getValue().statValue2Property().asObject());
		colStat2.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat2.setOnEditCommit(e -> { e.getRowValue().statValue2Property().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colStat3.setCellValueFactory(c -> c.getValue().statValue3Property().asObject());
		colStat3.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colStat3.setOnEditCommit(e -> { e.getRowValue().statValue3Property().set(e.getNewValue()); onRecalc.accept(e.getRowValue()); });

		colScore.setCellValueFactory(c -> c.getValue().talentScoreProperty().asObject());
		colScore.setEditable(false);

		colForecast.setCellValueFactory(c -> c.getValue().forecastFirstTeamProperty());
		colRecommendation.setCellValueFactory(c -> c.getValue().pipelineRecommendationProperty());

		colMaxSalary.setCellValueFactory(c -> c.getValue().maxYouthSalaryProperty().asObject());
		colMaxSalary.setCellFactory(TextFieldTableCell.forTableColumn(createEuroConverter()));
		colMaxSalary.setEditable(false);

		table.setRowFactory(tv -> {
			TableRow<YouthPlayerModel> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty()) {
					PlayerDetailDialog.show(row.getItem(), stage);
				}
			});
			return row;
		});
	}

	public static void setupNeedsTable(
			TableView<PositionNeedModel> table,
			TableColumn<PositionNeedModel, String> colNeedSlot,
			TableColumn<PositionNeedModel, String> colNeedRole,
			TableColumn<PositionNeedModel, Integer> colNeedCount,
			TableColumn<PositionNeedModel, String> colNeedBestPlayer,
			TableColumn<PositionNeedModel, Double> colNeedBestScore,
			TableColumn<PositionNeedModel, Double> colNeedAvgScore,
			TableColumn<PositionNeedModel, Double> colNeedAvgAge,
			TableColumn<PositionNeedModel, Integer> colNeedCritContracts,
			TableColumn<PositionNeedModel, String> colNeedDiagnosis,
			TableColumn<PositionNeedModel, String> colNeedRecommendation,
			TableColumn<PositionNeedModel, String> colNeedTargetProfile) {

		if (table == null) return;
		colNeedSlot.setCellValueFactory(c -> c.getValue().slotNameProperty());
		colNeedRole.setCellValueFactory(c -> c.getValue().roleLabelProperty());
		colNeedCount.setCellValueFactory(c -> c.getValue().playerCountProperty().asObject());
		colNeedBestPlayer.setCellValueFactory(c -> c.getValue().bestPlayerNameProperty());
		colNeedBestScore.setCellValueFactory(c -> c.getValue().bestScoreProperty().asObject());
		colNeedAvgScore.setCellValueFactory(c -> c.getValue().averageScoreProperty().asObject());
		colNeedAvgAge.setCellValueFactory(c -> c.getValue().averageAgeProperty().asObject());
		colNeedCritContracts.setCellValueFactory(c -> c.getValue().criticalContractsProperty().asObject());
		colNeedDiagnosis.setCellValueFactory(c -> c.getValue().diagnosisProperty());
		colNeedRecommendation.setCellValueFactory(c -> c.getValue().recommendationProperty());
		colNeedTargetProfile.setCellValueFactory(c -> c.getValue().targetProfileProperty());
	}

	public static void setupDepthChartTable(
			TableView<DepthChartRowModel> table,
			TableColumn<DepthChartRowModel, String> colDepthRole,
			TableColumn<DepthChartRowModel, Integer> colDepthRank,
			TableColumn<DepthChartRowModel, String> colDepthPlayerName,
			TableColumn<DepthChartRowModel, String> colDepthScore,
			TableColumn<DepthChartRowModel, String> colDepthTeamRole,
			TableColumn<DepthChartRowModel, String> colDepthAge,
			TableColumn<DepthChartRowModel, String> colDepthSalary) {

		if (table == null) return;
		colDepthRole.setCellValueFactory(c -> c.getValue().tacticalRoleProperty());
		colDepthRank.setCellValueFactory(c -> c.getValue().rankProperty().asObject());
		colDepthPlayerName.setCellValueFactory(c -> c.getValue().playerNameProperty());
		colDepthScore.setCellValueFactory(c -> c.getValue().moneyballScoreProperty());
		colDepthTeamRole.setCellValueFactory(c -> c.getValue().teamRoleProperty());
		colDepthAge.setCellValueFactory(c -> c.getValue().ageProperty());
		colDepthSalary.setCellValueFactory(c -> c.getValue().monthlySalaryProperty());
	}

	public static void setupShadowSquadTable(
			TableView<ShadowSquadRowModel> table,
			TableColumn<ShadowSquadRowModel, String> colShadowRole,
			TableColumn<ShadowSquadRowModel, String> colShadowStarter,
			TableColumn<ShadowSquadRowModel, Long> colShadowStarterSalary,
			TableColumn<ShadowSquadRowModel, Long> colShadowStarterValue,
			TableColumn<ShadowSquadRowModel, String> colShadowC1Name,
			TableColumn<ShadowSquadRowModel, Long> colShadowC1Fee,
			TableColumn<ShadowSquadRowModel, Long> colShadowC1Salary,
			TableColumn<ShadowSquadRowModel, String> colShadowC2Name,
			TableColumn<ShadowSquadRowModel, Long> colShadowC2Fee,
			TableColumn<ShadowSquadRowModel, Long> colShadowC2Salary,
			TableColumn<ShadowSquadRowModel, Long> colShadowNetGain,
			TableColumn<ShadowSquadRowModel, Long> colShadowWageSavings) {

		if (table == null) return;
		StringConverter<Long> euroConv = createEuroConverter();

		colShadowRole.setCellValueFactory(c -> c.getValue().tacticalRoleProperty());
		colShadowStarter.setCellValueFactory(c -> c.getValue().starterNameProperty());
		colShadowStarterSalary.setCellValueFactory(c -> c.getValue().starterSalaryProperty().asObject());
		colShadowStarterSalary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colShadowStarterValue.setCellValueFactory(c -> c.getValue().starterMarketValueProperty().asObject());
		colShadowStarterValue.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colShadowC1Name.setCellValueFactory(c -> c.getValue().candidate1NameProperty());
		colShadowC1Fee.setCellValueFactory(c -> c.getValue().candidate1FeeProperty().asObject());
		colShadowC1Fee.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colShadowC1Salary.setCellValueFactory(c -> c.getValue().candidate1SalaryProperty().asObject());
		colShadowC1Salary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colShadowC2Name.setCellValueFactory(c -> c.getValue().candidate2NameProperty());
		colShadowC2Fee.setCellValueFactory(c -> c.getValue().candidate2FeeProperty().asObject());
		colShadowC2Fee.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colShadowC2Salary.setCellValueFactory(c -> c.getValue().candidate2SalaryProperty().asObject());
		colShadowC2Salary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colShadowNetGain.setCellValueFactory(c -> c.getValue().netTransferGainProperty().asObject());
		colShadowNetGain.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colShadowWageSavings.setCellValueFactory(c -> c.getValue().monthlyWageSavingsProperty().asObject());
		colShadowWageSavings.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
	}

	public static void setupRegistrationTable(
			TableView<RegistrationRowModel> table,
			TableColumn<RegistrationRowModel, Boolean> colRegActive,
			TableColumn<RegistrationRowModel, String> colRegName,
			TableColumn<RegistrationRowModel, String> colRegRole,
			TableColumn<RegistrationRowModel, Integer> colRegAge,
			TableColumn<RegistrationRowModel, Boolean> colRegU21,
			TableColumn<RegistrationRowModel, Integer> colRegMinutes,
			TableColumn<RegistrationRowModel, Double> colRegRating,
			TableColumn<RegistrationRowModel, String> colRegTrend,
			TableColumn<RegistrationRowModel, Long> colRegSalary,
			TableColumn<RegistrationRowModel, String> colRegRecommendation,
			Runnable onRefreshKPIs) {

		if (table == null) return;
		StringConverter<Long> euroConv = createEuroConverter();

		colRegActive.setCellValueFactory(c -> c.getValue().registeredProperty());
		colRegActive.setCellFactory(ComboBoxTableCell.forTableColumn(true, false));
		colRegActive.setOnEditCommit(e -> { e.getRowValue().registeredProperty().set(e.getNewValue()); onRefreshKPIs.run(); });
		colRegName.setCellValueFactory(c -> c.getValue().nameProperty());
		colRegRole.setCellValueFactory(c -> c.getValue().roleProperty());
		colRegAge.setCellValueFactory(c -> c.getValue().ageProperty().asObject());
		colRegU21.setCellValueFactory(c -> c.getValue().u21ExemptProperty());
		colRegU21.setCellFactory(ComboBoxTableCell.forTableColumn(true, false));
		colRegU21.setOnEditCommit(e -> { e.getRowValue().u21ExemptProperty().set(e.getNewValue()); e.getRowValue().evaluate(); onRefreshKPIs.run(); });
		colRegMinutes.setCellValueFactory(c -> c.getValue().testMinutesProperty().asObject());
		colRegMinutes.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		colRegMinutes.setOnEditCommit(e -> { e.getRowValue().testMinutesProperty().set(e.getNewValue()); e.getRowValue().evaluate(); onRefreshKPIs.run(); });
		colRegRating.setCellValueFactory(c -> c.getValue().testRatingProperty().asObject());
		colRegRating.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		colRegRating.setOnEditCommit(e -> { e.getRowValue().testRatingProperty().set(e.getNewValue()); e.getRowValue().evaluate(); onRefreshKPIs.run(); });
		colRegTrend.setCellValueFactory(c -> c.getValue().fitnessTrendProperty());
		colRegSalary.setCellValueFactory(c -> c.getValue().salaryProperty().asObject());
		colRegSalary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		colRegRecommendation.setCellValueFactory(c -> c.getValue().recommendationProperty());
	}

	public static void setupHistoryTable(
			TableView<PlayerHistoryEntry> table,
			TableColumn<PlayerHistoryEntry, String> colSeason,
			TableColumn<PlayerHistoryEntry, String> colDate,
			TableColumn<PlayerHistoryEntry, String> colRole,
			TableColumn<PlayerHistoryEntry, Integer> colAge,
			TableColumn<PlayerHistoryEntry, Long> colMarketValue,
			TableColumn<PlayerHistoryEntry, Long> colSalary,
			TableColumn<PlayerHistoryEntry, Integer> colMinutes,
			TableColumn<PlayerHistoryEntry, Double> colScore,
			TableColumn<PlayerHistoryEntry, String> colRecommendation) {

		if (table == null) return;
		StringConverter<Long> euroConv = createEuroConverter();

		if (colSeason != null) colSeason.setCellValueFactory(c -> c.getValue().seasonProperty());
		if (colDate != null) colDate.setCellValueFactory(c -> c.getValue().snapshotDateProperty());
		if (colRole != null) colRole.setCellValueFactory(c -> c.getValue().roleProperty());

		if (colAge != null) {
			colAge.setCellValueFactory(c -> c.getValue().ageProperty().asObject());
			colAge.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		}

		if (colMarketValue != null) {
			colMarketValue.setCellValueFactory(c -> c.getValue().marketValueProperty().asObject());
			colMarketValue.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		}

		if (colSalary != null) {
			colSalary.setCellValueFactory(c -> c.getValue().monthlySalaryProperty().asObject());
			colSalary.setCellFactory(TextFieldTableCell.forTableColumn(euroConv));
		}

		if (colMinutes != null) {
			colMinutes.setCellValueFactory(c -> c.getValue().minutesPlayedProperty().asObject());
			colMinutes.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		}

		if (colScore != null) {
			colScore.setCellValueFactory(c -> c.getValue().moneyballScoreProperty().asObject());
			colScore.setCellFactory(col -> new TableCell<>() {
				@Override
				protected void updateItem(Double val, boolean empty) {
					super.updateItem(val, empty);
					if (empty || val == null) {
						setText(null);
						setStyle("");
					} else {
						setText(String.format(Locale.GERMANY, "%.1f", val));
						if (val >= 80.0) {
							setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
						} else if (val >= 65.0) {
							setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
						} else {
							setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
						}
					}
				}
			});
		}

		if (colRecommendation != null) {
			colRecommendation.setCellValueFactory(c -> c.getValue().recommendationProperty());
		}
	}

	public static void syncManualStatToRaw(ScoutingPlayerModel p, int statSlot, double val) {
		String role = p.targetRoleProperty().get() != null ? p.targetRoleProperty().get().toUpperCase() : "";
		if (role.contains("TW") || role.contains("TORWART")) {
			if (statSlot == 1) p.savePctProperty().set(val > 1.0 ? val : val * 100.0);
			if (statSlot == 2) p.xgPrevented90Property().set(val);
			if (statSlot == 3) p.passCompletionPctProperty().set(val > 1.0 ? val : val * 100.0);
		} else if (role.contains("STO") || role.contains("STÜRMER")) {
			if (statSlot == 1) p.xg90Property().set(val);
			if (statSlot == 2) p.xa90Property().set(val);
			if (statSlot == 3) p.shotsOnTargetPctProperty().set(val > 1.0 ? val : val * 100.0);
		} else {
			if (statSlot == 1) p.passCompletionPctProperty().set(val > 1.0 ? val : val * 100.0);
			if (statSlot == 2) p.interceptions90Property().set(val);
			if (statSlot == 3) p.progressivePasses90Property().set(val);
		}
	}

	public static void applyScoutingStatsForRole(ScoutingPlayerModel p, TacticRole role, String roleNameFallback) {
		String roleText = "";
		if (role != null) roleText += (role.getDisplayName() != null ? role.getDisplayName() : "") + " " + role.name() + " ";
		if (roleNameFallback != null) roleText += roleNameFallback;
		roleText = roleText.toUpperCase().trim();

		double s1 = 0.0, s2 = 0.0, s3 = 0.0;

		if (roleText.contains("STO") || roleText.contains("STÜRMER") || roleText.contains("ANGREIFEN") || roleText.contains("ST (Z)")) {
			s1 = p.xg90Property().get();
			s2 = p.xa90Property().get();
			double shots = p.shotsOnTargetPctProperty().get();
			s3 = shots > 1.0 ? shots / 100.0 : shots;
		} else if (roleText.contains("MTW") || roleText.contains("TORWART") || roleText.contains("TW")) {
			double save = p.savePctProperty().get();
			s1 = save > 1.0 ? save / 100.0 : save;
			s2 = p.xgPrevented90Property().get();
			double pass = p.passCompletionPctProperty().get();
			s3 = pass > 1.0 ? pass / 100.0 : pass;
		} else if (roleText.contains("OM") || roleText.contains("QM") || roleText.contains("FLÜGEL") || roleText.contains("WINGER")) {
			s1 = p.xa90Property().get();
			double cross = p.crossAccuracyPctProperty().get();
			s2 = cross > 1.0 ? cross / 100.0 : cross;
			double pass = p.passCompletionPctProperty().get();
			s3 = pass > 1.0 ? pass / 100.0 : pass;
		} else if (roleText.contains("REG") || roleText.contains("ZURÜCKGEZOGENER") || roleText.contains("SPIELMACHER")) {
			double pass = p.passCompletionPctProperty().get();
			s1 = pass > 1.0 ? pass / 100.0 : pass;
			s2 = p.progressivePasses90Property().get();
			double cross = p.crossAccuracyPctProperty().get();
			s3 = cross > 1.0 ? cross / 100.0 : cross;
		} else if (roleText.contains("ZSM") || roleText.contains("ZENTRAL") || roleText.contains("DM") || roleText.contains("BALLEROBERNDER")) {
			double pass = p.passCompletionPctProperty().get();
			s1 = pass > 1.0 ? pass / 100.0 : pass;
			s2 = p.interceptions90Property().get();
			double tackles = p.tacklesWonPctProperty().get();
			s3 = tackles > 1.0 ? tackles / 100.0 : tackles;
		} else if (roleText.contains("FV") || roleText.contains("KFV") || roleText.contains("AUSSENVERTEIDIGER") || roleText.contains("FLÜGELVERTEIDIGER")) {
			s1 = p.crossesAttempted90Property().get();
			s2 = p.xa90Property().get();
			s3 = p.interceptions90Property().get();
		} else if (roleText.contains("BSV") || roleText.contains("BALLSPIELEND")) {
			double pass = p.passCompletionPctProperty().get();
			s1 = pass > 1.0 ? pass / 100.0 : pass;
			s2 = p.interceptions90Property().get();
			s3 = p.progressivePasses90Property().get();
		} else if (roleText.contains("IV") || roleText.contains("INNENVERTEIDIGER") || roleText.contains("VERTEIDIGER")) {
			double tackles = p.tacklesWonPctProperty().get();
			s1 = tackles > 1.0 ? tackles / 100.0 : tackles;
			double headers = p.headersWonPctProperty().get();
			s2 = headers > 1.0 ? headers / 100.0 : headers;
			s3 = (double) p.mistakesLeadingToGoalProperty().get();
		} else if (roleText.contains("HRV") || roleText.contains("HALBRAUM")) {
			double pass = p.passCompletionPctProperty().get();
			s1 = pass > 1.0 ? pass / 100.0 : pass;
			double tackles = p.tacklesWonPctProperty().get();
			s2 = tackles > 1.0 ? tackles / 100.0 : tackles;
			s3 = p.interceptions90Property().get();
		} else {
			double pass = p.passCompletionPctProperty().get();
			s1 = pass > 1.0 ? pass / 100.0 : pass;
			s2 = p.interceptions90Property().get();
			s3 = p.progressivePasses90Property().get();
		}

		if (s1 > 0.0 || s2 > 0.0 || s3 > 0.0 || (p.statValue1Property().get() == 0.0 && p.statValue2Property().get() == 0.0)) {
			p.statValue1Property().set(s1);
			p.statValue2Property().set(s2);
			p.statValue3Property().set(s3);
		}
	}
}