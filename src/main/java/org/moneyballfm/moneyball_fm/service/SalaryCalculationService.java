package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.SalaryTierConfig;

public class SalaryCalculationService {

	public record SalaryOfferRecommendation(
			String tierId,
			long maxSalaryMonth,
			long suggestedStartingOffer,
			long fairTargetSalary,
			long maxWalkAwaySalary,
			String assessment,
			String statusColorHex
	) {}

	public static SalaryOfferRecommendation calculateOffer(double score, long demandedSalaryMonth, AppConfig config) {
		return calculateOffer(score, demandedSalaryMonth, 1.0, config);
	}

	public static SalaryOfferRecommendation calculateOffer(double score, long demandedSalaryMonth, double availabilityFactor, AppConfig config) {
		SalaryTierConfig matchedTier = null;
		if (config != null && config.getSalaryTiers() != null && !config.getSalaryTiers().isEmpty()) {
			matchedTier = config.getSalaryTiers().stream()
					.sorted((a, b) -> Double.compare(b.getTargetScoreMin(), a.getTargetScoreMin()))
					.filter(t -> score >= t.getTargetScoreMin())
					.findFirst()
					.orElse(config.getSalaryTiers().get(config.getSalaryTiers().size() - 1));
		}

		String tierId = (matchedTier != null) ? matchedTier.getId() : "Tier 3 (Rotation/Backup)";
		long baseMaxCap = (matchedTier != null) ? matchedTier.getMaxSalaryMonth() : 350_000L;

		// Risiko-Deckelung: AF anwenden (begrenzt zwischen 0.40 und 1.0)
		double af = Math.min(1.0, Math.max(0.40, availabilityFactor > 0 ? availabilityFactor : 1.0));
		long maxCap = Math.round(baseMaxCap * af);

		// Verhandlungsstufen (70 % Einstieg, 85 % Ziel, 100 % Walk-Away bezogen auf das gedämpfte Cap)
		long startingOffer = Math.round(maxCap * 0.70);
		long targetSalary = Math.round(maxCap * 0.85);
		long walkAway = maxCap;

		String assessment;
		String color;

		String afWarning = (af < 0.90) ? String.format(" [AF: %.0f%% Risikoabschlag]", (1.0 - af) * 100.0) : "";

		if (demandedSalaryMonth <= 0) {
			assessment = "Bereit für Verhandlung" + afWarning;
			color = "#2563eb";
		} else if (demandedSalaryMonth <= startingOffer) {
			assessment = "🟢 TOP-DEAL: Forderung unter Einstiegsangebot" + afWarning;
			color = "#16a34a";
		} else if (demandedSalaryMonth <= targetSalary) {
			assessment = "✅ FAIR: Im optimalen Budgetkorridor" + afWarning;
			color = "#15803d";
		} else if (demandedSalaryMonth <= walkAway) {
			assessment = "🟡 GRENZWERTIG: Nahe an Obergrenze" + afWarning;
			color = "#d97706";
		} else {
			long diff = demandedSalaryMonth - walkAway;
			assessment = "🔴 ABLEHNEN: Übersteigt Max-Cap um " + String.format("%,d €", diff) + afWarning;
			color = "#dc2626";
		}

		return new SalaryOfferRecommendation(
				tierId,
				maxCap,
				startingOffer,
				targetSalary,
				walkAway,
				assessment,
				color
		);
	}
}