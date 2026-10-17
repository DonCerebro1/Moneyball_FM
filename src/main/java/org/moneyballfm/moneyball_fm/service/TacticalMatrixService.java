package org.moneyballfm.moneyball_fm.service;

import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.TacticConfig;
import org.moneyballfm.moneyball_fm.model.Player;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.TacticRole;
import org.moneyballfm.moneyball_fm.model.TacticalMatrixRowModel;

import java.util.*;
import java.util.stream.Collectors;

public class TacticalMatrixService {

	public record RoleScore(TacticRole role, double score) {}
	public record TacticEvaluation(String tacticName, double avgTop11Score, String summary) {}

	/**
	 * Führt einen Scan über alle plausiblen Rollen für jeden Spieler im Kader durch.
	 */
	public static List<TacticalMatrixRowModel> analyzeSquadMatrix(List<PlayerRowModel> squad, AppConfig config) {
		List<TacticalMatrixRowModel> result = new ArrayList<>();
		if (squad == null || squad.isEmpty()) return result;

		for (PlayerRowModel row : squad) {
			Player p = row.getRawPlayer();
			if (p == null) continue;

			String currentRoleName = row.roleProperty().get();
			double curScore = row.moneyballScoreProperty().get();
			String naturalPos = p.getNaturalPosition() != null ? p.getNaturalPosition() : "-";

			TacticRole currentTacticRole = TacticRole.fromDisplayName(currentRoleName);

			// Für jede verfügbare Taktik-Rolle prüfen, ob sie positions-plausibel ist
			List<RoleScore> evaluatedRoles = new ArrayList<>();
			for (TacticRole tr : TacticRole.values()) {
				// Plausibel nach Position ODER es ist bereits seine aktuell zugewiesene Rolle
				if (tr == currentTacticRole || isPositionPlausible(naturalPos, tr)) {
					double score = calculateSimulatedScore(row, tr, config);
					evaluatedRoles.add(new RoleScore(tr, score));
				}
			}

			// Fallback, falls keine Rolle durch den Filter kam
			if (evaluatedRoles.isEmpty()) {
				for (TacticRole tr : TacticRole.values()) {
					double score = calculateSimulatedScore(row, tr, config);
					evaluatedRoles.add(new RoleScore(tr, score));
				}
			}

			// Absteigend nach berechnetem Score sortieren
			evaluatedRoles.sort((a, b) -> Double.compare(b.score(), a.score()));

			RoleScore top = evaluatedRoles.get(0);
			String bestRoleName = top.role().getDisplayName();
			double bestScore = top.score();

			// Sicherheitscheck: Beste Rolle darf nicht schlechter als die aktuelle Rolle sein
			if (bestScore < curScore && currentRoleName != null && !currentRoleName.isBlank()) {
				bestRoleName = currentRoleName;
				bestScore = curScore;
			}

			final String chosenBestRole = bestRoleName;

			// Top-Alternativen formatieren (nur Rollen mit realem Score > 0, ohne die Top-Rolle)
			String alternatives = evaluatedRoles.stream()
					.filter(rs -> !rs.role().getDisplayName().equalsIgnoreCase(chosenBestRole) && rs.score() > 0.0)
					.limit(3)
					.map(rs -> rs.role().getDisplayName() + " (" + String.format(Locale.GERMANY, "%.1f", rs.score()) + ")")
					.collect(Collectors.joining(" | "));

			if (alternatives.isBlank()) {
				alternatives = "Keine passenden Alternativen";
			}

			// Handlungsempfehlung anhand des Hebels (Delta) bestimmen
			double delta = Math.max(0.0, bestScore - curScore);
			String rec;
			if (delta >= 5.0) {
				rec = "Umschulen (" + bestRoleName + " +" + String.format(Locale.GERMANY, "%.1f", delta) + " Pkt.)";
			} else if (delta > 1.5) {
				rec = "Zweitrolle testen";
			} else {
				rec = "Optimal besetzt";
			}

			result.add(new TacticalMatrixRowModel(
					p.getName(),
					naturalPos,
					currentRoleName,
					curScore,
					bestRoleName,
					bestScore,
					alternatives,
					rec
			));
		}

		// Spieler mit dem größten Verbesserungspotenzial (Hebel) stehen ganz oben
		result.sort((a, b) -> Double.compare(b.scoreDeltaProperty().get(), a.scoreDeltaProperty().get()));
		return result;
	}

	/**
	 * Ermittelt für alle konfigurierten Taktiken die Passung der Top-11 Spieler.
	 */
	public static List<TacticEvaluation> evaluateTacticsFit(List<PlayerRowModel> squad, AppConfig config) {
		List<TacticEvaluation> evals = new ArrayList<>();
		if (config == null || config.getTactics() == null || squad == null || squad.isEmpty()) {
			return evals;
		}

		for (TacticConfig tactic : config.getTactics()) {
			if (tactic.getSlots() == null || tactic.getSlots().isEmpty()) continue;

			List<Double> topScores = new ArrayList<>();
			for (var slot : tactic.getSlots()) {
				String roleKey = slot.getRoleLabel() != null ? slot.getRoleLabel() : slot.getSlotName();
				TacticRole tr = TacticRole.fromDisplayName(roleKey);
				if (tr == null) tr = TacticRole.ZSM;

				final TacticRole evalRole = tr;
				double bestForSlot = squad.stream()
						.filter(p -> p.getRawPlayer() != null && isPositionPlausible(p.getRawPlayer().getNaturalPosition(), evalRole))
						.mapToDouble(p -> calculateSimulatedScore(p, evalRole, config))
						.max()
						.orElse(50.0);

				topScores.add(bestForSlot);
			}

			double avg = topScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
			avg = Math.round(avg * 10.0) / 10.0;

			String summary = avg >= 75.0 ? "Perfektes System" : (avg >= 70.0 ? "Solide Passung" : "Kader unpassend");
			evals.add(new TacticEvaluation(tactic.getName(), avg, summary));
		}

		evals.sort((a, b) -> Double.compare(b.avgTop11Score(), a.avgTop11Score()));
		return evals;
	}

	/**
	 * Prüft anhand der FM-Positionen, ob eine Rolle sportlich und trainingstechnisch plausibel ist.
	 */
	public static boolean isPositionPlausible(String naturalPosRaw, TacticRole role) {
		if (naturalPosRaw == null || naturalPosRaw.isBlank() || naturalPosRaw.equals("-")) return true;
		String pos = naturalPosRaw.toUpperCase();

		boolean hasTW = pos.contains("TW") || pos.contains("GK");
		boolean hasVZ = pos.contains("V (Z") || pos.contains("VZ") || pos.contains("DC") || pos.contains("CB")
				|| pos.contains("V (RZ)") || pos.contains("V (LZ)") || pos.contains("V (RLZ)");
		boolean hasVR = pos.contains("V (R") || pos.contains("VR") || pos.contains("DR") || pos.contains("RB") || pos.contains("FV (R");
		boolean hasVL = pos.contains("V (L") || pos.contains("VL") || pos.contains("DL") || pos.contains("LB") || pos.contains("FV (L");
		boolean hasSideDef = hasVR || hasVL || pos.contains("FV") || pos.contains("WB")
				|| pos.contains("V (RL)") || pos.contains("V (RLZ)") || pos.contains("V (RZ)") || pos.contains("V (LZ)");
		boolean hasDM = pos.contains("DM");
		boolean hasZM = pos.contains("M (Z)") || pos.contains("MZ") || pos.contains("CM") || pos.contains("M/OM (Z)");
		boolean hasOM = pos.contains("OM") || pos.contains("AM");
		boolean hasWinger = pos.contains("FLÜ") || pos.contains("W")
				|| pos.contains("OM (R") || pos.contains("OM (L") || pos.contains("OMR") || pos.contains("OML")
				|| pos.contains("M (R") || pos.contains("M (L") || pos.contains("MR") || pos.contains("ML");
		boolean hasST = pos.contains("ST") || pos.contains("CF");

		// 1. Torhüter
		if (role == TacticRole.TW || role == TacticRole.MTW) {
			return hasTW;
		}
		if (hasTW) return false;

		// 2. Zentrale Innenverteidigung & Libero
		if (role == TacticRole.IV || role == TacticRole.BSV || role == TacticRole.HRV || role == TacticRole.KIV || role == TacticRole.LIBERO) {
			return hasVZ || hasDM;
		}

		// 3. Außen- & Flügelverteidiger
		if (role == TacticRole.AV || role == TacticRole.FV || role == TacticRole.KFV || role == TacticRole.IAV || role == TacticRole.IFV) {
			return hasSideDef;
		}

		// 4. Reine Mittelstürmer
		if (role == TacticRole.STOESSTUERMER || role == TacticRole.KNIPSER || role == TacticRole.PRESSENDER_STUERMER
				|| role == TacticRole.FALSCHE_NEUN || role == TacticRole.HAENGENDE_SPITZE
				|| role == TacticRole.ZIELSPIELER || role == TacticRole.KOMPLETTER_STUERMER) {
			return hasST || hasOM;
		}

		// 5. Flügelspieler / Winger
		if (role == TacticRole.FLUEGELSPIELER || role == TacticRole.INVERSER_FLUEGELSPIELER
				|| role == TacticRole.INVERSER_AUSSENSTUERMER || role == TacticRole.DEFENSIVER_FLUEGELSPIELER
				|| role == TacticRole.RAUMDEUTER) {
			return hasWinger;
		}

		// 6. Zentrales Mittelfeld & Sechser
		return hasDM || hasZM || hasOM;
	}

	private static double calculateSimulatedScore(PlayerRowModel row, TacticRole targetRole, AppConfig config) {
		Player p = row.getRawPlayer();
		if (p == null) return 0.0;

		Player tempPlayer = new Player(
				p.getName(),
				targetRole.getDisplayName(),
				p.getAge(),
				p.getMarketValue(),
				p.getMonthlySalary(),
				p.getContractYears(),
				p.getAgility(),
				p.getPace(),
				p.getStamina(),
				p.getDetermination(),
				p.getMinutesPlayed(),
				p.getContractType(),
				p.getCurrentClub()
		);

		tempPlayer.putAllStats(p.getAllStats());

		PlayerRowModel tempRow = new PlayerRowModel(tempPlayer, 0.0, 0.0, 0.0);
		MoneyballCalculator.recalculate(tempRow, config);

		return tempRow.moneyballScoreProperty().get();
	}
}