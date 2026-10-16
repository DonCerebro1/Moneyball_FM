package org.moneyballfm.moneyball_fm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ConfigLoader {
	private static AppConfig cachedConfig;

	private static final String MAIN_CONFIG_FILENAME = "moneyball-config.yaml";

	// Exakte Namen deiner YAML-Dateien
	private static final List<String> DEFAULT_TACTIC_FILES = List.of(
			"1-taktik-4-3-3-gegenpress.yaml",
			"2-taktik-3-4-2-1.yaml"
	);

	public static AppConfig loadConfig() {
		if (cachedConfig != null) {
			return cachedConfig;
		}
		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

			// 1. Sicherstellen, dass moneyball-config.yaml extern existiert
			extractDefaultMainConfigIfMissing();

			// 2. Primär von externer Datei laden, Fallback auf Classpath
			Path externalMainConfig = Paths.get(MAIN_CONFIG_FILENAME);
			if (Files.exists(externalMainConfig)) {
				try (InputStream is = Files.newInputStream(externalMainConfig)) {
					cachedConfig = mapper.readValue(is, AppConfig.class);
					System.out.println("moneyball-config.yaml erfolgreich von extern geladen: " + externalMainConfig.toAbsolutePath());
				}
			} else {
				InputStream is = ConfigLoader.class.getResourceAsStream("/" + MAIN_CONFIG_FILENAME);
				if (is == null) {
					is = ConfigLoader.class.getResourceAsStream("/org/moneyballfm/moneyball_fm/" + MAIN_CONFIG_FILENAME);
				}
				if (is == null) {
					System.err.println("WARNUNG: moneyball-config.yaml konnte weder extern noch intern gefunden werden!");
					cachedConfig = new AppConfig();
				} else {
					cachedConfig = mapper.readValue(is, AppConfig.class);
				}
			}

			if (cachedConfig.getTactics() == null) {
				cachedConfig.setTactics(new ArrayList<>());
			}

			// Vor dem Einlesen: Standard-Taktiken entpacken bzw. aus src kopieren, falls Ordner leer ist
			extractDefaultTacticsIfEmpty();

			// Externe Taktiken aus dem Ordner "tactics" einlesen
			loadExternalTactics(mapper, cachedConfig);

			return cachedConfig;
		} catch (Exception e) {
			e.printStackTrace();
			return new AppConfig();
		}
	}

	public static void reloadConfig() {
		cachedConfig = null;
		loadConfig();
	}

	private static void extractDefaultMainConfigIfMissing() {
		Path target = Paths.get(MAIN_CONFIG_FILENAME);
		if (Files.exists(target)) {
			return;
		}

		try {
			// 1. Entwicklungsmodus: Aus src/main/resources kopieren
			Path devSource = Paths.get("src", "main", "resources", MAIN_CONFIG_FILENAME);
			if (Files.exists(devSource)) {
				Files.copy(devSource, target, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Standard moneyball-config.yaml aus src/main/resources kopiert nach: " + target.toAbsolutePath());
				return;
			}

			// 2. Build- / JAR-Modus: Aus Classpath entpacken
			InputStream in = ConfigLoader.class.getResourceAsStream("/" + MAIN_CONFIG_FILENAME);
			if (in == null) {
				in = ConfigLoader.class.getResourceAsStream("/org/moneyballfm/moneyball_fm/" + MAIN_CONFIG_FILENAME);
			}
			if (in == null && ConfigLoader.class.getClassLoader() != null) {
				in = ConfigLoader.class.getClassLoader().getResourceAsStream(MAIN_CONFIG_FILENAME);
			}

			if (in != null) {
				try (InputStream input = in;
				     OutputStream out = Files.newOutputStream(target)) {
					input.transferTo(out);
					System.out.println("Standard moneyball-config.yaml aus Classpath entpackt nach: " + target.toAbsolutePath());
				}
			} else {
				System.err.println("Warnung: Standard-moneyball-config.yaml nicht in Ressourcen gefunden.");
			}
		} catch (Exception ex) {
			System.err.println("Fehler beim Extrahieren der externen moneyball-config.yaml: " + ex.getMessage());
		}
	}

	private static void extractDefaultTacticsIfEmpty() {
		Path tacticDir = Paths.get("tactics");
		try {
			if (!Files.exists(tacticDir)) {
				Files.createDirectories(tacticDir);
				System.out.println("Ordner 'tactics' neu angelegt unter: " + tacticDir.toAbsolutePath());
			}

			boolean hasYaml;
			try (Stream<Path> entries = Files.list(tacticDir)) {
				hasYaml = entries.anyMatch(p -> p.toString().toLowerCase().endsWith(".yaml")
						|| p.toString().toLowerCase().endsWith(".yml"));
			}

			if (!hasYaml) {
				System.out.println("Keine YAML-Dateien im Ordner 'tactics' gefunden. Extrahiere Standard-Taktiken...");
				for (String filename : DEFAULT_TACTIC_FILES) {
					Path target = tacticDir.resolve(filename);

					// 1. IDE-Entwicklungsmodus: Direkt aus src/main/resources/tactics/ kopieren
					Path devSource = Paths.get("src", "main", "resources", "tactics", filename);
					if (Files.exists(devSource)) {
						Files.copy(devSource, target, StandardCopyOption.REPLACE_EXISTING);
						System.out.println("Standard-Taktik aus Quellordner kopiert: " + target.toAbsolutePath());
						continue;
					}

					// 2. Build- / Release-Modus: Aus Classpath/JAR entpacken
					InputStream in = ConfigLoader.class.getResourceAsStream("/tactics/" + filename);
					if (in == null) {
						in = ConfigLoader.class.getResourceAsStream("/" + filename);
					}
					if (in == null && ConfigLoader.class.getClassLoader() != null) {
						in = ConfigLoader.class.getClassLoader().getResourceAsStream("tactics/" + filename);
					}

					if (in != null) {
						try (InputStream input = in;
						     OutputStream out = Files.newOutputStream(target)) {
							input.transferTo(out);
							System.out.println("Standard-Taktik aus Classpath entpackt: " + target.toAbsolutePath());
						}
					} else {
						System.err.println("Warnung: Taktikdatei weder in src noch im Classpath gefunden: " + filename);
					}
				}
			}
		} catch (Exception ex) {
			System.err.println("Fehler beim Vorbereiten des tactics-Ordners: " + ex.getMessage());
		}
	}

	private static void loadExternalTactics(ObjectMapper mapper, AppConfig config) {
		Path tacticDir = Paths.get("tactics");
		if (!Files.exists(tacticDir)) {
			return;
		}

		try (Stream<Path> stream = Files.list(tacticDir)) {
			stream.filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
					.forEach(yamlFile -> {
						try {
							TacticConfig extTactic = mapper.readValue(yamlFile.toFile(), TacticConfig.class);
							if (extTactic != null && extTactic.getName() != null && !extTactic.getName().isBlank()) {
								config.getTactics().removeIf(t -> t.getName().equalsIgnoreCase(extTactic.getName()));
								config.getTactics().add(extTactic);
								System.out.println("Taktik erfolgreich geladen: " + extTactic.getName());
							}
						} catch (Exception ex) {
							System.err.println("Fehler beim Laden von " + yamlFile.getFileName() + ": " + ex.getMessage());
						}
					});
		} catch (Exception ex) {
			System.err.println("Fehler beim Scannen des tactics-Ordners: " + ex.getMessage());
		}
	}
}