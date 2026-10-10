package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.MetricConfig;
import org.moneyballfm.moneyball_fm.config.SalaryTierConfig;
import org.moneyballfm.moneyball_fm.config.SlotConfig;
import org.moneyballfm.moneyball_fm.config.TacticConfig;
import org.moneyballfm.moneyball_fm.model.Player;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.TacticRole;

public class MoneyballCalculator {

	public static void recalculate(PlayerRowModel row, AppConfig config) {
		if (row == null) return;

		// 1. Stats dynamisch aus dem intern gespeicherten FM-Export aktualisieren, falls die Rolle gewechselt wurde
		String currentRoleStr = row.roleProperty().get();
		TacticRole tacticRole = TacticRole.fromDisplayName(currentRoleStr);
		Player player = row.getPlayer();

		if (tacticRole != null && player != null) {
			double newS1 = player.getStat(tacticRole.getStat1Header().toLowerCase());
			double newS2 = player.getStat(tacticRole.getStat2Header().toLowerCase());
			double newS3 = player.getStat(tacticRole.getStat3Header().toLowerCase());

			// Nur überschreiben, wenn tatsächliche Werte in den Exportdaten vorhanden sind
			if (newS1 > 0.0 || newS2 > 0.0 || newS3 > 0.0) {
				row.statValue1Property().set(newS1);
				row.statValue2Property().set(newS2);
				row.statValue3Property().set(newS3);
				row.setStatLabels(tacticRole.getStat1Header(), tacticRole.getStat2Header(), tacticRole.getStat3Header());
			}
		}

		int mins = row.minutesPlayedProperty().get();
		double s1 = row.statValue1Property().get();
		double s2 = row.statValue2Property().get();
		double s3 = row.statValue3Property().get();

		// 2. Spieler ohne relevante Spielzeit oder komplett ohne Stats nicht künstlich bewerten
		if (mins < 90 || (s1 == 0.0 && s2 == 0.0 && s3 == 0.0)) {
			row.moneyballScoreProperty().set(0.0);
			row.sampleQualityProperty().set("⚪ Keine Spielzeit (< 90 Min.)");
			row.salaryTierProperty().set("Keine Bewertung");
			row.maxSalaryProperty().set(0L);
			row.salaryCheckProperty().set("— Zu wenig Spielzeit");
			calculateFairMarketValue(row, 0.0);
			return;
		}

		SlotConfig slot = findMatchingSlot(currentRoleStr, config);
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

			// Sanfte Dämpfung bei geringer Spielzeit (< 450 Min), um extreme Ausreißer zu glätten
			if (mins < 450) {
				double confidence = Math.min(1.0, mins / 450.0);
				rawScore = (rawScore * confidence) + (50.0 * (1.0 - confidence));
			}

			finalScore = Math.round(rawScore * 10.0) / 10.0;
		} else {
			// Kein Slot in Taktik gefunden
			finalScore = 0.0;
		}

		row.moneyballScoreProperty().set(finalScore);

		// 3. Gehalts-Tier & Max-Gehalt zuweisen
		assignSalaryTier(row, finalScore, config);

		// 4. Fairer Marktwert berechnen
		calculateFairMarketValue(row, finalScore);

		// 5. Stichproben-Qualität (Spielzeit)
		if (mins >= 900) {
			row.sampleQualityProperty().set("🟢 Hohe Aussagekraft (>= 10 Spiele)");
		} else if (mins >= 450) {
			row.sampleQualityProperty().set("🟡 Solide Basis (5-9 Spiele)");
		} else {
			row.sampleQualityProperty().set("🔴 Geringe Stichprobe (< 5 Spiele)");
		}
	}

	private static void calculateFairMarketValue(PlayerRowModel row, double score) {
		long currentMarketValue = row.marketValueProperty().get();
		if (currentMarketValue <= 0 || score <= 0.0) {
			row.fairMarketValueProperty().set(0L);
			return;
		}

		int age = row.ageProperty().get();

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

		// Normalisieren: Abkürzungen expandieren und Trennzeichen glätten
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

			// 1. Kürzel-Check (z.B. "zsm", "mtw", "iv", "kfv", "bsv", "hrv")
			if (!sName.isEmpty() && (p.startsWith(sName) || p.contains(sName))) {
				return slot;
			}

			// 2. Rollenname-Check (z.B. "ballspielender vert. links")
			if (!rLabel.isEmpty() && (p.contains(rLabel) || rLabel.contains(p))) {
				return slot;
			}

			// 3. Semantischer Schlagwort-Check
			if (p.contains("ballspiel") && (rLabel.contains("ballspiel") || sName.contains("bsv") || rLabel.contains("bsv"))) {
				return slot;
			}
			if (p.contains("halbraum") && (rLabel.contains("halbraum") || sName.contains("hrv") || rLabel.contains("hrv"))) {
				return slot;
			}
			if (p.contains("spielmacher") && rLabel.contains("spielmacher")) {
				return slot;
			}
			if (p.contains("regista") && (rLabel.contains("regista") || sName.contains("reg"))) {
				return slot;
			}
			if (p.contains("flügelvert") && rLabel.contains("flügelvert")) {
				if (p.contains("links") == rLabel.contains("links") || p.contains("rechts") == rLabel.contains("rechts")) {
					return slot;
				}
			}
			if (p.contains("stürmer") && rLabel.contains("stürmer")) {
				return slot;
			}
			if (p.contains("torwart") && rLabel.contains("torwart")) {
				return slot;
			}
			if (p.contains("innenvert") && (rLabel.contains("innenvert") || sName.contains("iv"))) {
				return slot;
			}
		}

		return null;
	}

	private static void assignSalaryTier(PlayerRowModel row, double score, AppConfig config) {
		if (score <= 0.0) {
			row.salaryTierProperty().set("Keine Bewertung");
			row.maxSalaryProperty().set(0L);
			row.salaryCheckProperty().set("— Zu wenig Spielzeit");
			return;
		}

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