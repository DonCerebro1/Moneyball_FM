package org.moneyballfm.moneyball_fm.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.moneyballfm.moneyball_fm.config.AppConfig;
import org.moneyballfm.moneyball_fm.config.ConfigLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LeagueReputationImporter {

	public record ImportResult(int importedCount, String message) {}

	/**
	 * Prüft, ob es sich um eine Ligen-Reputationsdatei handelt (Spalten Name & Nation/Ansehen).
	 */
	public static boolean isLeagueReputationFile(File file) {
		if (file == null || !file.exists()) return false;
		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
			String line;
			int count = 0;
			while ((line = reader.readLine()) != null && count < 30) {
				String clean = cleanRtfFormatting(line).toLowerCase();
				if (clean.contains("name") && (clean.contains("nation") || clean.contains("ansehen"))) {
					return true;
				}
				count++;
			}
		} catch (Exception ignored) {}
		return false;
	}

	/**
	 * Liest die rangbasierte Tabelle ein, ermittelt Koeffizienten anhand des Rangs
	 * und aktualisiert die moneyball-config.yaml.
	 */
	public static ImportResult importFromFile(File file) throws Exception {
		List<String> leagueOrder = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
			String line;
			boolean headerFound = false;

			while ((line = reader.readLine()) != null) {
				String clean = cleanRtfFormatting(line).trim();
				if (clean.isEmpty() || clean.startsWith("---") || clean.matches("^[\\s\\|\\-]+$")) {
					continue;
				}

				if (!headerFound) {
					String lower = clean.toLowerCase();
					if (lower.contains("name") && (lower.contains("nation") || lower.contains("ansehen"))) {
						headerFound = true;
					}
					continue;
				}

				// Zeile splitten nach Pipe | oder Tab \t
				String delimiter = clean.contains("|") ? "\\|" : "\t";
				String[] cols = clean.split(delimiter);
				List<String> validCols = new ArrayList<>();
				for (String c : cols) {
					String colClean = c.trim();
					if (!colClean.isEmpty() && !colClean.matches("^[\\-]+$")) {
						validCols.add(colClean);
					}
				}

				if (!validCols.isEmpty()) {
					String leagueName = validCols.get(0).trim();
					if (!leagueName.equalsIgnoreCase("name") && !leagueName.isBlank()) {
						leagueOrder.add(leagueName);
					}
				}
			}
		}

		if (leagueOrder.isEmpty()) {
			return new ImportResult(0, "Keine Ligen in der Datei gefunden.");
		}

		// Koeffizienten anhand der Platzierung vergeben
		Map<String, Double> newCoefficients = new LinkedHashMap<>();
		for (int rank = 1; rank <= leagueOrder.size(); rank++) {
			String league = leagueOrder.get(rank - 1);
			double coeff;

			if (rank <= 5) {
				coeff = 1.00; // Top 5 Ligen (Premier Division, Spaniens 1., Serie A, Bundesliga, Ligue 1)
			} else if (rank <= 15) {
				coeff = 0.90; // Top-Verfolger (Portugal, Eredivisie, Brasilien, Argentinien, Belgien etc.)
			} else if (rank <= 35) {
				coeff = 0.82; // Starke 2. Ligen & mittlere 1. Ligen (Championship, 2. Bundesliga, La Liga 2 etc.)
			} else if (rank <= 80) {
				coeff = 0.68; // Solide 3. Ligen & Entwicklungsligen (Sky Bet League One, 3. Liga, etc.)
			} else {
				coeff = 0.55; // Untere Amateurligen & Regionaldivisionen
			}

			newCoefficients.put(league.toLowerCase().trim(), coeff);
		}

		// Bestehende Config laden und updaten
		AppConfig config = ConfigLoader.loadConfig();
		if (config.getLeagueCoefficients() == null) {
			config.setLeagueCoefficients(new HashMap<>());
		}
		config.getLeagueCoefficients().putAll(newCoefficients);

		// moneyball-config.yaml schreiben (sowohl extern als auch im Quellordner)
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		Path targetExternal = Paths.get("moneyball-config.yaml");
		mapper.writeValue(targetExternal.toFile(), config);

		Path targetSrc = Paths.get("src", "main", "resources", "moneyball-config.yaml");
		if (Files.exists(targetSrc.getParent())) {
			mapper.writeValue(targetSrc.toFile(), config);
		}

		ConfigLoader.reloadConfig();

		return new ImportResult(
				newCoefficients.size(),
				String.format("%d Ligen aus Rangliste erfolgreich importiert!", newCoefficients.size())
		);
	}

	private static String cleanRtfFormatting(String raw) {
		if (raw == null) return "";
		// Entfernt gängige RTF-Steuerzeichen wie \cell, \row, \par, etc.
		return raw.replaceAll("\\\\[a-zA-Z0-9]+", " ")
				.replace("{", "")
				.replace("}", "")
				.replace("\u00A0", " ")
				.replaceAll("\\s+", " ")
				.trim();
	}
}