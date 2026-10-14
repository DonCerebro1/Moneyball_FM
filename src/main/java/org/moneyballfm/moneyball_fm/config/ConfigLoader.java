package org.moneyballfm.moneyball_fm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ConfigLoader {
	private static AppConfig cachedConfig;

	// Standard-Taktikdateien, die in src/main/resources/tactics/ liegen
	private static final List<String> DEFAULT_TACTIC_FILES = List.of(
			"4-3-3 Gegenpress.yaml",
			"2. Taktik (Asymmetrisch).yaml"
	);

	public static AppConfig loadConfig() {
		if (cachedConfig != null) {
			return cachedConfig;
		}
		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			InputStream is = ConfigLoader.class.getResourceAsStream("/moneyball-config.yaml");
			if (is == null) {
				is = ConfigLoader.class.getResourceAsStream("/org/moneyballfm/moneyball_fm/moneyball-config.yaml");
			}
			if (is == null) {
				System.err.println("WARNUNG: moneyball-config.yaml konnte nicht gefunden werden!");
				cachedConfig = new AppConfig();
			} else {
				cachedConfig = mapper.readValue(is, AppConfig.class);
			}

			if (cachedConfig.getTactics() == null) {
				cachedConfig.setTactics(new ArrayList<>());
			}

			// Vor dem Einlesen: Standard-Taktiken entpacken, falls der tactics-Ordner leer ist
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

	private static void extractDefaultTacticsIfEmpty() {
		Path tacticDir = Paths.get("tactics");
		try {
			if (!Files.exists(tacticDir)) {
				Files.createDirectories(tacticDir);
			}

			boolean isEmpty;
			try (Stream<Path> entries = Files.list(tacticDir)) {
				isEmpty = entries.noneMatch(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"));
			}

			if (isEmpty) {
				for (String filename : DEFAULT_TACTIC_FILES) {
					// Sucht in /tactics/<Name> oder direkt im Root der Ressourcen
					InputStream in = ConfigLoader.class.getResourceAsStream("/tactics/" + filename);
					if (in == null) {
						in = ConfigLoader.class.getResourceAsStream("/" + filename);
					}

					if (in != null) {
						Path target = tacticDir.resolve(filename);
						try (InputStream input = in;
						     OutputStream out = Files.newOutputStream(target)) {
							input.transferTo(out);
							System.out.println("Standard-Taktik entpackt: " + target.toAbsolutePath());
						}
					}
				}
			}
		} catch (Exception ex) {
			System.err.println("Hinweis beim Extrahieren der Standard-Taktiken: " + ex.getMessage());
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
								// Ersetzen, falls schon vorhanden, sonst anfügen
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