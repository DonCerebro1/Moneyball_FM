package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class RegistrationRowModel {

	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty role = new SimpleStringProperty();
	private final IntegerProperty age = new SimpleIntegerProperty();
	private final LongProperty salary = new SimpleLongProperty();
	private final LongProperty marketValue = new SimpleLongProperty();

	// Vorbereitungs-Kennzahlen
	private final IntegerProperty testMinutes = new SimpleIntegerProperty(0);
	private final DoubleProperty testRating = new SimpleDoubleProperty(0.0);
	private final StringProperty fitnessTrend = new SimpleStringProperty("—");

	// Registrierungs-Status
	private final BooleanProperty u21Exempt = new SimpleBooleanProperty(false);
	private final BooleanProperty registered = new SimpleBooleanProperty(false);
	private final StringProperty recommendation = new SimpleStringProperty("—");

	public RegistrationRowModel(PlayerRowModel player) {
		this.name.set(player.nameProperty().get());
		this.role.set(player.roleProperty().get());
		this.age.set(player.ageProperty().get());
		this.salary.set(player.monthlySalaryProperty().get());
		this.marketValue.set(player.marketValueProperty().get());

		// In Spanien: U21 (<= 21 Jahre) benötigt keinen der 25 A-Kader-Slots
		this.u21Exempt.set(player.ageProperty().get() <= 21);

		// Preseason-Entkopplung: Startet bei 0, Werte kommen ausschließlich über Testspiel-Import
		this.testMinutes.set(0);

		evaluate();
	}

	public void evaluate() {
		int mins = testMinutes.get();
		double rating = testRating.get();
		boolean u21 = u21Exempt.get();
		String r = role.get() != null ? role.get().toUpperCase() : "";
		boolean isGoalkeeper = r.contains("TORWART") || r.contains("TW") || r.contains("MTW");

		// 1. Fitness / Form-Trend (reine Vorbereitungsdaten)
		if (mins == 0) {
			fitnessTrend.set("⚪ Noch keine Testspiele");
		} else if (mins >= 210) {
			if (rating >= 7.2) {
				fitnessTrend.set("🟢 Topform & voll im Saft");
			} else if (rating >= 6.7 || rating == 0.0) {
				fitnessTrend.set("🟢 Voll im Rhythmus");
			} else {
				fitnessTrend.set("🟡 Fit, Formdelle");
			}
		} else if (mins >= 90) {
			fitnessTrend.set(rating >= 7.0 ? "🟢 Starke Ansätze" : "🟡 Teils spielfit");
		} else {
			fitnessTrend.set("🔴 Kaum Spielpraxis");
		}

		// 2. Registrierungs-Empfehlung
		if (u21) {
			// U21-Spieler müssen in Spanien keinen 25er-Kaderplatz belegen
			if (rating >= 7.2 && mins >= 70) {
				this.recommendation.set("🟢 U21 TOP-FORM (Frei)");
			} else {
				this.recommendation.set("🟡 U21 / B-TEAM (Frei)");
			}
			this.registered.set(false);
		} else if (isGoalkeeper) {
			// Torhüter müssen in La Liga fest im A-Kader stehen
			this.recommendation.set("🧤 TORWART (PFLICHT)");
			this.registered.set(true);
		} else if (salary.get() > 500_000 && rating > 0.0 && rating < 6.6) {
			// Teure Spieler mit schwachen Leistungen
			this.recommendation.set("🔴 STREICHEN / VERKAUF");
			this.registered.set(false);
		} else if (mins >= 150 && (rating >= 6.7 || rating == 0.0)) {
			// Regulär fitte Profis nach Vorbereitung
			this.recommendation.set("🟢 REGISTRIEREN (FEST)");
			this.registered.set(true);
		} else if (marketValue.get() >= 10_000_000L || salary.get() >= 250_000L) {
			// Wertvolle Stamm-/Kaderspieler auch vor Vorbereitungs-Spielen registriert halten
			this.recommendation.set("🟢 REGISTRIEREN (FEST)");
			this.registered.set(true);
		} else {
			this.recommendation.set("🟠 WACKELKANDIDAT");
			this.registered.set(false);
		}
	}

	// Properties
	public StringProperty nameProperty() { return name; }
	public StringProperty roleProperty() { return role; }
	public IntegerProperty ageProperty() { return age; }
	public LongProperty salaryProperty() { return salary; }
	public LongProperty marketValueProperty() { return marketValue; }
	public IntegerProperty testMinutesProperty() { return testMinutes; }
	public DoubleProperty testRatingProperty() { return testRating; }
	public StringProperty fitnessTrendProperty() { return fitnessTrend; }
	public BooleanProperty u21ExemptProperty() { return u21Exempt; }
	public BooleanProperty registeredProperty() { return registered; }
	public StringProperty recommendationProperty() { return recommendation; }

	public String getName() { return name.get(); }
	public int getTestMinutes() { return testMinutes.get(); }
	public void setTestMinutes(int minutes) { this.testMinutes.set(minutes); }
	public double getTestRating() { return testRating.get(); }
	public void setTestRating(double rating) { this.testRating.set(rating); }
	public boolean isRegistered() { return registered.get(); }
	public void setRegistered(boolean reg) { this.registered.set(reg); }
}