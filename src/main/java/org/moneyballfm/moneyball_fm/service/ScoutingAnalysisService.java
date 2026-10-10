package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.model.ScoutingPlayerModel;

public class ScoutingAnalysisService {

	public static void evaluate(ScoutingPlayerModel p, AppConfig config) {
		if (p == null) return;

		String role = p.targetRoleProperty().get() != null ? p.targetRoleProperty().get().trim() : "";
		double j = p.statValue1Property().get();
		double k = p.statValue2Property().get();
		double l = p.statValue3Property().get();

		// 1. ROLLEN-PERFORMANCE (Max. 65 Punkte) - Basiert auf deinen Kern-Benchmarks
		double statPoints;
		if (role.startsWith("Sto")) {
			statPoints = Math.min(j / 0.65, 1.25) * 22.75
					+ Math.min(k / 0.20, 1.35) * 22.75
					+ Math.min(l / 0.50, 1.20) * 19.50;
		} else if (role.startsWith("MTW")) {
			statPoints = Math.min(j / 0.80, 1.20) * 22.75
					+ Math.min(Math.max(k + 0.5, 0.0) / 0.80, 1.30) * 29.25
					+ Math.min(l / 0.85, 1.20) * 13.00;
		} else if (role.startsWith("OM") || role.startsWith("QM")) {
			statPoints = Math.min(j / 0.20, 1.30) * 26.00
					+ Math.min(k / 25.0, 1.30) * 22.75
					+ Math.min(l / 0.85, 1.20) * 16.25;
		} else if (role.startsWith("Reg")) {
			statPoints = Math.min(j / 0.88, 1.20) * 19.50
					+ Math.min(k / 5.0, 1.35) * 29.25
					+ Math.min(l / 25.0, 1.20) * 16.25;
		} else if (role.startsWith("ZSM")) {
			statPoints = Math.min(j / 0.86, 1.20) * 22.75
					+ Math.min(k / 2.50, 1.30) * 22.75
					+ Math.min(l / 0.75, 1.20) * 19.50;
		} else if (role.startsWith("FV") || role.startsWith("KFV")) {
			statPoints = Math.min(j / 2.50, 1.30) * 22.75
					+ Math.min(k / 0.22, 1.30) * 22.75
					+ Math.min(l / 1.80, 1.20) * 19.50;
		} else if (role.startsWith("BSV")) {
			statPoints = Math.min(j / 0.86, 1.20) * 22.75
					+ Math.min(k / 0.75, 1.20) * 22.75
					+ Math.min(l / 4.50, 1.30) * 19.50;
		} else if (role.startsWith("IV")) {
			statPoints = Math.min(j / 0.75, 1.20) * 26.00
					+ Math.min(k / 0.78, 1.20) * 29.25
					+ (l <= 1.0 ? 9.75 : 0.0);
		} else if (role.startsWith("HRV")) {
			statPoints = Math.min(j / 0.82, 1.20) * 22.75
					+ Math.min(k / 0.74, 1.20) * 22.75
					+ Math.min(l / 1.30, 1.20) * 19.50;
		} else {
			statPoints = 48.75;
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
			agePoints = 15.0; // Top-Entwicklungspotenzial & Wiederverkaufswert
		} else if (age <= 24) {
			agePoints = 13.0;
		} else if (age <= 27) {
			agePoints = 10.0; // Bestes Profi-Alter
		} else if (age <= 29) {
			agePoints = 7.0;
		} else {
			agePoints = 3.0;  // Kein Wiederverkaufswert, höheres Verletzungsrisiko
		}

		// Gesamter Scouting-Score
		double finalScore = Math.min(100.0, Math.max(0.0, Math.round(statPoints + athleticPoints + agePoints)));
		p.scoutingScoreProperty().set(finalScore);

		// 4. MAX-ABLÖSE (€) - Dynamisch nach Score und Vertrag
		long maxFeeBase;
		if (finalScore >= 95.0) maxFeeBase = 35_000_000L;
		else if (finalScore >= 90.0) maxFeeBase = 30_000_000L;
		else if (finalScore >= 80.0) maxFeeBase = 22_000_000L;
		else if (finalScore >= 70.0) maxFeeBase = 12_000_000L;
		else maxFeeBase = 4_000_000L;

		// Moneyball-Abschlag bei kurzer Restvertragslaufzeit
		double contractYears = p.contractYearsProperty().get();
		if (contractYears > 0.0 && contractYears <= 1.0) {
			maxFeeBase = (long) (maxFeeBase * 0.70); // Max 70 % bei auslaufendem Vertrag
		} else if (contractYears <= 2.0) {
			maxFeeBase = (long) (maxFeeBase * 0.85);
		}
		p.maxTransferFeeProperty().set(maxFeeBase);

		// 5. MAX-GEHALT / MONAT (€)
		long maxWage;
		if (finalScore >= 90.0) maxWage = 850_000L;
		else if (finalScore >= 80.0) maxWage = 550_000L;
		else if (finalScore >= 70.0) maxWage = 350_000L;
		else maxWage = 180_000L;

		p.maxWageProperty().set(maxWage);

		// 6. PREIS-CHECK & HANDLUNGSEMPFEHLUNG
		long asking = p.askingPriceProperty().get();
		long wage = p.wageDemandProperty().get();

		if (asking <= 0) {
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