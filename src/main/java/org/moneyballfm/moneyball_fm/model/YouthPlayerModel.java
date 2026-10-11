package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class YouthPlayerModel {

	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty targetRole = new SimpleStringProperty();
	private final IntegerProperty age = new SimpleIntegerProperty();

	// FM-Sterne (z.B. 4.5 Potenzial, 2.0 Ist-Stärke)
	private final DoubleProperty potentialStars = new SimpleDoubleProperty(0.0);
	private final DoubleProperty currentStars = new SimpleDoubleProperty(0.0);

	// Physis & Mentale Attribute
	private final IntegerProperty agility = new SimpleIntegerProperty(0);
	private final IntegerProperty pace = new SimpleIntegerProperty(0);
	private final IntegerProperty stamina = new SimpleIntegerProperty(0);
	private final IntegerProperty determination = new SimpleIntegerProperty(0);

	// Rollenspezifische Performance-Werte (Stats 1-3)
	private final DoubleProperty statValue1 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty statValue2 = new SimpleDoubleProperty(0.0);
	private final DoubleProperty statValue3 = new SimpleDoubleProperty(0.0);

	// Berechnete Pipeline-Werte
	private final DoubleProperty talentScore = new SimpleDoubleProperty(0.0);
	private final StringProperty forecastFirstTeam = new SimpleStringProperty("-");
	private final StringProperty pipelineRecommendation = new SimpleStringProperty("-");
	private final LongProperty maxYouthSalary = new SimpleLongProperty(0L);

	public YouthPlayerModel(String name, String targetRole, int age,
	                        double potentialStars, double currentStars,
	                        int agility, int pace, int stamina, int determination,
	                        double s1, double s2, double s3) {
		this.name.set(name);
		this.targetRole.set(targetRole);
		this.age.set(age);
		this.potentialStars.set(potentialStars);
		this.currentStars.set(currentStars);
		this.agility.set(agility);
		this.pace.set(pace);
		this.stamina.set(stamina);
		this.determination.set(determination);
		this.statValue1.set(s1);
		this.statValue2.set(s2);
		this.statValue3.set(s3);
	}

	// Properties & Getter / Setter
	public StringProperty nameProperty() { return name; }
	public String getName() { return name.get(); }

	public StringProperty targetRoleProperty() { return targetRole; }
	public String getTargetRole() { return targetRole.get(); }

	public IntegerProperty ageProperty() { return age; }
	public int getAge() { return age.get(); }

	public DoubleProperty potentialStarsProperty() { return potentialStars; }
	public double getPotentialStars() { return potentialStars.get(); }

	public DoubleProperty currentStarsProperty() { return currentStars; }
	public double getCurrentStars() { return currentStars.get(); }

	public IntegerProperty agilityProperty() { return agility; }
	public int getAgility() { return agility.get(); }

	public IntegerProperty paceProperty() { return pace; }
	public int getPace() { return pace.get(); }

	public IntegerProperty staminaProperty() { return stamina; }
	public int getStamina() { return stamina.get(); }

	public IntegerProperty determinationProperty() { return determination; }
	public int getDetermination() { return determination.get(); }

	public DoubleProperty statValue1Property() { return statValue1; }
	public DoubleProperty statValue2Property() { return statValue2; }
	public DoubleProperty statValue3Property() { return statValue3; }

	public DoubleProperty talentScoreProperty() { return talentScore; }
	public double getTalentScore() { return talentScore.get(); }

	public StringProperty forecastFirstTeamProperty() { return forecastFirstTeam; }
	public StringProperty pipelineRecommendationProperty() { return pipelineRecommendation; }
	public LongProperty maxYouthSalaryProperty() { return maxYouthSalary; }

	// Partieller Merge: Physis, Stats und Alter aktualisieren;
	// targetRole sowie manuell bewertete FM-Sterne (potentialStars, currentStars) bleiben erhalten!
	public void updateFromFresh(YouthPlayerModel fresh) {
		this.age.set(fresh.getAge());
		this.agility.set(fresh.getAgility());
		this.pace.set(fresh.getPace());
		this.stamina.set(fresh.getStamina());
		this.determination.set(fresh.getDetermination());
		this.statValue1.set(fresh.statValue1Property().get());
		this.statValue2.set(fresh.statValue2Property().get());
		this.statValue3.set(fresh.statValue3Property().get());
	}
}