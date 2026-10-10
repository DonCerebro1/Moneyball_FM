package org.moneyballfm.moneyball_fm.model;

import java.util.HashMap;
import java.util.Map;

public class Player {
	private String name;
	private String role;
	private int age;
	private long marketValue;
	private long monthlySalary;
	private double contractYears;
	private int agility;
	private int pace;
	private int stamina;
	private int determination;
	private int minutesPlayed;

	// Neu: Vertragsart ("Leihe", "Vollzeitvertrag") und aktueller Verein
	private String contractType = "Vollzeitvertrag";
	private String currentClub = "";

	// Beliebig viele Stats dynamisch erweiterbar
	private final Map<String, Double> stats = new HashMap<>();

	public Player(String name, String role, int age, long marketValue, long monthlySalary,
	              double contractYears, int agility, int pace, int stamina, int determination, int minutesPlayed) {
		this(name, role, age, marketValue, monthlySalary, contractYears, agility, pace, stamina, determination, minutesPlayed, "Vollzeitvertrag", "");
	}

	public Player(String name, String role, int age, long marketValue, long monthlySalary,
	              double contractYears, int agility, int pace, int stamina, int determination, int minutesPlayed,
	              String contractType, String currentClub) {
		this.name = name;
		this.role = role;
		this.age = age;
		this.marketValue = marketValue;
		this.monthlySalary = monthlySalary;
		this.contractYears = contractYears;
		this.agility = agility;
		this.pace = pace;
		this.stamina = stamina;
		this.determination = determination;
		this.minutesPlayed = minutesPlayed;
		this.contractType = (contractType != null) ? contractType : "Vollzeitvertrag";
		this.currentClub = (currentClub != null) ? currentClub : "";
	}

	public boolean isOnLoan() {
		return contractType != null && contractType.trim().equalsIgnoreCase("leihe");
	}

	public String getContractType() {
		return contractType;
	}

	public Player setContractType(String contractType) {
		this.contractType = contractType;
		return this;
	}

	public String getCurrentClub() {
		return currentClub;
	}

	public Player setCurrentClub(String currentClub) {
		this.currentClub = currentClub;
		return this;
	}

	public double getStat(String key) {
		return stats.getOrDefault(key.trim().toLowerCase(), 0.0);
	}

	public void putStat(String key, double value) {
		if (key != null) {
			stats.put(key.trim().toLowerCase(), value);
		}
	}

	public int getDetermination() {
		return determination;
	}

	public Player setDetermination(int determination) {
		this.determination = determination;
		return this;
	}

	public int getMinutesPlayed() {
		return minutesPlayed;
	}

	public Player setMinutesPlayed(int minutesPlayed) {
		this.minutesPlayed = minutesPlayed;
		return this;
	}

	public int getStamina() {
		return stamina;
	}

	public Player setStamina(int stamina) {
		this.stamina = stamina;
		return this;
	}

	public int getPace() {
		return pace;
	}

	public Player setPace(int pace) {
		this.pace = pace;
		return this;
	}

	public int getAgility() {
		return agility;
	}

	public Player setAgility(int agility) {
		this.agility = agility;
		return this;
	}

	public double getContractYears() {
		return contractYears;
	}

	public Player setContractYears(double contractYears) {
		this.contractYears = contractYears;
		return this;
	}

	public long getMonthlySalary() {
		return monthlySalary;
	}

	public Player setMonthlySalary(long monthlySalary) {
		this.monthlySalary = monthlySalary;
		return this;
	}

	public long getMarketValue() {
		return marketValue;
	}

	public Player setMarketValue(long marketValue) {
		this.marketValue = marketValue;
		return this;
	}

	public int getAge() {
		return age;
	}

	public Player setAge(int age) {
		this.age = age;
		return this;
	}

	public String getRole() {
		return role;
	}

	public Player setRole(String role) {
		this.role = role;
		return this;
	}

	public String getName() {
		return name;
	}

	public Player setName(String name) {
		this.name = name;
		return this;
	}
}