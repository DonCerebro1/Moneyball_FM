package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.MetricConfig;
import org.moneyballfm.moneyball_fm.config.SlotConfig;
import org.moneyballfm.moneyball_fm.config.TacticConfig;
import org.moneyballfm.moneyball_fm.model.DepthChartRowModel;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.PositionNeedModel;
import org.moneyballfm.moneyball_fm.model.YouthPlayerModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SquadAnalysisService {

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
					.filter(p -> matchesSlot(p.roleProperty().get(), slot))
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

			// 1. Profis (A-Kader) nach Moneyball-Score absteigend sortieren
			List<PlayerRowModel> matchingPros = players.stream()
					.filter(p -> matchesSlot(p.roleProperty().get(), slot))
					.sorted((a, b) -> Double.compare(b.moneyballScoreProperty().get(), a.moneyballScoreProperty().get()))
					.toList();

			// 2. Talente (B/C/U19) nach Talent-Score absteigend sortieren
			List<YouthPlayerModel> matchingYouth = (youthPlayers != null) ? youthPlayers.stream()
					.filter(y -> matchesSlot(y.targetRoleProperty().get(), slot))
					.sorted((a, b) -> Double.compare(b.talentScoreProperty().get(), a.talentScoreProperty().get()))
					.toList() : List.of();

			int proIndex = 0;
			int youthIndex = 0;

			// Ränge 1 bis 4 abbilden
			for (int r = 1; r <= 4; r++) {
				DepthChartRowModel row = new DepthChartRowModel(roleName, r);

				if (proIndex < matchingPros.size()) {
					// A-Kader Profi belegt den Platz
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
						row.teamRoleProperty().set("📋 Kader-Ergänzung (P4)");
					}
				} else if (youthIndex < matchingYouth.size()) {
					// Kein Profi vorhanden -> Talent aus B/C/Jugend springt als Aushilfe ein
					YouthPlayerModel y = matchingYouth.get(youthIndex++);
					row.playerNameProperty().set(y.nameProperty().get() + " (🌱 B/C-Aushilfe)");
					row.moneyballScoreProperty().set(String.valueOf(Math.round(y.talentScoreProperty().get() * 10.0) / 10.0));
					row.ageProperty().set(String.valueOf(y.ageProperty().get()));
					row.monthlySalaryProperty().set("Jugendvertrag");
					row.teamRoleProperty().set("⚠️ Nachwuchs-Backup (Kein Profi vorhanden)");
				} else {
					// Weder Profi noch Nachwuchsspieler vorhanden
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

	private static boolean matchesSlot(String playerRole, SlotConfig slot) {
		if (playerRole == null) return false;
		String r = playerRole.toLowerCase().trim();
		String slotName = (slot.getSlotName() != null) ? slot.getSlotName().toLowerCase().trim() : "";
		String roleLabel = (slot.getRoleLabel() != null) ? slot.getRoleLabel().toLowerCase().trim() : "";

		return r.contains(slotName) || slotName.contains(r) || r.contains(roleLabel) || roleLabel.contains(r);
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