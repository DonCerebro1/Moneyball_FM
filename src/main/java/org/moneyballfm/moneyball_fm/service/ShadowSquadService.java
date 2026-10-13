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

		Set<String> usedStarters = new HashSet<>();
		Set<String> usedScoutsAll = new HashSet<>();

		for (var slot : activeTactic.getSlots()) {
			String slotLabel = slot.getRoleLabel() != null ? slot.getRoleLabel().trim() : (slot.getSlotName() != null ? slot.getSlotName().trim() : "");
			String slotCode = slot.getSlotName() != null ? slot.getSlotName().trim() : "";

			// 1. Exakter Stammspieler-Abgleich über die tatsächlich gewählte Rolle
			PlayerRowModel starter = squadPlayers.stream()
					.filter(p -> !p.isOnLoan())
					.filter(p -> !usedStarters.contains(p.nameProperty().get()))
					.filter(p -> isExactRoleMatch(p.roleProperty().get(), slotLabel, slotCode))
					.max(Comparator.comparingDouble(p -> p.moneyballScoreProperty().get()))
					.orElse(null);

			// Fallback nur, wenn noch kein Spieler exakt auf dieser Rolle steht
			if (starter == null) {
				starter = squadPlayers.stream()
						.filter(p -> !p.isOnLoan())
						.filter(p -> !usedStarters.contains(p.nameProperty().get()))
						.filter(p -> isCompatibleRoleMatch(p.roleProperty().get(), slotLabel, slotCode))
						.max(Comparator.comparingDouble(p -> p.moneyballScoreProperty().get()))
						.orElse(null);
			}

			String starterName = "— Kein Stammspieler —";
			long starterSalary = 0L;
			long starterValue = 0L;

			if (starter != null) {
				starterName = starter.nameProperty().get();
				starterSalary = starter.monthlySalaryProperty().get();
				starterValue = starter.marketValueProperty().get();
				usedStarters.add(starterName);
			}

			// 2. Scouts für diesen Slot filtern
			List<ScoutingPlayerModel> matchedScouts = scoutPlayers.stream()
					.filter(s -> !usedScoutsAll.contains(s.nameProperty().get()))
					.filter(s -> isExactRoleMatch(s.targetRoleProperty().get(), slotLabel, slotCode)
							|| isCompatibleRoleMatch(s.targetRoleProperty().get(), slotLabel, slotCode))
					.filter(ShadowSquadService::isAffordable)
					.collect(Collectors.toList());

			// 3. Trennung: Qualifizierte Scouts vs. Scouts unter Beobachtung
			List<ScoutingPlayerModel> qualifiedScouts = matchedScouts.stream()
					.filter(s -> s.scoutingScoreProperty().get() > 0.0)
					.sorted(Comparator
							.comparingDouble((ScoutingPlayerModel s) -> -s.scoutingScoreProperty().get())
							.thenComparingLong(s -> s.askingPriceProperty().get()))
					.collect(Collectors.toList());

			boolean hasUnscoredScouts = matchedScouts.stream()
					.anyMatch(s -> s.scoutingScoreProperty().get() <= 0.0);

			// 4. Kandidat 1
			ScoutingPlayerModel c1 = !qualifiedScouts.isEmpty() ? qualifiedScouts.get(0) : null;
			String c1Name;
			long c1Fee = 0L;
			long c1Salary = 0L;

			if (c1 != null) {
				c1Name = c1.nameProperty().get();
				c1Fee = c1.askingPriceProperty().get();
				c1Salary = c1.wageDemandProperty().get();
				usedScoutsAll.add(c1Name);
			} else if (hasUnscoredScouts) {
				c1Name = "⏳ Unter Beobachtung (zu wenig Daten)";
			} else {
				c1Name = "— Kein Scout-Kandidat —";
			}

			// 5. Kandidat 2
			ScoutingPlayerModel c2 = qualifiedScouts.size() > 1 ? qualifiedScouts.get(1) : null;
			String c2Name;
			long c2Fee = 0L;
			long c2Salary = 0L;

			if (c2 != null) {
				c2Name = c2.nameProperty().get();
				c2Fee = c2.askingPriceProperty().get();
				c2Salary = c2.wageDemandProperty().get();
				usedScoutsAll.add(c2Name);
			} else if (c1 != null) {
				boolean hasRemainingUnscored = matchedScouts.stream()
						.filter(s -> !usedScoutsAll.contains(s.nameProperty().get()))
						.anyMatch(s -> s.scoutingScoreProperty().get() <= 0.0);

				c2Name = hasRemainingUnscored
						? "⏳ Unter Beobachtung (zu wenig Daten)"
						: "— Kein 2. Kandidat —";
			} else if (hasUnscoredScouts && matchedScouts.size() > 1) {
				c2Name = "⏳ Unter Beobachtung (zu wenig Daten)";
			} else {
				c2Name = "— Kein 2. Kandidat —";
			}

			// 6. Finanz-Kennzahlen
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

	/**
	 * Prüft, ob die Rolle exakt übereinstimmt (auch ohne Rollen-Aufgaben wie '(A)' oder '(V)').
	 */
	private static boolean isExactRoleMatch(String playerRole, String slotLabel, String slotCode) {
		if (playerRole == null || playerRole.isBlank()) return false;

		String p = playerRole.trim().toUpperCase();
		String l = slotLabel != null ? slotLabel.trim().toUpperCase() : "";
		String c = slotCode != null ? slotCode.trim().toUpperCase() : "";

		if (p.equals(l) || (!c.isBlank() && p.equals(c))) return true;

		String pClean = p.replaceAll("\\s*\\([^)]*\\)", "").trim();
		String lClean = l.replaceAll("\\s*\\([^)]*\\)", "").trim();

		return !pClean.isBlank() && pClean.equals(lClean);
	}

	/**
	 * Sauberer, positionssicherer Fallback für ähnliche Rollenbezeichnungen,
	 * ohne dass Innenverteidiger im Mittelfeld landen.
	 */
	private static boolean isCompatibleRoleMatch(String playerRole, String slotLabel, String slotCode) {
		if (playerRole == null || playerRole.isBlank()) return false;

		String p = playerRole.trim().toUpperCase();
		String l = slotLabel != null ? slotLabel.trim().toUpperCase() : "";

		// Torwart
		if (l.contains("TORWART") || l.contains("TW")) {
			return p.contains("TORWART") || p.contains("TW");
		}
		// Innenverteidigung
		if (l.contains("INNENVERTEIDIGER") || l.contains("BALLSPIELENDER VERT") || l.contains("HALBRAUMVERTEIDIGER")) {
			return p.contains("INNENVERTEIDIGER") || p.contains("BALLSPIELENDER VERT") || p.contains("HALBRAUMVERTEIDIGER");
		}
		// Außenverteidiger
		if (l.contains("FLÜGELVERTEIDIGER")) {
			return p.contains("FLÜGELVERTEIDIGER");
		}
		// Stürmer
		if (l.contains("STÖSSTÜRMER") || l.contains("STÜRMER")) {
			return p.contains("STÖSSTÜRMER") || p.contains("STÜRMER");
		}
		// Offensives Mittelfeld (Rechts / Links / Zentral)
		if (l.contains("OFFENSIVER MITTELFELD")) {
			if (!p.contains("OFFENSIVER MITTELFELD") && !p.contains("INVERTIERTER FLÜGEL")) return false;
			if (l.contains("RECHTS")) return p.contains("RECHTS");
			if (l.contains("LINKS")) return p.contains("LINKS");
			return true;
		}
		// Zentrales / Defensives Mittelfeld
		if (l.contains("SPIELMACHER") || l.contains("REGISTA") || l.contains("DEFENSIVER MITTELFELD")) {
			return p.contains("SPIELMACHER") || p.contains("REGISTA") || p.contains("DEFENSIVER MITTELFELD");
		}

		return false;
	}
}