package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.model.LoanPlayerModel;

public class LoanAnalysisService {

	public static void evaluate(LoanPlayerModel player) {
		boolean isOutgoing = player.getLoanType() == LoanPlayerModel.LoanType.OUTGOING;

		if (isOutgoing) {
			double calculatedScore = calculateLoanScore(player);
			player.moneyballScoreProperty().set(calculatedScore);
		}

		double score = player.getMoneyballScore();
		int games = player.getGamesPlayed();
		int minutes = player.getMinutesPlayed();
		double rating = player.getAverageRating();
		int age = player.getAge();
		boolean clauseOk = player.isClauseFulfilled();

		boolean hasSolidPlaytime = minutes >= 1200 || games >= 15;
		boolean hasMinimalPlaytime = minutes >= 450 || games >= 6;

		// 2. Regelbasierte Handlungsempfehlung & Zukunftsplan
		if (isOutgoing) {
			// EIGENER SPIELER (VERLIEHEN)
			if (minutes == 0 && games == 0) {
				player.recommendationProperty().set("⚠️ KEINE SPIELPRAXIS (0 Min)");
				player.futurePlanProperty().set("Keine Einsätze auf der Leihstation. Leih-Flop analysieren; neuen Verein suchen oder verkaufen.");
			} else if (!hasMinimalPlaytime || !clauseOk) {
				player.recommendationProperty().set("🚨 LEIHE ABBRECHEN & VERKAUFEN");
				player.futurePlanProperty().set("Entwicklung stagniert mangels Spielpraxis (< 450 Min). Leihvertrag kündigen oder Marktwert realisieren.");
			} else if ((score >= 75.0 || (rating >= 7.05 && hasSolidPlaytime))) {
				if (age <= 21) {
					player.recommendationProperty().set("📦 KADER-RÜCKKEHR (Als Backup/Rotation einplanen)");
					player.futurePlanProperty().set("Reif für 1. Mannschaft; starke Leih-Leistung spart teuren externen Transfer.");
				} else {
					player.recommendationProperty().set("⭐ KADER-RÜCKKEHR (Stammplatz-Duell)");
					player.futurePlanProperty().set("Leistungsträger auf Leihstation; fester Konkurrenzkampf in der Sommervorbereitung.");
				}
			} else if (age <= 22 && (score >= 55.0 || rating >= 6.75)) {
				player.recommendationProperty().set("🔄 WEITER VERLEIHEN (Noch 1 Jahr Spielpraxis)");
				player.futurePlanProperty().set("Guter Entwicklungstrend; benötigt eine weitere volle Saison als Stammkraft.");
			} else {
				player.recommendationProperty().set("💰 NACH LEIHE VERKAUFEN");
				player.futurePlanProperty().set("Niveau reicht perspektivisch nicht für den Kader; Marktwert nach Leihende mitnehmen.");
			}
		} else {
			// FREMDER SPIELER (GELIEHEN) -> Reiner Moneyball-Score & Spielzeit
			if (hasSolidPlaytime && score >= 75.0) {
				player.recommendationProperty().set("✅ KAUFOPTION ZIEHEN / VERPFLICHTEN");
				player.futurePlanProperty().set("Fester Leistungsträger (Score: " + score + "); Gehaltsgefüge prüfen und fest binden.");
			} else if (hasMinimalPlaytime && score >= 65.0) {
				player.recommendationProperty().set("🟡 LEIHE VERLÄNGERN (oder Schnäppchen-Kauf)");
				player.futurePlanProperty().set("Solide Ergänzung (Score: " + score + "); Festkauf nur unter Marktwert sinnvoll.");
			} else {
				player.recommendationProperty().set("❌ LEIH-ENDE (Nicht verpflichten)");
				player.futurePlanProperty().set("Score (" + score + ") genügt den Ansprüchen nicht; Kaderplatz freigeben.");
			}
		}
	}

	private static double calculateLoanScore(LoanPlayerModel player) {
		int minutes = player.getMinutesPlayed();
		int games = player.getGamesPlayed();
		double rating = player.getAverageRating();

		// 1. Wenn keine Spielzeit oder keine Note vorhanden -> strikt 0.0
		if ((minutes == 0 && games == 0) || rating <= 0.0) {
			return 0.0;
		}

		// 2. Basis-Punkte über FM-Note (6.00 = 0 Pkt, 6.70 = 49 Pkt, 7.00 = 70 Pkt, 7.30 = 91 Pkt)
		double baseScore = Math.max(0.0, (rating - 6.0) * 70.0);

		// 3. Scorer-Bonus (nur für Offensive / Mittelfeld)
		int goals = player.getGoals();
		int assists = player.getAssists();
		double scorerBonus = Math.min(15.0, (goals + assists) * 1.5);

		// 4. Liga-Faktor (Reputation des Wettbewerbs)
		String league = player.clubAndLeagueProperty().get() != null
				? player.clubAndLeagueProperty().get().toLowerCase() : "";

		double leagueFactor = 1.0;
		if (league.contains("first division") || league.contains("serie a") || league.contains("bundesliga")
				|| league.contains("premier league") || league.contains("la liga") || league.contains("ligue 1")) {
			// Vorsicht: Nicht mit Federation First Division verwechseln
			if (league.contains("federation first")) {
				leagueFactor = 0.85; // 3. Liga Spanien
			} else {
				leagueFactor = 1.10; // Top-Ligen
			}
		} else if (league.contains("second division") || league.contains("2. bundesliga")
				|| league.contains("championship") || league.contains("premiership") || league.contains("portugal")) {
			leagueFactor = 1.00; // Starke 2. Ligen / gute europäische Ligen
		} else if (league.contains("federation first") || league.contains("3. liga")) {
			leagueFactor = 0.85; // 3. Liga
		} else if (league.contains("federation second") || league.contains("federation third") || league.contains("regionalliga")) {
			leagueFactor = 0.70; // 4. & 5. Liga
		} else if (league.contains("u21") || league.contains("u19") || league.contains("reserve")) {
			leagueFactor = 0.60; // Nachwuchsligen
		}

		// 5. Spielzeit-Dämpfung
		double playtimeFactor;
		if (minutes >= 1800 || games >= 22) {
			playtimeFactor = 1.0;  // Fester Stammspieler / volle Aussagekraft
		} else if (minutes >= 1200 || games >= 15) {
			playtimeFactor = 0.90; // Regelmäßiger Starter
		} else if (minutes >= 600 || games >= 8) {
			playtimeFactor = 0.75; // Rotationsspieler
		} else if (minutes >= 200 || games >= 3) {
			playtimeFactor = 0.50; // Ergänzungsspieler
		} else {
			playtimeFactor = 0.25; // Kaum gespielt
		}

		double total = (baseScore + scorerBonus) * leagueFactor * playtimeFactor;
		total = Math.max(0.0, Math.min(100.0, total));

		return Math.round(total * 10.0) / 10.0;
	}
}