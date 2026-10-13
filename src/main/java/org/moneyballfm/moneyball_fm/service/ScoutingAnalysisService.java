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
				.filter(s -> s.transferActionProperty().get() != null && s.transferActionProperty().get().contains("SOFORT VERPFLICHTEN"))
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
		String fallbackRole = !availableRoles.isEmpty() ? availableRoles.get(0) : "Stoßstürmer (Angreifen)";

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

		if (roleText.contains("STO") || roleText.contains("STÜRMER") || roleText.contains("ANGREIFEN") || roleText.contains("ST (Z)")) {
			s1 = p.xg90Property().get();
			s2 = p.xa90Property().get();
			double shots = p.shotsOnTargetPctProperty().get();
			s3 = shots > 1.0 ? shots / 100.0 : shots;
		} else if (roleText.contains("MTW") || roleText.contains("TORWART") || roleText.contains("TW")) {
			double save = p.savePctProperty().get();
			s1 = save > 1.0 ? save / 100.0 : save;
			s2 = p.xgPrevented90Property().get();
			double pass = p.passCompletionPctProperty().get();
			s3 = pass > 1.0 ? pass / 100.0 : pass;
		} else if (roleText.contains("OM") || roleText.contains("QM") || roleText.contains("FLÜGEL") || roleText.contains("WINGER")) {
			s1 = p.xa90Property().get();
			double cross = p.crossAccuracyPctProperty().get();
			s2 = cross > 1.0 ? cross / 100.0 : cross;
			double pass = p.passCompletionPctProperty().get();
			s3 = pass > 1.0 ? pass / 100.0 : pass;
		} else if (roleText.contains("REG") || roleText.contains("ZURÜCKGEZOGENER") || roleText.contains("SPIELMACHER")) {
			double pass = p.passCompletionPctProperty().get();
			s1 = pass > 1.0 ? pass / 100.0 : pass;
			s2 = p.progressivePasses90Property().get();
			double cross = p.crossAccuracyPctProperty().get();
			s3 = cross > 1.0 ? cross / 100.0 : cross;
		} else if (roleText.contains("ZSM") || roleText.contains("ZENTRAL") || roleText.contains("DM") || roleText.contains("BALLEROBERNDER")) {
			double pass = p.passCompletionPctProperty().get();
			s1 = pass > 1.0 ? pass / 100.0 : pass;
			s2 = p.interceptions90Property().get();
			double tackles = p.tacklesWonPctProperty().get();
			s3 = tackles > 1.0 ? tackles / 100.0 : tackles;
		} else if (roleText.contains("FV") || roleText.contains("KFV") || roleText.contains("AUSSENVERTEIDIGER") || roleText.contains("FLÜGELVERTEIDIGER")) {
			s1 = p.crossesAttempted90Property().get();
			s2 = p.xa90Property().get();
			s3 = p.interceptions90Property().get();
		} else if (roleText.contains("BSV") || roleText.contains("BALLSPIELEND")) {
			double pass = p.passCompletionPctProperty().get();
			s1 = pass > 1.0 ? pass / 100.0 : pass;
			s2 = p.interceptions90Property().get();
			s3 = p.progressivePasses90Property().get();
		} else if (roleText.contains("IV") || roleText.contains("INNENVERTEIDIGER") || roleText.contains("VERTEIDIGER")) {
			double tackles = p.tacklesWonPctProperty().get();
			s1 = tackles > 1.0 ? tackles / 100.0 : tackles;
			double headers = p.headersWonPctProperty().get();
			s2 = headers > 1.0 ? headers / 100.0 : headers;
			s3 = (double) p.mistakesLeadingToGoalProperty().get();
		} else if (roleText.contains("HRV") || roleText.contains("HALBRAUM")) {
			double pass = p.passCompletionPctProperty().get();
			s1 = pass > 1.0 ? pass / 100.0 : pass;
			double tackles = p.tacklesWonPctProperty().get();
			s2 = tackles > 1.0 ? tackles / 100.0 : tackles;
			s3 = p.interceptions90Property().get();
		} else {
			double pass = p.passCompletionPctProperty().get();
			s1 = pass > 1.0 ? pass / 100.0 : pass;
			s2 = p.interceptions90Property().get();
			s3 = p.progressivePasses90Property().get();
		}

		if (s1 > 0.0 || s2 > 0.0 || s3 > 0.0 || (p.statValue1Property().get() == 0.0 && p.statValue2Property().get() == 0.0)) {
			p.statValue1Property().set(s1);
			p.statValue2Property().set(s2);
			p.statValue3Property().set(s3);
		}
	}

	public static void evaluate(ScoutingPlayerModel p, AppConfig config) {
		if (p == null) return;

		double j = p.statValue1Property().get();
		double k = p.statValue2Property().get();
		double l = p.statValue3Property().get();
		int minutes = p.minutesProperty().get();

		boolean allStatsZero = (j == 0.0 && k == 0.0 && l == 0.0);
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

		String roleRaw = p.targetRoleProperty().get() != null ? p.targetRoleProperty().get().trim() : "";
		String role = roleRaw.toUpperCase();

		double statPoints;

		if (role.contains("STO") || role.contains("STÜRMER") || role.contains("ST (Z)")) {
			statPoints = Math.min(j / 0.52, 1.25) * 22.75
					+ Math.min(k / 0.19, 1.35) * 22.75
					+ Math.min(l / 0.48, 1.20) * 19.50;
		} else if (role.contains("MTW") || role.contains("TORWART") || role.contains("TW")) {
			statPoints = Math.min(j / 0.78, 1.20) * 22.75
					+ Math.min(Math.max(k + 0.35, 0.0) / 0.60, 1.30) * 29.25
					+ Math.min(l / 0.82, 1.20) * 13.00;
		} else if (role.contains("OM") || role.contains("QM") || role.contains("FLÜGEL") || role.contains("WINGER")) {
			double crossPct = k > 1.0 ? k / 100.0 : k;
			statPoints = Math.min(j / 0.20, 1.30) * 26.00
					+ Math.min(crossPct / 0.22, 1.30) * 22.75
					+ Math.min(l / 0.80, 1.20) * 16.25;
		} else if (role.contains("REG") || role.contains("ZURÜCKGEZOGENER") || role.contains("SPIELMACHER")) {
			double crossPct = l > 1.0 ? l / 100.0 : l;
			statPoints = Math.min(j / 0.85, 1.20) * 19.50
					+ Math.min(k / 5.20, 1.35) * 29.25
					+ Math.min(crossPct / 0.22, 1.20) * 16.25;
		} else if (role.contains("ZSM") || role.contains("ZENTRAL") || role.contains("DM") || role.contains("BALLEROBERNDER")) {
			statPoints = Math.min(j / 0.86, 1.20) * 22.75
					+ Math.min(k / 2.80, 1.30) * 22.75
					+ Math.min(l / 0.74, 1.20) * 19.50;
		} else if (role.contains("FV") || role.contains("KFV") || role.contains("AUSSENVERTEIDIGER") || role.contains("FLÜGELVERTEIDIGER")) {
			double crossPct = k > 1.0 ? k / 100.0 : k;
			statPoints = Math.min(j / 2.50, 1.30) * 22.75
					+ Math.min(crossPct / 0.22, 1.30) * 22.75
					+ Math.min(l / 2.00, 1.20) * 19.50;
		} else if (role.contains("BSV") || role.contains("BALLSPIELEND")) {
			statPoints = Math.min(j / 0.85, 1.20) * 22.75
					+ Math.min(k / 0.75, 1.20) * 22.75
					+ Math.min(l / 4.50, 1.30) * 19.50;
		} else if (role.contains("IV") || role.contains("INNENVERTEIDIGER") || role.contains("VERTEIDIGER")) {
			double errorBonus = Math.max(0.0, 1.0 - (l * 0.5)) * 9.75;
			statPoints = Math.min(j / 0.74, 1.20) * 26.00
					+ Math.min(k / 0.76, 1.20) * 29.25
					+ errorBonus;
		} else if (role.contains("HRV") || role.contains("HALBRAUM")) {
			statPoints = Math.min(j / 0.83, 1.20) * 22.75
					+ Math.min(k / 0.74, 1.20) * 22.75
					+ Math.min(l / 1.40, 1.20) * 19.50;
		} else {
			statPoints = Math.min(j / 0.80, 1.20) * 22.0
					+ Math.min(k / 1.50, 1.20) * 22.0
					+ Math.min(l / 0.80, 1.20) * 21.0;
		}

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

		if (asking >= 900_000_000L) {
			p.priceCheckProperty().set("🔒 UNVERKÄUFLICH");
			p.transferActionProperty().set("❌ KEINE FREIGABE");
		} else if (asking <= 0) {
			p.priceCheckProperty().set("⚪ KEINE ANGABE");
			p.transferActionProperty().set("ℹ️ ABLÖSE ANFRAGEN");
		} else if (asking <= adjustedMaxFee * 0.50) {
			p.priceCheckProperty().set("🟢 TOP-SCHNÄPPCHEN");
			if (wage <= adjustedMaxWage && finalScore >= 80.0) {
				p.transferActionProperty().set(af < 0.80 ? "⚠️ RISIKO-SCHNÄPPCHEN (VERHANDELN)" : "⭐ SOFORT VERPFLICHTEN");
			} else {
				p.transferActionProperty().set("✅ VERHANDELN");
			}
		} else if (asking <= adjustedMaxFee) {
			p.priceCheckProperty().set("🟡 FAIRER PREIS");
			if (wage <= adjustedMaxWage && finalScore >= 75.0) {
				p.transferActionProperty().set(af < 0.80 ? "⚠️ BEOBACHTEN (VERLETZUNGSRISIKO)" : "✅ VERHANDELN / BEOBACHTEN");
			} else {
				p.transferActionProperty().set("⏳ WARTEN");
			}
		} else {
			p.priceCheckProperty().set("🔴 ÜBERTEUERT");
			p.transferActionProperty().set("❌ ABLEHNEN");
		}
	}
}