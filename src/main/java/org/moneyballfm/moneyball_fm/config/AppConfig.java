package org.moneyballfm.moneyball_fm.config;

import com.fasterxml.jackson.annotation.JsonProperty;
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
}