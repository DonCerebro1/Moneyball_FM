package org.moneyballfm.moneyball_fm.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricConfig {
	private String name;
	private double weight;
	private double benchmark;

	@JsonProperty("column_aliases")
	private List<String> columnAliases;

	public MetricConfig() {}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public double getWeight() { return weight; }
	public void setWeight(double weight) { this.weight = weight; }

	public double getBenchmark() { return benchmark; }
	public void setBenchmark(double benchmark) { this.benchmark = benchmark; }

	public List<String> getColumnAliases() { return columnAliases; }
	public void setColumnAliases(List<String> columnAliases) { this.columnAliases = columnAliases; }
}