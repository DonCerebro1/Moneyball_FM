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
import java.util.*;

public class FmExportParser {

	public static final long UNVERKAEUFLICH_VALUE = 999_000_000L;

	public record PreseasonData(String name, String position, int estimatedMinutes, double rating) {}

	private record RawPlayerData(
			String[] cols,
			Map<String, Integer> colMap,
			String name,
			String roleDetail,
			String rawPos,
			String naturalPos,
			boolean isStarter
	) {}

	// ==========================================
	// 1. KADER-ANALYSE PARSER (TWO-PASS)
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
		Set<String> seen = new HashSet<>();

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

				String name = getVal(cols, colMap, "name", "person", "spieler");
				if (name.isBlank()) continue;

				String key = name.trim().toLowerCase();
				if (seen.contains(key)) continue;
				seen.add(key);

				String pos = getVal(cols, colMap, "ausgewählte position", "ausgew. pos.", "pos", "position");
				String einsStr = getVal(cols, colMap, "eins", "spiele", "apps");
				double rating = parseDouble(getVal(cols, mapOrEmpty(colMap), "ø note", "note", "d-note", "drschn."));
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
		List<RawPlayerData> rawList = new ArrayList<>();

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

				String name = getVal(cols, colMap, "name", "person", "spieler");
				if (name.isBlank()) continue;

				String roleDetail = getVal(cols, colMap, "position/rolle/aufgabe", "position / rolle / aufgabe", "rolle/aufgabe", "rolle", "taktische rolle");
				String rawPos = getVal(cols, colMap, "ausgewählte position", "ausgew. pos.", "ausgew. pos");
				String naturalPos = getVal(cols, colMap, "position", "pos", "natürliche position");

				boolean isStarter = isStartingPosition(rawPos);
				rawList.add(new RawPlayerData(cols, colMap, name, roleDetail, rawPos, naturalPos, isStarter));
			}
		}

		return processTwoPassHierarchy(rawList);
	}

	private static List<PlayerRowModel> parseHtml(File file) throws IOException {
		List<RawPlayerData> rawList = new ArrayList<>();

		Document doc = Jsoup.parse(file, "UTF-8");
		Element table = doc.selectFirst("table");
		if (table == null) return Collections.emptyList();

		Elements rows = table.select("tr");
		if (rows.isEmpty()) return Collections.emptyList();

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

			String name = getVal(cols, colMap, "name", "person", "spieler");
			if (name.isBlank()) continue;

			String roleDetail = getVal(cols, colMap, "position/rolle/aufgabe", "position / rolle / aufgabe", "rolle/aufgabe", "rolle", "taktische rolle");
			String rawPos = getVal(cols, colMap, "ausgewählte position", "ausgew. pos.", "ausgew. pos");
			String naturalPos = getVal(cols, colMap, "position", "pos", "natürliche position");

			boolean isStarter = isStartingPosition(rawPos);
			rawList.add(new RawPlayerData(cols, colMap, name, roleDetail, rawPos, naturalPos, isStarter));
		}

		return processTwoPassHierarchy(rawList);
	}

	private static boolean isStartingPosition(String rawPos) {
		if (rawPos == null || rawPos.isBlank()) return false;
		String p = rawPos.trim().toUpperCase();
		if (p.matches("^[EB]\\d+$") || p.equals("-") || p.equals("—")) return false;
		return p.startsWith("OM") || p.startsWith("DM") || p.startsWith("V") || p.startsWith("FV") || p.startsWith("ST") || p.startsWith("TW") || p.startsWith("M");
	}

	// =========================================================================
	// TWO-PASS MAPPING LOGIK
	// =========================================================================

	private static List<PlayerRowModel> processTwoPassHierarchy(List<RawPlayerData> rawList) {
		List<PlayerRowModel> result = new ArrayList<>();
		Set<String> seenKeys = new HashSet<>();

		Map<TacticRole, List<String>> starterRoleMap = new HashMap<>();
		Map<RawPlayerData, String> resolvedRoles = new HashMap<>();

		for (RawPlayerData raw : rawList) {
			if (raw.isStarter()) {
				String preciseRole = resolveStarterRoleWithSide(raw.roleDetail(), raw.rawPos(), raw.naturalPos());
				resolvedRoles.put(raw, preciseRole);

				TacticRole baseRole = resolveRole(raw.roleDetail(), raw.rawPos(), raw.naturalPos());
				starterRoleMap.computeIfAbsent(baseRole, k -> new ArrayList<>()).add(preciseRole);
			}
		}

		for (RawPlayerData raw : rawList) {
			String finalRoleName;

			if (raw.isStarter()) {
				finalRoleName = resolvedRoles.get(raw);
			} else {
				finalRoleName = resolveBenchRoleHierarchy(raw.roleDetail(), raw.naturalPos(), starterRoleMap);
			}

			PlayerRowModel model = buildPlayerModel(raw.cols(), raw.colMap(), finalRoleName, raw.rawPos(), raw.naturalPos());
			if (model != null) {
				String key = model.nameProperty().get().trim().toLowerCase() + "_" + model.ageProperty().get();
				if (!seenKeys.contains(key)) {
					seenKeys.add(key);
					result.add(model);
				}
			}
		}

		return result;
	}

	private static String resolveStarterRoleWithSide(String roleDetail, String selectedPos, String naturalPos) {
		TacticRole tr = resolveRole(roleDetail, selectedPos, naturalPos);
		String sel = (selectedPos != null) ? selectedPos.trim().toUpperCase() : "";

		if (sel.contains("OMZL")) return "Offensiver Mittelfeldspieler Links";
		if (sel.contains("OMZR")) return "Offensiver Mittelfeldspieler Rechts";

		if (sel.contains("FVL")) return "Flügelverteidiger Links";
		if (sel.contains("FVR")) return tr == TacticRole.KFV ? "Komp. Flügelverteidiger Rechts" : "Flügelverteidiger Rechts";

		if (sel.contains("VZL")) return "Ballspielender Vert. Links";
		if (sel.contains("VZR")) return "Halbraumverteidiger Rechts";
		if (sel.equals("VZ")) return "Innenverteidiger Zentral";

		if (sel.contains("DMZR")) return "Regista (Zentral Rechts)";
		if (sel.contains("DMZL")) return "Zurückgezogener Spielmacher";

		return tr.getDisplayName();
	}

	private static String resolveBenchRoleHierarchy(String roleDetail, String naturalPos, Map<TacticRole, List<String>> starterRoleMap) {
		TacticRole baseRole = resolveRole(roleDetail, "", naturalPos);

		List<String> starterRoles = starterRoleMap.get(baseRole);

		if (starterRoles != null && starterRoles.size() == 1) {
			return starterRoles.get(0);
		}

		if (starterRoles != null && starterRoles.size() > 1) {
			String nat = (naturalPos != null) ? naturalPos.toUpperCase() : "";
			boolean canLeft = nat.contains("(L)") || nat.contains("(LZ)") || nat.contains("(RL)");
			boolean canRight = nat.contains("(R)") || nat.contains("(RZ)") || nat.contains("(RL)");

			for (String sRole : starterRoles) {
				if (sRole.contains("Links") && canLeft && !canRight) return sRole;
				if (sRole.contains("Rechts") && canRight && !canLeft) return sRole;
			}

			if (canLeft) {
				for (String sRole : starterRoles) {
					if (sRole.contains("Links")) return sRole;
				}
			}
			if (canRight) {
				for (String sRole : starterRoles) {
					if (sRole.contains("Rechts")) return sRole;
				}
			}
			return starterRoles.get(0);
		}

		return baseRole.getDisplayName();
	}

	private static PlayerRowModel buildPlayerModel(String[] cols, Map<String, Integer> map, String roleDisplayName, String rawPos, String naturalPosRaw) {
		String name = getVal(cols, map, "name", "person", "spieler");
		if (name.isBlank()) return null;

		if (name.contains("\n")) name = name.split("\n")[0].trim();
		if (name.contains(" - ")) name = name.split(" - ")[0].trim();

		TacticRole tacticRole = TacticRole.fromDisplayName(roleDisplayName);
		if (tacticRole == null) tacticRole = resolveRole(roleDisplayName, rawPos, naturalPosRaw);

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

		String rawEndDate = getVal(cols, map, "endet", "vertrag", "auslaufend", "expires");
		double contractYears = parseContractYears(rawEndDate);
		double loanRating = parseDouble(getVal(cols, map, "ø note", "note", "d-note", "drschn."));
		int goals = parseInt(getVal(cols, map, "tore", "goals"));
		int assists = parseInt(getVal(cols, map, "vor", "vorlagen", "assists"));

		String injury = getVal(cols, map, "verletzungsanfälligkeit", "verletzungsrisiko", "verletzungsanfälligkeit (fitness und verletzungen)", "injury susceptibility");
		if (injury.isBlank() || injury.equals("-")) injury = "Normal";

		Player player = new Player(name, roleDisplayName, age, value, salary, contractYears, bew, ant, aus, zie, minutes, art, club);
		player.setLeague(league);
		player.setNaturalPosition(naturalPosRaw);
		player.setLoanRating(loanRating);
		player.setGoals(goals);
		player.setAssists(assists);
		player.setInjurySusceptibility(injury);
		player.setContractEndDate(rawEndDate);

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			int idx = entry.getValue();
			if (idx < cols.length) {
				double val = parseDouble(cols[idx]);
				player.putStat(entry.getKey(), val);
			}
		}

		double s1 = parseStatValue(cols, map, tacticRole.getStat1Header());
		double s2 = parseStatValue(cols, map, tacticRole.getStat2Header());
		double s3 = parseStatValue(cols, map, tacticRole.getStat3Header());

		PlayerRowModel row = new PlayerRowModel(player, s1, s2, s3);
		row.setStatLabels(tacticRole.getStat1Header(), tacticRole.getStat2Header(), tacticRole.getStat3Header());

		return row;
	}

	private static double parseStatValue(String[] cols, Map<String, Integer> map, String header) {
		if (header == null || header.isBlank()) return 0.0;
		String h = header.toLowerCase().trim();

		if (h.contains("e pä/90") || h.contains("ent päs")) {
			return parseDouble(getVal(cols, map, "e pä/90", "e pä/90 min", "ent päs/90", "ent päs", "entsch pässe"));
		}
		if (h.contains("pr pässe")) {
			return parseDouble(getVal(cols, map, "pr pässe/90", "pr päs/90", "pr pässe"));
		}
		if (h.contains("xg verh")) {
			return parseDouble(getVal(cols, map, "xg verh/90", "xgverh90"));
		}
		if (h.contains("xg/90")) {
			return parseDouble(getVal(cols, map, "xg/90", "xg", "xg90"));
		}
		if (h.contains("xa/90")) {
			return parseDouble(getVal(cols, map, "xa/90", "xa", "xa90"));
		}
		if (h.contains("par %")) {
			return parseDouble(getVal(cols, map, "par %", "par"));
		}
		if (h.contains("abb/90")) {
			return parseDouble(getVal(cols, map, "abb/90", "abb90"));
		}
		if (h.contains("fpq %")) {
			return parseDouble(getVal(cols, map, "fpq %", "fpq"));
		}
		if (h.contains("kopf %")) {
			return parseDouble(getVal(cols, map, "kopf %", "kopf"));
		}
		if (h.contains("zwk %")) {
			return parseDouble(getVal(cols, map, "zwk %", "zwk"));
		}
		if (h.contains("pas %")) {
			return parseDouble(getVal(cols, map, "pas %", "pass %", "pas"));
		}
		if (h.contains("drb/90")) {
			return parseDouble(getVal(cols, map, "drb/90", "drb90"));
		}
		if (h.contains("schüsse %")) {
			return parseDouble(getVal(cols, map, "schüsse %", "schuss %"));
		}
		if (h.contains("verw. %")) {
			return parseDouble(getVal(cols, map, "verw. %", "verw %", "verw"));
		}

		return parseDouble(getVal(cols, map, header));
	}

	private static TacticRole resolveRole(String roleDetail, String selectedPos, String naturalPos) {
		if (roleDetail != null && !roleDetail.isBlank() && !roleDetail.equals("-") && !roleDetail.equals("—")) {
			String cleaned = cleanRoleText(roleDetail);
			TacticRole tr = matchTacticRoleByKeyword(cleaned);
			if (tr != null) return tr;

			tr = TacticRole.fromDisplayName(cleaned);
			if (tr != null && tr != TacticRole.ZSM) return tr;

			tr = TacticRole.fromPosition(cleaned);
			if (tr != null && tr != TacticRole.ZSM) return tr;
		}

		if (selectedPos != null && !selectedPos.isBlank() && !selectedPos.matches("^[EB]\\d+$")) {
			String cleaned = cleanRoleText(selectedPos);
			TacticRole tr = matchTacticRoleByKeyword(cleaned);
			if (tr != null) return tr;

			tr = TacticRole.fromPosition(cleaned);
			if (tr != null && tr != TacticRole.ZSM) return tr;
		}

		if (naturalPos != null && !naturalPos.isBlank() && !naturalPos.equals("-")) {
			String firstPos = naturalPos.contains(",") ? naturalPos.split(",")[0].trim() : naturalPos;
			String cleaned = cleanRoleText(firstPos);
			TacticRole tr = matchTacticRoleByKeyword(cleaned);
			if (tr != null) return tr;

			tr = TacticRole.fromPosition(cleaned);
			if (tr != null && tr != TacticRole.ZSM) return tr;
		}

		return TacticRole.ZSM;
	}

	private static String cleanRoleText(String raw) {
		if (raw == null) return "";
		return raw
				.replaceAll("\\s*\\([^)]*\\)", "")
				.replaceAll("(?i)\\s*-\\s*(unterstützen|verteidigen|angreifen|angriff|abdecken|vorstossen|halten|rückendeckung|vorstopper).*", "")
				.replaceAll("[\\.]+$", "")
				.trim();
	}

	private static TacticRole matchTacticRoleByKeyword(String text) {
		if (text == null || text.isBlank()) return null;
		String lower = text.toLowerCase().trim();

		if (lower.contains("torwart") || lower.contains("torhüter") || lower.equals("tw") || lower.contains("mtw") || lower.equals("gk")) {
			return lower.contains("mit") ? TacticRole.MTW : TacticRole.TW;
		}
		if (lower.contains("komp") && (lower.contains("flügel") || lower.contains("flugel") || lower.contains("kfv"))) {
			return TacticRole.KFV;
		}
		if (lower.contains("invers") && (lower.contains("flügelvert") || lower.contains("ifv"))) {
			return TacticRole.IFV;
		}
		if (lower.contains("invers") && (lower.contains("aussenvert") || lower.contains("iav"))) {
			return TacticRole.IAV;
		}
		if (lower.contains("flügelvert") || lower.contains("flugelvert") || lower.equals("fv") || lower.equals("wb")) {
			return TacticRole.FV;
		}
		if (lower.contains("außenvert") || lower.contains("aussenvert") || lower.equals("av") || lower.equals("fb")) {
			return TacticRole.AV;
		}
		if (lower.contains("ballspiel") || lower.contains("bsv") || lower.contains("bpd")) {
			return TacticRole.BSV;
		}
		if (lower.contains("halbraum") || lower.contains("hrv") || lower.contains("wcb")) {
			return TacticRole.HRV;
		}
		if (lower.contains("libero") || lower.equals("lib")) {
			return TacticRole.LIBERO;
		}
		if (lower.contains("komp") && lower.contains("innen")) {
			return TacticRole.KIV;
		}
		if (lower.contains("innenvert") || lower.equals("iv") || lower.startsWith("iv ") || lower.startsWith("iv-")
				|| lower.equals("vz") || lower.equals("cb") || lower.equals("dc")) {
			return TacticRole.IV;
		}
		if (lower.contains("regista") || lower.equals("reg") || lower.contains("dmzr")) {
			return TacticRole.REGISTA;
		}
		if (lower.contains("zurückgezogener") || lower.contains("zsm") || lower.contains("dlp")) {
			return TacticRole.ZSM;
		}
		if (lower.contains("vertikaler") || lower.contains("vsm") || lower.contains("rpm")) {
			return TacticRole.VERTIKALER_SPIELMACHER;
		}
		if (lower.contains("volante") || lower.contains("vol")) {
			return TacticRole.SEGUNDO_VOLANTE;
		}
		if (lower.contains("abräumer") || lower.equals("ab") || lower.contains("anchor")) {
			return TacticRole.ABRAEUMER;
		}
		if (lower.contains("tiefer sechs") || lower.equals("ts")) {
			return TacticRole.TIEFER_SECHSER;
		}
		if (lower.contains("ballerober") || lower.contains("bms") || lower.contains("bwm")) {
			return TacticRole.BALLEROBERNDER_MITTELFELDSSPIELER;
		}
		if (lower.contains("defensiv") && (lower.contains("mittel") || lower.equals("dm"))) {
			return TacticRole.DEFENSIVER_MITTELFELDSSPIELER;
		}
		if (lower.contains("box to box") || lower.contains("b2b") || lower.contains("bbm")) {
			return TacticRole.BOX_TO_BOX;
		}
		if (lower.contains("carrilero") || lower.equals("car")) {
			return TacticRole.CARRILERO;
		}
		if (lower.contains("mezzala") || lower.equals("mez")) {
			return TacticRole.MEZZALA;
		}
		if (lower.contains("vorgeschob") || lower.contains("vosm") || lower.equals("ap")) {
			return TacticRole.VORGESCHOBENER_SPIELMACHER;
		}
		if (lower.equals("zm") || lower.equals("cm") || lower.contains("zentraler mittel")) {
			return TacticRole.ZENTRALER_MITTELFELDSSPIELER;
		}
		if (lower.contains("invers") && (lower.contains("flügel") || lower.equals("ifl") || lower.equals("iw"))) {
			return TacticRole.INVERSER_FLUEGELSPIELER;
		}
		if (lower.contains("invers") && (lower.contains("stürmer") || lower.contains("aussenst") || lower.equals("ias") || lower.equals("if"))) {
			return TacticRole.INVERSER_AUSSENSTUERMER;
		}
		if (lower.contains("defensiv") && lower.contains("flügel")) {
			return TacticRole.DEFENSIVER_FLUEGELSPIELER;
		}
		if (lower.contains("flügel") || lower.equals("flü") || lower.equals("w")) {
			return TacticRole.FLUEGELSPIELER;
		}
		if (lower.contains("schatten") || lower.equals("ss")) {
			return TacticRole.SCHATTENSTUERMER;
		}
		if (lower.contains("raumdeuter") || lower.equals("rd")) {
			return TacticRole.RAUMDEUTER;
		}
		if (lower.contains("trequart") || lower.equals("treq")) {
			return TacticRole.TREQUARTISTA;
		}
		if (lower.contains("enganche") || lower.equals("eng")) {
			return TacticRole.ENGANCHE;
		}
		if (lower.contains("offensiv") || lower.equals("om") || lower.startsWith("om ") || lower.startsWith("om-") || lower.contains("omz") || lower.equals("am")) {
			return TacticRole.OFFENSIVER_MITTELFELDSSPIELER;
		}
		if (lower.contains("knipser") || lower.equals("kni") || lower.equals("poacher")) {
			return TacticRole.KNIPSER;
		}
		if (lower.contains("pressend") || lower.equals("ps") || lower.equals("pf")) {
			return TacticRole.PRESSENDER_STUERMER;
		}
		if (lower.contains("falsche neun") || lower.equals("f9")) {
			return TacticRole.FALSCHE_NEUN;
		}
		if (lower.contains("hängend") || lower.equals("hs") || lower.equals("dlf")) {
			return TacticRole.HAENGENDE_SPITZE;
		}
		if (lower.contains("komp") && lower.contains("stürm")) {
			return TacticRole.KOMPLETTER_STUERMER;
		}
		if (lower.contains("ziel") && lower.contains("stürm")) {
			return TacticRole.ZIELSPIELER;
		}
		if (lower.contains("stoßstürmer") || lower.contains("stossstürmer") || lower.equals("sts") || lower.equals("af") || lower.equals("st") || lower.contains("stürmer")) {
			return TacticRole.STOESSTUERMER;
		}

		return null;
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
		Set<String> seenKeys = new HashSet<>();

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
				String key = model.nameProperty().get().trim().toLowerCase() + "_" + model.ageProperty().get();
				if (!seenKeys.contains(key)) {
					seenKeys.add(key);
					result.add(model);
				}
			}
		}
		return result;
	}

	private static String cleanHeaderKey(String s) {
		if (s == null) return "";
		return cleanNbsps(s).toLowerCase().replace(" ", "").replace("/", "").replace("%", "").replace(".", "").replace("(", "").replace(")", "").trim();
	}

	private static List<ScoutingPlayerModel> parseScoutingPlainText(File file) throws IOException {
		List<ScoutingPlayerModel> result = new ArrayList<>();
		Set<String> seenKeys = new HashSet<>();

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
				if (model != null) {
					String key = model.nameProperty().get().trim().toLowerCase() + "_" + model.ageProperty().get();
					if (!seenKeys.contains(key)) {
						seenKeys.add(key);
						result.add(model);
					}
				}
			}
		}
		return result;
	}

	private static ScoutingPlayerModel extractScoutingRow(String[] cols, Map<String, Integer> map) {
		String name = getVal(cols, map, "name", "person", "spieler");
		if (name.isBlank() || name.equals("- -") || name.equals("-") || name.equals("—")) return null;

		if (name.contains("\n")) name = name.split("\n")[0].trim();
		if (name.contains(" - ")) name = name.split(" - ")[0].trim();

		String naturalPosRaw = getVal(cols, map, "position", "pos", "natürliche position");
		String firstPos = (!naturalPosRaw.isBlank() && naturalPosRaw.contains(","))
				? naturalPosRaw.split(",")[0].trim() : naturalPosRaw;

		String roleDetail = getVal(cols, map, "position/rolle/aufgabe", "position / rolle / aufgabe", "rolle/aufgabe", "rolle", "taktische rolle");
		String rawPos = getVal(cols, map, "ausgewählte position", "ausgew. pos.", "ausgew. pos");

		TacticRole tacticRole = resolveRole(firstPos, rawPos, roleDetail);
		String resolvedRole = (tacticRole != null) ? tacticRole.getDisplayName() : "Ballspielender Verteidiger";

		String rawEndDate = getVal(cols, map, "endet", "vertrag", "auslaufend", "expires");

		String league = getVal(cols, map, "liga", "wettbewerb", "division", "league", "comp");
		String clubRaw = getVal(cols, map, "verein", "club");
		String club = clubRaw;

		if (league.isBlank() && clubRaw.contains(" - ")) {
			String[] clubParts = clubRaw.split(" - ");
			club = clubParts[0].trim();
			league = clubParts[1].trim();
		} else if (clubRaw.contains(" - ")) {
			club = clubRaw.split(" - ")[0].trim();
		}

		ScoutingPlayerModel p = new ScoutingPlayerModel();
		p.nameProperty().set(name);
		p.targetRoleProperty().set(resolvedRole);
		p.clubProperty().set(club);
		p.setLeague(league);
		p.positionsProperty().set(naturalPosRaw);

		p.ageProperty().set(parseInt(getVal(cols, map, "alter", "age")));
		p.askingPriceProperty().set(parseMoney(getVal(cols, map, "transferwert", "marktwert", "wert")));
		p.wageDemandProperty().set(parseMoney(getVal(cols, map, "gehalt", "monatsgehalt")));
		p.contractYearsProperty().set(parseContractYears(rawEndDate));
		p.minutesProperty().set(parseInt(getVal(cols, map, "min.", "minuten", "min")));

		p.agilityProperty().set(parseInt(getVal(cols, map, "bew", "beweglichkeit", "bewegung")));
		p.paceProperty().set(parseInt(getVal(cols, map, "ant", "antritt")));
		p.staminaProperty().set(parseInt(getVal(cols, map, "aus", "ausdauer")));
		p.determinationProperty().set(parseInt(getVal(cols, map, "zie", "entsch", "zielstrebigkeit", "entschlossenheit")));

		String injury = getVal(cols, map, "verletzungsrisiko", "verletzungsanfälligkeit", "verletzungsanfälligkeit (fitness und verletzungen)", "injury susceptibility");
		if (injury.isBlank() || injury.equals("-")) injury = "Normal";
		p.setInjurySusceptibility(injury);

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

		double entP90 = parseDouble(getVal(cols, map, "e pä/90", "e pä/90 min", "ent päs/90", "entsch pässe/90", "key passes/90", "k ps/90"));
		int entPTotal = parseInt(getVal(cols, map, "ent päs", "entsch pässe", "key passes", "k ps"));

		p.progressivePasses90Property().set(prP);
		p.xg90Property().set(xg);
		p.xa90Property().set(xa);
		p.keyPasses90Property().set(entP90 > 0.0 ? entP90 : (double) entPTotal);
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
		p.keyPassesTotalProperty().set(entPTotal);

		p.statValue1Property().set(xg > 0 ? xg : (passPct > 0 ? passPct / 100.0 : prP));
		p.statValue2Property().set(xa > 0 ? xa : (zwkPct > 0 ? zwkPct / 100.0 : abb90));
		p.statValue3Property().set(schussPct > 0 ? schussPct / 100.0 : (drb > 0 ? drb : note));

		return p;
	}

	private static Map<String, Integer> mapOrEmpty(Map<String, Integer> m) {
		return m != null ? m : Collections.emptyMap();
	}

	private static String cleanNbsps(String s) {
		if (s == null) return "";
		return s.replace('\u00A0', ' ').replace("&nbsp;", " ").trim();
	}

	public static double parseContractYears(String dateStr) {
		return calculateContractYears(dateStr, 2037, true);
	}

	public static double calculateContractYears(String dateStr, int baseYear, boolean isWinter) {
		if (dateStr == null || dateStr.isBlank() || dateStr.equals("-") || dateStr.equals("—")) {
			return 2.0;
		}
		try {
			String delimiter = dateStr.contains(".") ? "\\." : "/";
			String[] parts = dateStr.split(delimiter);

			if (parts.length >= 3) {
				int month = Integer.parseInt(parts[1].trim());
				int endYear = Integer.parseInt(parts[2].trim());

				double remaining = endYear - baseYear;
				if (isWinter) {
					remaining -= 0.5;
				}

				if (month <= 7) {
					return Math.max(0.0, remaining);
				} else {
					return Math.max(0.0, remaining + 0.5);
				}
			}
		} catch (Exception ignored) {}

		return 2.0;
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
		String clean = cleanNbsps(val).trim();

		// Spannen wie "12 - 14" oder "11-13" erkennen und mitteln
		if (clean.contains("-")) {
			String[] parts = clean.split("-");
			if (parts.length == 2) {
				int min = parseSingleInt(parts[0].trim());
				int max = parseSingleInt(parts[1].trim());
				if (min > 0 && max > 0) {
					return (int) Math.round((min + max) / 2.0);
				} else if (max > 0) {
					return max;
				} else {
					return min;
				}
			}
		}

		return parseSingleInt(clean);
	}

	private static int parseSingleInt(String s) {
		String c = cleanNbsps(s).replace(".", "").replaceAll("[^0-9]", "");
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