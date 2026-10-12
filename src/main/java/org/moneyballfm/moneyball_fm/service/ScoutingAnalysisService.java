package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.model.ScoutingPlayerModel;

public class ScoutingAnalysisService {

	public static void evaluate(ScoutingPlayerModel p, AppConfig config) {
		if (p == null) return;

		String roleRaw = p.targetRoleProperty().get() != null ? p.targetRoleProperty().get().trim() : "";
		String role = roleRaw.toUpperCase();

		double j = p.statValue1Property().get();
		double k = p.statValue2Property().get();
		double l = p.statValue3Property().get();

		// 1. ROLLEN-PERFORMANCE (Max. 65 Punkte)
		double statPoints;

		// STÜRMER (xG/90: 0.52 | xA/90 bzw. Konversion: 0.19 | Schüsse aufs Tor: 48%)
		if (role.contains("STO") || role.contains("STÜRMER") || role.contains("ST (Z)")) {
			statPoints = Math.min(j / 0.52, 1.25) * 22.75
					+ Math.min(k / 0.19, 1.35) * 22.75
					+ Math.min(l / 0.48, 1.20) * 19.50;

			// MITFÜHRENDER / MITSPIELENDER TORWART (Gehaltene Bälle: 78% | xG verhindert/90: +0.25 | Passquote: 82%)
		} else if (role.contains("MTW") || role.contains("TORWART") || role.contains("TW")) {
			statPoints = Math.min(j / 0.78, 1.20) * 22.75
					+ Math.min(Math.max(k + 0.35, 0.0) / 0.60, 1.30) * 29.25
					+ Math.min(l / 0.82, 1.20) * 13.00;

			// OFFENSIVES MITTELFELD / FLÜGELSPIELER (xA/90: 0.20 | Flankenquote: 22% [0.22] | Passquote: 80%)
		} else if (role.contains("OM") || role.contains("QM") || role.contains("FLÜGEL") || role.contains("WINGER")) {
			double crossPct = k > 1.0 ? k / 100.0 : k;
			statPoints = Math.min(j / 0.20, 1.30) * 26.00
					+ Math.min(crossPct / 0.22, 1.30) * 22.75
					+ Math.min(l / 0.80, 1.20) * 16.25;

			// REGISTA / TIEFER SPIELMACHER (Passquote: 85% | Pr Pässe/90: 5.2 | Flankenquote bzw. Key Passes: 22%)
		} else if (role.contains("REG") || role.contains("ZURÜCKGEZOGENER") || role.contains("SPIELMACHER")) {
			double crossPct = l > 1.0 ? l / 100.0 : l;
			statPoints = Math.min(j / 0.85, 1.20) * 19.50
					+ Math.min(k / 5.20, 1.35) * 29.25
					+ Math.min(crossPct / 0.22, 1.20) * 16.25;

			// ZENTRALES MITTELFELD / BOX-TO-BOX / DEFENSIVES MITTELFELD (Passquote: 86% | Abfangen/90: 2.8 | Zweikampfquote: 74%)
		} else if (role.contains("ZSM") || role.contains("ZENTRAL") || role.contains("DM") || role.contains("BALLEROBERNDER")) {
			statPoints = Math.min(j / 0.86, 1.20) * 22.75
					+ Math.min(k / 2.80, 1.30) * 22.75
					+ Math.min(l / 0.74, 1.20) * 19.50;

			// FLÜGELVERTEIDIGER / AUSSENVERTEIDIGER (Abfangen/90: 2.5 | Flankenquote: 22% | Prog. Läufe / Dribblings: 2.0)
		} else if (role.contains("FV") || role.contains("KFV") || role.contains("AUSSENVERTEIDIGER") || role.contains("FLÜGELVERTEIDIGER")) {
			double crossPct = k > 1.0 ? k / 100.0 : k;
			statPoints = Math.min(j / 2.50, 1.30) * 22.75
					+ Math.min(crossPct / 0.22, 1.30) * 22.75
					+ Math.min(l / 2.00, 1.20) * 19.50;

			// BALLSPIELENDER VERTEIDIGER (Passquote: 85% | Zweikampfquote: 75% | Pr Pässe/90: 4.5)
		} else if (role.contains("BSV") || role.contains("BALLSPIELEND")) {
			statPoints = Math.min(j / 0.85, 1.20) * 22.75
					+ Math.min(k / 0.75, 1.20) * 22.75
					+ Math.min(l / 4.50, 1.30) * 19.50;

			// INNENVERTEIDIGER (Kopfballquote: 74% | Zweikampfquote: 76% | Fehler mit Torfolge)
		} else if (role.contains("IV") || role.contains("INNENVERTEIDIGER") || role.contains("VERTEIDIGER")) {
			double errorBonus = Math.max(0.0, 1.0 - (l * 0.5)) * 9.75;
			statPoints = Math.min(j / 0.74, 1.20) * 26.00
					+ Math.min(k / 0.76, 1.20) * 29.25
					+ errorBonus;

			// HALBRAUMVERTEIDIGER (Passquote: 83% | Zweikampfquote: 74% | Dribblings/Prog. Läufe: 1.40)
		} else if (role.contains("HRV") || role.contains("HALBRAUM")) {
			statPoints = Math.min(j / 0.83, 1.20) * 22.75
					+ Math.min(k / 0.74, 1.20) * 22.75
					+ Math.min(l / 1.40, 1.20) * 19.50;

			// DYNAMISCHER FALLBACK
		} else {
			statPoints = Math.min(j / 0.80, 1.20) * 22.0
					+ Math.min(k / 1.50, 1.20) * 22.0
					+ Math.min(l / 0.80, 1.20) * 21.0;
		}

		// 2. ATHLETIK-PROFIL (Max. 20 Punkte)
		int agility = p.agilityProperty().get();
		int pace = p.paceProperty().get();
		int stamina = p.staminaProperty().get();

		double athleticAvg = (agility + pace + stamina) / 3.0;
		double athleticPoints = Math.min(20.0, Math.max(4.0, ((athleticAvg - 6.0) / 11.0) * 20.0));

		// 3. ALTER & ENTWICKLUNGSFAKTOR (Max. 15 Punkte)
		int age = p.ageProperty().get();
		double agePoints;
		if (age <= 21) {
			agePoints = 15.0;
		} else if (age <= 24) {
			agePoints = 13.0;
		} else if (age <= 27) {
			agePoints = 10.0;
		} else if (age <= 29) {
			agePoints = 7.0;
		} else {
			agePoints = 3.0;
		}

		// Gesamter Scouting-Score (0 - 100)
		double finalScore = Math.min(100.0, Math.max(0.0, Math.round(statPoints + athleticPoints + agePoints)));
		p.scoutingScoreProperty().set(finalScore);

		// 4. MAX-ABLÖSE (€) - Angepasst an Top-Clubs
		long maxFeeBase;
		if (finalScore >= 95.0) maxFeeBase = 120_000_000L;
		else if (finalScore >= 90.0) maxFeeBase = 85_000_000L;
		else if (finalScore >= 80.0) maxFeeBase = 50_000_000L;
		else if (finalScore >= 70.0) maxFeeBase = 25_000_000L;
		else maxFeeBase = 10_000_000L;

		double contractYears = p.contractYearsProperty().get();
		if (contractYears > 0.0 && contractYears <= 1.0) {
			maxFeeBase = (long) (maxFeeBase * 0.70);
		} else if (contractYears <= 2.0) {
			maxFeeBase = (long) (maxFeeBase * 0.85);
		}
		p.maxTransferFeeProperty().set(maxFeeBase);

		// 5. MAX-GEHALT / MONAT (€)
		long maxWage;
		if (finalScore >= 95.0) maxWage = 1_500_000L;
		else if (finalScore >= 90.0) maxWage = 1_100_000L;
		else if (finalScore >= 80.0) maxWage = 750_000L;
		else if (finalScore >= 70.0) maxWage = 450_000L;
		else maxWage = 200_000L;

		p.maxWageProperty().set(maxWage);

		// 6. PREIS-CHECK & HANDLUNGSEMPFEHLUNG
		long asking = p.askingPriceProperty().get();
		long wage = p.wageDemandProperty().get();

		if (asking >= 900_000_000L) {
			p.priceCheckProperty().set("🔒 UNVERKÄUFLICH");
			p.transferActionProperty().set("❌ KEINE FREIGABE");
		} else if (asking <= 0) {
			p.priceCheckProperty().set("⚪ KEINE ANGABE");
			p.transferActionProperty().set("ℹ️ ABLÖSE ANFRAGEN");
		} else if (asking <= maxFeeBase * 0.50) {
			p.priceCheckProperty().set("🟢 TOP-SCHNÄPPCHEN");
			if (wage <= maxWage && finalScore >= 80.0) {
				p.transferActionProperty().set("⭐ SOFORT VERPFLICHTEN");
			} else {
				p.transferActionProperty().set("✅ VERHANDELN");
			}
		} else if (asking <= maxFeeBase) {
			p.priceCheckProperty().set("🟡 FAIRER PREIS");
			if (wage <= maxWage && finalScore >= 75.0) {
				p.transferActionProperty().set("✅ VERHANDELN / BEOBACHTEN");
			} else {
				p.transferActionProperty().set("⏳ WARTEN");
			}
		} else {
			p.priceCheckProperty().set("🔴 ÜBERTEUERT");
			p.transferActionProperty().set("❌ ABLEHNEN");
		}
	}
}