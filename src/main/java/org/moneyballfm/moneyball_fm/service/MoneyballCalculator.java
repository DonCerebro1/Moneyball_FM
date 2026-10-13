package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.MetricConfig;
import org.moneyballfm.moneyball_fm.config.SalaryTierConfig;
import org.moneyballfm.moneyball_fm.config.SlotConfig;
import org.moneyballfm.moneyball_fm.config.TacticConfig;
import org.moneyballfm.moneyball_fm.model.Player;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;

public class MoneyballCalculator {

	public static void recalculate(PlayerRowModel row, AppConfig config) {
		if (row == null) return;

		String currentRoleStr = row.roleProperty().get();
		Player player = row.getPlayer();
		SlotConfig slot = findMatchingSlot(currentRoleStr, config);

		// 1. Stats dynamisch aus den Metrik-Definitionen der aktiven Taktik laden
		if (slot != null && slot.getMetrics() != null && player != null) {
			for (int i = 0; i < slot.getMetrics().size(); i++) {
				MetricConfig m = slot.getMetrics().get(i);
				double val = 0.0;
				if (m.getColumnAliases() != null) {
					for (String alias : m.getColumnAliases()) {
						double statVal = player.getStat(alias);
						if (statVal > 0.0) {
							val = statVal;
							break;
						}
					}
				}
				if (val == 0.0 && m.getName() != null) {
					val = player.getStat(m.getName());
				}

				if (val > 0.0) {
					switch (i) {
						case 0 -> row.statValue1Property().set(val);
						case 1 -> row.statValue2Property().set(val);
						case 2 -> row.statValue3Property().set(val);
					}
				}
			}

			String lbl1 = !slot.getMetrics().isEmpty() ? slot.getMetrics().get(0).getName() : "Stat 1";
			String lbl2 = slot.getMetrics().size() > 1 ? slot.getMetrics().get(1).getName() : "Stat 2";
			String lbl3 = slot.getMetrics().size() > 2 ? slot.getMetrics().get(2).getName() : "Stat 3";
			row.setStatLabels(lbl1, lbl2, lbl3);
		}

		int mins = row.minutesPlayedProperty().get();
		double s1 = row.statValue1Property().get();
		double s2 = row.statValue2Property().get();
		double s3 = row.statValue3Property().get();

		// 2. Verfügbarkeitsfaktor (AF) berechnen
		double af = calculateAvailabilityFactor(mins, row.getInjurySusceptibility());
		row.setAvailabilityFactor(af);

		// 3. Nur abbrechen, wenn absolut keine Spielzeit (< 90 Min.) oder alle 3 Stats 0.0 sind
		if (mins < 90 || (s1 == 0.0 && s2 == 0.0 && s3 == 0.0)) {
			row.moneyballScoreProperty().set(0.0);
			row.sampleQualityProperty().set("⚪ Keine Spielzeit (< 90 Min.)");
			row.salaryTierProperty().set("Keine Bewertung");
			row.maxSalaryProperty().set(0L);
			row.salaryCheckProperty().set("— Zu wenig Spielzeit");
			calculateFairMarketValue(row, 0.0, af);
			return;
		}

		double finalScore;

		if (slot != null && slot.getMetrics() != null && !slot.getMetrics().isEmpty()) {
			double totalWeightedScore = 0.0;
			double totalWeight = 0.0;

			for (int i = 0; i < slot.getMetrics().size(); i++) {
				MetricConfig metric = slot.getMetrics().get(i);
				double val = switch (i) {
					case 0 -> s1;
					case 1 -> s2;
					case 2 -> s3;
					default -> 0.0;
				};

				double benchmark = metric.getBenchmark();

				// Prozent-Skalierung ausgleichen: Wenn Benchmark <= 1.0 (z. B. 0.85), FM-Wert aber > 1.0 (z. B. 95.0)
				if (benchmark > 0.0 && benchmark <= 1.0 && val > 1.0) {
					val = val / 100.0;
				}

				double metricScore;
				if (metric.isInverse()) {
					metricScore = Math.max(0.0, 100.0 - (val * 25.0));
				} else if (benchmark > 0) {
					double ratio = val / benchmark;
					metricScore = Math.min(100.0, ratio * 85.0);
				} else {
					metricScore = 50.0;
				}

				totalWeightedScore += metricScore * metric.getWeight();
				totalWeight += metric.getWeight();
			}

			double rawScore = (totalWeight > 0) ? (totalWeightedScore / totalWeight) : 50.0;

			// Sanfte Dämpfung bei geringer Spielzeit (< 450 Min), um extreme Ausreißer zu glätten
			if (mins < 450) {
				double confidence = Math.min(1.0, mins / 450.0);
				rawScore = (rawScore * confidence) + (50.0 * (1.0 - confidence));
			}

			finalScore = Math.round(rawScore * 10.0) / 10.0;
		} else {
			finalScore = 50.0;
		}

		row.moneyballScoreProperty().set(finalScore);

		// 4. Gehalts-Tier & Max-Gehalt
		assignSalaryTier(row, finalScore, af, config);

		// 5. Fairer Marktwert
		calculateFairMarketValue(row, finalScore, af);

		// 6. Stichproben-Qualität
		if (mins >= 900) {
			row.sampleQualityProperty().set("🟢 Hohe Aussagekraft (>= 10 Spiele)");
		} else if (mins >= 450) {
			row.sampleQualityProperty().set("🟡 Solide Basis (5-9 Spiele)");
		} else {
			row.sampleQualityProperty().set("🔴 Geringe Stichprobe (< 5 Spiele)");
		}
	}

	public static double calculateAvailabilityFactor(int minutes, String injuryText) {
		double minRatio = Math.min(1.0, Math.max(0.5, minutes / 1800.0));
		double riskFactor = 0.95;
		if (injuryText != null) {
			String lower = injuryText.toLowerCase().trim();
			if (lower.contains("sehr gering") || lower.contains("sehr niedrig")) riskFactor = 1.05;
			else if (lower.contains("unterdurchschnittlich") || lower.contains("gering") || lower.contains("niedrig")) riskFactor = 1.00;
			else if (lower.contains("sehr hoch") || lower.contains("akut")) riskFactor = 0.75;
			else if (lower.contains("hoch")) riskFactor = 0.85;
			else if (lower.contains("erhöht") || lower.contains("überdurchschnittlich")) riskFactor = 0.90;
		}
		double af = minRatio * riskFactor;
		return Math.round(Math.min(1.0, Math.max(0.4, af)) * 100.0) / 100.0;
	}

	private static void calculateFairMarketValue(PlayerRowModel row, double score, double af) {
		long currentMarketValue = row.marketValueProperty().get();
		if (currentMarketValue <= 0 || score <= 0.0) {
			row.fairMarketValueProperty().set(0L);
			return;
		}

		int age = row.ageProperty().get();
		double ageFactor = 1.0;
		if (age > 0 && age <= 21) ageFactor = 1.25;
		else if (age <= 24) ageFactor = 1.10;
		else if (age >= 32) ageFactor = 0.80;
		else if (age >= 29) ageFactor = 0.90;

		double performanceRatio = Math.max(0.3, score / 85.0);
		long fairValue = Math.round(currentMarketValue * performanceRatio * ageFactor * af);
		row.fairMarketValueProperty().set(fairValue);
	}

	private static SlotConfig findMatchingSlot(String playerRole, AppConfig config) {
		if (config == null || config.getTactics() == null || playerRole == null) return null;

		TacticConfig activeTactic = config.getTactics().stream()
				.filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(config.getActiveTactic()))
				.findFirst()
				.orElse(config.getTactics().isEmpty() ? null : config.getTactics().get(0));

		if (activeTactic == null || activeTactic.getSlots() == null) return null;

		String p = playerRole.toLowerCase()
				.replace("zurückgez.", "zurückgezogener")
				.replace("zurückgez", "zurückgezogener")
				.replace("komp.", "kompletter")
				.replace("vert.", "verteidiger")
				.replace("vert", "verteidiger")
				.replaceAll("[\\.\\(\\)\\[\\]\\-]", " ")
				.replaceAll("\\s+", " ")
				.trim();

		for (SlotConfig slot : activeTactic.getSlots()) {
			String sName = (slot.getSlotName() != null) ? slot.getSlotName().toLowerCase()
					.replaceAll("[\\.\\(\\)\\[\\]\\-]", " ").replaceAll("\\s+", " ").trim() : "";
			String rLabel = (slot.getRoleLabel() != null) ? slot.getRoleLabel().toLowerCase()
					.replaceAll("[\\.\\(\\)\\[\\]\\-]", " ").replaceAll("\\s+", " ").trim() : "";

			if (!sName.isEmpty() && (p.startsWith(sName) || p.contains(sName))) return slot;
			if (!rLabel.isEmpty() && (p.contains(rLabel) || rLabel.contains(p))) return slot;

			// Spezifische Matches
			if (p.contains("defensiv") && (rLabel.contains("defensiv") || sName.contains("dm"))) return slot;
			if (p.contains("ballspiel") && (rLabel.contains("ballspiel") || sName.contains("bsv"))) return slot;
			if (p.contains("halbraum") && (rLabel.contains("halbraum") || sName.contains("hrv"))) return slot;
			if (p.contains("regista") && (rLabel.contains("regista") || sName.contains("reg"))) return slot;
			if (p.contains("spielmacher") && rLabel.contains("spielmacher")) return slot;
			if (p.contains("flügelvert") && rLabel.contains("flügelvert")) {
				if (p.contains("links") == rLabel.contains("links") || p.contains("rechts") == rLabel.contains("rechts")) {
					return slot;
				}
			}
			if (p.contains("stürmer") && rLabel.contains("stürmer")) return slot;
			if (p.contains("torwart") && rLabel.contains("torwart")) return slot;
			if (p.contains("innenvert") && (rLabel.contains("innenvert") || sName.contains("iv"))) return slot;
		}

		return null;
	}

	private static void assignSalaryTier(PlayerRowModel row, double score, double af, AppConfig config) {
		if (score <= 0.0) {
			row.salaryTierProperty().set("Keine Bewertung");
			row.maxSalaryProperty().set(0L);
			row.salaryCheckProperty().set("— Zu wenig Spielzeit");
			return;
		}

		long baseMaxSal;
		String tierName;

		if (config != null && config.getSalaryTiers() != null && !config.getSalaryTiers().isEmpty()) {
			SalaryTierConfig matchedTier = config.getSalaryTiers().stream()
					.sorted((a, b) -> Double.compare(b.getTargetScoreMin(), a.getTargetScoreMin()))
					.filter(tier -> score >= tier.getTargetScoreMin())
					.findFirst()
					.orElse(config.getSalaryTiers().get(config.getSalaryTiers().size() - 1));

			tierName = matchedTier != null ? matchedTier.getId() : "Tier 4 (Verkauf)";
			baseMaxSal = matchedTier != null ? matchedTier.getMaxSalaryMonth() : 150_000L;
		} else {
			if (score >= 85.0) {
				tierName = "Tier 1 (Key/Star)";
				baseMaxSal = 1_000_000L;
			} else if (score >= 70.0) {
				tierName = "Tier 2 (Stamm)";
				baseMaxSal = 650_000L;
			} else if (score >= 55.0) {
				tierName = "Tier 3 (Rotation/Backup)";
				baseMaxSal = 350_000L;
			} else {
				tierName = "Tier 4 (Verkauf)";
				baseMaxSal = 150_000L;
			}
		}

		long adjustedMaxSal = Math.round(baseMaxSal * af);

		row.salaryTierProperty().set(tierName);
		row.maxSalaryProperty().set(adjustedMaxSal);

		long curSal = row.monthlySalaryProperty().get();
		if (curSal <= adjustedMaxSal * 0.75) {
			row.salaryCheckProperty().set("🟢 SCHNÄPPCHEN / TOP-DEAL");
		} else if (curSal <= adjustedMaxSal) {
			row.salaryCheckProperty().set("✅ FAIR / IM PLAN");
		} else {
			row.salaryCheckProperty().set("🔴 STARK ÜBERBEZAHLT");
		}
	}
}