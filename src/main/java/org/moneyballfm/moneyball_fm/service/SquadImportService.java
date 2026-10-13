package org.moneyballfm.moneyball_fm.service;

import javafx.collections.ObservableList;
import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.db.HistoryRepository;
import org.moneyballfm.moneyball_fm.model.LoanPlayerModel;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.RegistrationRowModel;
import org.moneyballfm.moneyball_fm.model.YouthPlayerModel;
import org.moneyballfm.moneyball_fm.parser.FmExportParser;

import java.io.File;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SquadImportService {

	public record ImportResult(int updatedCount, int addedCount, int removedCount, String message) {}

	public static String normalizePlayerKey(String rawName) {
		if (rawName == null || rawName.isBlank()) return "unknown";
		String normalized = Normalizer.normalize(rawName, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
		return normalized
				.replaceAll("(?i)^[\\*\\-\\s]+", "")
				.replaceAll("\\s*\\([^)]*\\)$", "")
				.replaceAll("[\\p{Punct}\\s]+", "")
				.trim()
				.toLowerCase();
	}

	public static String normalizeSquadPlayerKey(String rawName, int age) {
		int ageBracket = age > 0 ? (age / 3) : 0;
		return normalizePlayerKey(rawName) + "_b" + ageBracket;
	}

	public static int mergePreseasonStats(File file, ObservableList<RegistrationRowModel> registrationDataList) throws Exception {
		List<FmExportParser.PreseasonData> preData = FmExportParser.parsePreseasonFile(file);
		int updated = 0;

		for (FmExportParser.PreseasonData p : preData) {
			if (p.estimatedMinutes() == 0 && p.rating() == 0.0) continue;

			String pKey = normalizePlayerKey(p.name());
			for (RegistrationRowModel regRow : registrationDataList) {
				String regKey = normalizePlayerKey(regRow.nameProperty().get());
				if (regKey.equals(pKey)) {
					regRow.testMinutesProperty().set(p.estimatedMinutes());
					regRow.testRatingProperty().set(p.rating());
					regRow.evaluate();
					updated++;
					break;
				}
			}
		}
		return updated;
	}

	public static ImportResult importSquadFile(
			File droppedFile,
			String clubName,
			AppConfig appConfig,
			ObservableList<PlayerRowModel> playerDataList,
			ObservableList<LoanPlayerModel> outgoingLoanDataList,
			ObservableList<YouthPlayerModel> youthDataList) throws Exception {

		List<PlayerRowModel> rows = FmExportParser.parseFile(droppedFile);

		String myClub = (clubName != null && !clubName.isBlank())
				? clubName.trim().toLowerCase()
				: (appConfig.getMyClubName() != null ? appConfig.getMyClubName().toLowerCase() : "villarreal");

		String clubPrefix = myClub.substring(0, Math.min(3, myClub.length()));

		Set<String> importedKeys = new HashSet<>();
		for (PlayerRowModel row : rows) {
			String n = row.nameProperty().get();
			if (n != null && !n.isBlank()) {
				importedKeys.add(normalizeSquadPlayerKey(n, row.ageProperty().get()));
			}
		}

		Map<String, PlayerRowModel> existingKader = new HashMap<>();
		for (PlayerRowModel p : playerDataList) {
			String n = p.nameProperty().get();
			if (n != null) existingKader.put(normalizeSquadPlayerKey(n, p.ageProperty().get()), p);
		}

		Map<String, LoanPlayerModel> existingLoans = new HashMap<>();
		for (LoanPlayerModel l : outgoingLoanDataList) {
			String n = l.getPlayerName();
			if (n != null) existingLoans.put(normalizeSquadPlayerKey(n, l.ageProperty().get()), l);
		}

		Map<String, YouthPlayerModel> existingYouth = new HashMap<>();
		for (YouthPlayerModel y : youthDataList) {
			String n = y.getName();
			if (n != null) existingYouth.put(normalizeSquadPlayerKey(n, y.ageProperty().get()), y);
		}

		int updatedCount = 0;
		int addedCount = 0;

		for (PlayerRowModel row : rows) {
			String pName = row.nameProperty().get();
			if (pName == null || pName.isBlank()) continue;

			String pKey = normalizeSquadPlayerKey(pName, row.ageProperty().get());

			String club = row.currentClubProperty().get() != null
					? row.currentClubProperty().get().trim().toLowerCase() : "";
			String art = row.contractTypeProperty().get() != null
					? row.contractTypeProperty().get().trim().toLowerCase() : "";

			boolean isOutgoingLoan = !club.isBlank() && !club.startsWith(clubPrefix);
			boolean isIncomingLoan = art.contains("leihe");

			boolean isInLowerTeam = club.startsWith(clubPrefix) && (
					club.contains(" b") || club.contains(" c") ||
							club.contains("u19") || club.contains("u21") ||
							club.contains("u18") || club.contains("u23") ||
							club.contains("juvenil")
			);

			long wageThreshold = (appConfig != null) ? appConfig.getYouthWageThreshold() : 10000L;
			boolean isYouthContractWage = row.monthlySalaryProperty().get() < wageThreshold;

			boolean isReserveOrYouthTeam = isInLowerTeam ||
					(isYouthContractWage && row.ageProperty().get() <= 18 && row.minutesPlayedProperty().get() < 300);

			if (isOutgoingLoan) {
				removeFromOtherListsByKey(pKey, playerDataList, youthDataList);
				existingKader.remove(pKey);
				existingYouth.remove(pKey);

				String clubStr = row.currentClubProperty().get() != null ? row.currentClubProperty().get() : "";
				String leagueStr = row.getLeague();
				String clubAndLeague = (leagueStr != null && !leagueStr.isBlank())
						? clubStr + " (" + leagueStr + ")"
						: clubStr;

				int minutes = row.minutesPlayedProperty().get();
				int estimatedGames = minutes > 0 ? Math.max(1, minutes / 80) : 0;

				LoanPlayerModel freshLoan = new LoanPlayerModel(
						LoanPlayerModel.LoanType.OUTGOING,
						row.nameProperty().get(),
						row.roleProperty().get(),
						row.ageProperty().get(),
						clubAndLeague,
						estimatedGames,
						minutes,
						row.getLoanRating(),
						row.getGoals(),
						row.getAssists(),
						0.0,
						row.marketValueProperty().get(),
						minutes >= 900
				);

				if (existingLoans.containsKey(pKey)) {
					LoanPlayerModel existing = existingLoans.get(pKey);
					existing.updateFromFresh(freshLoan);
					LoanAnalysisService.evaluate(existing);
					updatedCount++;
				} else {
					LoanAnalysisService.evaluate(freshLoan);
					outgoingLoanDataList.add(freshLoan);
					existingLoans.put(pKey, freshLoan);
					addedCount++;
				}

			} else if (isReserveOrYouthTeam) {
				removeFromOtherListsByKey(pKey, playerDataList, outgoingLoanDataList);
				existingKader.remove(pKey);
				existingLoans.remove(pKey);

				YouthPlayerModel freshYouth = new YouthPlayerModel(
						row.nameProperty().get(),
						row.roleProperty().get(),
						row.ageProperty().get(),
						4.0,
						2.0,
						row.agilityProperty().get(),
						row.paceProperty().get(),
						row.staminaProperty().get(),
						row.determinationProperty().get(),
						row.statValue1Property().get(),
						row.statValue2Property().get(),
						row.statValue3Property().get()
				);

				if (existingYouth.containsKey(pKey)) {
					YouthPlayerModel existing = existingYouth.get(pKey);
					existing.updateFromFresh(freshYouth);
					YouthAnalysisService.evaluate(existing);
					updatedCount++;
				} else {
					YouthAnalysisService.evaluate(freshYouth);
					youthDataList.add(freshYouth);
					existingYouth.put(pKey, freshYouth);
					addedCount++;
				}

			} else {
				removeFromOtherListsByKey(pKey, outgoingLoanDataList, youthDataList);
				existingLoans.remove(pKey);
				existingYouth.remove(pKey);

				row.onLoanProperty().set(isIncomingLoan);

				if (existingKader.containsKey(pKey)) {
					PlayerRowModel existing = existingKader.get(pKey);

					String currentRole = existing.roleProperty().get();
					existing.updateFromFresh(row);
					if (currentRole != null && !currentRole.isBlank()) {
						existing.roleProperty().set(currentRole);
					}

					MoneyballCalculator.recalculate(existing, appConfig);
					updatedCount++;
				} else {
					MoneyballCalculator.recalculate(row, appConfig);
					playerDataList.add(row);
					existingKader.put(pKey, row);
					addedCount++;
				}
			}
		}

		// Abgangs-Prüfung
		int removedCount = 0;
		if (!importedKeys.isEmpty()) {
			List<PlayerRowModel> soldFromKader = playerDataList.stream()
					.filter(p -> !importedKeys.contains(normalizeSquadPlayerKey(p.nameProperty().get(), p.ageProperty().get())))
					.toList();
			removedCount += soldFromKader.size();
			playerDataList.removeAll(soldFromKader);

			List<YouthPlayerModel> soldFromYouth = youthDataList.stream()
					.filter(y -> !importedKeys.contains(normalizeSquadPlayerKey(y.getName(), y.ageProperty().get())))
					.toList();
			removedCount += soldFromYouth.size();
			youthDataList.removeAll(soldFromYouth);
		}

		String status = "Partieller Merge: " + updatedCount + " aktualisiert, " + addedCount + " neu hinzugefügt";
		if (removedCount > 0) {
			status += ", " + removedCount + " Abgänge/Verkäufe entfernt";
		}
		status += " (aus " + droppedFile.getName() + ").";

		return new ImportResult(updatedCount, addedCount, removedCount, status);
	}

	private static void removeFromOtherListsByKey(String key, ObservableList<?>... targetLists) {
		for (ObservableList<?> list : targetLists) {
			list.removeIf(item -> {
				if (item instanceof PlayerRowModel p) {
					String k = normalizeSquadPlayerKey(p.nameProperty().get(), p.ageProperty().get());
					return k.equalsIgnoreCase(key);
				} else if (item instanceof LoanPlayerModel l) {
					String k = normalizeSquadPlayerKey(l.getPlayerName(), l.ageProperty().get());
					return k.equalsIgnoreCase(key);
				} else if (item instanceof YouthPlayerModel y) {
					String k = normalizeSquadPlayerKey(y.getName(), y.ageProperty().get());
					return k.equalsIgnoreCase(key);
				}
				return false;
			});
		}
	}
}