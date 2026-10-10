package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DepthChartRowModel {
	private final StringProperty tacticalRole = new SimpleStringProperty();
	private final IntegerProperty rank = new SimpleIntegerProperty();
	private final StringProperty playerName = new SimpleStringProperty("—");
	private final StringProperty moneyballScore = new SimpleStringProperty("—");
	private final StringProperty teamRole = new SimpleStringProperty("Offene Planstelle");
	private final StringProperty age = new SimpleStringProperty("—");
	private final StringProperty monthlySalary = new SimpleStringProperty("—");

	public DepthChartRowModel(String tacticalRole, int rank) {
		this.tacticalRole.set(tacticalRole);
		this.rank.set(rank);
	}

	public StringProperty tacticalRoleProperty() { return tacticalRole; }
	public IntegerProperty rankProperty() { return rank; }
	public StringProperty playerNameProperty() { return playerName; }
	public StringProperty moneyballScoreProperty() { return moneyballScore; }
	public StringProperty teamRoleProperty() { return teamRole; }
	public StringProperty ageProperty() { return age; }
	public StringProperty monthlySalaryProperty() { return monthlySalary; }
}