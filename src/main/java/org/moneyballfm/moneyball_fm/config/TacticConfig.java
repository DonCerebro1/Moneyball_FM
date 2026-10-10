package org.moneyballfm.moneyball_fm.config;

import java.util.List;

public class TacticConfig {
	private String name;
	private List<SlotConfig> slots;

	public TacticConfig() {}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public List<SlotConfig> getSlots() { return slots; }
	public void setSlots(List<SlotConfig> slots) { this.slots = slots; }
}