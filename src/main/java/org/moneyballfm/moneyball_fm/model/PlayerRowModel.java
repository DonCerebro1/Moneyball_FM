package org.moneyballfm.moneyball_fm.model;

import javafx.beans.property.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerRowModel {
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty role = new SimpleStringProperty();
	private final IntegerProperty age = new SimpleIntegerProperty();
	private final LongProperty marketValue = new SimpleLongProperty();
	private final LongProperty fairMarketValue = new SimpleLongProperty();
	private final LongProperty monthlySalary = new SimpleLongProperty();
	private final LongProperty maxSalary = new SimpleLongProperty();
	private final DoubleProperty contractYears = new SimpleDoubleProperty();
	private final IntegerProperty minutesPlayed = new SimpleIntegerProperty();

	// Vertragsart & Verein (aus FM-Spalten ART und VEREIN)
	private final StringProperty contractType = new SimpleStringProperty("Vollzeitvertrag");
	private final StringProperty currentClub = new SimpleStringProperty("");
	private final BooleanProperty onLoan = new SimpleBooleanProperty(false);

	// Physis & Soft Skills
	private final IntegerProperty agility = new SimpleIntegerProperty();
	private final IntegerProperty pace = new SimpleIntegerProperty();
	private final IntegerProperty stamina = new SimpleIntegerProperty();
	private final IntegerProperty determination = new SimpleIntegerProperty();

	// Verletzung & Verfügbarkeit
	private final StringProperty injurySusceptibility = new SimpleStringProperty("Normal");
	private final DoubleProperty availabilityFactor = new SimpleDoubleProperty(1.0);

	// Die drei sichtbaren Hauptwerte & ihre dynamischen Bezeichnungen
	private final DoubleProperty statValue1 = new SimpleDoubleProperty();
	private final DoubleProperty statValue2 = new SimpleDoubleProperty();
	private final DoubleProperty statValue3 = new SimpleDoubleProperty();
	private final StringProperty stat1Label = new SimpleStringProperty("Stat 1");
	private final StringProperty stat2Label = new SimpleStringProperty("Stat 2");
	private final StringProperty stat3Label = new SimpleStringProperty("Stat 3");

	// Berechnete Felder (automatische Aktualisierung)
	private final DoubleProperty moneyballScore = new SimpleDoubleProperty(0.0);
	private final StringProperty salaryTier = new SimpleStringProperty("-");
	private final StringProperty salaryCheck = new SimpleStringProperty("-");
	private final StringProperty sampleQuality = new SimpleStringProperty("-");

	// Taktik-Gedächtnis: Merkt sich pro Taktik-Name die gewählte Rolle
	private final Map<String, String> roleByTactic = new HashMap<>();

	private final Player rawPlayer;

	public PlayerRowModel(Player player, double s1, double s2, double s3) {
		this.rawPlayer = player;
		this.name.set(player.getName());
		this.role.set(player.getRole());
		this.age.set(player.getAge());
		this.marketValue.set(player.getMarketValue());
		this.monthlySalary.set(player.getMonthlySalary());
		this.contractYears.set(player.getContractYears());
		this.minutesPlayed.set(player.getMinutesPlayed());

		this.contractType.set(player.getContractType());
		this.currentClub.set(player.getCurrentClub());
		this.onLoan.set(player.isOnLoan());

		this.agility.set(player.getAgility());
		this.pace.set(player.getPace());
		this.stamina.set(player.getStamina());
		this.determination.set(player.getDetermination());

		this.injurySusceptibility.set(player.getInjurySusceptibility());

		this.statValue1.set(s1);
		this.statValue2.set(s2);
		this.statValue3.set(s3);

		// Initial-Werte in der Map von rawPlayer sichern
		if (this.rawPlayer != null) {
			TacticRole initialRole = TacticRole.fromDisplayName(player.getRole());
			if (initialRole != null) {
				if (s1 > 0.0) this.rawPlayer.putStat(initialRole.getStat1Header(), s1);
				if (s2 > 0.0) this.rawPlayer.putStat(initialRole.getStat2Header(), s2);
				if (s3 > 0.0) this.rawPlayer.putStat(initialRole.getStat3Header(), s3);
				this.setStatLabels(initialRole.getStat1Header(), initialRole.getStat2Header(), initialRole.getStat3Header());
			}
		}

		// Startrolle für die Basistaktik registrieren
		if (player != null && player.getRole() != null && !player.getRole().isBlank()) {
			this.roleByTactic.put("4-3-3 Gegenpress", player.getRole());
		}

		// Synchronisation: Wenn die Rolle im Model geändert wird, auch das Player-Objekt anpassen
		this.role.addListener((obs, oldV, newV) -> {
			if (this.rawPlayer != null && newV != null) {
				this.rawPlayer.setRole(newV);
			}
		});
	}

	// Taktik-spezifische Rollen-Verwaltung
	public String getRoleForTactic(String tacticName) {
		if (tacticName == null) return role.get();
		return roleByTactic.get(tacticName);
	}

	public void setRoleForTactic(String tacticName, String selectedRole) {
		if (tacticName != null && selectedRole != null && !selectedRole.isBlank()) {
			roleByTactic.put(tacticName, selectedRole);
		}
		if (selectedRole != null && !selectedRole.equals(this.role.get())) {
			this.role.set(selectedRole);
		}
	}

	public boolean hasRoleForTactic(String tacticName) {
		return tacticName != null && roleByTactic.containsKey(tacticName);
	}

	// Serialisierung für H2: Speichert "TaktikA:RolleA|TaktikB:RolleB"
	public String serializeTacticalRoles() {
		if (roleByTactic.isEmpty()) {
			return this.role.get() != null ? this.role.get() : "";
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : roleByTactic.entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null && !entry.getValue().isBlank()) {
				if (sb.length() > 0) sb.append("|");
				sb.append(entry.getKey()).append(":").append(entry.getValue());
			}
		}
		return sb.length() > 0 ? sb.toString() : (this.role.get() != null ? this.role.get() : "");
	}

	// Deserialisierung aus H2
	public void deserializeTacticalRoles(String rawString, String currentActiveTactic) {
		if (rawString == null || rawString.isBlank()) return;

		if (!rawString.contains(":")) {
			this.role.set(rawString);
			if (currentActiveTactic != null) {
				this.roleByTactic.put(currentActiveTactic, rawString);
			}
			return;
		}

		String[] pairs = rawString.split("\\|");
		for (String pair : pairs) {
			String[] parts = pair.split(":", 2);
			if (parts.length == 2) {
				this.roleByTactic.put(parts[0].trim(), parts[1].trim());
			}
		}

		if (currentActiveTactic != null && this.roleByTactic.containsKey(currentActiveTactic)) {
			this.role.set(this.roleByTactic.get(currentActiveTactic));
		} else if (!this.roleByTactic.isEmpty()) {
			this.role.set(this.roleByTactic.values().iterator().next());
		}
	}

	public Player getPlayer() { return rawPlayer; }
	public Player getRawPlayer() { return rawPlayer; }

	public StringProperty nameProperty() { return name; }
	public StringProperty roleProperty() { return role; }
	public IntegerProperty ageProperty() { return age; }
	public LongProperty marketValueProperty() { return marketValue; }
	public LongProperty fairMarketValueProperty() { return fairMarketValue; }
	public LongProperty monthlySalaryProperty() { return monthlySalary; }
	public LongProperty maxSalaryProperty() { return maxSalary; }
	public DoubleProperty contractYearsProperty() { return contractYears; }
	public IntegerProperty minutesPlayedProperty() { return minutesPlayed; }

	// Getter & Properties für Leih-Status / Verein
	public StringProperty contractTypeProperty() { return contractType; }
	public String getContractType() { return contractType.get(); }
	public void setContractType(String type) { this.contractType.set(type); }

	public StringProperty currentClubProperty() { return currentClub; }
	public String getCurrentClub() { return currentClub.get(); }
	public void setCurrentClub(String club) { this.currentClub.set(club); }

	public BooleanProperty onLoanProperty() { return onLoan; }
	public boolean isOnLoan() { return onLoan.get(); }
	public void setOnLoan(boolean val) { this.onLoan.set(val); }

	public IntegerProperty agilityProperty() { return agility; }
	public IntegerProperty paceProperty() { return pace; }
	public IntegerProperty staminaProperty() { return stamina; }
	public IntegerProperty determinationProperty() { return determination; }

	// Verletzung & Verfügbarkeit
	public StringProperty injurySusceptibilityProperty() { return injurySusceptibility; }
	public String getInjurySusceptibility() { return injurySusceptibility.get(); }
	public void setInjurySusceptibility(String val) { this.injurySusceptibility.set(val); }

	public DoubleProperty availabilityFactorProperty() { return availabilityFactor; }
	public double getAvailabilityFactor() { return availabilityFactor.get(); }
	public void setAvailabilityFactor(double val) { this.availabilityFactor.set(val); }

	public DoubleProperty statValue1Property() { return statValue1; }
	public DoubleProperty statValue2Property() { return statValue2; }
	public DoubleProperty statValue3Property() { return statValue3; }

	public StringProperty stat1LabelProperty() { return stat1Label; }
	public StringProperty stat2LabelProperty() { return stat2Label; }
	public StringProperty stat3LabelProperty() { return stat3Label; }

	public void setStatLabels(String s1, String s2, String s3) {
		this.stat1Label.set(s1);
		this.stat2Label.set(s2);
		this.stat3Label.set(s3);
	}

	public DoubleProperty moneyballScoreProperty() { return moneyballScore; }
	public StringProperty salaryTierProperty() { return salaryTier; }
	public StringProperty salaryCheckProperty() { return salaryCheck; }
	public StringProperty sampleQualityProperty() { return sampleQuality; }

	// Delegations-Getter für Leihspieler-Werte aus Player
	public double getLoanRating() {
		return rawPlayer != null ? rawPlayer.getLoanRating() : 0.0;
	}

	public String getLeague() {
		return rawPlayer != null ? rawPlayer.getLeague() : "";
	}

	public int getGoals() {
		return rawPlayer != null ? rawPlayer.getGoals() : 0;
	}

	public int getAssists() {
		return rawPlayer != null ? rawPlayer.getAssists() : 0;
	}

	public String getNaturalPosition() {
		return rawPlayer != null ? rawPlayer.getNaturalPosition() : "";
	}

	public void applyRoleStats(TacticRole newRole) {
		if (newRole == null || this.rawPlayer == null) return;

		String prevRoleName = this.role.get();
		double currentS1 = this.statValue1.get();
		double currentS2 = this.statValue2.get();
		double currentS3 = this.statValue3.get();

		TacticRole oldRole = TacticRole.fromDisplayName(prevRoleName);
		if (oldRole != null) {
			if (currentS1 > 0.0) this.rawPlayer.putStat(oldRole.getStat1Header(), currentS1);
			if (currentS2 > 0.0) this.rawPlayer.putStat(oldRole.getStat2Header(), currentS2);
			if (currentS3 > 0.0) this.rawPlayer.putStat(oldRole.getStat3Header(), currentS3);
		}

		this.role.set(newRole.getDisplayName());
		this.rawPlayer.setRole(newRole.getDisplayName());

		double s1 = this.rawPlayer.getStat(newRole.getStat1Header());
		double s2 = this.rawPlayer.getStat(newRole.getStat2Header());
		double s3 = this.rawPlayer.getStat(newRole.getStat3Header());

		if (s1 > 0.0 || s2 > 0.0 || s3 > 0.0) {
			this.statValue1.set(s1);
			this.statValue2.set(s2);
			this.statValue3.set(s3);
		}

		this.setStatLabels(newRole.getStat1Header(), newRole.getStat2Header(), newRole.getStat3Header());
	}

	public void updateFromFresh(PlayerRowModel fresh) {
		this.age.set(fresh.ageProperty().get());
		this.marketValue.set(fresh.marketValueProperty().get());
		this.monthlySalary.set(fresh.monthlySalaryProperty().get());
		this.contractYears.set(fresh.contractYearsProperty().get());
		this.minutesPlayed.set(fresh.minutesPlayedProperty().get());
		this.contractType.set(fresh.contractTypeProperty().get());
		this.currentClub.set(fresh.currentClubProperty().get());
		this.onLoan.set(fresh.onLoanProperty().get());

		this.agility.set(fresh.agilityProperty().get());
		this.pace.set(fresh.paceProperty().get());
		this.stamina.set(fresh.staminaProperty().get());
		this.determination.set(fresh.determinationProperty().get());

		this.injurySusceptibility.set(fresh.injurySusceptibilityProperty().get());

		if (this.rawPlayer != null && fresh.getRawPlayer() != null) {
			Player fp = fresh.getRawPlayer();
			this.rawPlayer.setAge(fp.getAge())
					.setMarketValue(fp.getMarketValue())
					.setMonthlySalary(fp.getMonthlySalary())
					.setContractYears(fp.getContractYears())
					.setMinutesPlayed(fp.getMinutesPlayed())
					.setContractType(fp.getContractType())
					.setCurrentClub(fp.getCurrentClub())
					.setAgility(fp.getAgility())
					.setPace(fp.getPace())
					.setStamina(fp.getStamina())
					.setDetermination(fp.getDetermination())
					.setInjurySusceptibility(fp.getInjurySusceptibility())
					.setLoanRating(fp.getLoanRating())
					.setLeague(fp.getLeague())
					.setGoals(fp.getGoals())
					.setAssists(fp.getAssists());

			// 1. Frische Stats vollständig übernehmen
			this.rawPlayer.putAllStats(fp.getAllStats());
		}

		// 2. Frische Stat-Werte und Labels direkt aus dem frischen Model übernehmen
		this.statValue1.set(fresh.statValue1Property().get());
		this.statValue2.set(fresh.statValue2Property().get());
		this.statValue3.set(fresh.statValue3Property().get());
		this.setStatLabels(
				fresh.stat1LabelProperty().get(),
				fresh.stat2LabelProperty().get(),
				fresh.stat3LabelProperty().get()
		);
	}
}