package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.MetricConfig;
import org.moneyballfm.moneyball_fm.config.SalaryTierConfig;
import org.moneyballfm.moneyball_fm.config.SlotConfig;
import org.moneyballfm.moneyball_fm.config.TacticConfig;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;

public class MoneyballCalculator {

	public static void recalculate(PlayerRowModel row, AppConfig config) {
		if (config == null || config.getTactics() == null) return;

		// 1. Aktive Taktik suchen
		TacticConfig activeTactic = config.getTactics().stream()
				.filter(t -> t.getName().equalsIgnoreCase(config.getActiveTactic()))
				.findFirst()
				.orElse(config.getTactics().get(0));

		// 2. Passenden Slot für die Spielerrolle finden
		SlotConfig slot = activeTactic.getSlots().stream()
				.filter(s -> s.getSlotName().equalsIgnoreCase(row.roleProperty().get()))
				.findFirst()
				.orElse(null);

		double score = 0.0;
		if (slot != null && slot.getMetrics() != null) {
			double totalWeightedScore = 0.0;
			double totalWeight = 0.0;

			for (int i = 0; i < slot.getMetrics().size(); i++) {
				MetricConfig metric = slot.getMetrics().get(i);
				double val = switch (i) {
					case 0 -> row.statValue1Property().get();
					case 1 -> row.statValue2Property().get();
					case 2 -> row.statValue3Property().get();
					default -> 0.0;
				};

				double metricScore = (metric.getBenchmark() > 0)
						? (val / metric.getBenchmark()) * 100.0
						: 0.0;

				totalWeightedScore += Math.min(metricScore, 100.0) * metric.getWeight();
				totalWeight += metric.getWeight();
			}

			if (totalWeight > 0) {
				score = Math.round((totalWeightedScore / totalWeight) * 10.0) / 10.0;
			}
		}
		row.moneyballScoreProperty().set(score);

		// 3. Gehalts-Tier & Check
		assignSalaryTier(row, score, config);

		// 4. Stichproben-Qualität (Spielzeit)
		int min = row.minutesPlayedProperty().get();
		if (min >= 900) {
			row.sampleQualityProperty().set("🟢 Hohe Aussagekraft (>= 10 Spiele)");
		} else if (min < 450) {
			row.sampleQualityProperty().set("🔴 Geringe Stichprobe (< 5 Spiele)");
		} else {
			row.sampleQualityProperty().set("🟡 Mittlere Stichprobe");
		}
	}

	private static void assignSalaryTier(PlayerRowModel row, double score, AppConfig config) {
		if (config.getSalaryTiers() == null) return;

		SalaryTierConfig matchedTier = null;
		for (SalaryTierConfig tier : config.getSalaryTiers()) {
			if (score >= tier.getTargetScoreMin()) {
				matchedTier = tier;
				break;
			}
		}

		if (matchedTier != null) {
			row.salaryTierProperty().set(matchedTier.getId());
			long salary = row.monthlySalaryProperty().get();
			if (salary <= matchedTier.getMaxSalaryMonth() * 0.75) {
				row.salaryCheckProperty().set("🟢 SCHNÄPPCHEN / TOP-DEAL");
			} else if (salary <= matchedTier.getMaxSalaryMonth()) {
				row.salaryCheckProperty().set("✅ FAIR / IM PLAN");
			} else {
				row.salaryCheckProperty().set("🔴 STARK ÜBERBEZAHLT");
			}
		}
	}
}