package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class ScoutingPlayerModel {

	// Basisdaten
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty club = new SimpleStringProperty("");
	private final StringProperty league = new SimpleStringProperty("");
	private final StringProperty positions = new SimpleStringProperty("");
	private final StringProperty targetRole = new SimpleStringProperty();
	private final IntegerProperty age = new SimpleIntegerProperty();
	private final IntegerProperty minutes = new SimpleIntegerProperty(0);
	private final LongProperty askingPrice = new SimpleLongProperty();
	private final LongProperty wageDemand = new SimpleLongProperty();
	private final DoubleProperty contractYears = new SimpleDoubleProperty();

	// Liga-Koeffizient (Qualitätsfaktor)
	private final DoubleProperty leagueCoefficient = new SimpleDoubleProperty(1.0);

	// Attribute
	private final IntegerProperty agility = new SimpleIntegerProperty();
	private final IntegerProperty pace = new SimpleIntegerProperty();
	private final IntegerProperty stamina = new SimpleIntegerProperty();
	private final IntegerProperty determination = new SimpleIntegerProperty(0);

	// Verletzung & Verfügbarkeit
	private final StringProperty injurySusceptibility = new SimpleStringProperty("Normal");
	private final DoubleProperty availabilityFactor = new SimpleDoubleProperty(1.0);

	// Detaillierte Leistungsdaten (Rohwerte für dynamische Rollenberechnung)
	private final DoubleProperty progressivePasses90 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty xg90 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty xa90 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty keyPasses90 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty shotsOnTargetPct = new SimpleDoubleProperty(0.0);
	private final DoubleProperty shotConversionPct = new SimpleDoubleProperty(0.0);
	private final DoubleProperty passCompletionPct = new SimpleDoubleProperty(0.0);
	private final DoubleProperty xgPrevented90 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty savePct = new SimpleDoubleProperty(0.0);
	private final DoubleProperty interceptions90 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty crossAccuracyPct = new SimpleDoubleProperty(0.0);
	private final IntegerProperty goals = new SimpleIntegerProperty(0);
	private final IntegerProperty assists = new SimpleIntegerProperty(0);
	private final DoubleProperty averageRating = new SimpleDoubleProperty(0.0);
	private final DoubleProperty headersWonPct = new SimpleDoubleProperty(0.0);
	private final IntegerProperty mistakesLeadingToGoal = new SimpleIntegerProperty(0);
	private final DoubleProperty dribbles90 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty crossesAttempted90 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty tacklesWonPct = new SimpleDoubleProperty(0.0);
	private final IntegerProperty interceptionsTotal = new SimpleIntegerProperty(0);
	private final IntegerProperty progressivePassesTotal = new SimpleIntegerProperty(0);
	private final IntegerProperty keyPassesTotal = new SimpleIntegerProperty(0);

	// Rollen-Metriken (statValue1 - statValue3 für den ScoutingAnalysisService)
	private final DoubleProperty statValue1 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty statValue2 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty statValue3 = new SimpleDoubleProperty(0.0);

	// Berechnete Ergebnisse
	private final DoubleProperty scoutingScore = new SimpleDoubleProperty();
	private final LongProperty maxTransferFee = new SimpleLongProperty();
	private final LongProperty maxWage = new SimpleLongProperty();
	private final StringProperty priceCheck = new SimpleStringProperty();
	private final StringProperty transferAction = new SimpleStringProperty();

	public ScoutingPlayerModel() {
	}

	public ScoutingPlayerModel(String name, String targetRole, int age, long askingPrice, long wageDemand,
	                           double contractYears, int agility, int pace, int stamina,
	                           double stat1, double stat2, double stat3) {
		this.name.set(name);
		this.targetRole.set(targetRole);
		this.age.set(age);
		this.askingPrice.set(askingPrice);
		this.wageDemand.set(wageDemand);
		this.contractYears.set(contractYears);
		this.agility.set(agility);
		this.pace.set(pace);
		this.stamina.set(stamina);
		this.statValue1.set(stat1);
		this.statValue2.set(stat2);
		this.statValue3.set(stat3);
	}

	// --- Property Getter & Setter ---

	public StringProperty nameProperty() { return name; }
	public StringProperty clubProperty() { return club; }
	public StringProperty leagueProperty() { return league; }
	public String getLeague() { return league.get(); }
	public void setLeague(String val) { this.league.set(val != null ? val : ""); }

	public DoubleProperty leagueCoefficientProperty() { return leagueCoefficient; }
	public double getLeagueCoefficient() { return leagueCoefficient.get(); }
	public void setLeagueCoefficient(double val) { this.leagueCoefficient.set(val); }

	public StringProperty positionsProperty() { return positions; }
	public StringProperty targetRoleProperty() { return targetRole; }
	public IntegerProperty ageProperty() { return age; }
	public IntegerProperty minutesProperty() { return minutes; }
	public LongProperty askingPriceProperty() { return askingPrice; }
	public LongProperty wageDemandProperty() { return wageDemand; }
	public DoubleProperty contractYearsProperty() { return contractYears; }

	public IntegerProperty agilityProperty() { return agility; }
	public IntegerProperty paceProperty() { return pace; }
	public IntegerProperty staminaProperty() { return stamina; }
	public IntegerProperty determinationProperty() { return determination; }

	public StringProperty injurySusceptibilityProperty() { return injurySusceptibility; }
	public String getInjurySusceptibility() { return injurySusceptibility.get(); }
	public void setInjurySusceptibility(String val) { this.injurySusceptibility.set(val); }

	public DoubleProperty availabilityFactorProperty() { return availabilityFactor; }
	public double getAvailabilityFactor() { return availabilityFactor.get(); }
	public void setAvailabilityFactor(double val) { this.availabilityFactor.set(val); }

	public DoubleProperty progressivePasses90Property() { return progressivePasses90; }
	public DoubleProperty xg90Property() { return xg90; }
	public DoubleProperty xa90Property() { return xa90; }
	public DoubleProperty keyPasses90Property() { return keyPasses90; }
	public DoubleProperty shotsOnTargetPctProperty() { return shotsOnTargetPct; }
	public DoubleProperty shotConversionPctProperty() { return shotConversionPct; }
	public DoubleProperty passCompletionPctProperty() { return passCompletionPct; }
	public DoubleProperty xgPrevented90Property() { return xgPrevented90; }
	public DoubleProperty savePctProperty() { return savePct; }
	public DoubleProperty interceptions90Property() { return interceptions90; }
	public DoubleProperty crossAccuracyPctProperty() { return crossAccuracyPct; }
	public IntegerProperty goalsProperty() { return goals; }
	public IntegerProperty assistsProperty() { return assists; }
	public DoubleProperty averageRatingProperty() { return averageRating; }
	public DoubleProperty headersWonPctProperty() { return headersWonPct; }
	public IntegerProperty mistakesLeadingToGoalProperty() { return mistakesLeadingToGoal; }
	public DoubleProperty dribbles90Property() { return dribbles90; }
	public DoubleProperty crossesAttempted90Property() { return crossesAttempted90; }
	public DoubleProperty tacklesWonPctProperty() { return tacklesWonPct; }
	public IntegerProperty interceptionsTotalProperty() { return interceptionsTotal; }
	public IntegerProperty progressivePassesTotalProperty() { return progressivePassesTotal; }
	public IntegerProperty keyPassesTotalProperty() { return keyPassesTotal; }

	public DoubleProperty statValue1Property() { return statValue1; }
	public DoubleProperty statValue2Property() { return statValue2; }
	public DoubleProperty statValue3Property() { return statValue3; }

	public DoubleProperty scoutingScoreProperty() { return scoutingScore; }
	public LongProperty maxTransferFeeProperty() { return maxTransferFee; }
	public LongProperty maxWageProperty() { return maxWage; }
	public StringProperty priceCheckProperty() { return priceCheck; }
	public StringProperty transferActionProperty() { return transferAction; }
}