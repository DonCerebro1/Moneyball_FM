package org.moneyballfm.moneyball_fm.db;

import org.moneyballfm.moneyball_fm.model.*;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryRepository {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	public record SnapshotMetadata(String importDate, String inGameSeason) {}

	public static void saveSquadSnapshot(
			String clubName,
			String inGameSeason,
			List<PlayerRowModel> squad,
			List<LoanPlayerModel> outgoingLoans,
			List<LoanPlayerModel> incomingLoans,
			List<YouthPlayerModel> youth) {

		String season = (inGameSeason != null && !inGameSeason.isBlank()) ? inGameSeason.trim() : "Aktuell";

		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false);

			long snapshotId = -1;
			String insertSnapshot = "INSERT INTO snapshots (club_name, in_game_season, export_type, file_name, imported_at) VALUES (?, ?, 'SQUAD', 'Kader-Import', CURRENT_TIMESTAMP)";
			try (PreparedStatement ps = conn.prepareStatement(insertSnapshot, Statement.RETURN_GENERATED_KEYS)) {
				ps.setString(1, clubName);
				ps.setString(2, season);
				ps.executeUpdate();
				try (ResultSet rs = ps.getGeneratedKeys()) {
					if (rs.next()) snapshotId = rs.getLong(1);
				}
			}

			if (snapshotId <= 0) {
				conn.rollback();
				return;
			}

			writePlayersBatch(conn, snapshotId, squad, outgoingLoans, incomingLoans, youth);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void saveSquadSnapshot(
			String clubName,
			List<PlayerRowModel> squad,
			List<LoanPlayerModel> outgoingLoans,
			List<LoanPlayerModel> incomingLoans,
			List<YouthPlayerModel> youth) {
		saveSquadSnapshot(clubName, "Aktuell", squad, outgoingLoans, incomingLoans, youth);
	}

	public static void updateLatestSnapshotRoles(List<PlayerRowModel> squad) {
		if (squad == null || squad.isEmpty()) return;

		String findLatestSql = "SELECT id FROM snapshots WHERE export_type = 'SQUAD' ORDER BY imported_at DESC LIMIT 1";
		try (Connection conn = DatabaseManager.getConnection()) {
			long latestSnapshotId = -1;
			try (Statement stmt = conn.createStatement();
			     ResultSet rs = stmt.executeQuery(findLatestSql)) {
				if (rs.next()) {
					latestSnapshotId = rs.getLong("id");
				}
			}

			if (latestSnapshotId <= 0) return;

			String updateSql = "UPDATE player_history SET tactical_role = ? WHERE snapshot_id = ? AND LOWER(TRIM(player_name)) = LOWER(TRIM(?))";
			try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
				for (PlayerRowModel p : squad) {
					ps.setString(1, p.serializeTacticalRoles());
					ps.setLong(2, latestSnapshotId);
					ps.setString(3, p.nameProperty().get().trim());
					ps.addBatch();
				}
				ps.executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updateLatestSnapshotSeason(String newSeason) {
		if (newSeason == null || newSeason.isBlank()) return;
		String sql = "UPDATE snapshots SET in_game_season = ? WHERE id = (SELECT id FROM snapshots WHERE export_type = 'SQUAD' ORDER BY imported_at DESC LIMIT 1)";
		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, newSeason.trim());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void clearScoutingData() {
		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false);
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate("DELETE FROM scouting_history WHERE snapshot_id IN (SELECT id FROM snapshots WHERE export_type = 'SCOUTING')");
				stmt.executeUpdate("DELETE FROM snapshots WHERE export_type = 'SCOUTING'");
			}
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void saveScoutingSnapshot(
			String clubName,
			String inGameSeason,
			List<ScoutingPlayerModel> scouting) {

		String season = (inGameSeason != null && !inGameSeason.isBlank()) ? inGameSeason.trim() : "Aktuell";

		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false);

			long snapshotId = -1;
			String insertSnapshot = "INSERT INTO snapshots (club_name, in_game_season, export_type, file_name, imported_at) VALUES (?, ?, 'SCOUTING', 'Scouting-Import', CURRENT_TIMESTAMP)";
			try (PreparedStatement ps = conn.prepareStatement(insertSnapshot, Statement.RETURN_GENERATED_KEYS)) {
				ps.setString(1, clubName);
				ps.setString(2, season);
				ps.executeUpdate();
				try (ResultSet rs = ps.getGeneratedKeys()) {
					if (rs.next()) snapshotId = rs.getLong(1);
				}
			}

			if (snapshotId <= 0) {
				conn.rollback();
				return;
			}

			if (scouting != null && !scouting.isEmpty()) {
				Set<String> scoutKeys = new HashSet<>();
				String insertScout = """
                            INSERT INTO scouting_history (
                                snapshot_id, player_name, target_role, club, positions, age, asking_price,
                                wage_demand, contract_years, agility, pace, stamina,
                                stat1, stat2, stat3, scouting_score, transfer_action, price_check
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                         """;
				try (PreparedStatement sps = conn.prepareStatement(insertScout)) {
					for (ScoutingPlayerModel s : scouting) {
						int age = s.ageProperty().get();
						String key = (s.nameProperty().get() != null ? s.nameProperty().get().trim().toLowerCase() : "") + "_b" + (age > 0 ? (age / 3) : 0);
						if (key.isBlank() || scoutKeys.contains(key)) continue;
						scoutKeys.add(key);

						sps.setLong(1, snapshotId);
						sps.setString(2, s.nameProperty().get().trim());
						sps.setString(3, s.targetRoleProperty().get());
						sps.setString(4, s.clubProperty().get() != null ? s.clubProperty().get() : "");
						sps.setString(5, s.positionsProperty().get() != null ? s.positionsProperty().get() : "");
						sps.setInt(6, age);
						sps.setLong(7, s.askingPriceProperty().get());
						sps.setLong(8, s.wageDemandProperty().get());
						sps.setDouble(9, s.contractYearsProperty().get());
						sps.setInt(10, s.agilityProperty().get());
						sps.setInt(11, s.paceProperty().get());
						sps.setInt(12, s.staminaProperty().get());
						sps.setDouble(13, s.statValue1Property().get());
						sps.setDouble(14, s.statValue2Property().get());
						sps.setDouble(15, s.statValue3Property().get());
						sps.setDouble(16, s.scoutingScoreProperty().get());
						sps.setString(17, s.transferActionProperty().get());
						sps.setString(18, s.priceCheckProperty().get());
						sps.addBatch();
					}
					sps.executeBatch();
				}
			}

			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void saveScoutingSnapshot(
			String clubName,
			List<ScoutingPlayerModel> scouting) {
		saveScoutingSnapshot(clubName, "Aktuell", scouting);
	}

	private static void writePlayersBatch(
			Connection conn,
			long snapshotId,
			List<PlayerRowModel> squad,
			List<LoanPlayerModel> outgoingLoans,
			List<LoanPlayerModel> incomingLoans,
			List<YouthPlayerModel> youth) throws SQLException {

		String insertPlayer = """
             INSERT INTO player_history (
                 snapshot_id, player_name, category, tactical_role, age,
                 market_value, monthly_salary, contract_years, minutes_played, games_played,
                 avg_rating, agility, pace, stamina, determination,
                 potential_stars, current_stars, moneyball_score,
                 stat1, stat2, stat3, recommendation, club_or_league, clause_fulfilled
             ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         """;

		Set<String> insertedKeys = new HashSet<>();

		try (PreparedStatement ps = conn.prepareStatement(insertPlayer)) {
			for (PlayerRowModel p : squad) {
				int age = p.ageProperty().get();
				String key = (p.nameProperty().get() != null ? p.nameProperty().get().trim().toLowerCase() : "") + "_b" + (age > 0 ? (age / 3) : 0);
				if (key.isBlank() || insertedKeys.contains(key)) continue;
				insertedKeys.add(key);

				ps.setLong(1, snapshotId);
				ps.setString(2, p.nameProperty().get().trim());
				ps.setString(3, "FIRST_TEAM");
				ps.setString(4, p.serializeTacticalRoles());
				ps.setInt(5, age);
				ps.setLong(6, p.marketValueProperty().get());
				ps.setLong(7, p.monthlySalaryProperty().get());
				ps.setDouble(8, p.contractYearsProperty().get());
				ps.setInt(9, p.minutesPlayedProperty().get());
				ps.setInt(10, 0);
				ps.setDouble(11, 0.0);
				ps.setInt(12, p.agilityProperty().get());
				ps.setInt(13, p.paceProperty().get());
				ps.setInt(14, p.staminaProperty().get());
				ps.setInt(15, p.determinationProperty().get());
				ps.setDouble(16, 0.0);
				ps.setDouble(17, 0.0);
				ps.setDouble(18, p.moneyballScoreProperty().get());
				ps.setDouble(19, p.statValue1Property().get());
				ps.setDouble(20, p.statValue2Property().get());
				ps.setDouble(21, p.statValue3Property().get());
				ps.setString(22, p.salaryCheckProperty().get());
				ps.setString(23, "-");
				ps.setBoolean(24, false);
				ps.addBatch();
			}

			for (LoanPlayerModel l : outgoingLoans) {
				int age = l.ageProperty().get();
				String key = (l.getPlayerName() != null ? l.getPlayerName().trim().toLowerCase() : "") + "_b" + (age > 0 ? (age / 3) : 0);
				if (key.isBlank() || insertedKeys.contains(key)) continue;
				insertedKeys.add(key);

				ps.setLong(1, snapshotId);
				ps.setString(2, l.playerNameProperty().get().trim());
				ps.setString(3, "OUTGOING_LOAN");
				ps.setString(4, l.targetRoleProperty().get());
				ps.setInt(5, age);
				ps.setLong(6, l.marketValueProperty().get());
				ps.setLong(7, 0L);
				ps.setDouble(8, 0.0);
				ps.setInt(9, l.minutesPlayedProperty().get());
				ps.setInt(10, l.gamesPlayedProperty().get());
				ps.setDouble(11, l.averageRatingProperty().get());
				ps.setInt(12, 12);
				ps.setInt(13, 12);
				ps.setInt(14, 12);
				ps.setInt(15, 12);
				ps.setDouble(16, 0.0);
				ps.setDouble(17, 0.0);
				ps.setDouble(18, l.moneyballScoreProperty().get());
				ps.setDouble(19, (double) l.getGoals());
				ps.setDouble(20, (double) l.getAssists());
				ps.setDouble(21, 0.0);
				ps.setString(22, l.recommendationProperty().get());
				ps.setString(23, l.clubAndLeagueProperty().get());
				ps.setBoolean(24, l.clauseFulfilledProperty().get());
				ps.addBatch();
			}

			for (LoanPlayerModel l : incomingLoans) {
				int age = l.ageProperty().get();
				String key = (l.getPlayerName() != null ? l.getPlayerName().trim().toLowerCase() : "") + "_b" + (age > 0 ? (age / 3) : 0);
				if (key.isBlank() || insertedKeys.contains(key)) continue;
				insertedKeys.add(key);

				ps.setLong(1, snapshotId);
				ps.setString(2, l.playerNameProperty().get().trim());
				ps.setString(3, "INCOMING_LOAN");
				ps.setString(4, l.targetRoleProperty().get());
				ps.setInt(5, age);
				ps.setLong(6, l.marketValueProperty().get());
				ps.setLong(7, 0L);
				ps.setDouble(8, 0.0);
				ps.setInt(9, l.minutesPlayedProperty().get());
				ps.setInt(10, l.gamesPlayedProperty().get());
				ps.setDouble(11, l.averageRatingProperty().get());
				ps.setInt(12, 12);
				ps.setInt(13, 12);
				ps.setInt(14, 12);
				ps.setInt(15, 12);
				ps.setDouble(16, 0.0);
				ps.setDouble(17, 0.0);
				ps.setDouble(18, l.moneyballScoreProperty().get());
				ps.setDouble(19, (double) l.getGoals());
				ps.setDouble(20, (double) l.getAssists());
				ps.setDouble(21, 0.0);
				ps.setString(22, l.recommendationProperty().get());
				ps.setString(23, l.clubAndLeagueProperty().get());
				ps.setBoolean(24, l.clauseFulfilledProperty().get());
				ps.addBatch();
			}

			for (YouthPlayerModel y : youth) {
				int age = y.ageProperty().get();
				String key = (y.getName() != null ? y.getName().trim().toLowerCase() : "") + "_b" + (age > 0 ? (age / 3) : 0);
				if (key.isBlank() || insertedKeys.contains(key)) continue;
				insertedKeys.add(key);

				ps.setLong(1, snapshotId);
				ps.setString(2, y.nameProperty().get().trim());
				ps.setString(3, "YOUTH");
				ps.setString(4, y.targetRoleProperty().get());
				ps.setInt(5, age);
				ps.setLong(6, 0L);
				ps.setLong(7, 0L);
				ps.setDouble(8, 0.0);
				ps.setInt(9, 0);
				ps.setInt(10, 0);
				ps.setDouble(11, 0.0);
				ps.setInt(12, y.agilityProperty().get());
				ps.setInt(13, y.paceProperty().get());
				ps.setInt(14, y.staminaProperty().get());
				ps.setInt(15, y.determinationProperty().get());
				ps.setDouble(16, y.potentialStarsProperty().get());
				ps.setDouble(17, y.currentStarsProperty().get());
				ps.setDouble(18, y.talentScoreProperty().get());
				ps.setDouble(19, y.statValue1Property().get());
				ps.setDouble(20, y.statValue2Property().get());
				ps.setDouble(21, y.statValue3Property().get());
				ps.setString(22, y.pipelineRecommendationProperty().get());
				ps.setString(23, "-");
				ps.setBoolean(24, false);
				ps.addBatch();
			}

			ps.executeBatch();
		}
	}

	public static SnapshotMetadata loadLatestSnapshot(
			List<PlayerRowModel> squadTarget,
			List<LoanPlayerModel> outgoingLoansTarget,
			List<LoanPlayerModel> incomingLoansTarget,
			List<YouthPlayerModel> youthTarget,
			List<ScoutingPlayerModel> scoutTarget,
			String activeTactic) {

		String lastDate = null;
		String lastSeason = null;

		String squadSql = "SELECT id, imported_at, in_game_season FROM snapshots WHERE export_type = 'SQUAD' ORDER BY imported_at DESC LIMIT 1";
		try (Connection conn = DatabaseManager.getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(squadSql)) {

			if (rs.next()) {
				long squadSnapshotId = rs.getLong("id");
				Timestamp ts = rs.getTimestamp("imported_at");
				lastDate = ts != null ? ts.toLocalDateTime().format(FORMATTER) : null;
				lastSeason = rs.getString("in_game_season");

				String playerSql = "SELECT * FROM player_history WHERE snapshot_id = ?";
				try (PreparedStatement ps = conn.prepareStatement(playerSql)) {
					ps.setLong(1, squadSnapshotId);
					try (ResultSet prs = ps.executeQuery()) {
						while (prs.next()) {
							String category = prs.getString("category");
							String name = prs.getString("player_name");
							String rawRoleData = prs.getString("tactical_role");
							int age = prs.getInt("age");
							long marketVal = prs.getLong("market_value");
							long salary = prs.getLong("monthly_salary");
							double contractY = prs.getDouble("contract_years");
							int minutes = prs.getInt("minutes_played");
							int games = prs.getInt("games_played");
							double rating = prs.getDouble("avg_rating");
							int agility = prs.getInt("agility");
							int pace = prs.getInt("pace");
							int stamina = prs.getInt("stamina");
							int determ = prs.getInt("determination");
							double potStars = prs.getDouble("potential_stars");
							double curStars = prs.getDouble("current_stars");
							double s1 = prs.getDouble("stat1");
							double s2 = prs.getDouble("stat2");
							double s3 = prs.getDouble("stat3");
							String club = prs.getString("club_or_league");
							boolean clause = prs.getBoolean("clause_fulfilled");

							if ("FIRST_TEAM".equals(category)) {
								Player p = new Player(name, "", age, marketVal, salary, contractY > 0 ? contractY : 2.0, agility, pace, stamina, determ, minutes);
								PlayerRowModel row = new PlayerRowModel(p, s1, s2, s3);
								row.deserializeTacticalRoles(rawRoleData, activeTactic);
								squadTarget.add(row);
							} else if ("OUTGOING_LOAN".equals(category)) {
								int goals = (int) Math.round(s1);
								int assists = (int) Math.round(s2);
								LoanPlayerModel loan = new LoanPlayerModel(
										LoanPlayerModel.LoanType.OUTGOING, name, rawRoleData, age, club,
										games, minutes, rating, goals, assists, 0.0, marketVal, clause
								);
								outgoingLoansTarget.add(loan);
							} else if ("INCOMING_LOAN".equals(category)) {
								int goals = (int) Math.round(s1);
								int assists = (int) Math.round(s2);
								LoanPlayerModel loan = new LoanPlayerModel(
										LoanPlayerModel.LoanType.INCOMING, name, rawRoleData, age, club,
										games, minutes, rating, goals, assists, 0.0, marketVal, clause
								);
								incomingLoansTarget.add(loan);
							} else if ("YOUTH".equals(category)) {
								YouthPlayerModel youth = new YouthPlayerModel(
										name, rawRoleData, age, potStars, curStars, agility, pace, stamina, determ, s1, s2, s3
								);
								youthTarget.add(youth);
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (scoutTarget != null) {
			String scoutSnapSql = "SELECT id FROM snapshots WHERE export_type = 'SCOUTING' ORDER BY imported_at DESC LIMIT 1";
			try (Connection conn = DatabaseManager.getConnection();
			     Statement stmt = conn.createStatement();
			     ResultSet rs = stmt.executeQuery(scoutSnapSql)) {

				if (rs.next()) {
					long scoutSnapshotId = rs.getLong("id");
					String scoutSql = "SELECT * FROM scouting_history WHERE snapshot_id = ?";
					try (PreparedStatement ps = conn.prepareStatement(scoutSql)) {
						ps.setLong(1, scoutSnapshotId);
						try (ResultSet srs = ps.executeQuery()) {
							while (srs.next()) {
								ScoutingPlayerModel scout = new ScoutingPlayerModel(
										srs.getString("player_name"),
										srs.getString("target_role"),
										srs.getInt("age"),
										srs.getLong("asking_price"),
										srs.getLong("wage_demand"),
										srs.getDouble("contract_years"),
										srs.getInt("agility"),
										srs.getInt("pace"),
										srs.getInt("stamina"),
										srs.getDouble("stat1"),
										srs.getDouble("stat2"),
										srs.getDouble("stat3")
								);
								try {
									scout.clubProperty().set(srs.getString("club"));
									scout.positionsProperty().set(srs.getString("positions"));
								} catch (SQLException ignored) {}

								initRawStatsFromLoaded(scout, srs.getDouble("stat1"), srs.getDouble("stat2"), srs.getDouble("stat3"));
								scoutTarget.add(scout);
							}
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return new SnapshotMetadata(lastDate, lastSeason);
	}

	private static void initRawStatsFromLoaded(ScoutingPlayerModel p, double s1, double s2, double s3) {
		String r = p.targetRoleProperty().get() != null ? p.targetRoleProperty().get().toUpperCase() : "";
		if (r.contains("TW") || r.contains("TORWART")) {
			p.savePctProperty().set(s1 > 1.0 ? s1 : s1 * 100.0);
			p.xgPrevented90Property().set(s2);
			p.passCompletionPctProperty().set(s3 > 1.0 ? s3 : s3 * 100.0);
		} else if (r.contains("STO") || r.contains("STÜRMER")) {
			p.xg90Property().set(s1);
			p.xa90Property().set(s2);
			p.shotsOnTargetPctProperty().set(s3 > 1.0 ? s3 : s3 * 100.0);
		} else {
			p.passCompletionPctProperty().set(s1 > 1.0 ? s1 : s1 * 100.0);
			p.interceptions90Property().set(s2);
			p.progressivePasses90Property().set(s3);
		}
	}

	public static List<PlayerHistoryEntry> getPlayerHistory(String playerName, int currentAge) {
		List<PlayerHistoryEntry> list = new ArrayList<>();
		if (playerName == null || playerName.isBlank()) return list;

		String sql = (currentAge > 0)
				? """
                  SELECT s.id, s.imported_at, s.in_game_season, p.tactical_role, p.age,
                         p.market_value, p.monthly_salary, p.minutes_played,
                         p.moneyball_score, p.recommendation
                  FROM player_history p
                  JOIN snapshots s ON p.snapshot_id = s.id
                  WHERE LOWER(TRIM(p.player_name)) = LOWER(TRIM(?))
                    AND ABS(p.age - ?) <= 2
                  ORDER BY s.imported_at ASC, p.id ASC
               """
				: """
                  SELECT s.id, s.imported_at, s.in_game_season, p.tactical_role, p.age,
                         p.market_value, p.monthly_salary, p.minutes_played,
                         p.moneyball_score, p.recommendation
                  FROM player_history p
                  JOIN snapshots s ON p.snapshot_id = s.id
                  WHERE LOWER(TRIM(p.player_name)) = LOWER(TRIM(?))
                  ORDER BY s.imported_at ASC, p.id ASC
               """;

		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, playerName.trim());
			if (currentAge > 0) {
				ps.setInt(2, currentAge);
			}
			try (ResultSet rs = ps.executeQuery()) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
				Set<Long> seenSnapshots = new HashSet<>();
				while (rs.next()) {
					long snapId = rs.getLong("id");
					if (seenSnapshots.contains(snapId)) continue;
					seenSnapshots.add(snapId);

					Timestamp ts = rs.getTimestamp("imported_at");
					String dateStr = (ts != null) ? ts.toLocalDateTime().format(dtf) : "-";
					String season = rs.getString("in_game_season");
					String role = rs.getString("tactical_role");
					int age = rs.getInt("age");
					long mv = rs.getLong("market_value");
					long sal = rs.getLong("monthly_salary");
					int min = rs.getInt("minutes_played");
					double score = rs.getDouble("moneyball_score");
					String rec = rs.getString("recommendation");

					list.add(new PlayerHistoryEntry(dateStr, season, role, age, mv, sal, min, score, rec));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static List<PlayerHistoryEntry> getPlayerHistory(String playerName) {
		return getPlayerHistory(playerName, -1);
	}

	public static List<PlayerHistoryEntry> getScoutingHistory(String playerName, int currentAge) {
		List<PlayerHistoryEntry> list = new ArrayList<>();
		if (playerName == null || playerName.isBlank()) return list;

		String sql = (currentAge > 0)
				? """
                  SELECT s.id, s.imported_at, s.in_game_season, sc.target_role, sc.age,
                         sc.asking_price, sc.wage_demand, 0 AS minutes_played,
                         sc.scouting_score, sc.transfer_action
                  FROM scouting_history sc
                  JOIN snapshots s ON sc.snapshot_id = s.id
                  WHERE LOWER(TRIM(sc.player_name)) = LOWER(TRIM(?))
                    AND ABS(sc.age - ?) <= 2
                  ORDER BY s.imported_at ASC, sc.id ASC
               """
				: """
                  SELECT s.id, s.imported_at, s.in_game_season, sc.target_role, sc.age,
                         sc.asking_price, sc.wage_demand, 0 AS minutes_played,
                         sc.scouting_score, sc.transfer_action
                  FROM scouting_history sc
                  JOIN snapshots s ON sc.snapshot_id = s.id
                  WHERE LOWER(TRIM(sc.player_name)) = LOWER(TRIM(?))
                  ORDER BY s.imported_at ASC, sc.id ASC
               """;

		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, playerName.trim());
			if (currentAge > 0) {
				ps.setInt(2, currentAge);
			}
			try (ResultSet rs = ps.executeQuery()) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
				Set<Long> seenSnapshots = new HashSet<>();
				while (rs.next()) {
					long snapId = rs.getLong("id");
					if (seenSnapshots.contains(snapId)) continue;
					seenSnapshots.add(snapId);

					Timestamp ts = rs.getTimestamp("imported_at");
					String dateStr = (ts != null) ? ts.toLocalDateTime().format(dtf) : "-";
					String season = rs.getString("in_game_season");
					String role = rs.getString("target_role");
					int age = rs.getInt("age");
					long asking = rs.getLong("asking_price");
					long wage = rs.getLong("wage_demand");
					double score = rs.getDouble("scouting_score");
					String action = rs.getString("transfer_action");

					list.add(new PlayerHistoryEntry(dateStr, season, role, age, asking, wage, 0, score, action));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static List<PlayerHistoryEntry> getScoutingHistory(String playerName) {
		return getScoutingHistory(playerName, -1);
	}

	public static List<ClubHistoryEntry> getClubHistory(String clubName) {
		List<ClubHistoryEntry> list = new ArrayList<>();
		String sql = """
             SELECT s.id, s.imported_at, s.in_game_season,
                    COUNT(p.id) AS player_count,
                    COALESCE(AVG(p.age), 0.0) AS avg_age,
                    COALESCE(SUM(p.market_value), 0) AS total_market_value,
                    COALESCE(SUM(p.monthly_salary), 0) AS total_salary,
                    COALESCE(AVG(p.moneyball_score), 0.0) AS avg_score
             FROM snapshots s
             LEFT JOIN player_history p ON s.id = p.snapshot_id AND p.category = 'FIRST_TEAM'
             WHERE s.export_type = 'SQUAD'
               AND (? IS NULL OR ? = '' OR LOWER(TRIM(s.club_name)) = LOWER(TRIM(?)))
             GROUP BY s.id, s.imported_at, s.in_game_season
             ORDER BY s.imported_at ASC
       """;

		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			String c = (clubName != null && !clubName.isBlank()) ? clubName.trim() : null;
			ps.setString(1, c);
			ps.setString(2, c != null ? c : "");
			ps.setString(3, c != null ? c : "");

			try (ResultSet rs = ps.executeQuery()) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
				while (rs.next()) {
					Timestamp ts = rs.getTimestamp("imported_at");
					String dateStr = (ts != null) ? ts.toLocalDateTime().format(dtf) : "-";
					String season = rs.getString("in_game_season");
					int count = rs.getInt("player_count");
					double avgAge = rs.getDouble("avg_age");
					long totalMv = rs.getLong("total_market_value");
					long totalSal = rs.getLong("total_salary");
					double avgScore = rs.getDouble("avg_score");

					list.add(new ClubHistoryEntry(dateStr, season, count, avgAge, totalMv, totalSal, avgScore));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
}