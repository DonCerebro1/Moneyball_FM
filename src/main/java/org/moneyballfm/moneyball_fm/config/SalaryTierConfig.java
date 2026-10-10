package org.moneyballfm.moneyball_fm.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SalaryTierConfig {
	private String id;

	@JsonProperty("max_salary_month")
	private long maxSalaryMonth;

	@JsonProperty("target_score_min")
	private double targetScoreMin;

	public SalaryTierConfig() {}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public long getMaxSalaryMonth() { return maxSalaryMonth; }
	public void setMaxSalaryMonth(long maxSalaryMonth) { this.maxSalaryMonth = maxSalaryMonth; }

	public double getTargetScoreMin() { return targetScoreMin; }
	public void setTargetScoreMin(double targetScoreMin) { this.targetScoreMin = targetScoreMin; }
}