package org.moneyballfm.moneyball_fm.db;

import org.moneyballfm.moneyball_fm.model.*;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryRepository {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	public static long saveSquadSnapshot(
			String clubName,
			String season,
			String fileName,
			List<PlayerRowModel> squad,
			List<LoanPlayerModel> outgoingLoans,
			List<LoanPlayerModel> incomingLoans,
			List<YouthPlayerModel> youth) {

		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false);

			long snapshotId = -1;
			String insertSnapshot = "INSERT INTO snapshots (club_name, in_game_season, export_type, file_name) VALUES (?, ?, 'SQUAD', ?)";
			try (PreparedStatement ps = conn.prepareStatement(insertSnapshot, Statement.RETURN_GENERATED_KEYS)) {
				ps.setString(1, clubName);
				ps.setString(2, season);
				ps.setString(3, fileName);
				ps.executeUpdate();
				try (ResultSet rs = ps.getGeneratedKeys()) {
					if (rs.next()) snapshotId = rs.getLong(1);
				}
			}

			if (snapshotId <= 0) {
				conn.rollback();
				return -1;
			}

			writePlayersBatch(conn, snapshotId, squad, outgoingLoans, incomingLoans, youth);

			conn.commit();
			return snapshotId;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static void persistCurrentState(
			String clubName,
			List<PlayerRowModel> squad,
			List<LoanPlayerModel> outgoingLoans,
			List<LoanPlayerModel> incomingLoans,
			List<YouthPlayerModel> youth,
			List<ScoutingPlayerModel> scouting) {

		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false);

			long snapshotId = -1;
			String findLatestSql = "SELECT id FROM snapshots WHERE export_type = 'SQUAD' ORDER BY imported_at DESC LIMIT 1";
			try (Statement st = conn.createStatement();
			     ResultSet rs = st.executeQuery(findLatestSql)) {
				if (rs.next()) {
					snapshotId = rs.getLong("id");
				}
			}

			if (snapshotId <= 0) {
				String insertSnapshot = "INSERT INTO snapshots (club_name, in_game_season, export_type, file_name) VALUES (?, 'Aktuell', 'SQUAD', 'Manuelle Änderung')";
				try (PreparedStatement ps = conn.prepareStatement(insertSnapshot, Statement.RETURN_GENERATED_KEYS)) {
					ps.setString(1, clubName);
					ps.executeUpdate();
					try (ResultSet rs = ps.getGeneratedKeys()) {
						if (rs.next()) snapshotId = rs.getLong(1);
					}
				}
			} else {
				try (PreparedStatement delP = conn.prepareStatement("DELETE FROM player_history WHERE snapshot_id = ?");
				     PreparedStatement delS = conn.prepareStatement("DELETE FROM scouting_history WHERE snapshot_id = ?")) {
					delP.setLong(1, snapshotId);
					delP.executeUpdate();
					delS.setLong(1, snapshotId);
					delS.executeUpdate();
				}
			}

			if (snapshotId <= 0) {
				conn.rollback();
				return;
			}

			// 1. Spieler schreiben
			writePlayersBatch(conn, snapshotId, squad, outgoingLoans, incomingLoans, youth);

			// 2. Scouting schreiben
			if (scouting != null && !scouting.isEmpty()) {
				String insertScout = """
                INSERT INTO scouting_history (
                    snapshot_id, player_name, target_role, age, asking_price,
                    wage_demand, contract_years, agility, pace, stamina,
                    stat1, stat2, stat3, scouting_score, transfer_action, price_check
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
             """;
				try (PreparedStatement sps = conn.prepareStatement(insertScout)) {
					for (ScoutingPlayerModel s : scouting) {
						sps.setLong(1, snapshotId);
						sps.setString(2, s.nameProperty().get());
						sps.setString(3, s.targetRoleProperty().get());
						sps.setInt(4, s.ageProperty().get());
						sps.setLong(5, s.askingPriceProperty().get());
						sps.setLong(6, s.wageDemandProperty().get());
						sps.setDouble(7, s.contractYearsProperty().get());
						sps.setInt(8, s.agilityProperty().get());
						sps.setInt(9, s.paceProperty().get());
						sps.setInt(10, s.staminaProperty().get());
						sps.setDouble(11, s.statValue1Property().get());
						sps.setDouble(12, s.statValue2Property().get());
						sps.setDouble(13, s.statValue3Property().get());
						sps.setDouble(14, s.scoutingScoreProperty().get());
						sps.setString(15, s.transferActionProperty().get());
						sps.setString(16, s.priceCheckProperty().get());
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

		try (PreparedStatement ps = conn.prepareStatement(insertPlayer)) {
			// A-Kader
			for (PlayerRowModel p : squad) {
				ps.setLong(1, snapshotId);
				ps.setString(2, p.nameProperty().get());
				ps.setString(3, "FIRST_TEAM");
				ps.setString(4, p.roleProperty().get());
				ps.setInt(5, p.ageProperty().get());
				ps.setLong(6, p.marketValueProperty().get());
				ps.setLong(7, p.monthlySalaryProperty().get());
				ps.setDouble(8, 2.0);
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

			// Outgoing Loans
			for (LoanPlayerModel l : outgoingLoans) {
				ps.setLong(1, snapshotId);
				ps.setString(2, l.playerNameProperty().get());
				ps.setString(3, "OUTGOING_LOAN");
				ps.setString(4, l.targetRoleProperty().get());
				ps.setInt(5, l.ageProperty().get());
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
				ps.setDouble(19, 0.0);
				ps.setDouble(20, 0.0);
				ps.setDouble(21, 0.0);
				ps.setString(22, l.recommendationProperty().get());
				ps.setString(23, l.clubAndLeagueProperty().get());
				ps.setBoolean(24, l.clauseFulfilledProperty().get());
				ps.addBatch();
			}

			// Incoming Loans
			for (LoanPlayerModel l : incomingLoans) {
				ps.setLong(1, snapshotId);
				ps.setString(2, l.playerNameProperty().get());
				ps.setString(3, "INCOMING_LOAN");
				ps.setString(4, l.targetRoleProperty().get());
				ps.setInt(5, l.ageProperty().get());
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
				ps.setDouble(19, 0.0);
				ps.setDouble(20, 0.0);
				ps.setDouble(21, 0.0);
				ps.setString(22, l.recommendationProperty().get());
				ps.setString(23, l.clubAndLeagueProperty().get());
				ps.setBoolean(24, l.clauseFulfilledProperty().get());
				ps.addBatch();
			}

			// Jugend
			for (YouthPlayerModel y : youth) {
				ps.setLong(1, snapshotId);
				ps.setString(2, y.nameProperty().get());
				ps.setString(3, "YOUTH");
				ps.setString(4, y.targetRoleProperty().get());
				ps.setInt(5, y.ageProperty().get());
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

	public static String loadLatestSnapshot(
			List<PlayerRowModel> squadTarget,
			List<LoanPlayerModel> outgoingLoansTarget,
			List<LoanPlayerModel> incomingLoansTarget,
			List<YouthPlayerModel> youthTarget,
			List<ScoutingPlayerModel> scoutTarget) {

		String latestSql = "SELECT id, imported_at FROM snapshots WHERE export_type = 'SQUAD' ORDER BY imported_at DESC LIMIT 1";

		try (Connection conn = DatabaseManager.getConnection();
		     Statement stmt = conn.createStatement();
		     ResultSet rs = stmt.executeQuery(latestSql)) {

			if (!rs.next()) return null;
			long snapshotId = rs.getLong("id");
			Timestamp ts = rs.getTimestamp("imported_at");
			String formattedDate = ts != null ? ts.toLocalDateTime().format(FORMATTER) : null;

			// 1. Spieler aus player_history laden
			String playerSql = "SELECT * FROM player_history WHERE snapshot_id = ?";
			try (PreparedStatement ps = conn.prepareStatement(playerSql)) {
				ps.setLong(1, snapshotId);
				try (ResultSet prs = ps.executeQuery()) {
					while (prs.next()) {
						String category = prs.getString("category");
						String name = prs.getString("player_name");
						String role = prs.getString("tactical_role");
						int age = prs.getInt("age");
						long marketVal = prs.getLong("market_value");
						long salary = prs.getLong("monthly_salary");
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
							Player p = new Player(name, role, age, marketVal, salary, 2.0, agility, pace, stamina, determ, minutes);
							PlayerRowModel row = new PlayerRowModel(p, s1, s2, s3);
							squadTarget.add(row);
						} else if ("OUTGOING_LOAN".equals(category)) {
							LoanPlayerModel loan = new LoanPlayerModel(
									LoanPlayerModel.LoanType.OUTGOING, name, role, age, club,
									games, minutes, rating, 0.0, marketVal, clause
							);
							outgoingLoansTarget.add(loan);
						} else if ("INCOMING_LOAN".equals(category)) {
							LoanPlayerModel loan = new LoanPlayerModel(
									LoanPlayerModel.LoanType.INCOMING, name, role, age, club,
									games, minutes, rating, 0.0, marketVal, clause
							);
							incomingLoansTarget.add(loan);
						} else if ("YOUTH".equals(category)) {
							YouthPlayerModel youth = new YouthPlayerModel(
									name, role, age, potStars, curStars, agility, pace, stamina, determ, s1, s2, s3
							);
							youthTarget.add(youth);
						}
					}
				}
			}

			// 2. Scouting aus scouting_history laden
			if (scoutTarget != null) {
				String scoutSql = "SELECT * FROM scouting_history WHERE snapshot_id = ?";
				try (PreparedStatement ps = conn.prepareStatement(scoutSql)) {
					ps.setLong(1, snapshotId);
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
							scoutTarget.add(scout);
						}
					}
				}
			}

			return formattedDate;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Lädt alle historischen Snapshots für einen konkreten Spieler chronologisch sortiert
	public static List<PlayerHistoryEntry> getPlayerHistory(String playerName) {
		List<PlayerHistoryEntry> list = new java.util.ArrayList<>();
		String sql = """
            SELECT s.imported_at, s.in_game_season, p.tactical_role, p.age,
                   p.market_value, p.monthly_salary, p.minutes_played,
                   p.moneyball_score, p.recommendation
            FROM player_history p
            JOIN snapshots s ON p.snapshot_id = s.id
            WHERE LOWER(p.player_name) = LOWER(?)
            ORDER BY s.imported_at ASC
        """;

		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, playerName);
			try (ResultSet rs = ps.executeQuery()) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
				while (rs.next()) {
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
}