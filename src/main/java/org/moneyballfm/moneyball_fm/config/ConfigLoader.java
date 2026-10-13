package org.moneyballfm.moneyball_fm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class ConfigLoader {
	private static AppConfig cachedConfig;

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

	private static void loadExternalTactics(ObjectMapper mapper, AppConfig config) {
		Path tacticDir = Paths.get("tactics");
		if (!Files.exists(tacticDir)) {
			try {
				Files.createDirectories(tacticDir);
			} catch (Exception ignored) {}
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