package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class PlayerRowModel {
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty role = new SimpleStringProperty();
	private final IntegerProperty age = new SimpleIntegerProperty();
	private final LongProperty marketValue = new SimpleLongProperty();
	private final LongProperty monthlySalary = new SimpleLongProperty();
	private final DoubleProperty contractYears = new SimpleDoubleProperty();
	private final IntegerProperty minutesPlayed = new SimpleIntegerProperty();

	//Die drei sichtbaren Hauptwerte aus einer Excel
	private final DoubleProperty statValue1 = new SimpleDoubleProperty();
	private final DoubleProperty statValue2 = new SimpleDoubleProperty();
	private final DoubleProperty statValue3 = new SimpleDoubleProperty();

	//Berechnete Felder (automatische Aktualisierung)
	private final DoubleProperty moneyballScore = new SimpleDoubleProperty();
	private final StringProperty salaryTier = new SimpleStringProperty();
	private final StringProperty salaryCheck = new SimpleStringProperty();
	private final StringProperty sampleQuality = new SimpleStringProperty();

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

		this.statValue1.set(s1);
		this.statValue2.set(s2);
		this.statValue3.set(s3);
	}

	public Player getRawPlayer() { return rawPlayer; }

	public StringProperty nameProperty() { return name; }
	public StringProperty roleProperty() { return role; }
	public IntegerProperty ageProperty() { return age; }
	public LongProperty marketValueProperty() { return marketValue; }
	public LongProperty monthlySalaryProperty() { return monthlySalary; }
	public DoubleProperty contractYearsProperty() { return contractYears; }
	public IntegerProperty minutesPlayedProperty() { return minutesPlayed; }

	public DoubleProperty statValue1Property() { return statValue1; }
	public DoubleProperty statValue2Property() { return statValue2; }
	public DoubleProperty statValue3Property() { return statValue3; }

	public DoubleProperty moneyballScoreProperty() { return moneyballScore; }
	public StringProperty salaryTierProperty() { return salaryTier; }
	public StringProperty salaryCheckProperty() { return salaryCheck; }
	public StringProperty sampleQualityProperty() { return sampleQuality; }


}
