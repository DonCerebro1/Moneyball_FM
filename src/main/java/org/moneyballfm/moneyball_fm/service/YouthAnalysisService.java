package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.model.YouthPlayerModel;

import java.util.List;

public class YouthAnalysisService {

	public record YouthKpis(long topCount, long loanCount, long developCount, long sellCount) {}

	public static YouthKpis calculateYouthKpis(List<YouthPlayerModel> youthList) {
		if (youthList == null || youthList.isEmpty()) {
			return new YouthKpis(0, 0, 0, 0);
		}

		long topCount = youthList.stream()
				.filter(y -> y.pipelineRecommendationProperty().get() != null &&
						(y.pipelineRecommendationProperty().get().contains("1. MANNSCHAFT") || y.pipelineRecommendationProperty().get().contains("TOP-TALENT")))
				.count();

		long loanCount = youthList.stream()
				.filter(y -> y.pipelineRecommendationProperty().get() != null &&
						y.pipelineRecommendationProperty().get().contains("VERLEIHEN"))
				.count();

		long developCount = youthList.stream()
				.filter(y -> y.pipelineRecommendationProperty().get() != null &&
						(y.pipelineRecommendationProperty().get().contains("HALTEN") || y.pipelineRecommendationProperty().get().contains("ENTWICKELN")))
				.count();

		long sellCount = youthList.stream()
				.filter(y -> y.pipelineRecommendationProperty().get() != null &&
						y.pipelineRecommendationProperty().get().contains("VERKAUF"))
				.count();

		return new YouthKpis(topCount, loanCount, developCount, sellCount);
	}

	public static void evaluate(YouthPlayerModel talent) {
		if (talent == null) return;

		int age = talent.getAge();
		int determination = talent.getDetermination();
		double potStars = talent.getPotentialStars();
		double curStars = talent.getCurrentStars();

		// 1. Physische DNA (Bew, Ant, Aus) - max. 25 Punkte
		double physicalAvg = (talent.getAgility() + talent.getPace() + talent.getStamina()) / 3.0;
		double physicalPoints = Math.min(25.0, (physicalAvg / 15.0) * 25.0);

		// 2. Mentalität & Entschlossenheit - max. 15 Punkte
		double mentalPoints = Math.min(15.0, (determination / 20.0) * 15.0);

		// 3. FM-Potenzial-Sterne - max. 25 Punkte
		double starBonus = (potStars > 0.0) ? ((potStars / 5.0) * 25.0) : 10.0;

		// 4. Gemessene Match-Performance (Stats 1-3) - max. 25 Punkte
		double s1 = talent.statValue1Property().get();
		double s2 = talent.statValue2Property().get();
		double s3 = talent.statValue3Property().get();
		boolean hasMatchData = (s1 > 0.0 || s2 > 0.0 || s3 > 0.0);

		double performancePoints = 0.0;
		if (hasMatchData) {
			double normS1 = Math.min(10.0, s1 > 1.0 ? (s1 / 10.0) : (s1 * 10.0));
			double normS2 = Math.min(10.0, s2 > 1.0 ? (s2 / 10.0) : (s2 * 10.0));
			double normS3 = Math.min(5.0, s3 > 1.0 ? (s3 / 20.0) : (s3 * 5.0));
			performancePoints = Math.min(25.0, normS1 + normS2 + normS3);
		}

		// 5. Moderater Entwicklungsbonus nach Alter - max. 10 Punkte
		double ageBonus = Math.max(0.0, (21 - age) * 1.5);

		// Gesamtsumme (0 - 100)
		double totalScore = physicalPoints + mentalPoints + starBonus + ageBonus + performancePoints;

		if (!hasMatchData) {
			totalScore = Math.min(72.0, totalScore);
		}

		totalScore = Math.min(100.0, Math.max(0.0, totalScore));
		talent.talentScoreProperty().set(Math.round(totalScore * 10.0) / 10.0);

		// 6. Prognose 1. Mannschaft
		if (totalScore >= 80.0 && hasMatchData) {
			talent.forecastFirstTeamProperty().set("⭐ HOCHVERANLAGTES TALENT (Kader-Perspektive)");
			talent.maxYouthSalaryProperty().set(50000L);
		} else if (totalScore >= 65.0 || potStars >= 4.0) {
			if (!hasMatchData) {
				talent.forecastFirstTeamProperty().set("🌱 ROH-TALENT (Keine Leistungsdaten)");
				talent.maxYouthSalaryProperty().set(20000L);
			} else {
				talent.forecastFirstTeamProperty().set("⚽ SOLIDES ENTWICKLUNGS-TALENT (Liga-Niveau)");
				talent.maxYouthSalaryProperty().set(30000L);
			}
		} else {
			talent.forecastFirstTeamProperty().set("⏳ PERSPEKTIV-SPIELER (Begrenztes Potenzial)");
			talent.maxYouthSalaryProperty().set(10000L);
		}

		// 7. Handlungsempfehlung Pipeline
		if ((curStars >= 2.5 || totalScore >= 82.0) && age >= 18 && hasMatchData) {
			talent.pipelineRecommendationProperty().set("⭐ TOP-TALENT (1. MANNSCHAFT HOCHZIEHEN)");
		} else if (age <= 18 || !hasMatchData) {
			talent.pipelineRecommendationProperty().set("🌱 IN U19/U21 HALTEN (Reifen)");
		} else if (age >= 19 && (potStars >= 3.5 || totalScore >= 65.0)) {
			talent.pipelineRecommendationProperty().set("🚜 VERLEIHEN (Spielpraxis Liga 1/2)");
		} else {
			talent.pipelineRecommendationProperty().set("💰 VERKAUFSMÖGLICHKEIT (Marktwert mitnehmen)");
		}
	}
}