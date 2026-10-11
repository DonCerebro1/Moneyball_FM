package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.ScoutingPlayerModel;
import org.moneyballfm.moneyball_fm.model.ShadowSquadRowModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ShadowSquadService {

	public static List<ShadowSquadRowModel> generateShadowSquad(
			List<PlayerRowModel> squadPlayers,
			List<ScoutingPlayerModel> scoutPlayers,
			AppConfig config) {

		List<ShadowSquadRowModel> rows = new ArrayList<>();
		if (config == null || config.getTactics() == null || config.getTactics().isEmpty()) return rows;

		var activeTactic = config.getTactics().stream()
				.filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(config.getActiveTactic()))
				.findFirst()
				.orElse(config.getTactics().get(0));

		if (activeTactic == null || activeTactic.getSlots() == null) return rows;

		// Bereits zugeordnete Stammspieler und Scouts merken, um Doppelungen bei doppelten Rollen (z. B. 2x IV) zu vermeiden
		Set<String> usedStarters = new HashSet<>();
		Set<String> usedC1Scouts = new HashSet<>();

		for (var slot : activeTactic.getSlots()) {
			String slotLabel = slot.getRoleLabel() != null ? slot.getRoleLabel() : slot.getSlotName();
			String slotCode = slot.getSlotName() != null ? slot.getSlotName() : "";

			// 1. Stammspieler ermitteln (Höchster Score im A-Kader auf dieser Rolle, noch nicht belegt)
			PlayerRowModel starter = squadPlayers.stream()
					.filter(p -> !p.isOnLoan()) // Keine ausgeliehenen Fremdspieler als Stamm-Verkaufskandidaten
					.filter(p -> !usedStarters.contains(p.nameProperty().get()))
					.filter(p -> matchesRole(p.roleProperty().get(), slotLabel, slotCode))
					.max(Comparator.comparingDouble(p -> p.moneyballScoreProperty().get()))
					.orElse(null);

			String starterName = "— Kein Stammspieler —";
			long starterSalary = 0L;
			long starterValue = 0L;

			if (starter != null) {
				starterName = starter.nameProperty().get();
				starterSalary = starter.monthlySalaryProperty().get();
				starterValue = starter.marketValueProperty().get();
				usedStarters.add(starterName);
			}

			// 2. Schatten-Kandidaten aus dem Scouting-Tab filtern (Höchster Score zuerst)
			List<ScoutingPlayerModel> matchedScouts = scoutPlayers.stream()
					.filter(s -> matchesRole(s.targetRoleProperty().get(), slotLabel, slotCode))
					.sorted((a, b) -> Double.compare(b.scoutingScoreProperty().get(), a.scoutingScoreProperty().get()))
					.collect(Collectors.toList());

			// Kandidat 1 ermitteln (der noch nicht als C1 in einem anderen Slot vergeben ist)
			ScoutingPlayerModel c1 = matchedScouts.stream()
					.filter(s -> !usedC1Scouts.contains(s.nameProperty().get()))
					.findFirst()
					.orElse(null);

			String c1Name = "— Kein Scout-Kandidat —";
			long c1Fee = 0L;
			long c1Salary = 0L;

			if (c1 != null) {
				c1Name = c1.nameProperty().get();
				c1Fee = c1.askingPriceProperty().get();
				c1Salary = c1.wageDemandProperty().get();
				usedC1Scouts.add(c1Name);
			}

			// Kandidat 2 ermitteln (nächster bester Scout, ungleich C1)
			ScoutingPlayerModel c2 = matchedScouts.stream()
					.filter(s -> s != c1)
					.findFirst()
					.orElse(null);

			String c2Name = c2 != null ? c2.nameProperty().get() : (c1 != null ? "— Kein 2. Kandidat —" : "—");
			long c2Fee = c2 != null ? c2.askingPriceProperty().get() : 0L;
			long c2Salary = c2 != null ? c2.wageDemandProperty().get() : 0L;

			// 3. Finanz-Differenz (nur berechnen, wenn sowohl Stammspieler als auch C1-Kandidat vorhanden sind!)
			long netTransferGain = 0L;
			long monthlyWageSavings = 0L;

			if (starter != null && c1 != null) {
				netTransferGain = starterValue - c1Fee;
				monthlyWageSavings = starterSalary - c1Salary;
			}

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

	/**
	 * Prüft intelligent, ob eine Spieler-Rolle zu einem Taktik-Slot passt:
	 * - Exakter Match (z.B. "Stoßstürmer (A)" == "Stoßstürmer (A)")
	 * - Basis-Match ohne Rollen-Aufgabe (z.B. "Stoßstürmer" matcht "Stoßstürmer (A)")
	 * - Slot-Code Match (z.B. "ST", "ZM", "IV")
	 */
	private static boolean matchesRole(String playerRole, String slotLabel, String slotCode) {
		if (playerRole == null || playerRole.isBlank()) return false;

		String cleanPlayerRole = playerRole.trim().toLowerCase();
		String cleanSlotLabel = slotLabel != null ? slotLabel.trim().toLowerCase() : "";
		String cleanSlotCode = slotCode != null ? slotCode.trim().toLowerCase() : "";

		if (cleanPlayerRole.equalsIgnoreCase(cleanSlotLabel)) return true;
		if (!cleanSlotCode.isBlank() && cleanPlayerRole.equalsIgnoreCase(cleanSlotCode)) return true;

		// Klammern entfernen: "Stoßstürmer (A)" -> "stoßstürmer"
		String basePlayer = cleanPlayerRole.replaceAll("\\s*\\([^)]*\\)", "").trim();
		String baseSlot = cleanSlotLabel.replaceAll("\\s*\\([^)]*\\)", "").trim();

		if (!basePlayer.isBlank() && !baseSlot.isBlank() && basePlayer.equalsIgnoreCase(baseSlot)) {
			return true;
		}

		return false;
	}
}