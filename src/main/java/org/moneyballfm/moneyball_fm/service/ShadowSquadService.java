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

		// Doppelbelegungen bei gleichen Positionen (z. B. 2x IV, 2x ZM) vermeiden
		Set<String> usedStarters = new HashSet<>();
		Set<String> usedScoutsAll = new HashSet<>();

		for (var slot : activeTactic.getSlots()) {
			String slotLabel = slot.getRoleLabel() != null ? slot.getRoleLabel() : slot.getSlotName();
			String slotCode = slot.getSlotName() != null ? slot.getSlotName() : "";

			// 1. Stammspieler ermitteln (Höchster Score im A-Kader auf dieser Rolle)
			PlayerRowModel starter = squadPlayers.stream()
					.filter(p -> !p.isOnLoan())
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

			// 2. Scouting-Targets filtern & ranken
			// Bevorzuge Spieler, die nicht als "ABLEHNEN" / "ÜBERTEUERT" / "UNVERKÄUFLICH" markiert sind
			List<ScoutingPlayerModel> matchedScouts = scoutPlayers.stream()
					.filter(s -> !usedScoutsAll.contains(s.nameProperty().get()))
					.filter(s -> matchesRole(s.targetRoleProperty().get(), slotLabel, slotCode))
					.sorted(Comparator
							.comparing((ScoutingPlayerModel s) -> isAffordable(s) ? 0 : 1)
							.thenComparing((ScoutingPlayerModel s) -> -s.scoutingScoreProperty().get()))
					.collect(Collectors.toList());

			// Kandidat 1
			ScoutingPlayerModel c1 = !matchedScouts.isEmpty() ? matchedScouts.get(0) : null;
			String c1Name = "— Kein Scout-Kandidat —";
			long c1Fee = 0L;
			long c1Salary = 0L;

			if (c1 != null) {
				c1Name = c1.nameProperty().get();
				c1Fee = c1.askingPriceProperty().get();
				c1Salary = c1.wageDemandProperty().get();
				usedScoutsAll.add(c1Name);
			}

			// Kandidat 2
			ScoutingPlayerModel c2 = matchedScouts.size() > 1 ? matchedScouts.get(1) : null;
			String c2Name = c2 != null ? c2.nameProperty().get() : (c1 != null ? "— Kein 2. Kandidat —" : "—");
			long c2Fee = c2 != null ? c2.askingPriceProperty().get() : 0L;
			long c2Salary = c2 != null ? c2.wageDemandProperty().get() : 0L;

			if (c2 != null) {
				usedScoutsAll.add(c2Name);
			}

			// 3. Finanz-Differenz (Gewinn/Ersparnis bei Verkauf des Starters & Kauf von C1)
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

	private static boolean isAffordable(ScoutingPlayerModel s) {
		if (s.askingPriceProperty().get() >= 900_000_000L) return false;
		String action = s.transferActionProperty().get();
		if (action == null) return true;
		return !action.contains("ABLEHNEN") && !action.contains("ZU HOCH") && !action.contains("KEINE FREIGABE");
	}

	private static boolean matchesRole(String playerRole, String slotLabel, String slotCode) {
		if (playerRole == null || playerRole.isBlank()) return false;

		String p = playerRole.trim().toUpperCase();
		String l = slotLabel != null ? slotLabel.trim().toUpperCase() : "";
		String c = slotCode != null ? slotCode.trim().toUpperCase() : "";

		if (p.equals(l) || (!c.isBlank() && p.equals(c))) return true;

		// Bereinigung von Aufgaben wie "(A)", "(V)", "(U)"
		String pBase = p.replaceAll("\\s*\\([^)]*\\)", "").trim();
		String lBase = l.replaceAll("\\s*\\([^)]*\\)", "").trim();

		if (!pBase.isBlank() && !lBase.isBlank()) {
			if (pBase.equals(lBase) || pBase.contains(lBase) || lBase.contains(pBase)) {
				return true;
			}
		}

		// Gruppierter Positionsabgleich als Fallback
		if ((l.contains("TORWART") || l.contains("TW") || l.contains("MTW")) && (p.contains("TORWART") || p.contains("TW") || p.contains("MTW"))) return true;
		if ((l.contains("STÜRMER") || l.contains("STO")) && (p.contains("STÜRMER") || p.contains("STO"))) return true;
		if ((l.contains("INNENVERTEIDIGER") || l.contains("IV") || l.contains("BSV")) && (p.contains("INNENVERTEIDIGER") || p.contains("IV") || p.contains("BSV"))) return true;
		if ((l.contains("FLÜGELVERTEIDIGER") || l.contains("FV") || l.contains("KFV")) && (p.contains("FLÜGELVERTEIDIGER") || p.contains("FV") || p.contains("KFV"))) return true;
		if ((l.contains("SPIELMACHER") || l.contains("REG") || l.contains("ZSM") || l.contains("ZENTRAL")) &&
				(p.contains("SPIELMACHER") || p.contains("REG") || p.contains("ZSM") || p.contains("ZENTRAL") || p.contains("DM"))) return true;

		return false;
	}
}