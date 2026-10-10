package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class PlayerRowModel {
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty role = new SimpleStringProperty();
	private final IntegerProperty age = new SimpleIntegerProperty();
	private final LongProperty marketValue = new SimpleLongProperty();
	private final LongProperty fairMarketValue = new SimpleLongProperty();
	private final LongProperty monthlySalary = new SimpleLongProperty();
	private final LongProperty maxSalary = new SimpleLongProperty();
	private final DoubleProperty contractYears = new SimpleDoubleProperty();
	private final IntegerProperty minutesPlayed = new SimpleIntegerProperty();

	// Vertragsart & Verein (aus FM-Spalten ART und VEREIN)
	private final StringProperty contractType = new SimpleStringProperty("Vollzeitvertrag");
	private final StringProperty currentClub = new SimpleStringProperty("");
	private final BooleanProperty onLoan = new SimpleBooleanProperty(false);

	// Physis & Soft Skills
	private final IntegerProperty agility = new SimpleIntegerProperty();
	private final IntegerProperty pace = new SimpleIntegerProperty();
	private final IntegerProperty stamina = new SimpleIntegerProperty();
	private final IntegerProperty determination = new SimpleIntegerProperty();

	// Die drei sichtbaren Hauptwerte & ihre dynamischen Bezeichnungen
	private final DoubleProperty statValue1 = new SimpleDoubleProperty();
	private final DoubleProperty statValue2 = new SimpleDoubleProperty();
	private final DoubleProperty statValue3 = new SimpleDoubleProperty();
	private final StringProperty stat1Label = new SimpleStringProperty("Stat 1");
	private final StringProperty stat2Label = new SimpleStringProperty("Stat 2");
	private final StringProperty stat3Label = new SimpleStringProperty("Stat 3");

	// Berechnete Felder (automatische Aktualisierung)
	private final DoubleProperty moneyballScore = new SimpleDoubleProperty(0.0);
	private final StringProperty salaryTier = new SimpleStringProperty("-");
	private final StringProperty salaryCheck = new SimpleStringProperty("-");
	private final StringProperty sampleQuality = new SimpleStringProperty("-");

	private final Player rawPlayer;

	public PlayerRowModel(Player player, double s1, double s2, double s3) {
		this.rawPlayer = player;
		this.name.set(player.getName());
		this.role.set(player.getRole());
		this.age.set(player.getAge());
		this.marketValue.set(player.getMarketValue());
		this.monthlySalary.set(player.getMonthlySalary());
		this.contractYears.set(player.getContractYears());
		this.minutesPlayed.set(player.getMinutesPlayed());

		this.contractType.set(player.getContractType());
		this.currentClub.set(player.getCurrentClub());
		this.onLoan.set(player.isOnLoan());

		this.agility.set(player.getAgility());
		this.pace.set(player.getPace());
		this.stamina.set(player.getStamina());
		this.determination.set(player.getDetermination());

		this.statValue1.set(s1);
		this.statValue2.set(s2);
		this.statValue3.set(s3);
	}

	public Player getRawPlayer() { return rawPlayer; }

	public StringProperty nameProperty() { return name; }
	public StringProperty roleProperty() { return role; }
	public IntegerProperty ageProperty() { return age; }
	public LongProperty marketValueProperty() { return marketValue; }
	public LongProperty fairMarketValueProperty() { return fairMarketValue; }
	public LongProperty monthlySalaryProperty() { return monthlySalary; }
	public LongProperty maxSalaryProperty() { return maxSalary; }
	public DoubleProperty contractYearsProperty() { return contractYears; }
	public IntegerProperty minutesPlayedProperty() { return minutesPlayed; }

	// Getter & Properties für Leih-Status / Verein
	public StringProperty contractTypeProperty() { return contractType; }
	public String getContractType() { return contractType.get(); }
	public void setContractType(String type) { this.contractType.set(type); }

	public StringProperty currentClubProperty() { return currentClub; }
	public String getCurrentClub() { return currentClub.get(); }
	public void setCurrentClub(String club) { this.currentClub.set(club); }

	public BooleanProperty onLoanProperty() { return onLoan; }
	public boolean isOnLoan() { return onLoan.get(); }
	public void setOnLoan(boolean val) { this.onLoan.set(val); }

	public IntegerProperty agilityProperty() { return agility; }
	public IntegerProperty paceProperty() { return pace; }
	public IntegerProperty staminaProperty() { return stamina; }
	public IntegerProperty determinationProperty() { return determination; }

	public DoubleProperty statValue1Property() { return statValue1; }
	public DoubleProperty statValue2Property() { return statValue2; }
	public DoubleProperty statValue3Property() { return statValue3; }

	public StringProperty stat1LabelProperty() { return stat1Label; }
	public StringProperty stat2LabelProperty() { return stat2Label; }
	public StringProperty stat3LabelProperty() { return stat3Label; }

	public void setStatLabels(String s1, String s2, String s3) {
		this.stat1Label.set(s1);
		this.stat2Label.set(s2);
		this.stat3Label.set(s3);
	}

	public DoubleProperty moneyballScoreProperty() { return moneyballScore; }
	public StringProperty salaryTierProperty() { return salaryTier; }
	public StringProperty salaryCheckProperty() { return salaryCheck; }
	public StringProperty sampleQualityProperty() { return sampleQuality; }
}