package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.model.YouthPlayerModel;

public class YouthAnalysisService {

	public static void evaluate(YouthPlayerModel talent) {
		int age = talent.getAge();
		int determination = talent.getDetermination();
		double potStars = talent.getPotentialStars();
		double curStars = talent.getCurrentStars();

		// 1. Physische DNA (Bew, Ant, Aus)
		double physicalAvg = (talent.getAgility() + talent.getPace() + talent.getStamina()) / 3.0;

		// 2. Automatischer Alters- und Mentalitätsfaktor (Moneyball-Basis)
		// Je jünger (z.B. 16-17), desto mehr Entwicklungsfenster
		double ageFactor = Math.max(0.0, (22 - age) * 4.0); // 16 Jahre = 24 Pkt, 19 Jahre = 12 Pkt
		double mentalPoints = Math.min(25.0, (determination / 20.0) * 25.0);
		double physicalPoints = Math.min(30.0, (physicalAvg / 15.0) * 30.0);

		// 3. Sterne-Einfluss (wenn per Dropdown gepflegt, fließt es gewichtet ein)
		double starBonus = (potStars > 0.0) ? ((potStars / 5.0) * 25.0) : 15.0;

		double totalScore = Math.min(100.0, Math.max(0.0, ageFactor + mentalPoints + physicalPoints + starBonus - 4.0));
		talent.talentScoreProperty().set(Math.round(totalScore * 10.0) / 10.0);

		// 4. Prognose 1. Mannschaft (angelehnt an deine Excel-Werte)
		if (totalScore >= 80.0 || potStars >= 4.5) {
			talent.forecastFirstTeamProperty().set("⭐ HOCHVERANLAGTES TALENT (Kader-Perspektive)");
			talent.maxYouthSalaryProperty().set(50000L);
		} else if (totalScore >= 68.0 || potStars >= 3.5) {
			talent.forecastFirstTeamProperty().set("⚽ SOLIDES ENTWICKLUNGS-TALENT (Liga-Niveau)");
			talent.maxYouthSalaryProperty().set(30000L);
		} else {
			talent.forecastFirstTeamProperty().set("⏳ PERSPEKTIV-SPIELER (Begrenztes Potenzial)");
			talent.maxYouthSalaryProperty().set(15000L);
		}

		// 5. Handlungsempfehlung Pipeline
		if ((curStars >= 2.5 || totalScore >= 85.0) && age >= 18) {
			talent.pipelineRecommendationProperty().set("⭐ TOP-TALENT (1. MANNSCHAFT HOCHZIEHEN)");
		} else if (age <= 18) {
			talent.pipelineRecommendationProperty().set("🌱 IN U19/U21 HALTEN (Reifen)");
		} else if (age >= 19 && (potStars >= 3.5 || totalScore >= 70.0)) {
			talent.pipelineRecommendationProperty().set("🚜 VERLEIHEN (Spielpraxis Liga 1/2)");
		} else {
			talent.pipelineRecommendationProperty().set("💰 VERKAUFSMÖGLICHKEIT (Marktwert mitnehmen)");
		}
	}
}