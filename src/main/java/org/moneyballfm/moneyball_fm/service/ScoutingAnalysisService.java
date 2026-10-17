package org.moneyballfm.moneyball_fm.service;

import javafx.collections.ObservableList;
import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.model.ScoutingPlayerModel;
import org.moneyballfm.moneyball_fm.model.TacticRole;
import org.moneyballfm.moneyball_fm.parser.FmExportParser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoutingAnalysisService {

	public record ScoutKpis(long signCount, long bargainCount, long rejectCount) {}
	public record ScoutImportResult(int addedCount, int updatedCount, String message) {}

	public static ScoutKpis calculateScoutKpis(List<ScoutingPlayerModel> scouts) {
		if (scouts == null || scouts.isEmpty()) {
			return new ScoutKpis(0, 0, 0);
		}

		long signCount = scouts.stream()
				.filter(s -> {
					String action = s.transferActionProperty().get();
					return action != null && (action.contains("SOFORT VERPFLICHTEN")
							|| action.contains("UNTERKLASSIGES SCHNÄPPCHEN")
							|| action.contains("TALENT-DEAL"));
				})
				.count();

		long bargainCount = scouts.stream()
				.filter(s -> s.priceCheckProperty().get() != null && s.priceCheckProperty().get().contains("TOP-SCHNÄPPCHEN"))
				.count();

		long rejectCount = scouts.stream()
				.filter(s -> s.transferActionProperty().get() != null && (s.transferActionProperty().get().contains("ABLEHNEN") || s.transferActionProperty().get().contains("ZU HOCH")))
				.count();

		return new ScoutKpis(signCount, bargainCount, rejectCount);
	}

	public static ScoutImportResult importScoutingFile(
			File file,
			ObservableList<ScoutingPlayerModel> scoutDataList,
			ObservableList<String> availableRoles,
			AppConfig appConfig) throws Exception {

		List<ScoutingPlayerModel> incomingScouts = FmExportParser.parseScoutingFile(file);
		String fallbackRole = !availableRoles.isEmpty() ? availableRoles.get(0) : "Stoßstürmer (Mitte)";

		Map<String, ScoutingPlayerModel> existingMap = new HashMap<>();
		for (ScoutingPlayerModel existing : scoutDataList) {
			String name = existing.nameProperty().get();
			if (name != null) existingMap.put(SquadImportService.normalizePlayerKey(name), existing);
		}

		int updated = 0, added = 0;
		for (ScoutingPlayerModel fresh : incomingScouts) {
			String name = fresh.nameProperty().get();
			if (name == null || name.isBlank()) continue;
			String key = SquadImportService.normalizePlayerKey(name);

			// Datei-Importe sind niemals manuelle Einträge
			fresh.setManualEntry(false);

			String roleToApply = fresh.targetRoleProperty().get();
			if (existingMap.containsKey(key)) {
				String existingRole = existingMap.get(key).targetRoleProperty().get();
				if (existingRole != null && !existingRole.isBlank()) roleToApply = existingRole;
			}

			final String targetSearch = (roleToApply != null) ? roleToApply.toLowerCase() : "";
			String matchedRole = availableRoles.stream()
					.filter(r -> r.equalsIgnoreCase(targetSearch) || r.toLowerCase().contains(targetSearch) || targetSearch.contains(r.toLowerCase()))
					.findFirst().orElse(fallbackRole);

			fresh.targetRoleProperty().set(matchedRole);
			TacticRole tr = TacticRole.fromDisplayName(matchedRole);
			if (tr == null) tr = TacticRole.fromPosition(matchedRole);
			if (tr == null && TacticRole.values().length > 0) tr = TacticRole.values()[0];

			applyScoutingStatsForRole(fresh, tr, matchedRole);
			evaluate(fresh, appConfig);

			if (existingMap.containsKey(key)) {
				copyScoutData(fresh, existingMap.get(key));
				existingMap.get(key).ageProperty().set(fresh.ageProperty().get());
				updated++;
			} else {
				scoutDataList.add(fresh);
				existingMap.put(key, fresh);
				added++;
			}
		}

		String msg = "Scouting gemerged: " + added + " neu hinzugefügt, " + updated + " aktualisiert (aus " + file.getName() + ").";
		return new ScoutImportResult(added, updated, msg);
	}

	public static void copyScoutData(ScoutingPlayerModel source, ScoutingPlayerModel target) {
		target.ageProperty().set(source.ageProperty().get());
		target.clubProperty().set(source.clubProperty().get());
		target.leagueProperty().set(source.leagueProperty().get());
		target.leagueCoefficientProperty().set(source.leagueCoefficientProperty().get());
		target.positionsProperty().set(source.positionsProperty().get());
		target.askingPriceProperty().set(source.askingPriceProperty().get());
		target.wageDemandProperty().set(source.wageDemandProperty().get());
		target.contractYearsProperty().set(source.contractYearsProperty().get());
		target.minutesProperty().set(source.minutesProperty().get());
		target.agilityProperty().set(source.agilityProperty().get());
		target.paceProperty().set(source.paceProperty().get());
		target.staminaProperty().set(source.staminaProperty().get());
		target.determinationProperty().set(source.determinationProperty().get());
		target.injurySusceptibilityProperty().set(source.injurySusceptibilityProperty().get());
		target.availabilityFactorProperty().set(source.availabilityFactorProperty().get());
		target.setManualEntry(source.isManualEntry());

		target.progressivePasses90Property().set(source.progressivePasses90Property().get());
		target.xg90Property().set(source.xg90Property().get());
		target.xa90Property().set(source.xa90Property().get());
		target.keyPasses90Property().set(source.keyPasses90Property().get());
		target.shotsOnTargetPctProperty().set(source.shotsOnTargetPctProperty().get());
		target.shotConversionPctProperty().set(source.shotConversionPctProperty().get());
		target.passCompletionPctProperty().set(source.passCompletionPctProperty().get());
		target.xgPrevented90Property().set(source.xgPrevented90Property().get());
		target.savePctProperty().set(source.savePctProperty().get());
		target.interceptions90Property().set(source.interceptions90Property().get());
		target.crossAccuracyPctProperty().set(source.crossAccuracyPctProperty().get());
		target.goalsProperty().set(source.goalsProperty().get());
		target.assistsProperty().set(source.assistsProperty().get());
		target.averageRatingProperty().set(source.averageRatingProperty().get());
		target.headersWonPctProperty().set(source.headersWonPctProperty().get());
		target.mistakesLeadingToGoalProperty().set(source.mistakesLeadingToGoalProperty().get());
		target.dribbles90Property().set(source.dribbles90Property().get());
		target.crossesAttempted90Property().set(source.crossesAttempted90Property().get());
		target.tacklesWonPctProperty().set(source.tacklesWonPctProperty().get());
		target.interceptionsTotalProperty().set(source.interceptionsTotalProperty().get());
		target.progressivePassesTotalProperty().set(source.progressivePassesTotalProperty().get());
		target.keyPassesTotalProperty().set(source.keyPassesTotalProperty().get());

		target.statValue1Property().set(source.statValue1Property().get());
		target.statValue2Property().set(source.statValue2Property().get());
		target.statValue3Property().set(source.statValue3Property().get());
		target.scoutingScoreProperty().set(source.scoutingScoreProperty().get());
		target.maxTransferFeeProperty().set(source.maxTransferFeeProperty().get());
		target.maxWageProperty().set(source.maxWageProperty().get());
		target.priceCheckProperty().set(source.priceCheckProperty().get());
		target.transferActionProperty().set(source.transferActionProperty().get());
	}

	public static void applyScoutingStatsForRole(ScoutingPlayerModel p, TacticRole role, String roleNameFallback) {
		String roleText = "";
		if (role != null) roleText += (role.getDisplayName() != null ? role.getDisplayName() : "") + " " + role.name() + " ";
		if (roleNameFallback != null) roleText += roleNameFallback;
		roleText = roleText.toUpperCase().trim();

		double s1 = 0.0, s2 = 0.0, s3 = 0.0;

		// 1. Torwart: Par %, xG verh/90, Pas %
		if (roleText.contains("MTW") || roleText.contains("TORWART") || roleText.contains("TW")) {
			s1 = normalizePercent(p.savePctProperty().get());
			s2 = p.xgPrevented90Property().get();
			s3 = normalizePercent(p.passCompletionPctProperty().get());
		}
		// 2. Stoßstürmer: xG/90, Verw. %, Schüsse %
		else if (roleText.contains("STO") || roleText.contains("STÜRMER") || roleText.contains("ST (Z)")) {
			s1 = p.xg90Property().get();
			s2 = normalizePercent(p.shotConversionPctProperty().get());
			s3 = normalizePercent(p.shotsOnTargetPctProperty().get());
		}
		// 3. Zehner / OM: xA/90, E Pä/90, Pas %
		else if (roleText.contains("OM") || roleText.contains("ZEHNER") || roleText.contains("ENGANCHE") || roleText.contains("TREQUARTISTA")) {
			s1 = p.xa90Property().get();
			s2 = p.keyPasses90Property().get();
			s3 = normalizePercent(p.passCompletionPctProperty().get());
		}
		// 4. Regista: Pas %, Pr Pässe/90, E Pä/90
		else if (roleText.contains("REGISTA") || roleText.contains("REG")) {
			s1 = normalizePercent(p.passCompletionPctProperty().get());
			s2 = p.progressivePasses90Property().get();
			s3 = p.keyPasses90Property().get();
		}
		// 5. ZSM / Sechser: Pas %, AbB/90, Zwk %
		else if (roleText.contains("ZSM") || roleText.contains("ZENTRAL") || roleText.contains("DM") || roleText.contains("BALLEROBERNDER")) {
			s1 = normalizePercent(p.passCompletionPctProperty().get());
			s2 = p.interceptions90Property().get();
			s3 = normalizePercent(p.tacklesWonPctProperty().get());
		}
		// 6. Flügelverteidiger (FV / KFV): AbB/90, FPQ %, Drb/90
		else if (roleText.contains("FV") || roleText.contains("KFV") || roleText.contains("AUSSENVERTEIDIGER") || roleText.contains("FLÜGELVERTEIDIGER")) {
			s1 = p.interceptions90Property().get();
			s2 = normalizePercent(p.crossAccuracyPctProperty().get());
			s3 = p.dribbles90Property().get();
		}
		// 7. Ballspielender Verteidiger (BSV): Pas %, Zwk %, Pr Pässe/90
		else if (roleText.contains("BSV") || roleText.contains("BALLSPIELEND")) {
			s1 = normalizePercent(p.passCompletionPctProperty().get());
			s2 = normalizePercent(p.tacklesWonPctProperty().get());
			s3 = p.progressivePasses90Property().get();
		}
		// 8. Innenverteidiger (IV): Kopf %, Zwk %, AbB/90
		else if (roleText.contains("IV") || roleText.contains("INNENVERTEIDIGER") || roleText.contains("VERTEIDIGER")) {
			s1 = normalizePercent(p.headersWonPctProperty().get());
			s2 = normalizePercent(p.tacklesWonPctProperty().get());
			s3 = p.interceptions90Property().get();
		}
		// 9. Halbraumverteidiger (HRV): Pas %, Zwk %, Drb/90
		else if (roleText.contains("HRV") || roleText.contains("HALBRAUM")) {
			s1 = normalizePercent(p.passCompletionPctProperty().get());
			s2 = normalizePercent(p.tacklesWonPctProperty().get());
			s3 = p.dribbles90Property().get();
		}
		// 10. Flügelspieler
		else if (roleText.contains("FLÜGEL") || roleText.contains("WINGER")) {
			s1 = normalizePercent(p.crossAccuracyPctProperty().get());
			s2 = p.dribbles90Property().get();
			s3 = p.xa90Property().get();
		}
		// Fallback
		else {
			s1 = normalizePercent(p.passCompletionPctProperty().get());
			s2 = p.interceptions90Property().get();
			s3 = p.progressivePasses90Property().get();
		}

		p.statValue1Property().set(s1);
		p.statValue2Property().set(s2);
		p.statValue3Property().set(s3);
	}

	public static void evaluate(ScoutingPlayerModel p, AppConfig config) {
		if (p == null) return;

		double s1 = p.statValue1Property().get();
		double s2 = p.statValue2Property().get();
		double s3 = p.statValue3Property().get();
		int minutes = p.minutesProperty().get();

		boolean allStatsZero = (s1 == 0.0 && s2 == 0.0 && s3 == 0.0);
		boolean sampleTooSmall = (minutes > 0 && minutes < 450);

		if (allStatsZero || sampleTooSmall) {
			p.scoutingScoreProperty().set(0.0);
			p.setAvailabilityFactor(1.0);
			p.maxTransferFeeProperty().set(0L);
			p.maxWageProperty().set(0L);
			p.priceCheckProperty().set("⚪ KEINE DATEN");
			p.transferActionProperty().set("🔍 WEITER BEOBACHTEN");
			return;
		}

		double af = MoneyballCalculator.calculateAvailabilityFactor(minutes, p.getInjurySusceptibility());
		p.setAvailabilityFactor(af);

		// Liga-Koeffizient ermitteln & im Model abspeichern
		double leagueCoeff = 1.0;
		if (config != null) {
			leagueCoeff = config.getLeagueCoefficient(p.getLeague());
		}
		p.setLeagueCoefficient(leagueCoeff);

		String roleRaw = p.targetRoleProperty().get() != null ? p.targetRoleProperty().get().trim() : "";
		String role = roleRaw.toUpperCase();

		double statPoints;

		// 1. Stoßstürmer (xG: 0.79, Verw: 0.23, Schüsse: 0.53)
		if (role.contains("STO") || role.contains("STÜRMER") || role.contains("ST (Z)")) {
			statPoints = Math.min(s1 / 0.79, 1.30) * 26.0
					+ Math.min(s2 / 0.23, 1.30) * 19.5
					+ Math.min(s3 / 0.53, 1.30) * 19.5;
		}
		// 2. Torwart (Par: 0.88, xG verh: 0.44, Pas: 0.97)
		else if (role.contains("MTW") || role.contains("TORWART") || role.contains("TW")) {
			statPoints = Math.min(s1 / 0.88, 1.25) * 22.75
					+ Math.min(Math.max(s2 + 0.35, 0.0) / 0.44, 1.30) * 22.75
					+ Math.min(s3 / 0.97, 1.20) * 19.50;
		}
		// 3. Zehner / OM (xA: 0.19, E Pä/90: 2.20, Pas: 0.87)
		else if (role.contains("OM") || role.contains("ZEHNER") || role.contains("ENGANCHE") || role.contains("TREQUARTISTA")) {
			statPoints = Math.min(s1 / 0.19, 1.35) * 22.75
					+ Math.min(s2 / 2.20, 1.35) * 22.75
					+ Math.min(s3 / 0.87, 1.20) * 19.50;
		}
		// 4. Regista (Pas: 0.88, Pr Pässe: 5.90, E Pä/90: 1.20)
		else if (role.contains("REG") || role.contains("ZURÜCKGEZOGENER") || role.contains("SPIELMACHER")) {
			statPoints = Math.min(s1 / 0.88, 1.20) * 22.75
					+ Math.min(s2 / 5.90, 1.35) * 22.75
					+ Math.min(s3 / 1.20, 1.35) * 19.50;
		}
		// 5. ZSM / Sechser (Pas: 0.89, AbB/90: 2.50, Zwk: 0.79)
		else if (role.contains("ZSM") || role.contains("ZENTRAL") || role.contains("DM") || role.contains("BALLEROBERNDER")) {
			statPoints = Math.min(s1 / 0.89, 1.20) * 22.75
					+ Math.min(s2 / 2.50, 1.30) * 22.75
					+ Math.min(s3 / 0.79, 1.25) * 19.50;
		}
		// 6. Flügelverteidiger (AbB/90: 2.80, FPQ: 0.26, Drb/90: 2.10)
		else if (role.contains("FV") || role.contains("KFV") || role.contains("AUSSENVERTEIDIGER") || role.contains("FLÜGELVERTEIDIGER")) {
			statPoints = Math.min(s1 / 2.80, 1.30) * 22.75
					+ Math.min(s2 / 0.26, 1.30) * 19.50
					+ Math.min(s3 / 2.10, 1.35) * 22.75;
		}
		// 7. Ballspielender Verteidiger (Pas: 0.88, Zwk: 0.78, Pr Pässe: 5.50)
		else if (role.contains("BSV") || role.contains("BALLSPIELEND")) {
			statPoints = Math.min(s1 / 0.88, 1.20) * 22.75
					+ Math.min(s2 / 0.78, 1.25) * 22.75
					+ Math.min(s3 / 5.50, 1.30) * 19.50;
		}
		// 8. Innenverteidiger (Kopf: 0.78, Zwk: 0.82, AbB/90: 2.00)
		else if (role.contains("IV") || role.contains("INNENVERTEIDIGER") || role.contains("VERTEIDIGER")) {
			statPoints = Math.min(s1 / 0.78, 1.25) * 26.00
					+ Math.min(s2 / 0.82, 1.25) * 26.00
					+ Math.min(s3 / 2.00, 1.30) * 13.00;
		}
		// 9. Halbraumverteidiger (Pas: 0.85, Zwk: 0.76, Drb/90: 1.40)
		else if (role.contains("HRV") || role.contains("HALBRAUM")) {
			statPoints = Math.min(s1 / 0.85, 1.20) * 22.75
					+ Math.min(s2 / 0.76, 1.25) * 22.75
					+ Math.min(s3 / 1.40, 1.30) * 19.50;
		}
		// 10. Flügelspieler
		else if (role.contains("FLÜGEL") || role.contains("WINGER")) {
			statPoints = Math.min(s1 / 0.25, 1.30) * 21.0
					+ Math.min(s2 / 2.20, 1.35) * 22.0
					+ Math.min(s3 / 0.22, 1.35) * 22.0;
		}
		// Fallback
		else {
			statPoints = Math.min(s1 / 0.85, 1.20) * 22.0
					+ Math.min(s2 / 2.00, 1.25) * 22.0
					+ Math.min(s3 / 3.50, 1.25) * 21.0;
		}

		// Leistungs-Punkte mit dem Liga-Schwierigkeitsfaktor skalieren
		statPoints *= leagueCoeff;

		int agility = p.agilityProperty().get();
		int pace = p.paceProperty().get();
		int stamina = p.staminaProperty().get();

		double athleticAvg = (agility + pace + stamina) / 3.0;
		double athleticPoints = Math.min(20.0, Math.max(4.0, ((athleticAvg - 6.0) / 11.0) * 20.0));

		int age = p.ageProperty().get();
		double agePoints;
		if (age <= 21) agePoints = 15.0;
		else if (age <= 24) agePoints = 13.0;
		else if (age <= 27) agePoints = 10.0;
		else if (age <= 29) agePoints = 7.0;
		else agePoints = 3.0;

		double finalScore = Math.min(100.0, Math.max(0.0, Math.round(statPoints + athleticPoints + agePoints)));
		p.scoutingScoreProperty().set(finalScore);

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

		long adjustedMaxFee = Math.round(maxFeeBase * af);
		p.maxTransferFeeProperty().set(adjustedMaxFee);

		long maxWageBase;
		if (finalScore >= 95.0) maxWageBase = 1_500_000L;
		else if (finalScore >= 90.0) maxWageBase = 1_100_000L;
		else if (finalScore >= 80.0) maxWageBase = 750_000L;
		else if (finalScore >= 70.0) maxWageBase = 450_000L;
		else maxWageBase = 200_000L;

		long adjustedMaxWage = Math.round(maxWageBase * af);
		p.maxWageProperty().set(adjustedMaxWage);

		long asking = p.askingPriceProperty().get();
		long wage = p.wageDemandProperty().get();

		// Schwellenwerte für unterklassige Ligen (2. und 3. Ligen)
		boolean isSecondTier = (leagueCoeff <= 0.85 && leagueCoeff > 0.72);
		boolean isThirdTier = (leagueCoeff <= 0.72);

		double minScoreForDeal;
		String signActionLabel;

		if (isThirdTier) {
			minScoreForDeal = 60.0;
			signActionLabel = "⭐ TALENT-DEAL (UNTERKLASSIG)";
		} else if (isSecondTier) {
			minScoreForDeal = 68.0;
			signActionLabel = "⭐ UNTERKLASSIGES SCHNÄPPCHEN";
		} else {
			minScoreForDeal = 80.0;
			signActionLabel = "⭐ SOFORT VERPFLICHTEN";
		}

		if (asking >= 900_000_000L) {
			p.priceCheckProperty().set("🔒 UNVERKÄUFLICH");
			p.transferActionProperty().set("❌ KEINE FREIGABE");
		} else if (asking <= 0) {
			p.priceCheckProperty().set("⚪ KEINE ANGABE");
			p.transferActionProperty().set("ℹ️ ABLÖSE ANFRAGEN");
		} else if (asking <= adjustedMaxFee * 0.50) {
			p.priceCheckProperty().set("🟢 TOP-SCHNÄPPCHEN");
			if (wage <= adjustedMaxWage && finalScore >= minScoreForDeal) {
				p.transferActionProperty().set(af < 0.80 ? "⚠️ RISIKO-SCHNÄPPCHEN (VERHANDELN)" : signActionLabel);
			} else {
				p.transferActionProperty().set("✅ VERHANDELN");
			}
		} else if (asking <= adjustedMaxFee) {
			p.priceCheckProperty().set("🟡 FAIRER PREIS");
			double minScoreFair = isThirdTier ? 55.0 : (isSecondTier ? 65.0 : 75.0);
			if (wage <= adjustedMaxWage && finalScore >= minScoreFair) {
				p.transferActionProperty().set(af < 0.80 ? "⚠️ BEOBACHTEN (VERLETZUNGSRISIKO)" : "✅ VERHANDELN / BEOBACHTEN");
			} else {
				p.transferActionProperty().set("⏳ WARTEN");
			}
		} else {
			p.priceCheckProperty().set("🔴 ÜBERTEUERT");
			p.transferActionProperty().set("❌ ABLEHNEN");
		}
	}

	private static double normalizePercent(double val) {
		return val > 1.0 ? val / 100.0 : val;
	}
}