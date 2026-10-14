package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.MetricConfig;
import org.moneyballfm.moneyball_fm.config.SlotConfig;
import org.moneyballfm.moneyball_fm.config.TacticConfig;
import org.moneyballfm.moneyball_fm.model.DepthChartRowModel;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.PositionNeedModel;
import org.moneyballfm.moneyball_fm.model.TacticRole;
import org.moneyballfm.moneyball_fm.model.YouthPlayerModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SquadAnalysisService {

	public record SquadKpis(
			double avgAge,
			double avgAthletics,
			long totalMonthlyWage,
			long totalMarketValue,
			int avgMinutes
	) {}

	public record NeedsSummary(
			int criticalCount,
			int warningCount,
			int optimalCount
	) {}

	public static SquadKpis calculateSquadKpis(List<PlayerRowModel> players) {
		if (players == null || players.isEmpty()) {
			return new SquadKpis(0.0, 0.0, 0L, 0L, 0);
		}

		double avgAge = players.stream()
				.mapToInt(p -> p.ageProperty().get())
				.average()
				.orElse(0.0);

		double avgAthletics = players.stream()
				.mapToDouble(p -> (p.agilityProperty().get() + p.paceProperty().get() + p.staminaProperty().get()) / 3.0)
				.average()
				.orElse(0.0);

		long totalWage = players.stream()
				.mapToLong(p -> p.monthlySalaryProperty().get())
				.sum();

		long totalMarketValue = players.stream()
				.mapToLong(p -> p.marketValueProperty().get())
				.sum();

		double avgMinutes = players.stream()
				.mapToInt(p -> p.minutesPlayedProperty().get())
				.average()
				.orElse(0.0);

		return new SquadKpis(
				avgAge,
				avgAthletics,
				totalWage,
				totalMarketValue,
				(int) Math.round(avgMinutes)
		);
	}

	public static NeedsSummary summarizeNeeds(List<PositionNeedModel> needs) {
		if (needs == null || needs.isEmpty()) {
			return new NeedsSummary(0, 0, 0);
		}

		int critical = 0;
		int warning = 0;
		int optimal = 0;

		for (PositionNeedModel n : needs) {
			String diag = n.diagnosisProperty().get();
			if (diag == null) continue;

			if (diag.startsWith("🚨") || diag.startsWith("🔴")) {
				critical++;
			} else if (diag.startsWith("⚠️") || diag.startsWith("⏳") || diag.startsWith("👴") || diag.startsWith("🟡")) {
				warning++;
			} else if (diag.startsWith("🟢")) {
				optimal++;
			}
		}

		return new NeedsSummary(critical, warning, optimal);
	}

	public static List<PositionNeedModel> analyzeSquadNeeds(List<PlayerRowModel> players, AppConfig config) {
		List<PositionNeedModel> result = new ArrayList<>();
		if (config == null || config.getTactics() == null || config.getTactics().isEmpty()) {
			return result;
		}

		TacticConfig activeTactic = config.getTactics().stream()
				.filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(config.getActiveTactic()))
				.findFirst()
				.orElse(config.getTactics().get(0));

		if (activeTactic.getSlots() == null) return result;

		final double critContractYears = 1.0;

		for (SlotConfig slot : activeTactic.getSlots()) {
			PositionNeedModel need = new PositionNeedModel(slot.getSlotName(), slot.getRoleLabel());

			List<PlayerRowModel> matchingPlayers = players.stream()
					.filter(p -> matchesPlayerSlot(p, slot))
					.toList();

			int count = matchingPlayers.size();
			need.playerCountProperty().set(count);

			if (count > 0) {
				PlayerRowModel best = matchingPlayers.stream()
						.max((a, b) -> Double.compare(a.moneyballScoreProperty().get(), b.moneyballScoreProperty().get()))
						.orElse(null);

				if (best != null) {
					need.bestPlayerNameProperty().set(best.nameProperty().get());
					need.bestScoreProperty().set(best.moneyballScoreProperty().get());
				}

				double avgScore = matchingPlayers.stream()
						.mapToDouble(p -> p.moneyballScoreProperty().get())
						.average().orElse(0.0);
				need.averageScoreProperty().set(Math.round(avgScore * 10.0) / 10.0);

				double avgAge = matchingPlayers.stream()
						.mapToInt(p -> p.ageProperty().get())
						.average().orElse(0.0);
				need.averageAgeProperty().set(Math.round(avgAge * 10.0) / 10.0);

				long critCount = matchingPlayers.stream()
						.filter(p -> p.contractYearsProperty().get() > 0.0 && p.contractYearsProperty().get() <= critContractYears)
						.count();
				need.criticalContractsProperty().set((int) critCount);

				evaluateDiagnosis(need, count, best != null ? best.moneyballScoreProperty().get() : 0.0, avgAge, (int) critCount);

			} else {
				need.diagnosisProperty().set("🚨 POSITION UNBESETZT (0 Spieler)");
				need.recommendationProperty().set("🚨 SOFORT TRANSFER TÄTIGEN (Prio 1)");
			}

			need.targetProfileProperty().set(buildTargetProfile(slot));
			result.add(need);
		}

		return result;
	}

	public static List<DepthChartRowModel> generateDepthChart(List<PlayerRowModel> players, List<YouthPlayerModel> youthPlayers, AppConfig config) {
		List<DepthChartRowModel> result = new ArrayList<>();
		if (config == null || config.getTactics() == null || config.getTactics().isEmpty()) {
			return result;
		}

		TacticConfig activeTactic = config.getTactics().stream()
				.filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(config.getActiveTactic()))
				.findFirst()
				.orElse(config.getTactics().get(0));

		if (activeTactic.getSlots() == null) return result;

		NumberFormat euroFmt = NumberFormat.getNumberInstance(Locale.GERMANY);

		for (SlotConfig slot : activeTactic.getSlots()) {
			String roleName = slot.getRoleLabel() != null && !slot.getRoleLabel().isBlank()
					? slot.getRoleLabel() : slot.getSlotName();

			List<PlayerRowModel> matchingPros = players.stream()
					.filter(p -> matchesPlayerSlot(p, slot))
					.sorted((a, b) -> Double.compare(b.moneyballScoreProperty().get(), a.moneyballScoreProperty().get()))
					.toList();

			List<YouthPlayerModel> matchingYouth = (youthPlayers != null) ? youthPlayers.stream()
					.filter(y -> matchesRoleAndSide(y.targetRoleProperty().get(), slot))
					.sorted((a, b) -> Double.compare(b.talentScoreProperty().get(), a.talentScoreProperty().get()))
					.toList() : List.of();

			int proIndex = 0;
			int youthIndex = 0;

			for (int r = 1; r <= 4; r++) {
				String displayRole = (r == 1) ? roleName : "";
				DepthChartRowModel row = new DepthChartRowModel(displayRole, r);

				if (proIndex < matchingPros.size()) {
					PlayerRowModel p = matchingPros.get(proIndex++);
					row.playerNameProperty().set(p.nameProperty().get());
					row.moneyballScoreProperty().set(String.valueOf(Math.round(p.moneyballScoreProperty().get() * 10.0) / 10.0));
					row.ageProperty().set(String.valueOf(p.ageProperty().get()));
					row.monthlySalaryProperty().set(p.monthlySalaryProperty().get() > 0
							? euroFmt.format(p.monthlySalaryProperty().get()) + " €" : "—");

					if (r == 1) {
						row.teamRoleProperty().set("⭐ Stammspieler (P1)");
					} else if (r == 2) {
						row.teamRoleProperty().set("🔄 Backup (P2)");
					} else if (r == 3) {
						row.teamRoleProperty().set("🌱 Rotation (P3)");
					} else {
						row.teamRoleProperty().set("📋 Ergänzung (P4)");
					}
				} else if (youthIndex < matchingYouth.size()) {
					YouthPlayerModel y = matchingYouth.get(youthIndex++);
					String teamTag = y.ageProperty().get() <= 18 ? " [U19]" : " [B-Team]";
					row.playerNameProperty().set(y.nameProperty().get() + teamTag);
					row.moneyballScoreProperty().set(String.valueOf(Math.round(y.talentScoreProperty().get() * 10.0) / 10.0));
					row.ageProperty().set(String.valueOf(y.ageProperty().get()));
					row.monthlySalaryProperty().set("Jugendvertrag");
					row.teamRoleProperty().set("🌱 Nachwuchs-Backup");
				} else {
					row.playerNameProperty().set("—");
					row.moneyballScoreProperty().set("—");
					row.teamRoleProperty().set("Offene Planstelle");
					row.ageProperty().set("—");
					row.monthlySalaryProperty().set("—");
				}

				result.add(row);
			}
		}

		return result;
	}

	private static boolean matchesPlayerSlot(PlayerRowModel player, SlotConfig slot) {
		if (player == null || slot == null) return false;

		String playerRole = player.roleProperty().get();
		if (playerRole == null || playerRole.isBlank()) return false;

		return matchesRoleAndSide(playerRole, slot);
	}

	private static boolean matchesRoleAndSide(String playerRole, SlotConfig slot) {
		if (playerRole == null || slot == null) return false;

		String slotName = slot.getSlotName() != null ? slot.getSlotName().trim() : "";
		String roleLabel = slot.getRoleLabel() != null ? slot.getRoleLabel().trim() : slotName;

		// 1. Exakter Namensabgleich
		if (playerRole.equalsIgnoreCase(roleLabel) || playerRole.equalsIgnoreCase(slotName)) {
			return true;
		}

		// 2. Normalisierter String-Abgleich
		String pClean = normalizeRole(playerRole);
		String sClean = normalizeRole(roleLabel);
		String sNameClean = normalizeRole(slotName);

		if (pClean.equals(sClean) || pClean.equals(sNameClean)) {
			return true;
		}

		// Torwart-Brücke: Füllt denselben Torwart-Slot im Tiefenchart
		boolean pIsGk = pClean.contains("torwart") || pClean.contains("tw") || pClean.contains("mtw") || pClean.contains("gk");
		boolean sIsGk = sClean.contains("torwart") || sClean.contains("tw") || sClean.contains("mtw") || sNameClean.contains("tw") || sNameClean.contains("mtw") || sNameClean.contains("gk");
		if (pIsGk && sIsGk) {
			return true;
		}

		// 3. Seiten-Ausschluss (Links vs. Rechts)
		boolean pLeft = pClean.contains("links") || playerRole.toLowerCase().contains("(l)");
		boolean pRight = pClean.contains("rechts") || playerRole.toLowerCase().contains("(r)");

		boolean sLeft = sClean.contains("links") || sNameClean.contains("links") || roleLabel.toLowerCase().contains("(l)");
		boolean sRight = sClean.contains("rechts") || sNameClean.contains("rechts") || roleLabel.toLowerCase().contains("(r)");

		if ((pLeft && sRight) || (pRight && sLeft)) {
			return false;
		}

		// 4. Striktes TacticRole-Enum
		TacticRole pRole = resolveRoleSafely(playerRole);
		TacticRole sRole = resolveSlotTacticRole(slot);

		if (pRole != null && sRole != null && pRole == sRole) {
			if (sLeft && !pLeft && pRight) return false;
			if (sRight && !pRight && pLeft) return false;
			return true;
		}

		return false;
	}

	private static String normalizeRole(String text) {
		if (text == null) return "";
		return text.toLowerCase()
				.replace("zurückgez.", "zurückgezogener")
				.replace("zurückgez", "zurückgezogener")
				.replace("komp.", "kompletter")
				.replace("vert.", "verteidiger")
				.replace("vert", "verteidiger")
				.replaceAll("[\\.\\(\\)\\[\\]\\-]", "")
				.replaceAll("\\s+", "")
				.trim();
	}

	private static TacticRole resolveSlotTacticRole(SlotConfig slot) {
		String name = slot.getSlotName() != null ? slot.getSlotName() : "";
		String label = slot.getRoleLabel() != null ? slot.getRoleLabel() : "";

		TacticRole tr = resolveRoleSafely(label);
		if (tr != null && tr != TacticRole.ZSM) return tr;

		tr = resolveRoleSafely(name);
		if (tr != null && tr != TacticRole.ZSM) return tr;

		return TacticRole.fromPosition(label + " " + name);
	}

	private static TacticRole resolveRoleSafely(String text) {
		if (text == null || text.isBlank()) return TacticRole.ZSM;

		String clean = text.toLowerCase()
				.replaceAll("(?i)\\b(links|rechts|zentral|mitte)\\b", "")
				.replaceAll("\\s*\\([^)]*\\)", "")
				.replace("offensives mittelfeldspieler", "offensiver mittelfeldspieler")
				.trim();

		if (clean.contains("offensiv") || clean.equals("om") || clean.contains("omz")) {
			return TacticRole.OFFENSIVER_MITTELFELDSSPIELER;
		}

		TacticRole tr = TacticRole.fromDisplayName(clean);
		if (tr != null) return tr;

		return TacticRole.fromPosition(clean);
	}

	private static void evaluateDiagnosis(PositionNeedModel need, int count, double topScore, double avgAge, int critContracts) {
		if (count == 1) {
			need.diagnosisProperty().set("⚠️ KEIN BACKUP (Nur 1 Spieler)");
			need.recommendationProperty().set("⚠️ Backup oder Talent verpflichten");
		} else if (topScore < 70.0) {
			need.diagnosisProperty().set("🔴 LEISTUNGSSCHWACH (Top-Score < 70)");
			need.recommendationProperty().set("🔴 Upgrade für Stammelf scouten");
		} else if (critContracts > 0) {
			need.diagnosisProperty().set("⏳ VERTRAGSLAGE KRITISCH (" + critContracts + " Spieler)");
			need.recommendationProperty().set("📝 Vertrag verlängern oder verkaufen");
		} else if (avgAge >= 30.0) {
			need.diagnosisProperty().set("👴 ÜBERALTERT (Ø Alter >= 30)");
			need.recommendationProperty().set("🟡 Nachfolger / Perspektive aufbauen");
		} else if (topScore >= 85.0 && count >= 2) {
			need.diagnosisProperty().set("🟢 OPTIMAL BESETZT");
			need.recommendationProperty().set("✅ Keine Handlung nötig");
		} else {
			need.diagnosisProperty().set("🟡 SOLIDE BESETZT");
			need.recommendationProperty().set("ℹ️ Markt beobachten");
		}
	}

	private static String buildTargetProfile(SlotConfig slot) {
		if (slot.getMetrics() == null || slot.getMetrics().isEmpty()) return "-";
		StringBuilder sb = new StringBuilder();
		for (MetricConfig m : slot.getMetrics()) {
			if (sb.length() > 0) sb.append(" | ");
			sb.append(m.getName()).append(": >=").append(m.getBenchmark());
		}
		return sb.toString();
	}
}