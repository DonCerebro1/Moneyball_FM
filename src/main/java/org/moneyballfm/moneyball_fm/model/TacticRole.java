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
			MetricProfile.TORWART
	),
	TW(
			"TW (V)",
			"Torwart",
			List.of("TW (V)", "GK (D)"),
			MetricProfile.TORWART_KLASSISCH
	),

	// ==========================================
	// VERTEIDIGUNG ZENTRAL
	// ==========================================
	BSV(
			"BSV (V)",
			"Ballspielender Verteidiger",
			List.of("BSV", "BPD", "BALLSPIELENDER VERT", "BALLSPIELENDER VERTEIDIGER", "VZL", "BSV (L)", "BSV (R)", "BALLSPIELENDER VERT. LINKS"),
			MetricProfile.IV_AUFBAU
	),
	HRV(
			"HRV (U)",
			"Halbraumverteidiger",
			List.of("HRV", "WCB", "HALBRAUMVERTEIDIGER", "VZR", "HRV (R)", "HRV (L)", "HALBRAUMVERTEIDIGER RECHTS"),
			MetricProfile.IV_AUFBAU
	),
	LIBERO(
			"LIB (U)",
			"Libero",
			List.of("LIB", "LIBERO"),
			MetricProfile.IV_AUFBAU
	),
	IV(
			"IV (V)",
			"Innenverteidiger",
			List.of("IV", "VZ", "IV (Z)", "DC", "CB", "CD", "INNENVERTEIDIGER", "INNENVERTEITIGER", "INNENVERTEIDIGER ZENTRAL"),
			MetricProfile.IV_ZERSTOERER
	),
	KIV(
			"KIV (V)",
			"Kompromissloser Innenverteidiger",
			List.of("KIV", "NCB", "KOMPROMISSLOSER INNENVERTEIDIGER", "KOMPROMISSLOSER INNENVERTEITIGER"),
			MetricProfile.IV_ZERSTOERER
	),

	// ==========================================
	// AUSSEN- & FLÜGELVERTEIDIGER (LINKS / RECHTS)
	// ==========================================
	IFV(
			"IFV (U)",
			"Inverser Flügelverteidiger",
			List.of("IFV", "IWB", "INVERSER FLÜGELVERTEIDIGER", "INVERSER FLUGELVERTEIDIGER"),
			MetricProfile.AV_INVERS
	),
	AV(
			"AV (U)",
			"Außenverteidiger",
			List.of("AV", "FB", "AUSSENVERTEIDIGER", "AUßENVERTEIDIGER"),
			MetricProfile.AV_DEFENSIV
	),
	IAV(
			"IAV (V)",
			"Inverser Außenverteidiger",
			List.of("IAV", "IFB", "INVERSER AUSSENVERTEIDIGER", "INVERSER AUßENVERTEIDIGER"),
			MetricProfile.AV_DEFENSIV
	),
	FV(
			"FV (U)",
			"Flügelverteidiger",
			List.of("FV", "WB", "FLÜGELVERTEIDIGER", "FLÜGELVERTEIGER", "FLÜGELVERTEIDIGER LINKS", "FLÜGELVERTEIDIGER RECHTS"),
			MetricProfile.AV_OFFENSIV
	),
	KAV(
			"KAV (V)",
			"Kompromissloser Außenverteidiger",
			List.of("KAV", "NFB", "KOMPROMISSLOSER AUSSENVERTEIDIGER", "KOMPROMISSLOSER AUßENVERTEIDIGER"),
			MetricProfile.IV_ZERSTOERER
	),
	KFV(
			"KFV (U)",
			"Kompletter Flügelverteidiger",
			List.of("KFV", "CWB", "KOMPLETTER FLÜGELVERTEIDIGER", "KOMP. FLÜGELVERTEIDIGER", "KOMP. FLÜGELVERTEIDIGER RECHTS"),
			MetricProfile.AV_OFFENSIV
	),

	// ==========================================
	// DEFENSIVES MITTELFELD (DM)
	// ==========================================
	ZSM(
			"ZSM (U)",
			"Zurückgezogener Spielmacher",
			List.of("ZSM", "DLP", "ZURÜCKGEZOGENER SPIELMACHER"),
			MetricProfile.SECHSER_SPIELMACHER
	),
	ABRAEUMER(
			"AB (V)",
			"Abräumer",
			List.of("AB", "A", "ABRÄUMER", "ANCHOR", "ANKER"),
			MetricProfile.IV_ZERSTOERER
	),
	TIEFER_SECHSER(
			"TS (V)",
			"Tiefer Sechser",
			List.of("TS", "DM (V)", "TIEFER SECHSER", "HALF BACK", "HB"),
			MetricProfile.SECHSER_ABRAEUMER
	),
	SEGUNDO_VOLANTE(
			"VOL (U)",
			"Segundo Volante",
			List.of("VOL", "SV", "SEGUNDO VOLANTE"),
			MetricProfile.ACHTER_ALLROUND
	),
	VERTIKALER_SPIELMACHER(
			"VSM (U)",
			"Vertikaler Spielmacher",
			List.of("VSM", "RPM", "VERTIKALER SPIELMACHER", "ROAMING PLAYMAKER"),
			MetricProfile.SECHSER_SPIELMACHER
	),
	REGISTA(
			"REG (U)",
			"Regista",
			List.of("REG", "REGISTA", "DMZR", "REGISTA (ZENTRAL RECHTS)"),
			MetricProfile.SECHSER_SPIELMACHER
	),
	DEFENSIVER_MITTELFELDSSPIELER(
			"DM (V)",
			"Defensiver Mittelfeldspieler",
			List.of("DM", "DM (U)", "DEFENSIVER MITTELFELD", "DEFENSIVER MITTELFELDSPIELER"),
			MetricProfile.SECHSER_ABRAEUMER
	),
	BALLEROBERNDER_MITTELFELDSSPIELER(
			"BMS (U)",
			"Ballerobernder Mittelfeldspieler",
			List.of("BMS", "BEM", "BWM", "BALLEROBERNDER MITTELFELDSPIELER"),
			MetricProfile.SECHSER_ABRAEUMER
	),

	// ==========================================
	// ZENTRALES MITTELFELD (ZM)
	// ==========================================
	VORGESCHOBENER_SPIELMACHER(
			"VoSm (U)",
			"Vorgeschobener Spielmacher",
			List.of("VOSM", "AP", "VORGESCHOBENER SPIELMACHER"),
			MetricProfile.ACHTER_KREATIV
	),
	ZENTRALER_MITTELFELDSSPIELER(
			"ZM (U)",
			"Zentraler Mittelfeldspieler",
			List.of("ZM", "CM", "ZENTRALER MITTELFELDSPIELER"),
			MetricProfile.ACHTER_ALLROUND
	),
	CARRILERO(
			"CAR (U)",
			"Carrilero",
			List.of("CAR", "CARRILERO"),
			MetricProfile.SECHSER_ABRAEUMER
	),
	MEZZALA(
			"MEZ (U)",
			"Mezzala",
			List.of("MEZ", "MEZZALA"),
			MetricProfile.ACHTER_KREATIV
	),
	BOX_TO_BOX(
			"BBM (U)",
			"Box to Box Mittelfeldspieler",
			List.of("BBM", "B2B", "BOX TO BOX", "BOX-TO-BOX", "BOX TO BOX MITTELFELDSPIELER"),
			MetricProfile.ACHTER_ALLROUND
	),

	// ==========================================
	// MITTELFELD AUSSEN (ML / MR)
	// ==========================================
	FLUEGELSPIELER(
			"FLÜ (U)",
			"Flügelspieler",
			List.of("FLÜ", "W", "FLG", "FLÜGELSPIELER"),
			MetricProfile.FLUEGEL_VORBEREITER
	),
	INVERSER_FLUEGELSPIELER(
			"IFL (U)",
			"Inverser Flügelspieler",
			List.of("IFL", "IW", "INVERSER FLÜGELSPIELER"),
			MetricProfile.FLUEGEL_INVERS
	),
	AEUSSERER_SPIELMACHER(
			"ASP (U)",
			"Äußerer Spielmacher",
			List.of("ASP", "WM", "ÄUßERER SPIELMACHER", "AEUSSERER SPIELMACHER", "WIDE PLAYMAKER"),
			MetricProfile.SECHSER_SPIELMACHER
	),
	DEFENSIVER_FLUEGELSPIELER(
			"DFL (U)",
			"Defensiver Flügelspieler",
			List.of("DFL", "DW", "DEFENSIVER FLÜGELSPIELER"),
			MetricProfile.AV_OFFENSIV
	),
	AEUSSERER_MITTELFELDSSPIELER(
			"AM (U)",
			"Äußerer Mittelfeldspieler",
			List.of("AM", "LM", "RM", "ÄUßERE MITTELFELDSPIELER", "ÄUßERER MITTELFELDSPIELER", "WIDE MIDFIELDER"),
			MetricProfile.FLUEGEL_VORBEREITER
	),

	// ==========================================
	// OFFENSIVES MITTELFELD (AUSSEN & ZENTRAL)
	// ==========================================
	INVERSER_AUSSENSTUERMER(
			"IAS (A)",
			"Inverser Aussenstürmer",
			List.of("IAS", "IF", "INVERSER AUSSENSTÜRMER", "INVERSER AUSSENSTUERMER", "INSIDE FORWARD"),
			MetricProfile.FLUEGEL_INVERS
	),
	TREQUARTISTA(
			"TREQ (A)",
			"Trequartista",
			List.of("TREQ", "T", "TREQUARTISTA", "TREQUISTA"),
			MetricProfile.ZEHNER_KREATIV
	),
	RAUMDEUTER(
			"RD (A)",
			"Raumdeuter",
			List.of("RD", "RAUMDEUTER"),
			MetricProfile.TORJAEGER
	),
	AEUSSERER_ZIELSPIELER(
			"AZS (U)",
			"Äußerer Zielspieler",
			List.of("AZS", "WT", "ÄUßERE ZIELSPIELER", "ÄUßERER ZIELSPIELER", "WIDE TARGET FORWARD"),
			MetricProfile.ZIELSTUERMER
	),
	OFFENSIVER_MITTELFELDSSPIELER(
			"OM (U)",
			"Offensiver Mittelfeldspieler",
			List.of("OM", "AM", "OMZ", "OFFENSIVES MITTELFELD", "OFFENSIVER MITTELFELDSPIELER", "OFFENSIVES MITTELFELDSPIELER", "OFFENSIVER MITTELFELDSPIELER LINKS", "OFFENSIVER MITTELFELDSPIELER RECHTS"),
			MetricProfile.ZEHNER_KREATIV
	),
	SCHATTENSTUERMER(
			"SS (A)",
			"Schattenstürmer",
			List.of("SS", "SCHATTENSTÜRMER", "SHADOW STRIKER"),
			MetricProfile.TORJAEGER
	),
	ENGANCHE(
			"ENG (U)",
			"Enganche",
			List.of("ENG", "ENGANCHE"),
			MetricProfile.ZEHNER_KREATIV
	),

	// ==========================================
	// STÜRMER (ST)
	// ==========================================
	STOESSTUERMER(
			"StS (A)",
			"Stoßstürmer",
			List.of("STS", "AF", "STOßSTÜRMER", "STOSSSTÜRMER", "ADVANCED FORWARD"),
			MetricProfile.TORJAEGER
	),
	HAENGENDE_SPITZE(
			"HS (U)",
			"Hängende Spitze",
			List.of("HS", "DLF", "HÄNGENDE SPITZE", "HÄS", "DEEP LYING FORWARD"),
			MetricProfile.ZEHNER_KREATIV
	),
	KOMPLETTER_STUERMER(
			"KLT (A)",
			"Kompletter Stürmer",
			List.of("KLT", "CF", "KOMPLETTER STÜRMER"),
			MetricProfile.ZIELSTUERMER
	),
	KNIPSER(
			"KNI (A)",
			"Knipser",
			List.of("KNI", "P", "POACHER", "KNIPSER"),
			MetricProfile.TORJAEGER
	),
	PRESSENDER_STUERMER(
			"PS (A)",
			"Pressender Stürmer",
			List.of("PS", "PF", "PRESSENDER STÜRMER"),
			MetricProfile.TORJAEGER
	),
	ZIELSPIELER(
			"ZS (U)",
			"Zielspieler",
			List.of("ZS", "TF", "ZIELSPIELER", "TARGET FORWARD"),
			MetricProfile.ZIELSTUERMER
	),
	FALSCHE_NEUN(
			"F9 (U)",
			"Falsche Neun",
			List.of("F9", "FALSE NINE", "FALSCHE NEUN"),
			MetricProfile.ACHTER_KREATIV
	);

	private final String code;
	private final String displayName;
	private final List<String> aliases;
	private final MetricProfile profile;

	TacticRole(String code, String displayName, List<String> aliases, MetricProfile profile) {
		this.code = code;
		this.displayName = displayName;
		this.aliases = aliases;
		this.profile = profile;
	}

	public String getCode() { return code; }
	public String getDisplayName() { return displayName; }
	public MetricProfile getProfile() { return profile; }

	public String getStat1Header() { return profile.getStat1(); }
	public String getStat2Header() { return profile.getStat2(); }
	public String getStat3Header() { return profile.getStat3(); }

	public static TacticRole fromDisplayName(String name) {
		if (name == null || name.isBlank()) return ZSM;

		// 1. Exakter Match auf DisplayName, Code oder Enum-Name
		for (TacticRole r : values()) {
			if (r.getDisplayName().equalsIgnoreCase(name) ||
					r.getCode().equalsIgnoreCase(name) ||
					r.name().equalsIgnoreCase(name)) {
				return r;
			}
		}

		// 2. Suche über Aliases (inklusive neuer Zwei-Pass-Namen)
		String clean = name.toUpperCase().replace(".", "").replace("\u00A0", " ").trim();
		for (TacticRole r : values()) {
			for (String alias : r.aliases) {
				String cleanAlias = alias.toUpperCase().replace(".", "").trim();
				if (clean.equalsIgnoreCase(cleanAlias) || clean.equals(cleanAlias)) {
					return r;
				}
			}
		}

		// 3. Fallback über Positions- / Keyword-Parsing
		return fromPosition(name);
	}

	public static TacticRole fromPosition(String pos) {
		if (pos == null || pos.isBlank() || pos.equals("-")) {
			return ZSM;
		}

		String clean = pos;
		if (clean.contains("-")) {
			clean = clean.split("-")[0];
		}

		clean = clean.replaceAll("(?i)\\s*\\((unterstützen|verteidigen|angreifen|rückendeckung|vorstopper)\\)", "");

		clean = clean.toUpperCase()
				.replace("\u00A0", " ")
				.replace(".", "")
				.replace("—", "")
				.replaceAll("\\s+", " ")
				.trim();

		if (clean.matches("^[EB]\\d+$") || clean.equals("ERS") || clean.isEmpty()) {
			return ZSM;
		}

		for (TacticRole r : values()) {
			String cleanDisplayName = r.displayName.replaceAll("(?i)\\s*\\((unterstützen|verteidigen|angreifen)\\)", "").toUpperCase().replace(".", "").trim();
			String cleanCode = r.code.replaceAll("(?i)\\s*\\((unterstützen|verteidigen|angreifen)\\)", "").toUpperCase().replace(".", "").trim();

			if (clean.equalsIgnoreCase(r.name()) ||
					clean.equalsIgnoreCase(cleanCode) ||
					clean.equalsIgnoreCase(cleanDisplayName)) {
				return r;
			}
		}

		for (TacticRole r : values()) {
			for (String alias : r.aliases) {
				String cleanAlias = alias.toUpperCase().replace(".", "").trim();
				if (clean.equalsIgnoreCase(cleanAlias) || clean.startsWith(cleanAlias + " ")) {
					return r;
				}
			}
		}

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