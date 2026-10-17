package org.moneyballfm.moneyball_fm.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppConfig {
	private Map<String, Object> thresholds;

	@JsonProperty("salary_tiers")
	private List<SalaryTierConfig> salaryTiers;

	@JsonProperty("active_tactic")
	private String activeTactic;

	private List<TacticConfig> tactics;

	@JsonProperty("my_club_name")
	private String myClubName = "Villarreal";

	@JsonProperty("youth_wage_threshold")
	private long youthWageThreshold = 10000L;

	@JsonProperty("league_coefficients")
	private Map<String, Double> leagueCoefficients = new HashMap<>();

	@JsonProperty("default_league_coefficient")
	private double defaultLeagueCoefficient = 0.70;

	public AppConfig() {}

	public Map<String, Object> getThresholds() { return thresholds; }
	public void setThresholds(Map<String, Object> thresholds) { this.thresholds = thresholds; }

	public List<SalaryTierConfig> getSalaryTiers() { return salaryTiers; }
	public void setSalaryTiers(List<SalaryTierConfig> salaryTiers) { this.salaryTiers = salaryTiers; }

	public String getActiveTactic() { return activeTactic; }
	public void setActiveTactic(String activeTactic) { this.activeTactic = activeTactic; }

	public List<TacticConfig> getTactics() { return tactics; }
	public void setTactics(List<TacticConfig> tactics) { this.tactics = tactics; }

	public String getMyClubName() { return myClubName; }
	public void setMyClubName(String myClubName) { this.myClubName = myClubName; }

	public long getYouthWageThreshold() {
		return youthWageThreshold <= 0 ? 10000L : youthWageThreshold;
	}

	public void setYouthWageThreshold(long youthWageThreshold) {
		this.youthWageThreshold = youthWageThreshold;
	}

	public Map<String, Double> getLeagueCoefficients() {
		return leagueCoefficients;
	}

	public void setLeagueCoefficients(Map<String, Double> leagueCoefficients) {
		this.leagueCoefficients = leagueCoefficients;
	}

	public double getDefaultLeagueCoefficient() {
		return defaultLeagueCoefficient <= 0.0 ? 0.70 : defaultLeagueCoefficient;
	}

	public void setDefaultLeagueCoefficient(double defaultLeagueCoefficient) {
		this.defaultLeagueCoefficient = defaultLeagueCoefficient;
	}

	/**
	 * Ermittelt den Liga-Koeffizienten für einen gegebenen Liganamen.
	 * Prüft zuerst auf exakten Match, danach auf Substrings (längere Schlüssel zuerst).
	 */
	public double getLeagueCoefficient(String leagueName) {
		if (leagueName == null || leagueName.isBlank() || leagueCoefficients == null || leagueCoefficients.isEmpty()) {
			return getDefaultLeagueCoefficient();
		}

		String cleanInput = leagueName.toLowerCase().trim();

		// 1. Direkter / Exakter Match
		for (Map.Entry<String, Double> entry : leagueCoefficients.entrySet()) {
			if (entry.getKey() != null && entry.getKey().trim().equalsIgnoreCase(cleanInput)) {
				return entry.getValue();
			}
		}

		// 2. Substring Match: Längere Schlüssel zuerst prüfen (z. B. "spanische 2." vor "spanische")
		String bestMatchKey = null;
		Double bestMatchVal = null;

		for (Map.Entry<String, Double> entry : leagueCoefficients.entrySet()) {
			if (entry.getKey() != null) {
				String key = entry.getKey().toLowerCase().trim();
				if (!key.isEmpty() && cleanInput.contains(key)) {
					if (bestMatchKey == null || key.length() > bestMatchKey.length()) {
						bestMatchKey = key;
						bestMatchVal = entry.getValue();
					}
				}
			}
		}

		if (bestMatchVal != null) {
			return bestMatchVal;
		}

		return getDefaultLeagueCoefficient();
	}
}