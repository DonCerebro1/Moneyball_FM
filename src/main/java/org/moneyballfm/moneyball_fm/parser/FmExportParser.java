package org.moneyballfm.moneyball_fm.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.moneyballfm.moneyball_fm.model.Player;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FmExportParser {

	public static List<PlayerRowModel> parseFile(File file) throws IOException {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".html") || name.endsWith(".htm")) {
			return parseHtml(file);
		} else {
			// FM exportiert .rtf und .txt/.tsv als Klartext-Tabellen mit Pipes (|) oder Tabs
			return parsePlainTextFile(file);
		}
	}

	private static List<PlayerRowModel> parsePlainTextFile(File file) throws IOException {
		List<PlayerRowModel> result = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
			String line;
			Map<String, Integer> colMap = null;

			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("---") || line.matches("^[\\s\\|\\-]+$")) {
					continue; // Trennzeilen wie | ------- | überspringen
				}

				// Zeile anhand von | oder Tabs splitten
				String delimiter = line.contains("|") ? "\\|" : "\t";
				String[] rawCols = line.split(delimiter);
				List<String> colsList = new ArrayList<>();
				for (String col : rawCols) {
					String trimmed = col.trim();
					// Führende/nachlaufende leere Spalten durch Rand-Pipes filtern
					colsList.add(trimmed);
				}

				// Wenn am Rand Pipes waren, ist das erste Element oft leer -> entfernen
				if (!colsList.isEmpty() && colsList.get(0).isEmpty()) {
					colsList.remove(0);
				}
				if (!colsList.isEmpty() && colsList.get(colsList.size() - 1).isEmpty()) {
					colsList.remove(colsList.size() - 1);
				}

				String[] cols = colsList.toArray(new String[0]);

				// Headerzeile identifizieren (enthält "Name" und "Alter" oder "Gehalt")
				if (colMap == null) {
					boolean isHeader = false;
					for (String c : cols) {
						String lower = c.toLowerCase();
						if (lower.equals("name") || lower.equals("person") || lower.equals("spieler")) {
							isHeader = true;
							break;
						}
					}
					if (isHeader) {
						colMap = new HashMap<>();
						for (int i = 0; i < cols.length; i++) {
							colMap.put(cols[i].toLowerCase().trim(), i);
						}
					}
					continue;
				}

				// Datenzeile verarbeiten
				PlayerRowModel model = extractRow(cols, colMap);
				if (model != null) {
					result.add(model);
				}
			}
		}
		return result;
	}

	private static List<PlayerRowModel> parseHtml(File file) throws IOException {
		List<PlayerRowModel> result = new ArrayList<>();
		Document doc = Jsoup.parse(file, "UTF-8");
		Element table = doc.selectFirst("table");
		if (table == null) return result;

		Elements rows = table.select("tr");
		if (rows.isEmpty()) return result;

		Element headerRow = rows.get(0);
		Elements headers = headerRow.select("th");
		if (headers.isEmpty()) headers = headerRow.select("td");

		Map<String, Integer> colMap = new HashMap<>();
		for (int i = 0; i < headers.size(); i++) {
			colMap.put(headers.get(i).text().trim().toLowerCase(), i);
		}

		for (int r = 1; r < rows.size(); r++) {
			Elements tds = rows.get(r).select("td");
			if (tds.isEmpty()) continue;
			String[] cols = tds.stream().map(td -> td.text().trim()).toArray(String[]::new);
			PlayerRowModel model = extractRow(cols, colMap);
			if (model != null) result.add(model);
		}
		return result;
	}

	private static PlayerRowModel extractRow(String[] cols, Map<String, Integer> map) {
		String name = getVal(cols, map, "name", "person", "spieler");
		if (name.isBlank()) return null;

		String role = getVal(cols, map, "ausgewählte position", "pos", "rolle", "taktische rolle");
		if (role.isBlank() || role.equals("-")) role = "DMZL"; // Fallback oder aus Datei

		int age = parseInt(getVal(cols, map, "alter", "age"));
		long value = parseMoney(getVal(cols, map, "transferwert", "marktwert", "wert"));
		long salary = parseMoney(getVal(cols, map, "gehalt", "monatsgehalt"));
		int minutes = parseInt(getVal(cols, map, "min.", "minuten", "min"));

		// Beispiel-Mapping für die 3 Stats (z. B. xG/90, xA/90, Pas %)
		// Du kannst hier genau die Spalten abgreifen, die du für deinen Score brauchst!
		double s1 = parseDouble(getVal(cols, map, "xg/90", "stats wert 1"));
		double s2 = parseDouble(getVal(cols, map, "xa/90", "stats wert 2"));
		double s3 = parseDouble(getVal(cols, map, "pas %", "stats wert 3"));

		Player p = new Player(name, role, age, value, salary, 2.0, 10, 10, 10, minutes);
		return new PlayerRowModel(p, s1, s2, s3);
	}

	private static String getVal(String[] cols, Map<String, Integer> map, String... keys) {
		for (String k : keys) {
			Integer idx = map.get(k.toLowerCase());
			if (idx != null && idx < cols.length) {
				return cols[idx].trim();
			}
		}
		return "";
	}

	private static int parseInt(String val) {
		if (val == null || val.isBlank() || val.equals("-")) return 0;
		// Punkte wie bei 2.893 Minuten entfernen
		String c = val.replace(".", "").replaceAll("[^0-9]", "");
		return c.isEmpty() ? 0 : Integer.parseInt(c);
	}

	private static double parseDouble(String val) {
		if (val == null || val.isBlank() || val.equals("-")) return 0.0;
		boolean isPercent = val.contains("%");
		String c = val.replace(",", ".").replaceAll("[^0-9.]", "");
		if (c.isEmpty()) return 0.0;
		try {
			double parsed = Double.parseDouble(c);
			return (isPercent && parsed > 1.0) ? (parsed / 100.0) : parsed;
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	private static long parseMoney(String val) {
		if (val == null || val.isBlank() || val.equals("-")) return 0L;
		String clean = val.toLowerCase().replace("€", "").trim();

		// Falls Range wie €111Mio - €133Mio -> den ersten Wert nehmen
		if (clean.contains("-")) {
			clean = clean.split("-")[0].trim();
		}

		// /M. oder /J. Gehaltsangaben bereinigen
		clean = clean.replace("/m.", "").replace("/m", "").replace("/j.", "").replace("/j", "").trim();

		double factor = 1.0;
		if (clean.contains("mio") || clean.contains("m")) {
			factor = 1_000_000.0;
			clean = clean.replaceAll("[a-z ]", "");
		} else if (clean.contains("k") || clean.contains("tsd")) {
			factor = 1_000.0;
			clean = clean.replaceAll("[a-z ]", "");
		} else {
			// Tausendertrennzeichen entfernen (z. B. 1.107.000)
			clean = clean.replace(".", "");
		}

		clean = clean.replace(",", ".");
		try {
			return (long) (Double.parseDouble(clean) * factor);
		} catch (Exception e) {
			return 0L;
		}
	}
}