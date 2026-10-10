package org.moneyballfm.moneyball_fm.model;

import java.util.List;

public enum TacticRole {
	MTW(
			"MTW (U)",
			"Mitspielender Torwart",
			List.of("TW", "MTW", "TORWART", "GK", "SK"),
			"Par %", "xG verh/90", "Pas %"
	),
	STRIKER(
			"Stoßstürmer (A)",
			"Stoßstürmer (Mitte)",
			List.of("STS", "STZ", "ST", "TJ", "STÖSS", "STOSS", "ANGREIFER", "ST (Z)", "TJ (A)", "AF", "CF", "PF",
					"PRESSEND", "PRESSENDER", "KNIPSER", "ZIELSPIELER"),
			"xG/90", "Verw. %", "Schüsse %"
	),
	OM_L(
			"OM (L)",
			"Offensiver Mittelfeldspieler Links",
			List.of("OMZL", "AML", "OM (L)", "OML", "IF (L)", "IW (L)", "W (L)", "ML",
					"FLÜGELSPIELER (L)", "INVERSER FLÜGELSPIELER (L)", "FLÜGELSTÜRMER (L)"),
			"xA/90", "Ent Päs", "Pas %"
	),
	OM_R(
			"OM (R)",
			"Offensiver Mittelfeldspieler Rechts",
			List.of("OMZR", "AMR", "OM (R)", "OMR", "IF (R)", "IW (R)", "W (R)", "MR", "OMZ", "OM (Z)", "OM",
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
			List.of("DMZL", "ZM", "DLP", "ZSM", "DM", "DM (L)", "DMZ (L)", "ZM (L)", "ZM (Z)", "CM", "AP"),
			"Pas %", "AbB/90", "Zwk %"
	),
	FV_L(
			"FV (L)",
			"Flügelverteidiger Links",
			List.of("FVL", "VL", "FV (L)", "V (L)", "DL", "WBL", "FV", "AV (L)", "AV"),
			"AbB/90", "FPQ %", "Drb/90"
	),
	KFV_R(
			"KFV (R)",
			"Komp. Flügelverteidiger Rechts",
			List.of("FVR", "VR", "KFV (R)", "FV (R)", "V (R)", "DR", "WBR", "KFV", "AV (R)"),
			"AbB/90", "FPQ %", "Drb/90"
	),
	BSV_L(
			"BSV (L)",
			"Ballspielender Vert. Links",
			List.of("BSV (L)", "VZL", "BSV", "BPD", "V (ZL)", "VZ (L)"),
			"Pas %", "Zwk %", "Pr Pässe/90"
	),
	IV_Z(
			"IV (Z)",
			"Innenverteidiger Zentral",
			List.of("VZ", "IV", "IV (Z)", "V (Z)", "DC", "CB", "CD"),
			"Kopf %", "Zwk %", "T Feh"
	),
	HRV_R(
			"HRV (R)",
			"Halbraumverteidiger Rechts",
			List.of("VZR", "HRV", "HRV (R)", "WCB", "V (ZR)", "VZ (R)"),
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

		// 1. Aufgabe nach Bindestrich abschneiden (z. B. "FV - Unterstützen" -> "FV")
		String clean = pos;
		if (clean.contains("-")) {
			clean = clean.split("-")[0];
		}

		// 2. Normalisieren
		clean = clean.toUpperCase()
				.replace("\u00A0", " ")
				.replace(".", "")
				.replace("—", "")
				.replaceAll("\\s+", " ")
				.trim();

		// 3. Bankspieler und Platzhalter abfangen
		if (clean.matches("^[EB]\\d+$") || clean.equals("ERS") || clean.isEmpty()) {
			return ZSM;
		}

		// 4. Direkter Match auf Name oder DisplayName
		for (TacticRole r : values()) {
			if (clean.equalsIgnoreCase(r.name()) ||
					clean.equalsIgnoreCase(r.code.replace(".", "")) ||
					clean.equalsIgnoreCase(r.displayName)) {
				return r;
			}
		}

		// 5. Alias-Match
		for (TacticRole r : values()) {
			for (String alias : r.aliases) {
				String cleanAlias = alias.toUpperCase().replace(".", "").trim();
				if (clean.equals(cleanAlias) || clean.startsWith(cleanAlias + " ")) {
					return r;
				}
			}
		}

		// 6. Schlagwort-Fallbacks für verbleibende Kürzel
		if (clean.contains("TW") || clean.contains("GK") || clean.contains("TORWART")) return MTW;
		if (clean.contains("VZR") || clean.contains("HRV") || clean.contains("HALBRAUM")) return HRV_R;
		if (clean.contains("VZL") || clean.contains("BSV") || clean.contains("BALLSPIEL")) return BSV_L;
		if (clean.contains("KFV") || clean.contains("KOMP") || (clean.contains("FLÜGELVERT") && clean.contains("R"))) return KFV_R;
		if (clean.contains("FV") || (clean.contains("FLÜGELVERT") && clean.contains("L"))) return FV_L;
		if (clean.contains("V (Z)") || clean.contains("VZ") || clean.contains("IV") || clean.contains("DC")) return IV_Z;
		if (clean.contains("REG") || clean.contains("REGISTA")) return REGISTA;
		if (clean.contains("STS") || clean.contains("STÖSS") || clean.contains("STOSS") || clean.contains("STÜRMER") || clean.contains("PRESSEND") || clean.matches(".*\\b(ST|TJ)\\b.*")) return STRIKER;
		if (clean.contains("OM") || clean.contains("FLÜGEL")) return OM_R;

		return ZSM;
	}
}