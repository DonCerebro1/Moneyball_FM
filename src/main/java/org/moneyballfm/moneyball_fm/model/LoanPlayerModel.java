package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class LoanPlayerModel {

	public enum LoanType {
		OUTGOING("📤 Verliehen"),
		INCOMING("📥 Geliehen");

		private final String label;
		LoanType(String label) { this.label = label; }
		public String getLabel() { return label; }
		@Override public String toString() { return label; }
	}

	private final ObjectProperty<LoanType> loanType = new SimpleObjectProperty<>(LoanType.OUTGOING);
	private final StringProperty playerName = new SimpleStringProperty("");
	private final StringProperty targetRole = new SimpleStringProperty("");
	private final IntegerProperty age = new SimpleIntegerProperty(21);
	private final StringProperty clubAndLeague = new SimpleStringProperty("");
	private final IntegerProperty gamesPlayed = new SimpleIntegerProperty(0);
	private final IntegerProperty minutesPlayed = new SimpleIntegerProperty(0);
	private final DoubleProperty averageRating = new SimpleDoubleProperty(6.80);
	private final IntegerProperty goals = new SimpleIntegerProperty(0);
	private final IntegerProperty assists = new SimpleIntegerProperty(0);
	private final DoubleProperty moneyballScore = new SimpleDoubleProperty(0.0);
	private final LongProperty marketValue = new SimpleLongProperty(0L);
	private final BooleanProperty clauseFulfilled = new SimpleBooleanProperty(true);
	private final StringProperty recommendation = new SimpleStringProperty("");
	private final StringProperty futurePlan = new SimpleStringProperty("");

	// Bestehender Konstruktor (Rückwärtskompatibilität)
	public LoanPlayerModel(LoanType type, String name, String targetRole, int age, String clubAndLeague,
	                       int games, int minutes, double rating, double score, long marketValue, boolean clause) {
		this(type, name, targetRole, age, clubAndLeague, games, minutes, rating, 0, 0, score, marketValue, clause);
	}

	// Neuer Konstruktor inklusive Tore und Vorlagen
	public LoanPlayerModel(LoanType type, String name, String targetRole, int age, String clubAndLeague,
	                       int games, int minutes, double rating, int goals, int assists,
	                       double score, long marketValue, boolean clause) {
		this.loanType.set(type);
		this.playerName.set(name);
		this.targetRole.set(targetRole);
		this.age.set(age);
		this.clubAndLeague.set(clubAndLeague);
		this.gamesPlayed.set(games);
		this.minutesPlayed.set(minutes);
		this.averageRating.set(rating);
		this.goals.set(goals);
		this.assists.set(assists);
		this.moneyballScore.set(score);
		this.marketValue.set(marketValue);
		this.clauseFulfilled.set(clause);
	}

	// Property-Getter
	public ObjectProperty<LoanType> loanTypeProperty() { return loanType; }
	public StringProperty playerNameProperty() { return playerName; }
	public StringProperty targetRoleProperty() { return targetRole; }
	public IntegerProperty ageProperty() { return age; }
	public StringProperty clubAndLeagueProperty() { return clubAndLeague; }
	public IntegerProperty gamesPlayedProperty() { return gamesPlayed; }
	public IntegerProperty minutesPlayedProperty() { return minutesPlayed; }
	public DoubleProperty averageRatingProperty() { return averageRating; }
	public IntegerProperty goalsProperty() { return goals; }
	public IntegerProperty assistsProperty() { return assists; }
	public DoubleProperty moneyballScoreProperty() { return moneyballScore; }
	public LongProperty marketValueProperty() { return marketValue; }
	public BooleanProperty clauseFulfilledProperty() { return clauseFulfilled; }
	public StringProperty recommendationProperty() { return recommendation; }
	public StringProperty futurePlanProperty() { return futurePlan; }

	// Standard-Getter & Setter
	public LoanType getLoanType() { return loanType.get(); }
	public void setLoanType(LoanType t) { this.loanType.set(t); }
	public String getPlayerName() { return playerName.get(); }
	public void setPlayerName(String name) { this.playerName.set(name); }
	public String getTargetRole() { return targetRole.get(); }
	public void setTargetRole(String role) { this.targetRole.set(role); }
	public int getAge() { return age.get(); }
	public void setAge(int age) { this.age.set(age); }
	public String getClubAndLeague() { return clubAndLeague.get(); }
	public void setClubAndLeague(String clubAndLeague) { this.clubAndLeague.set(clubAndLeague); }
	public int getGamesPlayed() { return gamesPlayed.get(); }
	public void setGamesPlayed(int games) { this.gamesPlayed.set(games); }
	public int getMinutesPlayed() { return minutesPlayed.get(); }
	public void setMinutesPlayed(int minutes) { this.minutesPlayed.set(minutes); }
	public double getAverageRating() { return averageRating.get(); }
	public void setAverageRating(double rating) { this.averageRating.set(rating); }
	public int getGoals() { return goals.get(); }
	public void setGoals(int goals) { this.goals.set(goals); }
	public int getAssists() { return assists.get(); }
	public void setAssists(int assists) { this.assists.set(assists); }
	public double getMoneyballScore() { return moneyballScore.get(); }
	public void setMoneyballScore(double score) { this.moneyballScore.set(score); }
	public long getMarketValue() { return marketValue.get(); }
	public void setMarketValue(long value) { this.marketValue.set(value); }
	public boolean isClauseFulfilled() { return clauseFulfilled.get(); }
	public void setClauseFulfilled(boolean clause) { this.clauseFulfilled.set(clause); }
	public String getRecommendation() { return recommendation.get(); }
	public void setRecommendation(String rec) { this.recommendation.set(rec); }
	public String getFuturePlan() { return futurePlan.get(); }
	public void setFuturePlan(String plan) { this.futurePlan.set(plan); }
}