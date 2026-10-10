package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class ScoutingPlayerModel {

	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty targetRole = new SimpleStringProperty();
	private final IntegerProperty age = new SimpleIntegerProperty();
	private final LongProperty askingPrice = new SimpleLongProperty();
	private final LongProperty wageDemand = new SimpleLongProperty();
	private final DoubleProperty contractYears = new SimpleDoubleProperty();

	private final IntegerProperty agility = new SimpleIntegerProperty();
	private final IntegerProperty pace = new SimpleIntegerProperty();
	private final IntegerProperty stamina = new SimpleIntegerProperty();

	private final DoubleProperty statValue1 = new DoublePropertyBase(0.0) {
		@Override public Object getBean() { return ScoutingPlayerModel.this; }
		@Override public String getName() { return "statValue1"; }
	};
	private final DoubleProperty statValue2 = new SimpleDoubleProperty();
	private final DoubleProperty statValue3 = new SimpleDoubleProperty();

	private final DoubleProperty scoutingScore = new SimpleDoubleProperty();
	private final LongProperty maxTransferFee = new SimpleLongProperty();
	private final LongProperty maxWage = new SimpleLongProperty();
	private final StringProperty priceCheck = new SimpleStringProperty();
	private final StringProperty transferAction = new SimpleStringProperty();

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

	public StringProperty nameProperty() { return name; }
	public StringProperty targetRoleProperty() { return targetRole; }
	public IntegerProperty ageProperty() { return age; }
	public LongProperty askingPriceProperty() { return askingPrice; }
	public LongProperty wageDemandProperty() { return wageDemand; }
	public DoubleProperty contractYearsProperty() { return contractYears; }

	public IntegerProperty agilityProperty() { return agility; }
	public IntegerProperty paceProperty() { return pace; }
	public IntegerProperty staminaProperty() { return stamina; }

	public DoubleProperty statValue1Property() { return statValue1; }
	public DoubleProperty statValue2Property() { return statValue2; }
	public DoubleProperty statValue3Property() { return statValue3; }

	public DoubleProperty scoutingScoreProperty() { return scoutingScore; }
	public LongProperty maxTransferFeeProperty() { return maxTransferFee; }
	public LongProperty maxWageProperty() { return maxWage; }
	public StringProperty priceCheckProperty() { return priceCheck; }
	public StringProperty transferActionProperty() { return transferAction; }
}