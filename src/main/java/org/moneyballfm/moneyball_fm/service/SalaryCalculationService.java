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
		SalaryTierConfig matchedTier = null;
		if (config != null && config.getSalaryTiers() != null && !config.getSalaryTiers().isEmpty()) {
			matchedTier = config.getSalaryTiers().stream()
					.sorted((a, b) -> Double.compare(b.getTargetScoreMin(), a.getTargetScoreMin()))
					.filter(t -> score >= t.getTargetScoreMin())
					.findFirst()
					.orElse(config.getSalaryTiers().get(config.getSalaryTiers().size() - 1));
		}

		String tierId = (matchedTier != null) ? matchedTier.getId() : "Tier 3 (Rotation/Backup)";
		long maxCap = (matchedTier != null) ? matchedTier.getMaxSalaryMonth() : 350_000L;

		// Verhandlungsstufen (70 % Einstieg, 85 % Ziel, 100 % Walk-Away)
		long startingOffer = Math.round(maxCap * 0.70);
		long targetSalary = Math.round(maxCap * 0.85);
		long walkAway = maxCap;

		String assessment;
		String color;

		if (demandedSalaryMonth <= 0) {
			assessment = "Bereit für Verhandlung";
			color = "#2563eb";
		} else if (demandedSalaryMonth <= startingOffer) {
			assessment = "🟢 TOP-DEAL: Forderung liegt unter dem Einstiegsangebot";
			color = "#16a34a";
		} else if (demandedSalaryMonth <= targetSalary) {
			assessment = "✅ FAIR: Forderung im optimalen Budgetkorridor";
			color = "#15803d";
		} else if (demandedSalaryMonth <= walkAway) {
			assessment = "🟡 GRENZWERTIG: Forderung nahe an der Obergrenze";
			color = "#d97706";
		} else {
			long diff = demandedSalaryMonth - walkAway;
			assessment = "🔴 ABLEHNEN: Übersteigt Max-Cap um " + String.format("%,d €", diff);
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