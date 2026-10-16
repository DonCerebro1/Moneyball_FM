package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class PlayerHistoryEntry {
	private final StringProperty snapshotDate;
	private final StringProperty season;
	private final StringProperty role;
	private final IntegerProperty age;
	private final LongProperty marketValue;
	private final LongProperty monthlySalary;
	private final IntegerProperty minutesPlayed;
	private final DoubleProperty moneyballScore;
	private final StringProperty recommendation;

	public PlayerHistoryEntry(String snapshotDate, String season, String role, int age,
	                          long marketValue, long monthlySalary, int minutesPlayed,
	                          double moneyballScore, String recommendation) {
		this.snapshotDate = new SimpleStringProperty(snapshotDate != null ? snapshotDate : "-");
		this.season = new SimpleStringProperty(season != null && !season.isBlank() ? season : "Aktuell");
		this.role = new SimpleStringProperty(role != null ? role : "-");
		this.age = new SimpleIntegerProperty(age);
		this.marketValue = new SimpleLongProperty(marketValue);
		this.monthlySalary = new SimpleLongProperty(monthlySalary);
		this.minutesPlayed = new SimpleIntegerProperty(minutesPlayed);
		this.moneyballScore = new SimpleDoubleProperty(moneyballScore);
		this.recommendation = new SimpleStringProperty(recommendation != null ? recommendation : "-");
	}

	// JavaFX-Properties für TableView-Binding
	public StringProperty snapshotDateProperty() { return snapshotDate; }
	public StringProperty seasonProperty() { return season; }
	public StringProperty roleProperty() { return role; }
	public IntegerProperty ageProperty() { return age; }
	public LongProperty marketValueProperty() { return marketValue; }
	public LongProperty monthlySalaryProperty() { return monthlySalary; }
	public IntegerProperty minutesPlayedProperty() { return minutesPlayed; }
	public DoubleProperty moneyballScoreProperty() { return moneyballScore; }
	public StringProperty recommendationProperty() { return recommendation; }

	// Direkte Getter für Berechnungen & Vergleiche
	public String getSnapshotDate() { return snapshotDate.get(); }
	public String getSeason() { return season.get(); }
	public String getRole() { return role.get(); }
	public int getAge() { return age.get(); }
	public long getMarketValue() { return marketValue.get(); }
	public long getMonthlySalary() { return monthlySalary.get(); }
	public int getMinutesPlayed() { return minutesPlayed.get(); }
	public double getMoneyballScore() { return moneyballScore.get(); }
	public String getRecommendation() { return recommendation.get(); }

	// Alias-Methoden im Record-Stil für kompakte Aufrufe
	public double moneyballScore() { return moneyballScore.get(); }
	public long marketValue() { return marketValue.get(); }
	public long monthlySalary() { return monthlySalary.get(); }
	public String season() { return season.get(); }
	public String role() { return role.get(); }
	public int age() { return age.get(); }
	public int minutesPlayed() { return minutesPlayed.get(); }
	public String importDate() { return snapshotDate.get(); }
	public String recommendation() { return recommendation.get(); }
}