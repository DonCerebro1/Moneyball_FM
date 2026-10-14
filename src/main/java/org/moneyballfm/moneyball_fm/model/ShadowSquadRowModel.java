package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;

public class ShadowSquadRowModel {

	private final StringProperty tacticalRole = new SimpleStringProperty();
	private final StringProperty starterName = new SimpleStringProperty();
	private final LongProperty starterSalary = new SimpleLongProperty();
	private final LongProperty starterMarketValue = new SimpleLongProperty();

	private final StringProperty candidate1Name = new SimpleStringProperty();
	private final LongProperty candidate1Fee = new SimpleLongProperty();
	private final LongProperty candidate1Salary = new SimpleLongProperty();

	private final StringProperty candidate2Name = new SimpleStringProperty();
	private final LongProperty candidate2Fee = new SimpleLongProperty();
	private final LongProperty candidate2Salary = new SimpleLongProperty();

	private final LongProperty netTransferGain = new SimpleLongProperty();
	private final LongProperty monthlyWageSavings = new SimpleLongProperty();

	public ShadowSquadRowModel(String tacticalRole, String starterName, long starterSalary, long starterMarketValue,
	                           String candidate1Name, long candidate1Fee, long candidate1Salary,
	                           String candidate2Name, long candidate2Fee, long candidate2Salary,
	                           long netTransferGain, long monthlyWageSavings) {
		this.tacticalRole.set(tacticalRole);
		this.starterName.set(starterName);
		this.starterSalary.set(starterSalary);
		this.starterMarketValue.set(starterMarketValue);

		this.candidate1Name.set(candidate1Name);
		this.candidate1Fee.set(candidate1Fee);
		this.candidate1Salary.set(candidate1Salary);

		this.candidate2Name.set(candidate2Name);
		this.candidate2Fee.set(candidate2Fee);
		this.candidate2Salary.set(candidate2Salary);

		this.netTransferGain.set(netTransferGain);
		this.monthlyWageSavings.set(monthlyWageSavings);
	}

	// Property-Getter (für TableView-Bindings)
	public StringProperty tacticalRoleProperty() { return tacticalRole; }
	public StringProperty starterNameProperty() { return starterName; }
	public LongProperty starterSalaryProperty() { return starterSalary; }
	public LongProperty starterMarketValueProperty() { return starterMarketValue; }

	public StringProperty candidate1NameProperty() { return candidate1Name; }
	public LongProperty candidate1FeeProperty() { return candidate1Fee; }
	public LongProperty candidate1SalaryProperty() { return candidate1Salary; }

	public StringProperty candidate2NameProperty() { return candidate2Name; }
	public LongProperty candidate2FeeProperty() { return candidate2Fee; }
	public LongProperty candidate2SalaryProperty() { return candidate2Salary; }

	public LongProperty netTransferGainProperty() { return netTransferGain; }
	public LongProperty monthlyWageSavingsProperty() { return monthlyWageSavings; }

	// Direkte Value-Getter (optional, für Services & Tests)
	public String getTacticalRole() { return tacticalRole.get(); }
	public String getStarterName() { return starterName.get(); }
	public long getStarterSalary() { return starterSalary.get(); }
	public long getStarterMarketValue() { return starterMarketValue.get(); }
	public String getCandidate1Name() { return candidate1Name.get(); }
	public long getCandidate1Fee() { return candidate1Fee.get(); }
	public long getCandidate1Salary() { return candidate1Salary.get(); }
	public String getCandidate2Name() { return candidate2Name.get(); }
	public long getCandidate2Fee() { return candidate2Fee.get(); }
	public long getCandidate2Salary() { return candidate2Salary.get(); }
	public long getNetTransferGain() { return netTransferGain.get(); }
	public long getMonthlyWageSavings() { return monthlyWageSavings.get(); }
}