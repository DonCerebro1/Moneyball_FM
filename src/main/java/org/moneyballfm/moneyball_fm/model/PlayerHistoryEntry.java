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
		this.snapshotDate = new SimpleStringProperty(snapshotDate);
		this.season = new SimpleStringProperty(season);
		this.role = new SimpleStringProperty(role);
		this.age = new SimpleIntegerProperty(age);
		this.marketValue = new SimpleLongProperty(marketValue);
		this.monthlySalary = new SimpleLongProperty(monthlySalary);
		this.minutesPlayed = new SimpleIntegerProperty(minutesPlayed);
		this.moneyballScore = new SimpleDoubleProperty(moneyballScore);
		this.recommendation = new SimpleStringProperty(recommendation);
	}

	public StringProperty snapshotDateProperty() { return snapshotDate; }
	public StringProperty seasonProperty() { return season; }
	public StringProperty roleProperty() { return role; }
	public IntegerProperty ageProperty() { return age; }
	public LongProperty marketValueProperty() { return marketValue; }
	public LongProperty monthlySalaryProperty() { return monthlySalary; }
	public IntegerProperty minutesPlayedProperty() { return minutesPlayed; }
	public DoubleProperty moneyballScoreProperty() { return moneyballScore; }
	public StringProperty recommendationProperty() { return recommendation; }
}