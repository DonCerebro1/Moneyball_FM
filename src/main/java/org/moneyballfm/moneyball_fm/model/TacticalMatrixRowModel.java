package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class TacticalMatrixRowModel {

	private final StringProperty playerName = new SimpleStringProperty();
	private final StringProperty naturalPositions = new SimpleStringProperty();
	private final StringProperty currentRole = new SimpleStringProperty();
	private final DoubleProperty currentScore = new SimpleDoubleProperty();
	private final StringProperty bestRole = new SimpleStringProperty();
	private final DoubleProperty bestScore = new SimpleDoubleProperty();
	private final DoubleProperty scoreDelta = new SimpleDoubleProperty();
	private final StringProperty topAlternatives = new SimpleStringProperty();
	private final StringProperty recommendation = new SimpleStringProperty();

	public TacticalMatrixRowModel(String name, String naturalPos, String curRole, double curScore,
	                              String bestR, double bestS, String alternatives, String rec) {
		this.playerName.set(name);
		this.naturalPositions.set(naturalPos);
		this.currentRole.set(curRole);
		this.currentScore.set(Math.round(curScore * 10.0) / 10.0);
		this.bestRole.set(bestR);
		this.bestScore.set(Math.round(bestS * 10.0) / 10.0);
		this.scoreDelta.set(Math.round((bestS - curScore) * 10.0) / 10.0);
		this.topAlternatives.set(alternatives);
		this.recommendation.set(rec);
	}

	public StringProperty playerNameProperty() { return playerName; }
	public StringProperty naturalPositionsProperty() { return naturalPositions; }
	public StringProperty currentRoleProperty() { return currentRole; }
	public DoubleProperty currentScoreProperty() { return currentScore; }
	public StringProperty bestRoleProperty() { return bestRole; }
	public DoubleProperty bestScoreProperty() { return bestScore; }
	public DoubleProperty scoreDeltaProperty() { return scoreDelta; }
	public StringProperty topAlternativesProperty() { return topAlternatives; }
	public StringProperty recommendationProperty() { return recommendation; }
}