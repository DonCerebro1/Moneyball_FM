package org.moneyballfm.moneyball_fm.model;

import java.util.List;

public enum TacticRole {
	MTW(
			"MTW (U)",
			"Mitspielender Torwart",
			List.of("TW", "MTW", "TORWART", "GK", "SK", "TORHÜTER"),
			"Par %", "xG verh/90", "Pas %"
	),
	STRIKER(
			"Stoßstürmer (A)",
			"Stoßstürmer (Mitte)",
			List.of("STS", "STZ", "ST", "TJ", "STÖSS", "STOSS", "ANGREIFER", "ST (Z)", "TJ (A)", "AF", "CF", "PF",
					"PRESSEND", "PRESSENDER", "KNIPSER", "ZIELSPIELER", "HÄNGENDER STÜRMER", "KOMPLETTER STÜRMER",
					"HÄS", "STS -", "HÄS -"),
			"xG/90", "Verw. %", "Schüsse %"
	),
	OM_L(
			"OM (L)",
			"Offensiver Mittelfeldspieler Links",
			List.of("OMZL", "AML", "OM (L)", "OML", "IF (L)", "IW (L)", "W (L)", "ML", "M (L)",
					"FLÜGELSPIELER (L)", "INVERSER FLÜGELSPIELER (L)", "FLÜGELSTÜRMER (L)"),
			"xA/90", "Ent Päs", "Pas %"
	),
	OM_R(
			"OM (R)",
			"Offensiver Mittelfeldspieler Rechts",
			List.of("OMZR", "AMR", "OM (R)", "OMR", "IF (R)", "IW (R)", "W (R)", "MR", "M (R)", "OMZ", "OM (Z)", "OM",
					"OFFENSIVER MITTELFELDSPIELER", "OFFENSIVER MITTELFELD", "OFFENSIVER MITTELFELDSPIELER (Z)",
					"FLÜGELSPIELER (R)", "INVERSER FLÜGELSPIELER (R)", "FLÜGELSTÜRMER (R)", "FLÜGELSPIELER"),
			"xA/90", "Ent Päs", "Pas %"
	),
	REGISTA(
			"Regista (U)",
			"Regista (Zentral Rechts)",
			List.of("DMZR", "REG", "REGISTA", "DM (R)", "DMZ (R)"),
			"Pas %", "Pr Pässe/90", "Ent Päs"
	),
	ZSM(
			"ZSM (U)",
			"Zurückgezogener Spielmacher",
			List.of("DMZL", "ZM", "DLP", "ZSM", "DM", "DM (L)", "DMZ (L)", "ZM (L)", "ZM (Z)", "CM", "AP",
					"ZENTRALER MITTELFELDSPIELER", "DEFENSIVER MITTELFELDSPIELER", "BALLEROBERNDER MITTELFELDSPIELER",
					"BEM", "VOSM", "ZSM -", "BEM -", "VOSM -"),
			"Pas %", "AbB/90", "Zwk %"
	),
	FV_L(
			"FV (L)",
			"Flügelverteidiger Links",
			List.of("FVL", "VL", "FV (L)", "V (L)", "DL", "WBL", "FV", "AV (L)", "AV", "AUSSENVERTEIDIGER (L)",
					"FLÜGELVERTEIDIGER LINKS", "FLÜGELVERTEIDIGER (LINKS)", "FV -"),
			"AbB/90", "FPQ %", "Drb/90"
	),
	KFV_R(
			"KFV (R)",
			"Komp. Flügelverteidiger Rechts",
			List.of("FVR", "VR", "KFV (R)", "FV (R)", "V (R)", "DR", "WBR", "KFV", "AV (R)", "AUSSENVERTEIDIGER (R)",
					"KOMPLETTER FLÜGELVERTEIDIGER", "KOMP. FLÜGELVERTEIDIGER", "KFV -"),
			"AbB/90", "FPQ %", "Drb/90"
	),
	BSV_L(
			"BSV (L)",
			"Ballspielender Vert. Links",
			List.of("BSV (L)", "VZL", "BSV", "BPD", "V (ZL)", "VZ (L)", "BALLSPIELENDER VERTEIDIGER", "BSV -"),
			"Pas %", "Zwk %", "Pr Pässe/90"
	),
	IV_Z(
			"IV (Z)",
			"Innenverteidiger Zentral",
			List.of("VZ", "IV", "IV (Z)", "V (Z)", "DC", "CB", "CD", "INNENVERTEIDIGER", "IV -"),
			"Kopf %", "Zwk %", "T Feh"
	),
	HRV_R(
			"HRV (R)",
			"Halbraumverteidiger Rechts",
			List.of("VZR", "HRV", "HRV (R)", "WCB", "V (ZR)", "VZ (R)", "HALBRAUMVERTEIDIGER", "HRV -"),
			"Pas %", "Zwk %", "Drb/90"
	);

	private final String code;
	private final String displayName;
	private final List<String> aliases;
	private final String stat1Header;
	private final String stat2Header;
	private final String stat3Header;

	TacticRole(String code, String displayName, List<String> aliases, String s1, String s2, String s3) {
		this.code = code;
		this.displayName = displayName;
		this.aliases = aliases;
		this.stat1Header = s1;
		this.stat2Header = s2;
		this.stat3Header = s3;
	}

	public String getCode() { return code; }
	public String getDisplayName() { return displayName; }
	public String getStat1Header() { return stat1Header; }
	public String getStat2Header() { return stat2Header; }
	public String getStat3Header() { return stat3Header; }

	public static TacticRole fromDisplayName(String name) {
		if (name == null || name.isBlank()) return ZSM;
		for (TacticRole r : values()) {
			if (r.getDisplayName().equalsIgnoreCase(name) ||
					r.getCode().equalsIgnoreCase(name) ||
					r.name().equalsIgnoreCase(name)) {
				return r;
			}
		}
		return fromPosition(name);
	}

	public static TacticRole fromPosition(String pos) {
		if (pos == null || pos.isBlank() || pos.equals("-")) {
			return ZSM;
		}

		String rawUpper = pos.toUpperCase().trim();

		// 1. Aufgaben nach Bindestrich abtrennen (z. B. "FV - Unterstützen" -> "FV")
		String clean = pos;
		if (clean.contains("-")) {
			clean = clean.split("-")[0];
		}

		// 2. Nur Spielaufgaben in Klammern entfernen (Unterstützen, Verteidigen, Angreifen, Rückendeckung, Vorstopper)
		// Richtungsangaben wie (L), (R), (Z), (RL) bleiben erhalten!
		clean = clean.replaceAll("(?i)\\s*\\((unterstützen|verteidigen|angreifen|rückendeckung|vorstopper)\\)", "");

		// 3. Normalisieren
		clean = clean.toUpperCase()
				.replace("\u00A0", " ")
				.replace(".", "")
				.replace("—", "")
				.replaceAll("\\s+", " ")
				.trim();

		// 4. Bankspieler-Kürzel und leere Werte abfangen
		if (clean.matches("^[EB]\\d+$") || clean.equals("ERS") || clean.isEmpty()) {
			return ZSM;
		}

		// 5. Direkter Match auf Enum-Name, Code oder DisplayName
		for (TacticRole r : values()) {
			String cleanDisplayName = r.displayName.replaceAll("(?i)\\s*\\((unterstützen|verteidigen|angreifen)\\)", "").toUpperCase().replace(".", "").trim();
			String cleanCode = r.code.replaceAll("(?i)\\s*\\((unterstützen|verteidigen|angreifen)\\)", "").toUpperCase().replace(".", "").trim();

			if (clean.equalsIgnoreCase(r.name()) ||
					clean.equalsIgnoreCase(cleanCode) ||
					clean.equalsIgnoreCase(cleanDisplayName)) {
				return r;
			}
		}

		// 6. Alias-Match (exakt oder Wortanfang)
		for (TacticRole r : values()) {
			for (String alias : r.aliases) {
				String cleanAlias = alias.toUpperCase().replace(".", "").trim();
				if (clean.equalsIgnoreCase(cleanAlias) || clean.startsWith(cleanAlias + " ")) {
					return r;
				}
			}
		}

		// 7. Gezielte Hierarchie-Prüfung (Reihenfolge ist entscheidend!)

		// Torhüter
		if (clean.contains("TORWART") || clean.contains("TORHÜTER") || clean.contains("TW") || clean.contains("GK") || clean.contains("MTW")) {
			return MTW;
		}

		// Spezial-Verteidiger
		if (clean.contains("HALBRAUM") || clean.contains("HRV") || clean.contains("VZR") || clean.contains("WCB")) {
			return HRV_R;
		}
		if (clean.contains("BALLSPIEL") || clean.contains("BSV") || clean.contains("VZL") || clean.contains("BPD")) {
			return BSV_L;
		}

		// Flügelverteidiger / Komplette Flügelverteidiger (IMMER vor OM/Flügelstürmern prüfen!)
		if (clean.contains("KOMP") || clean.contains("KFV") || (clean.contains("FLÜGELVERT") && (clean.contains("(R)") || clean.contains("RECHTS")))) {
			return KFV_R;
		}
		if (clean.contains("FLÜGELVERT") || clean.contains("FV") || clean.contains("AV") || clean.contains("WBL") || clean.contains("WBR")) {
			if (clean.contains("(R)") || clean.contains("RECHTS") || rawUpper.contains("(R)")) return KFV_R;
			return FV_L;
		}
		// Reine Außenverteidiger-Kürzel wie V (R) oder V (L)
		if (clean.startsWith("V (R)") || clean.equals("VR") || clean.equals("DR")) {
			return KFV_R;
		}
		if (clean.startsWith("V (L)") || clean.equals("VL") || clean.equals("DL")) {
			return FV_L;
		}

		// Reine Innenverteidiger
		if (clean.contains("INNENVERT") || clean.contains("IV") || clean.contains("VZ") || clean.contains("V (Z)") || clean.contains("DC") || clean.contains("CB")) {
			return IV_Z;
		}

		// Regista & Spielmacher
		if (clean.contains("REGISTA") || clean.contains("REG") || clean.contains("DMZR")) {
			return REGISTA;
		}

		// Stürmer
		if (clean.contains("STÖSS") || clean.contains("STOSS") || clean.contains("STÜRMER") || clean.contains("ANGREIFER") ||
				clean.contains("PRESSEND") || clean.contains("KNIPSER") || clean.contains("HÄS") || clean.matches(".*\\b(ST|TJ|STS|STZ)\\b.*")) {
			return STRIKER;
		}

		// Offensives Mittelfeld & Flügelspieler
		if (clean.contains("OFFENSIV") || clean.contains("OM") || clean.contains("FLÜGEL") || clean.contains("AML") || clean.contains("AMR")) {
			if (clean.contains("(L)") || clean.contains("LINKS") || rawUpper.contains("(L)")) return OM_L;
			return OM_R;
		}

		// Zentrales & Defensives Mittelfeld
		if (clean.contains("MITTELFELD") || clean.contains("DM") || clean.contains("ZM") || clean.contains("CM") || clean.contains("ZSM") || clean.contains("BEM")) {
			return ZSM;
		}

		return ZSM;
	}
}