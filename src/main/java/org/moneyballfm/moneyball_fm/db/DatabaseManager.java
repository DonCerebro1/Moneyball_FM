package org.moneyballfm.moneyball_fm.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

	private static final String DB_DIR = "./data";
	private static final String DB_URL = "jdbc:h2:file:" + DB_DIR + "/moneyball_fm;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
	private static final String DB_USER = "sa";
	private static final String DB_PASSWORD = "";

	private static Connection connection;

	public static synchronized Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			File dir = new File(DB_DIR);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			initSchema(connection);
		}
		return connection;
	}

	private static void initSchema(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			// 1. Snapshots
			stmt.execute("""
                CREATE TABLE IF NOT EXISTS snapshots (
                    id IDENTITY PRIMARY KEY,
                    imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    club_name VARCHAR(100) NOT NULL,
                    in_game_season VARCHAR(20),
                    export_type VARCHAR(30) NOT NULL,
                    file_name VARCHAR(255)
                );
            """);

			// 2. Spieler-Historie
			stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_history (
                    id IDENTITY PRIMARY KEY,
                    snapshot_id BIGINT NOT NULL,
                    player_name VARCHAR(150) NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    tactical_role VARCHAR(1000),
                    age INT,
                    market_value BIGINT,
                    monthly_salary BIGINT,
                    contract_years DOUBLE,
                    minutes_played INT,
                    games_played INT,
                    avg_rating DOUBLE,
                    agility INT,
                    pace INT,
                    stamina INT,
                    determination INT,
                    potential_stars DOUBLE,
                    current_stars DOUBLE,
                    moneyball_score DOUBLE,
                    stat1 DOUBLE,
                    stat2 DOUBLE,
                    stat3 DOUBLE,
                    recommendation VARCHAR(255),
                    club_or_league VARCHAR(150),
                    clause_fulfilled BOOLEAN,
                    CONSTRAINT fk_snapshot FOREIGN KEY (snapshot_id) REFERENCES snapshots(id) ON DELETE CASCADE
                );
            """);

			// 3. Scouting-Historie (inkl. is_manual)
			stmt.execute("""
                CREATE TABLE IF NOT EXISTS scouting_history (
                    id IDENTITY PRIMARY KEY,
                    snapshot_id BIGINT NOT NULL,
                    player_name VARCHAR(150) NOT NULL,
                    target_role VARCHAR(100),
                    club VARCHAR(255),
                    positions VARCHAR(255),
                    age INT,
                    asking_price BIGINT,
                    wage_demand BIGINT,
                    contract_years DOUBLE,
                    agility INT,
                    pace INT,
                    stamina INT,
                    stat1 DOUBLE,
                    stat2 DOUBLE,
                    stat3 DOUBLE,
                    scouting_score DOUBLE,
                    transfer_action VARCHAR(255),
                    price_check VARCHAR(255),
                    is_manual BOOLEAN DEFAULT FALSE,
                    CONSTRAINT fk_scout_snapshot FOREIGN KEY (snapshot_id) REFERENCES snapshots(id) ON DELETE CASCADE
                );
            """);

			// Migrationen für bereits existierende Datenbank-Dateien
			stmt.execute("ALTER TABLE scouting_history ADD COLUMN IF NOT EXISTS club VARCHAR(255);");
			stmt.execute("ALTER TABLE scouting_history ADD COLUMN IF NOT EXISTS positions VARCHAR(255);");
			stmt.execute("ALTER TABLE scouting_history ADD COLUMN IF NOT EXISTS is_manual BOOLEAN DEFAULT FALSE;");

			// Bestehende player_history Tabelle auf 1000 Zeichen vergrößern
			try {
				stmt.execute("ALTER TABLE player_history ALTER COLUMN tactical_role VARCHAR(1000);");
			} catch (SQLException ignored) {}
		}
	}
}