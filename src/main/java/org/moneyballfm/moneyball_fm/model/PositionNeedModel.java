package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class PositionNeedModel {
	private final StringProperty slotName = new SimpleStringProperty();
	private final StringProperty roleLabel = new SimpleStringProperty();
	private final IntegerProperty playerCount = new SimpleIntegerProperty(0);
	private final StringProperty bestPlayerName = new SimpleStringProperty("-");
	private final DoubleProperty bestScore = new SimpleDoubleProperty(0.0);
	private final DoubleProperty averageScore = new SimpleDoubleProperty(0.0);
	private final DoubleProperty averageAge = new SimpleDoubleProperty(0.0);
	private final IntegerProperty criticalContracts = new SimpleIntegerProperty(0);
	private final StringProperty diagnosis = new SimpleStringProperty("-");
	private final StringProperty recommendation = new SimpleStringProperty("-");
	private final StringProperty targetProfile = new SimpleStringProperty("-");

	public PositionNeedModel(String slotName, String roleLabel) {
		this.slotName.set(slotName);
		this.roleLabel.set(roleLabel);
	}

	public StringProperty slotNameProperty() { return slotName; }
	public StringProperty roleLabelProperty() { return roleLabel; }
	public IntegerProperty playerCountProperty() { return playerCount; }
	public StringProperty bestPlayerNameProperty() { return bestPlayerName; }
	public DoubleProperty bestScoreProperty() { return bestScore; }
	public DoubleProperty averageScoreProperty() { return averageScore; }
	public DoubleProperty averageAgeProperty() { return averageAge; }
	public IntegerProperty criticalContractsProperty() { return criticalContracts; }
	public StringProperty diagnosisProperty() { return diagnosis; }
	public StringProperty recommendationProperty() { return recommendation; }
	public StringProperty targetProfileProperty() { return targetProfile; }
}