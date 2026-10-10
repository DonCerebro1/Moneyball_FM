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
	private final DoubleProperty moneyballScore = new SimpleDoubleProperty(0.0);
	private final LongProperty marketValue = new SimpleLongProperty(0L);
	private final BooleanProperty clauseFulfilled = new SimpleBooleanProperty(true);
	private final StringProperty recommendation = new SimpleStringProperty("");
	private final StringProperty futurePlan = new SimpleStringProperty("");

	public LoanPlayerModel(LoanType type, String name, String targetRole, int age, String clubAndLeague,
	                       int games, int minutes, double rating, double score, long marketValue, boolean clause) {
		this.loanType.set(type);
		this.playerName.set(name);
		this.targetRole.set(targetRole);
		this.age.set(age);
		this.clubAndLeague.set(clubAndLeague);
		this.gamesPlayed.set(games);
		this.minutesPlayed.set(minutes);
		this.averageRating.set(rating);
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
	public DoubleProperty moneyballScoreProperty() { return moneyballScore; }
	public LongProperty marketValueProperty() { return marketValue; }
	public BooleanProperty clauseFulfilledProperty() { return clauseFulfilled; }
	public StringProperty recommendationProperty() { return recommendation; }
	public StringProperty futurePlanProperty() { return futurePlan; }

	// Standard-Getter & Setter
	public LoanType getLoanType() { return loanType.get(); }
	public void setLoanType(LoanType t) { this.loanType.set(t); }
	public String getPlayerName() { return playerName.get(); }
	public int getAge() { return age.get(); }
	public int getGamesPlayed() { return gamesPlayed.get(); }
	public int getMinutesPlayed() { return minutesPlayed.get(); }
	public double getAverageRating() { return averageRating.get(); }
	public double getMoneyballScore() { return moneyballScore.get(); }
	public boolean isClauseFulfilled() { return clauseFulfilled.get(); }
}