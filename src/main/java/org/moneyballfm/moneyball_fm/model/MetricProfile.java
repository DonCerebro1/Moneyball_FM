package org.moneyballfm.moneyball_fm.model;

public enum MetricProfile {

	// 1. Torhüter
	TORWART("Par %", "xG verh/90", "Pas %"),
	TORWART_KLASSISCH("Par %", "xG verh/90", "AbB/90"),

	// 2. Innenverteidigung
	IV_ZERSTOERER("Kopf %", "Zwk %", "AbB/90"),
	IV_AUFBAU("Pas %", "Zwk %", "Pr Pässe/90"),

	// 3. Außen- & Flügelverteidiger
	AV_DEFENSIV("Zwk %", "AbB/90", "Pas %"),
	AV_OFFENSIV("AbB/90", "FPQ %", "Drb/90"),
	AV_INVERS("Pas %", "Pr Pässe/90", "AbB/90"),

	// 4. Sechser & Mittelfeld-Zentrale
	SECHSER_ABRAEUMER("Pas %", "AbB/90", "Zwk %"),
	SECHSER_SPIELMACHER("Pas %", "Pr Pässe/90", "E Pä/90"),

	// 5. Achter
	ACHTER_ALLROUND("Drb/90", "Zwk %", "Pas %"),
	ACHTER_KREATIV("E Pä/90", "xA/90", "Pr Pässe/90"),

	// 6. Flügelspieler & Außen
	FLUEGEL_VORBEREITER("FPQ %", "Drb/90", "xA/90"),
	FLUEGEL_INVERS("Drb/90", "E Pä/90", "Schüsse %"),

	// 7. Zehner & Spielmacher
	ZEHNER_KREATIV("xA/90", "E Pä/90", "Pas %"),

	// 8. Stürmer
	TORJAEGER("xG/90", "Verw. %", "Schüsse %"),
	ZIELSTUERMER("Kopf %", "E Pä/90", "xG/90");

	private final String stat1;
	private final String stat2;
	private final String stat3;

	MetricProfile(String stat1, String stat2, String stat3) {
		this.stat1 = stat1;
		this.stat2 = stat2;
		this.stat3 = stat3;
	}

	public String getStat1() { return stat1; }
	public String getStat2() { return stat2; }
	public String getStat3() { return stat3; }
}