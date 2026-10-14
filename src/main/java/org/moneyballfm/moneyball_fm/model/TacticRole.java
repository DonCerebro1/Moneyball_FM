package org.moneyballfm.moneyball_fm.model;

import java.util.List;

public enum TacticRole {

	// ==========================================
	// TORWART
	// ==========================================
	MTW(
			"MTW (U)",
			"Mitspielender Torwart",
			List.of("TW", "MTW", "TORWART", "GK", "SK", "TORHÜTER", "MITSPIELENDER TORWART"),
			"Par %", "xG verh/90", "Pas %"
	),
	TW(
			"TW (V)",
			"Torwart",
			List.of("TW (V)", "GK (D)"),
			"Par %", "xG verh/90", "AbB/90"
	),

	// ==========================================
	// VERTEIDIGUNG ZENTRAL
	// ==========================================
	BSV(
			"BSV (V)",
			"Ballspielender Verteidiger",
			List.of("BSV", "BPD", "BALLSPIELENDER VERT", "BALLSPIELENDER VERTEIDIGER", "VZL", "BSV (L)", "BSV (R)"),
			"Pr Pässe/90", "Pas %", "Zwk %"
	),
	HRV(
			"HRV (U)",
			"Halbraumverteidiger",
			List.of("HRV", "WCB", "HALBRAUMVERTEIDIGER", "VZR", "HRV (R)", "HRV (L)"),
			"Pr Pässe/90", "AbB/90", "Pas %"
	),
	LIBERO(
			"LIB (U)",
			"Libero",
			List.of("LIB", "LIBERO"),
			"Pr Pässe/90", "Pas %", "AbB/90"
	),
	IV(
			"IV (V)",
			"Innenverteidiger",
			List.of("IV", "VZ", "IV (Z)", "DC", "CB", "CD", "INNENVERTEIDIGER", "INNENVERTEITIGER"),
			"Kopf %", "Zwk %", "AbB/90"
	),
	KIV(
			"KIV (V)",
			"Kompromissloser Innenverteidiger",
			List.of("KIV", "NCB", "KOMPROMISSLOSER INNENVERTEIDIGER", "KOMPROMISSLOSER INNENVERTEITIGER"),
			"Kopf %", "Zwk %", "AbB/90"
	),

	// ==========================================
	// AUSSEN- & FLÜGELVERTEIDIGER (LINKS / RECHTS)
	// ==========================================
	IFV(
			"IFV (U)",
			"Inverser Flügelverteidiger",
			List.of("IFV", "IWB", "INVERSER FLÜGELVERTEIDIGER", "INVERSER FLUGELVERTEIDIGER"),
			"Pas %", "Pr Pässe/90", "AbB/90"
	),
	AV(
			"AV (U)",
			"Außenverteidiger",
			List.of("AV", "FB", "AUSSENVERTEIDIGER", "AUßENVERTEIDIGER"),
			"Zwk %", "AbB/90", "Pas %"
	),
	IAV(
			"IAV (V)",
			"Inverser Außenverteidiger",
			List.of("IAV", "IFB", "INVERSER AUSSENVERTEIDIGER", "INVERSER AUßENVERTEIDIGER"),
			"Pas %", "Zwk %", "AbB/90"
	),
	FV(
			"FV (U)",
			"Flügelverteidiger",
			List.of("FV", "WB", "FLÜGELVERTEIDIGER", "FLÜGELVERTEIGER"),
			"FPQ %", "Drb/90", "Zwk %"
	),
	KAV(
			"KAV (V)",
			"Kompromissloser Außenverteidiger",
			List.of("KAV", "NFB", "KOMPROMISSLOSER AUSSENVERTEIDIGER", "KOMPROMISSLOSER AUßENVERTEIDIGER"),
			"Zwk %", "AbB/90", "Kopf %"
	),
	KFV(
			"KFV (U)",
			"Kompletter Flügelverteidiger",
			List.of("KFV", "CWB", "KOMPLETTER FLÜGELVERTEIDIGER", "KOMP. FLÜGELVERTEIDIGER"),
			"Ent Päs", "Drb/90", "FPQ %"
	),

	// ==========================================
	// DEFENSIVES MITTELFELD (DM)
	// ==========================================
	ZSM(
			"ZSM (U)",
			"Zurückgezogener Spielmacher",
			List.of("ZSM", "DLP", "ZURÜCKGEZOGENER SPIELMACHER"),
			"Pr Pässe/90", "Pas %", "Ent Päs"
	),
	ABRAEUMER(
			"AB (V)",
			"Abräumer",
			List.of("AB", "A", "ABRÄUMER", "ANCHOR", "ANKER"),
			"Zwk %", "AbB/90", "Kopf %"
	),
	TIEFER_SECHSER(
			"TS (V)",
			"Tiefer Sechser",
			List.of("TS", "DM (V)", "TIEFER SECHSER", "HALF BACK", "HB"),
			"AbB/90", "Zwk %", "Pas %"
	),
	SEGUNDO_VOLANTE(
			"VOL (U)",
			"Segundo Volante",
			List.of("VOL", "SV", "SEGUNDO VOLANTE"),
			"Drb/90", "Zwk %", "Pr Pässe/90"
	),
	VERTIKALER_SPIELMACHER(
			"VSM (U)",
			"Vertikaler Spielmacher",
			List.of("VSM", "RPM", "VERTIKALER SPIELMACHER", "ROAMING PLAYMAKER"),
			"Pr Pässe/90", "Ent Päs", "Drb/90"
	),
	REGISTA(
			"REG (U)",
			"Regista",
			List.of("REG", "REGISTA", "DMZR"),
			"Pr Pässe/90", "Ent Päs", "Pas %"
	),
	DEFENSIVER_MITTELFELDSSPIELER(
			"DM (V)",
			"Defensiver Mittelfeldspieler",
			List.of("DM", "DM (U)", "DEFENSIVER MITTELFELD", "DEFENSIVER MITTELFELDSPIELER"),
			"Zwk %", "AbB/90", "Pas %"
	),
	BALLEROBERNDER_MITTELFELDSSPIELER(
			"BMS (U)",
			"Ballerobernder Mittelfeldspieler",
			List.of("BMS", "BEM", "BWM", "BALLEROBERNDER MITTELFELDSPIELER"),
			"Zwk %", "AbB/90", "Pas %"
	),

	// ==========================================
	// ZENTRALES MITTELFELD (ZM)
	// ==========================================
	VORGESCHOBENER_SPIELMACHER(
			"VoSm (U)",
			"Vorgeschobener Spielmacher",
			List.of("VOSM", "AP", "VORGESCHOBENER SPIELMACHER"),
			"Ent Päs", "xA/90", "Pr Pässe/90"
	),
	ZENTRALER_MITTELFELDSSPIELER(
			"ZM (U)",
			"Zentraler Mittelfeldspieler",
			List.of("ZM", "CM", "ZENTRALER MITTELFELDSPIELER"),
			"Pas %", "Zwk %", "Pr Pässe/90"
	),
	CARRILERO(
			"CAR (U)",
			"Carrilero",
			List.of("CAR", "CARRILERO"),
			"Pas %", "AbB/90", "Zwk %"
	),
	MEZZALA(
			"MEZ (U)",
			"Mezzala",
			List.of("MEZ", "MEZZALA"),
			"Drb/90", "Ent Päs", "xA/90"
	),
	BOX_TO_BOX(
			"BBM (U)",
			"Box to Box Mittelfeldspieler",
			List.of("BBM", "B2B", "BOX TO BOX", "BOX-TO-BOX", "BOX TO BOX MITTELFELDSPIELER"),
			"Drb/90", "Zwk %", "Pas %"
	),

	// ==========================================
	// MITTELFELD AUSSEN (ML / MR)
	// ==========================================
	FLUEGELSPIELER(
			"FLÜ (U)",
			"Flügelspieler",
			List.of("FLÜ", "W", "FLG", "FLÜGELSPIELER"),
			"FPQ %", "Drb/90", "xA/90"
	),
	INVERSER_FLUEGELSPIELER(
			"IFL (U)",
			"Inverser Flügelspieler",
			List.of("IFL", "IW", "INVERSER FLÜGELSPIELER"),
			"Drb/90", "Ent Päs", "Schüsse %"
	),
	AEUSSERER_SPIELMACHER(
			"ASP (U)",
			"Äußerer Spielmacher",
			List.of("ASP", "WM", "ÄUßERER SPIELMACHER", "AEUSSERER SPIELMACHER", "WIDE PLAYMAKER"),
			"Ent Päs", "Pr Pässe/90", "Pas %"
	),
	DEFENSIVER_FLUEGELSPIELER(
			"DFL (U)",
			"Defensiver Flügelspieler",
			List.of("DFL", "DW", "DEFENSIVER FLÜGELSPIELER"),
			"Zwk %", "AbB/90", "FPQ %"
	),
	AEUSSERER_MITTELFELDSSPIELER(
			"AM (U)",
			"Äußere Mittelfeldspieler",
			List.of("AM", "LM", "RM", "ÄUßERE MITTELFELDSPIELER", "ÄUßERER MITTELFELDSPIELER", "WIDE MIDFIELDER"),
			"Pas %", "FPQ %", "Zwk %"
	),

	// ==========================================
	// OFFENSIVES MITTELFELD (AUSSEN & ZENTRAL)
	// ==========================================
	INVERSER_AUSSENSTUERMER(
			"IAS (A)",
			"Inverser Aussenstürmer",
			List.of("IAS", "IF", "INVERSER AUSSENSTÜRMER", "INVERSER AUSSENSTUERMER", "INSIDE FORWARD"),
			"xG/90", "Schüsse %", "Drb/90"
	),
	TREQUARTISTA(
			"TREQ (A)",
			"Trequartista",
			List.of("TREQ", "T", "TREQUARTISTA", "TREQUISTA"),
			"Ent Päs", "xA/90", "Drb/90"
	),
	RAUMDEUTER(
			"RD (A)",
			"Raumdeuter",
			List.of("RD", "RAUMDEUTER"),
			"xG/90", "Schüsse %", "Verw. %"
	),
	AEUSSERER_ZIELSPIELER(
			"AZS (U)",
			"Äußere Zielspieler",
			List.of("AZS", "WT", "ÄUßERE ZIELSPIELER", "ÄUßERER ZIELSPIELER", "WIDE TARGET FORWARD"),
			"Kopf %", "Ent Päs", "xG/90"
	),
	OFFENSIVER_MITTELFELDSSPIELER(
			"OM (U)",
			"Offensives Mittelfeldspieler",
			List.of("OM", "AM", "OMZ", "OFFENSIVES MITTELFELD", "OFFENSIVER MITTELFELDSPIELER"),
			"Ent Päs", "xA/90", "xG/90"
	),
	SCHATTENSTUERMER(
			"SS (A)",
			"Schattenstürmer",
			List.of("SS", "SCHATTENSTÜRMER", "SHADOW STRIKER"),
			"xG/90", "Schüsse %", "Ent Päs"
	),
	ENGANCHE(
			"ENG (U)",
			"Enganche",
			List.of("ENG", "ENGANCHE"),
			"Ent Päs", "xA/90", "Pas %"
	),

	// ==========================================
	// STÜRMER (ST)
	// ==========================================
	STOESSTUERMER(
			"StS (A)",
			"Stoßstürmer",
			List.of("STS", "AF", "STOßSTÜRMER", "STOSSSTÜRMER", "ADVANCED FORWARD"),
			"xG/90", "Schüsse %", "Verw. %"
	),
	HAENGENDE_SPITZE(
			"HS (U)",
			"Hängende Spitze",
			List.of("HS", "DLF", "HÄNGENDE SPITZE", "HÄS", "DEEP LYING FORWARD"),
			"Ent Päs", "xA/90", "xG/90"
	),
	KOMPLETTER_STUERMER(
			"KLT (A)",
			"Kompletter Stürmer",
			List.of("KLT", "CF", "KOMPLETTER STÜRMER"),
			"xG/90", "Kopf %", "Ent Päs"
	),
	KNIPSER(
			"KNI (A)",
			"Knipser",
			List.of("KNI", "P", "POACHER", "KNIPSER"),
			"xG/90", "Schüsse %", "Verw. %"
	),
	PRESSENDER_STUERMER(
			"PS (A)",
			"Pressender Stürmer",
			List.of("PS", "PF", "PRESSENDER STÜRMER"),
			"Zwk %", "xG/90", "Kopf %"
	),
	ZIELSPIELER(
			"ZS (U)",
			"Zielspieler",
			List.of("ZS", "TF", "ZIELSPIELER", "TARGET FORWARD"),
			"Kopf %", "Ent Päs", "xG/90"
	),
	FALSCHE_NEUN(
			"F9 (U)",
			"Falsche Neun",
			List.of("F9", "FALSE NINE", "FALSCHE NEUN"),
			"Pr Pässe/90", "Ent Päs", "xA/90"
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

		// 1. Aufgaben nach Bindestrich abtrennen
		String clean = pos;
		if (clean.contains("-")) {
			clean = clean.split("-")[0];
		}

		// 2. Spielaufgaben in Klammern entfernen
		clean = clean.replaceAll("(?i)\\s*\\((unterstützen|verteidigen|angreifen|rückendeckung|vorstopper)\\)", "");

		// 3. Normalisieren
		clean = clean.toUpperCase()
				.replace("\u00A0", " ")
				.replace(".", "")
				.replace("—", "")
				.replaceAll("\\s+", " ")
				.trim();

		if (clean.matches("^[EB]\\d+$") || clean.equals("ERS") || clean.isEmpty()) {
			return ZSM;
		}

		// 4. Direkter Match auf Enum-Name, Code oder DisplayName
		for (TacticRole r : values()) {
			String cleanDisplayName = r.displayName.replaceAll("(?i)\\s*\\((unterstützen|verteidigen|angreifen)\\)", "").toUpperCase().replace(".", "").trim();
			String cleanCode = r.code.replaceAll("(?i)\\s*\\((unterstützen|verteidigen|angreifen)\\)", "").toUpperCase().replace(".", "").trim();

			if (clean.equalsIgnoreCase(r.name()) ||
					clean.equalsIgnoreCase(cleanCode) ||
					clean.equalsIgnoreCase(cleanDisplayName)) {
				return r;
			}
		}

		// 5. Alias-Match
		for (TacticRole r : values()) {
			for (String alias : r.aliases) {
				String cleanAlias = alias.toUpperCase().replace(".", "").trim();
				if (clean.equalsIgnoreCase(cleanAlias) || clean.startsWith(cleanAlias + " ")) {
					return r;
				}
			}
		}

		// 6. Substring-Fallbacks (Prioritätsreihenfolge)
		if (clean.contains("TORWART") || clean.contains("TORHÜTER") || clean.contains("TW") || clean.contains("GK")) {
			return clean.contains("MIT") ? MTW : TW;
		}
		if (clean.contains("BALLSPIEL")) return BSV;
		if (clean.contains("HALBRAUM")) return HRV;
		if (clean.contains("LIBERO")) return LIBERO;
		if (clean.contains("KOMP") && clean.contains("INNEN")) return KIV;
		if (clean.contains("KOMP") && (clean.contains("FLÜGEL") || clean.contains("FLUGEL"))) return KFV;
		if (clean.contains("KOMP") && clean.contains("STÜRMER")) return KOMPLETTER_STUERMER;
		if (clean.contains("INVERS") && (clean.contains("FLÜGELVERT") || clean.contains("FLUGELVERT"))) return IFV;
		if (clean.contains("INVERS") && clean.contains("AUSSENVERT")) return IAV;
		if (clean.contains("INVERS") && clean.contains("AUSSENST")) return INVERSER_AUSSENSTUERMER;
		if (clean.contains("INVERS") && clean.contains("FLÜGEL")) return INVERSER_FLUEGELSPIELER;
		if (clean.contains("MEZZALA")) return MEZZALA;
		if (clean.contains("CARRILERO")) return CARRILERO;
		if (clean.contains("BOX TO BOX") || clean.contains("B2B")) return BOX_TO_BOX;
		if (clean.contains("VOLANTE")) return SEGUNDO_VOLANTE;
		if (clean.contains("REGISTA")) return REGISTA;
		if (clean.contains("VERTIKAL") && clean.contains("SPIELM")) return VERTIKALER_SPIELMACHER;
		if (clean.contains("VORGESCHOB") || clean.contains("VOSM")) return VORGESCHOBENER_SPIELMACHER;
		if (clean.contains("ZURÜCKGEZOG") || clean.contains("ZSM")) return ZSM;
		if (clean.contains("ABRÄUMER")) return ABRAEUMER;
		if (clean.contains("TIEFER SECHS")) return TIEFER_SECHSER;
		if (clean.contains("BALLEROBER")) return BALLEROBERNDER_MITTELFELDSSPIELER;
		if (clean.contains("DEFENSIV") && clean.contains("FLÜGEL")) return DEFENSIVER_FLUEGELSPIELER;
		if (clean.contains("DEFENSIV") && clean.contains("MITTEL")) return DEFENSIVER_MITTELFELDSSPIELER;
		if (clean.contains("SCHATTEN")) return SCHATTENSTUERMER;
		if (clean.contains("RAUMDEUTER")) return RAUMDEUTER;
		if (clean.contains("ENGANCHE")) return ENGANCHE;
		if (clean.contains("TREQU")) return TREQUARTISTA;
		if (clean.contains("HÄNGEND")) return HAENGENDE_SPITZE;
		if (clean.contains("FALSCHE NEUN")) return FALSCHE_NEUN;
		if (clean.contains("KNIPSER")) return KNIPSER;
		if (clean.contains("PRESSEND")) return PRESSENDER_STUERMER;
		if (clean.contains("ZIEL") && clean.contains("ÄUßER")) return AEUSSERER_ZIELSPIELER;
		if (clean.contains("ZIEL") && clean.contains("STÜRMER")) return ZIELSPIELER;
		if (clean.contains("STÖSS") || clean.contains("STOSS")) return STOESSTUERMER;
		if (clean.contains("FLÜGELVERT") || clean.contains("FLUGELVERT")) return FV;
		if (clean.contains("AUSSENVERT") || clean.contains("AUßENVERT")) return AV;
		if (clean.contains("FLÜGEL")) return FLUEGELSPIELER;
		if (clean.contains("INNENVERT") || clean.contains("IV") || clean.contains("VZ")) return IV;
		if (clean.contains("OFFENSIV") || clean.contains("OM")) return OFFENSIVER_MITTELFELDSSPIELER;
		if (clean.contains("STÜRMER") || clean.contains("ST")) return STOESSTUERMER;

		return ZSM;
	}
}