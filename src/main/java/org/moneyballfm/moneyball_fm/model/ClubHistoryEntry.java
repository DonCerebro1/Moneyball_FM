package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class ClubHistoryEntry {
	private final StringProperty snapshotDate;
	private final StringProperty season;
	private final IntegerProperty playerCount;
	private final DoubleProperty avgAge;
	private final LongProperty totalMarketValue;
	private final LongProperty totalMonthlySalary;
	private final DoubleProperty avgMoneyballScore;

	public ClubHistoryEntry(String snapshotDate, String season, int playerCount,
	                        double avgAge, long totalMarketValue, long totalMonthlySalary,
	                        double avgMoneyballScore) {
		this.snapshotDate = new SimpleStringProperty(snapshotDate != null ? snapshotDate : "-");
		this.season = new SimpleStringProperty(season != null && !season.isBlank() ? season : "Aktuell");
		this.playerCount = new SimpleIntegerProperty(playerCount);
		this.avgAge = new SimpleDoubleProperty(avgAge);
		this.totalMarketValue = new SimpleLongProperty(totalMarketValue);
		this.totalMonthlySalary = new SimpleLongProperty(totalMonthlySalary);
		this.avgMoneyballScore = new SimpleDoubleProperty(avgMoneyballScore);
	}

	public StringProperty snapshotDateProperty() { return snapshotDate; }
	public StringProperty seasonProperty() { return season; }
	public IntegerProperty playerCountProperty() { return playerCount; }
	public DoubleProperty avgAgeProperty() { return avgAge; }
	public LongProperty totalMarketValueProperty() { return totalMarketValue; }
	public LongProperty totalMonthlySalaryProperty() { return totalMonthlySalary; }
	public DoubleProperty avgMoneyballScoreProperty() { return avgMoneyballScore; }

	public String getSnapshotDate() { return snapshotDate.get(); }
	public String getSeason() { return season.get(); }
	public int getPlayerCount() { return playerCount.get(); }
	public double getAvgAge() { return avgAge.get(); }
	public long getTotalMarketValue() { return totalMarketValue.get(); }
	public long getTotalMonthlySalary() { return totalMonthlySalary.get(); }
	public double getAvgMoneyballScore() { return avgMoneyballScore.get(); }
}