package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.ScoutingPlayerModel;
import org.moneyballfm.moneyball_fm.model.ShadowSquadRowModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ShadowSquadService {

	public static List<ShadowSquadRowModel> generateShadowSquad(
			List<PlayerRowModel> squadPlayers,
			List<ScoutingPlayerModel> scoutPlayers,
			AppConfig config) {

		List<ShadowSquadRowModel> rows = new ArrayList<>();
		if (config == null || config.getTactics() == null) return rows;

		var activeTactic = config.getTactics().stream()
				.filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(config.getActiveTactic()))
				.findFirst()
				.orElse(config.getTactics().get(0));

		if (activeTactic == null || activeTactic.getSlots() == null) return rows;

		for (var slot : activeTactic.getSlots()) {
			String slotLabel = slot.getRoleLabel() != null ? slot.getRoleLabel() : slot.getSlotName();

			// 1. Stammspieler ermitteln (Höchster Score im A-Kader auf dieser Rolle)
			PlayerRowModel starter = squadPlayers.stream()
					.filter(p -> p.roleProperty().get() != null &&
							(p.roleProperty().get().equalsIgnoreCase(slotLabel) ||
									p.roleProperty().get().equalsIgnoreCase(slot.getSlotName())))
					.max(Comparator.comparingDouble(p -> p.moneyballScoreProperty().get()))
					.orElse(null);

			String starterName = starter != null ? starter.nameProperty().get() : "— Kein Stammspieler —";
			long starterSalary = starter != null ? starter.monthlySalaryProperty().get() : 0L;
			long starterValue = starter != null ? starter.marketValueProperty().get() : 0L;

			// 2. Schatten-Kandidaten aus dem Scouting-Tab filtern (Die besten 2 nach Scouting-Score)
			List<ScoutingPlayerModel> matchedScouts = scoutPlayers.stream()
					.filter(s -> s.targetRoleProperty().get() != null &&
							(s.targetRoleProperty().get().equalsIgnoreCase(slotLabel) ||
									s.targetRoleProperty().get().equalsIgnoreCase(slot.getSlotName()) ||
									slotLabel.toLowerCase().startsWith(s.targetRoleProperty().get().toLowerCase().substring(0, Math.min(3, s.targetRoleProperty().get().length())))))
					.sorted((a, b) -> Double.compare(b.scoutingScoreProperty().get(), a.scoutingScoreProperty().get()))
					.collect(Collectors.toList());

			// Kandidat 1 (Bester)
			String c1Name = "— Kein Scout-Kandidat —";
			long c1Fee = 0L;
			long c1Salary = 0L;
			if (!matchedScouts.isEmpty()) {
				ScoutingPlayerModel best = matchedScouts.get(0);
				c1Name = best.nameProperty().get();
				c1Fee = best.askingPriceProperty().get();
				c1Salary = best.wageDemandProperty().get();
			}

			// Kandidat 2 (Zweitbester)
			String c2Name = "— Kein 2. Kandidat —";
			long c2Fee = 0L;
			long c2Salary = 0L;
			if (matchedScouts.size() > 1) {
				ScoutingPlayerModel second = matchedScouts.get(1);
				c2Name = second.nameProperty().get();
				c2Fee = second.askingPriceProperty().get();
				c2Salary = second.wageDemandProperty().get();
			} else if (matchedScouts.isEmpty()) {
				c2Name = "—";
			}

			// 3. Berechnungen wie in Excel Spalte K und L
			long netTransferGain = starterValue - c1Fee;
			long monthlyWageSavings = starterSalary - c1Salary;

			rows.add(new ShadowSquadRowModel(
					slotLabel,
					starterName,
					starterSalary,
					starterValue,
					c1Name,
					c1Fee,
					c1Salary,
					c2Name,
					c2Fee,
					c2Salary,
					netTransferGain,
					monthlyWageSavings
			));
		}

		return rows;
	}
}