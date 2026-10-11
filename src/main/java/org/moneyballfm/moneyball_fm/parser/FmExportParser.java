package org.moneyballfm.moneyball_fm.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.moneyballfm.moneyball_fm.model.Player;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.TacticRole;

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
			return parsePlainTextFile(file);
		}
	}

	private static List<PlayerRowModel> parsePlainTextFile(File file) throws IOException {
		List<PlayerRowModel> result = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
			String line;
			Map<String, Integer> colMap = null;

			while ((line = reader.readLine()) != null) {
				line = cleanNbsps(line).trim();
				if (line.isEmpty() || line.startsWith("---") || line.matches("^[\\s\\|\\-]+$")) {
					continue;
				}

				String delimiter = line.contains("|") ? "\\|" : "\t";
				String[] rawCols = line.split(delimiter);
				List<String> colsList = new ArrayList<>();
				for (String col : rawCols) {
					colsList.add(cleanNbsps(col).trim());
				}

				if (!colsList.isEmpty() && colsList.get(0).isEmpty()) {
					colsList.remove(0);
				}
				if (!colsList.isEmpty() && colsList.get(colsList.size() - 1).isEmpty()) {
					colsList.remove(colsList.size() - 1);
				}

				String[] cols = colsList.toArray(new String[0]);

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
							colMap.put(cleanNbsps(cols[i]).toLowerCase().trim(), i);
						}
					}
					continue;
				}

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
			colMap.put(cleanNbsps(headers.get(i).text()).toLowerCase().trim(), i);
		}

		for (int r = 1; r < rows.size(); r++) {
			Elements tds = rows.select("td");
			if (tds.isEmpty()) continue;
			String[] cols = tds.stream().map(td -> cleanNbsps(td.text()).trim()).toArray(String[]::new);
			PlayerRowModel model = extractRow(cols, colMap);
			if (model != null) result.add(model);
		}
		return result;
	}

	private static PlayerRowModel extractRow(String[] cols, Map<String, Integer> map) {
		String name = getVal(cols, map, "name", "person", "spieler");
		if (name.isBlank()) return null;

		// 1. Detaillierte Rollenspalte (z. B. "Kompletter Flügelverteidiger (Unterstützen)")
		String roleDetail = getVal(cols, map,
				"position/rolle/aufgabe",
				"position / rolle / aufgabe",
				"rolle/aufgabe",
				"rolle",
				"taktische rolle"
		);

		// 2. Ausgewählte Position im aktuellen Spieltagskader (z. B. "TW", "FVR", "E1")
		String rawPos = getVal(cols, map, "ausgewählte position", "ausgew. pos.", "ausgew. pos");

		// 3. Gelernte Stammposition(en) (z. B. "V (RZ), FV (R)" oder "DM, M/OM (Z)")
		String naturalPosRaw = getVal(cols, map, "position", "pos", "natürliche position");

		String targetPositionStr = "";

		// Gültige Rolle prüfen (generische Dummy-Texte wie "– Spielposition" ausschließen)
		boolean hasValidRoleDetail = !roleDetail.isBlank()
				&& !roleDetail.equals("-")
				&& !roleDetail.equals("—")
				&& !roleDetail.toLowerCase().contains("spielposition");

		boolean hasValidRawPos = !rawPos.isBlank()
				&& !rawPos.toUpperCase().matches("^[EB]\\d+$")
				&& !rawPos.equals("-")
				&& !rawPos.equals("—");

		if (hasValidRoleDetail) {
			// Priorität 1: Reale Taktikrolle aus der Taktik-Ansicht
			targetPositionStr = roleDetail;
		} else if (hasValidRawPos) {
			// Priorität 2: Startelfposition
			targetPositionStr = rawPos;
		} else {
			// Priorität 3: Fallback für Leihspieler / Reservisten -> Erste gelernte Hauptposition
			if (naturalPosRaw.contains(",")) {
				targetPositionStr = naturalPosRaw.split(",")[0].trim();
			} else {
				targetPositionStr = naturalPosRaw;
			}
		}

		String cleanPos = cleanNbsps(targetPositionStr).replaceAll("[\\.]+$", "").trim();
		TacticRole tacticRole = TacticRole.fromPosition(cleanPos);

		int age = parseInt(getVal(cols, map, "alter", "age"));
		long value = parseMoney(getVal(cols, map, "transferwert", "marktwert", "wert"));
		long salary = parseMoney(getVal(cols, map, "gehalt", "monatsgehalt"));
		int minutes = parseInt(getVal(cols, map, "min.", "minuten", "min"));

		String art = getVal(cols, map, "art", "vertragsart");
		if (art.isBlank()) art = "Vollzeitvertrag";

		String club = getVal(cols, map, "verein", "club");
		String league = getVal(cols, map, "liga", "wettbewerb", "division");

		int bew = parseInt(getVal(cols, map, "bew", "bewegung"));
		int ant = parseInt(getVal(cols, map, "ant", "antritt"));
		int aus = parseInt(getVal(cols, map, "aus", "ausdauer"));
		int zie = parseInt(getVal(cols, map, "zie", "ziel", "zielstrebigkeit"));

		double contractYears = parseContractYears(getVal(cols, map, "endet", "vertrag"));

		// Leih- und Scorer-Felder parsen
		double loanRating = parseDouble(getVal(cols, map, "ø note", "note", "d-note", "drschn."));
		int goals = parseInt(getVal(cols, map, "tore", "goals"));
		int assists = parseInt(getVal(cols, map, "vor", "vorlagen", "assists"));

		Player player = new Player(
				name,
				tacticRole.getDisplayName(),
				age,
				value,
				salary,
				contractYears,
				bew,
				ant,
				aus,
				zie,
				minutes,
				art,
				club
		);

		player.setLeague(league);
		player.setNaturalPosition(naturalPosRaw);
		player.setLoanRating(loanRating);
		player.setGoals(goals);
		player.setAssists(assists);

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			int idx = entry.getValue();
			if (idx < cols.length) {
				double val = parseDouble(cols[idx]);
				player.putStat(entry.getKey(), val);
			}
		}

		double s1 = parseDouble(getVal(cols, map, tacticRole.getStat1Header()));
		double s2 = parseDouble(getVal(cols, map, tacticRole.getStat2Header()));
		double s3 = parseDouble(getVal(cols, map, tacticRole.getStat3Header()));

		PlayerRowModel row = new PlayerRowModel(player, s1, s2, s3);
		row.setStatLabels(tacticRole.getStat1Header(), tacticRole.getStat2Header(), tacticRole.getStat3Header());

		return row;
	}

	private static String cleanNbsps(String s) {
		if (s == null) return "";
		return s.replace('\u00A0', ' ').replace("&nbsp;", " ").trim();
	}

	private static double parseContractYears(String dateStr) {
		if (dateStr == null || dateStr.isBlank() || dateStr.equals("-")) return 1.0;
		try {
			String[] parts = dateStr.split("/");
			if (parts.length == 3) {
				int endYear = Integer.parseInt(parts[2].trim());
				int currentSeasonYear = 2038;
				return Math.max(0.5, endYear - currentSeasonYear);
			}
		} catch (Exception ignored) {}
		return 1.0;
	}

	private static String getVal(String[] cols, Map<String, Integer> map, String... keys) {
		for (String k : keys) {
			String searchKey = cleanNbsps(k).toLowerCase().trim();
			Integer idx = map.get(searchKey);
			if (idx != null && idx < cols.length) {
				return cols[idx].trim();
			}
		}
		return "";
	}

	private static int parseInt(String val) {
		if (val == null || val.isBlank() || val.equals("-")) return 0;
		String c = cleanNbsps(val).replace(".", "").replaceAll("[^0-9]", "");
		return c.isEmpty() ? 0 : Integer.parseInt(c);
	}

	private static double parseDouble(String val) {
		if (val == null || val.isBlank() || val.equals("-") || val.equals("—")) return 0.0;
		String c = cleanNbsps(val).replace(",", ".").replaceAll("[^0-9.]", "");
		if (c.isEmpty()) return 0.0;
		try {
			return Double.parseDouble(c);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	private static long parseMoney(String val) {
		if (val == null || val.isBlank() || val.equals("-")) return 0L;
		String clean = cleanNbsps(val).toLowerCase().replace("€", "").trim();

		if (clean.contains("-")) {
			String[] parts = clean.split("-");
			if (parts.length == 2) {
				long minVal = parseSingleAmount(parts[0].trim());
				long maxVal = parseSingleAmount(parts[1].trim());
				if (minVal > 0 && maxVal > 0) {
					return (minVal + maxVal) / 2;
				} else if (maxVal > 0) {
					return maxVal;
				} else {
					return minVal;
				}
			}
		}

		return parseSingleAmount(clean);
	}

	private static long parseSingleAmount(String clean) {
		if (clean == null || clean.isBlank()) return 0L;

		clean = clean.replace("/m.", "").replace("/m", "").replace("/j.", "").replace("/j", "").trim();

		double factor = 1.0;
		if (clean.contains("mio") || clean.contains("m")) {
			factor = 1_000_000.0;
			clean = clean.replaceAll("[a-z ]", "");
		} else if (clean.contains("k") || clean.contains("tsd")) {
			factor = 1_000.0;
			clean = clean.replaceAll("[a-z ]", "");
		} else {
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