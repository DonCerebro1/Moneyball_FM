package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.MetricConfig;
import org.moneyballfm.moneyball_fm.config.SalaryTierConfig;
import org.moneyballfm.moneyball_fm.config.SlotConfig;
import org.moneyballfm.moneyball_fm.config.TacticConfig;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;

public class MoneyballCalculator {

	public static void recalculate(PlayerRowModel row, AppConfig config) {
		if (row == null) return;

		SlotConfig slot = findMatchingSlot(row.roleProperty().get(), config);
		double finalScore;

		if (slot != null && slot.getMetrics() != null && !slot.getMetrics().isEmpty()) {
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

				double metricScore;
				if (metric.isInverse()) {
					// Je weniger Fehler/Gegentore, desto näher an 100
					metricScore = Math.max(0.0, 100.0 - (val * 25.0));
				} else if (metric.getBenchmark() > 0) {
					// Benchmark = 85 Punkte. Bei ~18 % Übererfüllung werden die 100 erreicht.
					double ratio = val / metric.getBenchmark();
					metricScore = Math.min(100.0, ratio * 85.0);
				} else {
					metricScore = 50.0;
				}

				totalWeightedScore += metricScore * metric.getWeight();
				totalWeight += metric.getWeight();
			}

			double rawScore = (totalWeight > 0) ? (totalWeightedScore / totalWeight) : 50.0;

			// Sanfte Dämpfung bei geringer Spielzeit (< 450 Min), um Verzerrungen zu dämpfen
			int mins = row.minutesPlayedProperty().get();
			if (mins < 450 && mins > 0) {
				double confidence = Math.min(1.0, mins / 450.0);
				rawScore = (rawScore * confidence) + (65.0 * (1.0 - confidence));
			}

			finalScore = Math.round(rawScore * 10.0) / 10.0;
		} else {
			finalScore = 70.0;
		}

		row.moneyballScoreProperty().set(finalScore);

		// 2. Gehalts-Tier & Max-Gehalt zuweisen
		assignSalaryTier(row, finalScore, config);

		// 3. Fairer Marktwert berechnen
		calculateFairMarketValue(row, finalScore);

		// 4. Stichproben-Qualität (Spielzeit)
		int min = row.minutesPlayedProperty().get();
		if (min >= 900) {
			row.sampleQualityProperty().set("🟢 Hohe Aussagekraft (>= 10 Spiele)");
		} else if (min >= 450) {
			row.sampleQualityProperty().set("🟡 Solide Basis (5-9 Spiele)");
		} else {
			row.sampleQualityProperty().set("🔴 Geringe Stichprobe (< 5 Spiele)");
		}
	}

	private static void calculateFairMarketValue(PlayerRowModel row, double score) {
		long currentMarketValue = row.marketValueProperty().get();
		if (currentMarketValue <= 0) {
			row.fairMarketValueProperty().set(0L);
			return;
		}

		int age = row.ageProperty().get();

		// Altersmultiplikator: Talente mit Potenzialbonus, ältere Spieler mit Wertverlust
		double ageFactor = 1.0;
		if (age > 0 && age <= 21) {
			ageFactor = 1.25;
		} else if (age <= 24) {
			ageFactor = 1.10;
		} else if (age >= 32) {
			ageFactor = 0.80;
		} else if (age >= 29) {
			ageFactor = 0.90;
		}

		// Verhältnis zum Benchmark-Score (85.0 entspricht dem Marktwert-Standard)
		double performanceRatio = Math.max(0.3, score / 85.0);
		long fairValue = Math.round(currentMarketValue * performanceRatio * ageFactor);

		row.fairMarketValueProperty().set(fairValue);
	}

	private static SlotConfig findMatchingSlot(String playerRole, AppConfig config) {
		if (config == null || config.getTactics() == null || playerRole == null) return null;

		TacticConfig activeTactic = config.getTactics().stream()
				.filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(config.getActiveTactic()))
				.findFirst()
				.orElse(config.getTactics().isEmpty() ? null : config.getTactics().get(0));

		if (activeTactic == null || activeTactic.getSlots() == null) return null;

		String roleClean = playerRole.toLowerCase().trim();
		return activeTactic.getSlots().stream()
				.filter(s -> {
					String slotName = (s.getSlotName() != null) ? s.getSlotName().toLowerCase().trim() : "";
					String roleLabel = (s.getRoleLabel() != null) ? s.getRoleLabel().toLowerCase().trim() : "";
					return roleClean.contains(slotName) || slotName.contains(roleClean)
							|| roleClean.contains(roleLabel) || roleLabel.contains(roleClean);
				})
				.findFirst()
				.orElse(null);
	}

	private static void assignSalaryTier(PlayerRowModel row, double score, AppConfig config) {
		if (config != null && config.getSalaryTiers() != null && !config.getSalaryTiers().isEmpty()) {
			SalaryTierConfig matchedTier = config.getSalaryTiers().stream()
					.sorted((a, b) -> Double.compare(b.getTargetScoreMin(), a.getTargetScoreMin()))
					.filter(tier -> score >= tier.getTargetScoreMin())
					.findFirst()
					.orElse(config.getSalaryTiers().get(config.getSalaryTiers().size() - 1));

			if (matchedTier != null) {
				row.salaryTierProperty().set(matchedTier.getId());
				row.maxSalaryProperty().set(matchedTier.getMaxSalaryMonth());

				long salary = row.monthlySalaryProperty().get();
				if (salary <= matchedTier.getMaxSalaryMonth() * 0.75) {
					row.salaryCheckProperty().set("🟢 SCHNÄPPCHEN / TOP-DEAL");
				} else if (salary <= matchedTier.getMaxSalaryMonth()) {
					row.salaryCheckProperty().set("✅ FAIR / IM PLAN");
				} else {
					row.salaryCheckProperty().set("🔴 STARK ÜBERBEZAHLT");
				}
				return;
			}
		}

		// Fallback-Stufen
		long maxSal = 150_000L;
		String tier = "Tier 4 (Verkauf)";

		if (score >= 85.0) {
			tier = "Tier 1 (Key/Star)";
			maxSal = 1_000_000L;
		} else if (score >= 70.0) {
			tier = "Tier 2 (Stamm)";
			maxSal = 650_000L;
		} else if (score >= 55.0) {
			tier = "Tier 3 (Rotation/Backup)";
			maxSal = 350_000L;
		}

		row.salaryTierProperty().set(tier);
		row.maxSalaryProperty().set(maxSal);

		long curSal = row.monthlySalaryProperty().get();
		if (curSal <= maxSal * 0.75) {
			row.salaryCheckProperty().set("🟢 SCHNÄPPCHEN / TOP-DEAL");
		} else if (curSal <= maxSal) {
			row.salaryCheckProperty().set("✅ FAIR / IM PLAN");
		} else {
			row.salaryCheckProperty().set("🔴 STARK ÜBERBEZAHLT");
		}
	}
}