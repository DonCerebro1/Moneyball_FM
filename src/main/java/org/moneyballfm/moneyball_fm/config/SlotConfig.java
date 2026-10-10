package org.moneyballfm.moneyball_fm.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SlotConfig {
	@JsonProperty("slot_name")
	private String slotName;

	@JsonProperty("role_label")
	private String roleLabel;

	private List<MetricConfig> metrics;

	public SlotConfig() {}

	public String getSlotName() { return slotName; }
	public void setSlotName(String slotName) { this.slotName = slotName; }

	public String getRoleLabel() { return roleLabel; }
	public void setRoleLabel(String roleLabel) { this.roleLabel = roleLabel; }

	public List<MetricConfig> getMetrics() { return metrics; }
	public void setMetrics(List<MetricConfig> metrics) { this.metrics = metrics; }
}