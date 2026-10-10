package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.model.LoanPlayerModel;

public class LoanAnalysisService {

	public static void evaluate(LoanPlayerModel player) {
		boolean isOutgoing = player.getLoanType() == LoanPlayerModel.LoanType.OUTGOING;

		// 1. Score-Ermittlung:
		// Für eigene, verliehene Spieler (extern) berechnen wir den Leih-Score aus Note & Minuten.
		// Für eingehend geliehene Kaderspieler bleibt der echte Moneyball-Score aus Tab 1 erhalten.
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

		boolean hasSolidPlaytime = minutes >= 900 || games >= 12;

		// 2. Regelbasierte Handlungsempfehlung & Zukunftsplan
		if (isOutgoing) {
			// EIGENER SPIELER (VERLIEHEN)
			if (!clauseOk || (!hasSolidPlaytime && (games > 0 || minutes > 0))) {
				player.recommendationProperty().set("🚨 LEIHE ABBRECHEN & VERKAUFEN");
				player.futurePlanProperty().set("Entwicklung stagniert mangels Spielpraxis. Sofort abbrechen oder Marktwert realisieren.");
			} else if ((score >= 75.0 || rating >= 7.05) && hasSolidPlaytime) {
				if (age <= 21) {
					player.recommendationProperty().set("📦 KADER-RÜCKKEHR (Als Backup/Rotation einplanen)");
					player.futurePlanProperty().set("Reif für 1. Mannschaft; spart teuren externen Transfer.");
				} else {
					player.recommendationProperty().set("⭐ KADER-RÜCKKEHR (Stammplatz-Duell)");
					player.futurePlanProperty().set("Leistungsträger auf Leihstation; fester Konkurrenzkampf in der Sommervorbereitung.");
				}
			} else if (age <= 22 && (score >= 60.0 || rating >= 6.75)) {
				player.recommendationProperty().set("🔄 WEITER VERLEIHEN (Noch 1 Jahr Spielpraxis)");
				player.futurePlanProperty().set("Guter Entwicklungstrend; benötigt eine weitere volle Saison als Stammkraft.");
			} else {
				player.recommendationProperty().set("💰 NACH LEIHE VERKAUFEN");
				player.futurePlanProperty().set("Niveau reicht perspektivisch nicht; Marktwert nach Leihende mitnehmen.");
			}
		} else {
			// FREMDER SPIELER (GELIEHEN) -> Reiner Moneyball-Score & Spielzeit
			if (hasSolidPlaytime && score >= 75.0) {
				player.recommendationProperty().set("✅ KAUFOPTION ZIEHEN / VERPFLICHTEN");
				player.futurePlanProperty().set("Fester Leistungsträger (Score: " + score + "); Gehaltsgefüge prüfen und fest binden.");
			} else if (hasSolidPlaytime && score >= 65.0) {
				player.recommendationProperty().set("🟡 LEIHE VERLÄNGERN (oder Schnäppchen-Kauf)");
				player.futurePlanProperty().set("Solide Ergänzung (Score: " + score + "); Festkauf nur unter Marktwert sinnvoll.");
			} else {
				player.recommendationProperty().set("❌ LEIH-ENDE (Nicht verpflichten)");
				player.futurePlanProperty().set("Score (" + score + ") genügt den Ansprüchen nicht; Kaderplatz freigeben.");
			}
		}
	}

	private static double calculateLoanScore(LoanPlayerModel player) {
		double rating = player.getAverageRating();
		int minutes = player.getMinutesPlayed();
		int games = player.getGamesPlayed();
		boolean clauseOk = player.isClauseFulfilled();
		String clubLeague = player.clubAndLeagueProperty().get() != null
				? player.clubAndLeagueProperty().get().toLowerCase() : "";

		// Basis-Punkte über FM-Note:
		// 6.00 = 0 Pkt, 6.70 = 42 Pkt, 7.00 = 60 Pkt, 7.30 = 78 Pkt, 7.50 = 90 Pkt
		double baseScore = Math.max(0.0, (rating - 6.0) * 60.0);

		// Spielzeit-Bonus / Malus (harte Einsatzminuten wiegen am schwersten)
		double playtimeModifier = 0.0;
		if (minutes >= 1800 || games >= 22) {
			playtimeModifier = 15.0; // Absoluter Dauerbrenner
		} else if (minutes >= 1200 || games >= 15) {
			playtimeModifier = 10.0; // Fester Stammspieler
		} else if (minutes >= 600 || games >= 8) {
			playtimeModifier = 2.0;  // Rotationsspieler
		} else if (games > 0 || minutes > 0) {
			playtimeModifier = -12.0; // Kaum Einsatzzeit
		}

		// Klausel-Malus
		double clauseModifier = clauseOk ? 2.0 : -15.0;

		// Liga-Niveau-Faktor (Erkennung von 2./3. Liga oder U21)
		double leagueModifier = 0.0;
		if (clubLeague.contains("2.") || clubLeague.contains("liga 2") || clubLeague.contains("segunda") || clubLeague.contains("championship")) {
			leagueModifier = -3.0; // Leichte Anpassung für Zweitklassigkeit
		} else if (clubLeague.contains("3.") || clubLeague.contains("liga 3") || clubLeague.contains("u21") || clubLeague.contains("reserve")) {
			leagueModifier = -8.0; // Deutlicher Abschlag für 3. Liga oder Jugendliga
		} else if (!clubLeague.isBlank()) {
			leagueModifier = 3.0;  // Erstliga-Bonus
		}

		double total = baseScore + playtimeModifier + clauseModifier + leagueModifier;
		total = Math.max(0.0, Math.min(100.0, total));

		return Math.round(total * 10.0) / 10.0;
	}
}