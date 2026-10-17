package org.moneyballfm.moneyball_fm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.moneyballfm.moneyball_fm.config.AppConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NationCoefficientsImporter {

	public static boolean isNationRankingFile(File file) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
			String line;
			int checked = 0;
			while ((line = reader.readLine()) != null && checked < 25) {
				String lower = line.toLowerCase();
				if (lower.contains("5-jahreswertung")
						|| (lower.contains("koeff.") && lower.contains("nation"))
						|| (lower.contains("pos") && lower.contains("nation") && lower.contains("37/38"))) {
					return true;
				}
				checked++;
			}
		} catch (Exception ignored) {}
		return false;
	}

	public static int importAndGenerateLeagueTiers(File file, AppConfig config) throws Exception {
		Document doc = Jsoup.parse(file, "UTF-8");
		Elements rows = doc.select("table tr");
		if (rows.isEmpty()) return 0;

		Element headerRow = rows.get(0);
		Elements headers = headerRow.select("th, td");
		int nationIdx = -1;
		int pointsIdx = -1;

		for (int i = 0; i < headers.size(); i++) {
			String h = headers.get(i).text().toLowerCase().trim();
			if (h.contains("nation") || h.contains("land")) {
				nationIdx = i;
			} else if (h.contains("koeff") || h.contains("punkte") || h.contains("gesamt")) {
				pointsIdx = i;
			}
		}

		if (nationIdx == -1) nationIdx = 1;
		if (pointsIdx == -1) pointsIdx = headers.size() - 1;

		Map<String, Double> nationScores = new LinkedHashMap<>();
		double maxPoints = 0.0;

		for (int r = 1; r < rows.size(); r++) {
			Elements cells = rows.get(r).select("td, th");
			if (cells.size() <= Math.max(nationIdx, pointsIdx)) continue;

			String nation = cells.get(nationIdx).text().trim().toLowerCase();
			String ptsRaw = cells.get(pointsIdx).text().trim()
					.replace(".", "").replace(",", ".").replaceAll("[^0-9.]", "");

			if (nation.isBlank() || ptsRaw.isBlank()) continue;

			try {
				double points = Double.parseDouble(ptsRaw);
				nationScores.put(nation, points);
				if (points > maxPoints) {
					maxPoints = points;
				}
			} catch (NumberFormatException ignored) {}
		}

		if (nationScores.isEmpty() || maxPoints <= 0.0) return 0;

		Map<String, Double> generatedLeagues = new LinkedHashMap<>(config.getLeagueCoefficients());

		for (Map.Entry<String, Double> entry : nationScores.entrySet()) {
			String nation = entry.getKey();
			double points = entry.getValue();

			double tier1 = Math.min(1.00, Math.max(0.68, Math.sqrt(points / maxPoints)));
			tier1 = Math.round(tier1 * 100.0) / 100.0;

			double tier2 = Math.round((tier1 * 0.82) * 100.0) / 100.0;
			double tier3 = Math.round((tier1 * 0.68) * 100.0) / 100.0;

			mapLeaguesForNation(nation, tier1, tier2, tier3, generatedLeagues);
		}

		config.setLeagueCoefficients(generatedLeagues);

		Path externalConfig = Paths.get("moneyball-config.yaml");
		if (Files.exists(externalConfig)) {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			mapper.writeValue(externalConfig.toFile(), config);
		}

		return generatedLeagues.size();
	}

	private static void mapLeaguesForNation(String nation, double t1, double t2, double t3, Map<String, Double> map) {
		if (nation.contains("england")) {
			map.put("premier division", t1);
			map.put("premier league", t1);
			map.put("sky bet championship", t2);
			map.put("championship", t2);
			map.put("sky bet league one", t3);
			map.put("league one", t3);
		} else if (nation.contains("spanien")) {
			map.put("spaniens first division", t1);
			map.put("la liga", t1);
			map.put("spanische 1.", t1);
			map.put("spaniens second division", t2);
			map.put("segunda división", t2);
			map.put("la liga 2", t2);
			map.put("spanische 2.", t2);
			map.put("primera federación", t3);
			map.put("spanische 3.", t3);
		} else if (nation.contains("italien")) {
			map.put("serie a", t1);
			map.put("italiens 1.", t1);
			map.put("serie b", t2);
			map.put("italiens 2.", t2);
			map.put("serie c", t3);
		} else if (nation.contains("deutschland")) {
			map.put("bundesliga", t1);
			map.put("deutsche 1.", t1);
			map.put("2. bundesliga", t2);
			map.put("deutsche 2.", t2);
			map.put("3. liga", t3);
		} else if (nation.contains("frankreich")) {
			map.put("ligue 1 uber eats", t1);
			map.put("ligue 1", t1);
			map.put("ligue 2", t2);
			map.put("national", t3);
		} else if (nation.contains("portugal")) {
			map.put("portugals premier league", t1);
			map.put("liga portugal", t1);
			map.put("liga portugal 2", t2);
		} else if (nation.contains("niederlande")) {
			map.put("eredivisie", t1);
			map.put("eerste divisie", t2);
		} else if (nation.contains("belgien")) {
			map.put("jupiler pro league", t1);
			map.put("challenger pro league", t2);
		} else if (nation.contains("kroatien")) {
			map.put("kroatiens first league", t1);
			map.put("supersport hnl", t1);
			map.put("kroatiens 2.", t2);
		} else {
			map.put(nation + " 1.", t1);
			map.put(nation + " 2.", t2);
			map.put(nation + " 3.", t3);
		}
	}
}