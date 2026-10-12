package org.moneyballfm.moneyball_fm.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.moneyballfm.moneyball_fm.model.Player;
import org.moneyballfm.moneyball_fm.model.PlayerRowModel;
import org.moneyballfm.moneyball_fm.model.ScoutingPlayerModel;
import org.moneyballfm.moneyball_fm.model.TacticRole;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FmExportParser {

	public static final long UNVERKAEUFLICH_VALUE = 999_000_000L;

	public record PreseasonData(String name, String position, int estimatedMinutes, double rating) {}

	// ==========================================
	// 1. KADER-ANALYSE PARSER
	// ==========================================

	public static List<PlayerRowModel> parseFile(File file) throws IOException {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".html") || name.endsWith(".htm")) {
			return parseHtml(file);
		} else {
			return parsePlainTextFile(file);
		}
	}

	public static boolean isPreseasonFile(File file) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
			String line;
			int checkedLines = 0;
			while ((line = reader.readLine()) != null && checkedLines < 25) {
				String lower = line.toLowerCase();
				if (lower.contains("eins") && (lower.contains("ø note") || lower.contains("note"))
						&& !lower.contains("transferwert") && !lower.contains("marktwert") && !lower.contains("gehalt")) {
					return true;
				}
				checkedLines++;
			}
		} catch (Exception ignored) {}
		return false;
	}

	public static List<PreseasonData> parsePreseasonFile(File file) throws IOException {
		List<PreseasonData> result = new ArrayList<>();

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
							colMap.put(cleanHeaderKey(cols[i]), i);
							colMap.put(cleanNbsps(cols[i]).toLowerCase().trim(), i);
						}
					}
					continue;
				}

				String name = getVal(cols, colMap, "name", "person", "spieler");
				if (name.isBlank()) continue;

				String pos = getVal(cols, colMap, "ausgewählte position", "ausgew. pos.", "pos", "position");
				String einsStr = getVal(cols, colMap, "eins", "spiele", "apps");
				double rating = parseDouble(getVal(cols, colMap, "ø note", "note", "d-note", "drschn."));
				int minutes = calculateMinutesFromAppearances(einsStr);

				result.add(new PreseasonData(name, pos, minutes, rating));
			}
		}
		return result;
	}

	private static int calculateMinutesFromAppearances(String einsStr) {
		if (einsStr == null || einsStr.isBlank() || einsStr.equals("-")) return 0;

		int starts = 0;
		int subs = 0;

		if (einsStr.contains("(")) {
			String[] parts = einsStr.split("\\(");
			starts = parseInt(parts[0].trim());
			subs = parseInt(parts[1].replace(")", "").trim());
		} else {
			starts = parseInt(einsStr.trim());
		}

		return (starts * 75) + (subs * 30);
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
							colMap.put(cleanHeaderKey(cols[i]), i);
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
		Elements headers = headerRow.select("th, td");

		Map<String, Integer> colMap = new HashMap<>();
		for (int i = 0; i < headers.size(); i++) {
			colMap.put(cleanHeaderKey(headers.get(i).text()), i);
			colMap.put(cleanNbsps(headers.get(i).text()).toLowerCase().trim(), i);
		}

		for (int r = 1; r < rows.size(); r++) {
			Elements cells = rows.get(r).select("th, td");
			if (cells.isEmpty()) continue;
			String[] cols = cells.stream().map(c -> cleanNbsps(c.text()).trim()).toArray(String[]::new);
			PlayerRowModel model = extractRow(cols, colMap);
			if (model != null) result.add(model);
		}
		return result;
	}

	private static PlayerRowModel extractRow(String[] cols, Map<String, Integer> map) {
		String name = getVal(cols, map, "name", "person", "spieler");
		if (name.isBlank()) return null;

		String roleDetail = getVal(cols, map,
				"position/rolle/aufgabe",
				"position / rolle / aufgabe",
				"rolle/aufgabe",
				"rolle",
				"taktische rolle"
		);

		String rawPos = getVal(cols, map, "ausgewählte position", "ausgew. pos.", "ausgew. pos");
		String naturalPosRaw = getVal(cols, map, "position", "pos", "natürliche position");

		String targetPositionStr = "";

		boolean hasValidRoleDetail = !roleDetail.isBlank()
				&& !roleDetail.equals("-")
				&& !roleDetail.equals("—")
				&& !roleDetail.toLowerCase().contains("spielposition");

		boolean hasValidRawPos = !rawPos.isBlank()
				&& !rawPos.toUpperCase().matches("^[EB]\\d+$")
				&& !rawPos.equals("-")
				&& !rawPos.equals("—");

		if (hasValidRoleDetail) {
			targetPositionStr = roleDetail;
		} else if (hasValidRawPos) {
			targetPositionStr = rawPos;
		} else {
			if (naturalPosRaw != null && naturalPosRaw.contains(",")) {
				targetPositionStr = naturalPosRaw.split(",")[0].trim();
			} else if (naturalPosRaw != null && !naturalPosRaw.isBlank() && !naturalPosRaw.equals("-")) {
				targetPositionStr = naturalPosRaw;
			} else {
				targetPositionStr = "FVR";
			}
		}

		String cleanPos = cleanNbsps(targetPositionStr).replaceAll("[\\.]+$", "").trim();
		TacticRole tacticRole = TacticRole.fromPosition(cleanPos);
		if (tacticRole == null) {
			tacticRole = TacticRole.values()[0];
		}

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

	// ==========================================
	// 2. SCOUTING-PARSER
	// ==========================================

	public static List<ScoutingPlayerModel> parseScoutingFile(File file) throws IOException {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".html") || name.endsWith(".htm")) {
			return parseScoutingHtml(file);
		} else {
			return parseScoutingPlainText(file);
		}
	}

	private static List<ScoutingPlayerModel> parseScoutingHtml(File file) throws IOException {
		List<ScoutingPlayerModel> result = new ArrayList<>();
		Document doc = Jsoup.parse(file, "UTF-8");
		Element table = doc.selectFirst("table");
		if (table == null) return result;

		Elements rows = table.select("tr");
		if (rows.isEmpty()) return result;

		Element headerRow = rows.get(0);
		Elements headers = headerRow.select("th, td");

		Map<String, Integer> colMap = new HashMap<>();
		for (int i = 0; i < headers.size(); i++) {
			colMap.put(cleanHeaderKey(headers.get(i).text()), i);
			colMap.put(cleanNbsps(headers.get(i).text()).toLowerCase().trim(), i);
		}

		for (int r = 1; r < rows.size(); r++) {
			Elements cells = rows.get(r).select("td, th");
			if (cells.isEmpty()) continue;

			String[] cols = cells.stream()
					.map(c -> cleanNbsps(c.text()).trim())
					.toArray(String[]::new);

			ScoutingPlayerModel model = extractScoutingRow(cols, colMap);
			if (model != null) {
				result.add(model);
			}
		}
		return result;
	}

	private static String cleanHeaderKey(String s) {
		if (s == null) return "";
		return cleanNbsps(s).toLowerCase().replace(" ", "").replace("/", "").replace("%", "").replace(".", "").trim();
	}

	private static List<ScoutingPlayerModel> parseScoutingPlainText(File file) throws IOException {
		List<ScoutingPlayerModel> result = new ArrayList<>();

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

				if (!colsList.isEmpty() && colsList.get(0).isEmpty()) colsList.remove(0);
				if (!colsList.isEmpty() && colsList.get(colsList.size() - 1).isEmpty()) colsList.remove(colsList.size() - 1);

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
							colMap.put(cleanHeaderKey(cols[i]), i);
							colMap.put(cleanNbsps(cols[i]).toLowerCase().trim(), i);
						}
					}
					continue;
				}

				ScoutingPlayerModel model = extractScoutingRow(cols, colMap);
				if (model != null) result.add(model);
			}
		}
		return result;
	}

	private static ScoutingPlayerModel extractScoutingRow(String[] cols, Map<String, Integer> map) {
		String name = getVal(cols, map, "name", "person", "spieler");
		if (name.isBlank() || name.equals("- -") || name.equals("-") || name.equals("—")) return null;

		if (name.contains(" - ")) {
			name = name.split(" - ")[0].trim();
		}

		String roleDetail = getVal(cols, map,
				"position/rolle/aufgabe",
				"position / rolle / aufgabe",
				"rolle/aufgabe",
				"rolle",
				"taktische rolle"
		);
		String rawPos = getVal(cols, map, "ausgewählte position", "ausgew. pos.", "ausgew. pos");
		String naturalPosRaw = getVal(cols, map, "position", "pos", "natürliche position");

		String targetPositionStr = "";
		boolean hasValidRoleDetail = !roleDetail.isBlank() && !roleDetail.equals("-") && !roleDetail.equals("—");
		boolean hasValidRawPos = !rawPos.isBlank() && !rawPos.toUpperCase().matches("^[EB]\\d+$") && !rawPos.equals("-") && !rawPos.equals("—");

		if (hasValidRoleDetail) {
			targetPositionStr = roleDetail;
		} else if (hasValidRawPos) {
			targetPositionStr = rawPos;
		} else if (!naturalPosRaw.isBlank() && !naturalPosRaw.equals("-")) {
			targetPositionStr = naturalPosRaw.contains(",") ? naturalPosRaw.split(",")[0].trim() : naturalPosRaw;
		} else {
			targetPositionStr = "FVR";
		}

		String cleanPos = cleanNbsps(targetPositionStr).replaceAll("[\\.]+$", "").trim();
		TacticRole tacticRole = TacticRole.fromPosition(cleanPos);
		String resolvedRole = (tacticRole != null) ? tacticRole.getDisplayName() : "Stoßstürmer (Angreifen)";

		ScoutingPlayerModel p = new ScoutingPlayerModel();
		p.nameProperty().set(name);
		p.targetRoleProperty().set(resolvedRole);
		p.clubProperty().set(getVal(cols, map, "verein", "club"));
		p.positionsProperty().set(naturalPosRaw);

		p.ageProperty().set(parseInt(getVal(cols, map, "alter", "age")));
		p.askingPriceProperty().set(parseMoney(getVal(cols, map, "transferwert", "marktwert", "wert")));
		p.wageDemandProperty().set(parseMoney(getVal(cols, map, "gehalt", "monatsgehalt")));
		p.contractYearsProperty().set(parseContractYears(getVal(cols, map, "endet", "vertrag")));
		p.minutesProperty().set(parseInt(getVal(cols, map, "min.", "minuten", "min")));

		// Physis & Mentale Werte
		p.agilityProperty().set(parseInt(getVal(cols, map, "bew", "beweglichkeit")));
		p.paceProperty().set(parseInt(getVal(cols, map, "ant", "antritt")));
		p.staminaProperty().set(parseInt(getVal(cols, map, "aus", "ausdauer")));
		p.determinationProperty().set(parseInt(getVal(cols, map, "zie", "entsch", "zielstrebigkeit", "entschlossenheit")));

		// Detaillierte Leistungsdaten
		double prP = parseDouble(getVal(cols, map, "pr pässe/90", "pr päs/90", "pr pässe", "prpaesse90", "prpaesse"));
		double xg = parseDouble(getVal(cols, map, "xg/90", "xg", "xg90"));
		double xa = parseDouble(getVal(cols, map, "xa/90", "xa", "xa90"));
		double schussPct = parseDouble(getVal(cols, map, "schüsse %", "schuss %", "schuesse"));
		double verwPct = parseDouble(getVal(cols, map, "verw. %", "verw %", "verw"));
		double passPct = parseDouble(getVal(cols, map, "pas %", "pass %", "pas", "pass"));
		double xgVerh = parseDouble(getVal(cols, map, "xg verh/90", "xgverh90"));
		double parPct = parseDouble(getVal(cols, map, "par %", "par"));
		double abb90 = parseDouble(getVal(cols, map, "abb/90", "abb90"));
		double fpqPct = parseDouble(getVal(cols, map, "fpq %", "fpq"));
		int tore = parseInt(getVal(cols, map, "tore", "goals"));
		int vor = parseInt(getVal(cols, map, "vor", "vorlagen", "assists"));
		double note = parseDouble(getVal(cols, map, "ø note", "note", "d-note", "onote"));
		double kopfPct = parseDouble(getVal(cols, map, "kopf %", "kopf"));
		int tfeh = parseInt(getVal(cols, map, "t feh", "tfeh"));
		double drb = parseDouble(getVal(cols, map, "drb/90", "drb90"));
		double fla90 = parseDouble(getVal(cols, map, "vers fla/90", "fla/90", "versfla90"));
		double zwkPct = parseDouble(getVal(cols, map, "zwk %", "zwk"));
		int abbTotal = parseInt(getVal(cols, map, "abb"));
		int prPTotal = parseInt(getVal(cols, map, "pr pässe", "prpaesse"));
		int entP = parseInt(getVal(cols, map, "ent päs", "entsch pässe", "entpaes"));

		p.progressivePasses90Property().set(prP);
		p.xg90Property().set(xg);
		p.xa90Property().set(xa);
		p.shotsOnTargetPctProperty().set(schussPct);
		p.shotConversionPctProperty().set(verwPct);
		p.passCompletionPctProperty().set(passPct);
		p.xgPrevented90Property().set(xgVerh);
		p.savePctProperty().set(parPct);
		p.interceptions90Property().set(abb90);
		p.crossAccuracyPctProperty().set(fpqPct);
		p.goalsProperty().set(tore);
		p.assistsProperty().set(vor);
		p.averageRatingProperty().set(note);
		p.headersWonPctProperty().set(kopfPct);
		p.mistakesLeadingToGoalProperty().set(tfeh);
		p.dribbles90Property().set(drb);
		p.crossesAttempted90Property().set(fla90);
		p.tacklesWonPctProperty().set(zwkPct);
		p.interceptionsTotalProperty().set(abbTotal);
		p.progressivePassesTotalProperty().set(prPTotal);
		p.keyPassesTotalProperty().set(entP);

		// Initiale Fallback-Belegung für Stats 1-3
		p.statValue1Property().set(xg > 0 ? xg : (passPct > 0 ? passPct / 100.0 : prP));
		p.statValue2Property().set(xa > 0 ? xa : (zwkPct > 0 ? zwkPct / 100.0 : abb90));
		p.statValue3Property().set(schussPct > 0 ? schussPct / 100.0 : (drb > 0 ? drb : note));

		return p;
	}

	// ==========================================
	// 3. COMMON HILFSMETHODEN
	// ==========================================

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

			String normalizedKey = cleanHeaderKey(k);
			Integer normIdx = map.get(normalizedKey);
			if (normIdx != null && normIdx < cols.length) {
				return cols[normIdx].trim();
			}
		}
		return "";
	}

	private static int parseInt(String val) {
		if (val == null || val.isBlank() || val.equals("-") || val.equals("—")) return 0;
		String c = cleanNbsps(val).replace(".", "").replaceAll("[^0-9]", "");
		return c.isEmpty() ? 0 : Integer.parseInt(c);
	}

	private static double parseDouble(String val) {
		if (val == null || val.isBlank() || val.equals("-") || val.equals("—")) return 0.0;
		String c = cleanNbsps(val).replace("%", "").replace(",", ".").replaceAll("[^0-9.-]", "");
		if (c.isEmpty() || c.equals("-")) return 0.0;
		try {
			return Double.parseDouble(c);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	private static long parseMoney(String val) {
		if (val == null || val.isBlank() || val.equals("-") || val.equals("—")) return 0L;
		String clean = cleanNbsps(val).toLowerCase().replace("€", "").trim();

		// Alle Varianten von "Unverkäuflich" & "Nicht zum Verkauf"
		if (clean.contains("unverkäuflich")
				|| clean.contains("unverkaeuflich")
				|| clean.contains("nicht zum verkauf")
				|| clean.contains("nicht zu verkaufen")
				|| clean.contains("not for sale")) {
			return UNVERKAEUFLICH_VALUE;
		}

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