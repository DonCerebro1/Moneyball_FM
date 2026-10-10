package org.moneyballfm.moneyball_fm.model;

import java.util.List;

public enum TacticRole {
	MTW(
			"MTW (U)",
			"Mitspielender Torwart",
			List.of("TW", "MTW"),
			"Par %", "xG verh/90", "Pas %"
	),
	STRIKER(
			"Stoßstürmer (A)",
			"Stoßstürmer (Mitte)",
			List.of("STZ", "ST", "TJ"),
			"xG/90", "Verw. %", "Schüsse %"
	),
	OM_L(
			"OM (L)",
			"Offensiver Mittelfeldspieler Links",
			List.of("OMZL", "AML"),
			"xA/90", "Ent Päs", "Pas %"
	),
	OM_R(
			"OM (R)",
			"Offensiver Mittelfeldspieler Rechts",
			List.of("OMZR", "OMZ", "AMR"),
			"xA/90", "Ent Päs", "Pas %"
	),
	REGISTA(
			"Regista (U)",
			"Regista (Zentral Rechts)",
			List.of("DMZR", "REG"),
			"Pas %", "Pr Pässe/90", "Ent Päs"
	),
	ZSM(
			"ZSM (U)",
			"Zurückgez. Spielmacher (Zentral Links)",
			List.of("DMZL", "ZM", "DLP"),
			"Pas %", "AbB/90", "Zwk %"
	),
	FV_L(
			"FV (L)",
			"Flügelverteidiger Links",
			List.of("FVL", "VL"),
			"AbB/90", "FPQ %", "Drb/90"
	),
	KFV_R(
			"KFV (R)",
			"Komp. Flügelverteidiger Rechts",
			List.of("FVR", "VR"),
			"AbB/90", "FPQ %", "Drb/90"
	),
	BSV_L(
			"BSV (L)",
			"Ballspielender Vert. Links",
			List.of("VZL"),
			"Pas %", "Zwk %", "Pr Pässe/90"
	),
	IV_Z(
			"IV (Z)",
			"Innenverteidiger Zentral (Rück)",
			List.of("VZ", "IV"),
			"Kopf %", "Zwk %", "T Feh"
	),
	HRV_R(
			"HRV (R)",
			"Halbraumverteidiger Rechts",
			List.of("VZR"),
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

	public static TacticRole fromPosition(String pos) {
		if (pos == null || pos.isBlank() || pos.equals("-")) {
			return ZSM; // Standard-Fallback für Ersatzspieler
		}
		String clean = pos.toUpperCase().trim();
		for (TacticRole r : values()) {
			for (String alias : r.aliases) {
				if (clean.equals(alias) || clean.startsWith(alias)) {
					return r;
				}
			}
		}
		return ZSM;
	}
}