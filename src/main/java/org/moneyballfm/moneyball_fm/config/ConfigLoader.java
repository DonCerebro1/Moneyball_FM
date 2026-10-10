package org.moneyballfm.moneyball_fm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;

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
				// Fallback, falls im Package-Pfad
				is = ConfigLoader.class.getResourceAsStream("/org/moneyballfm/moneyball_fm/moneyball-config.yaml");
			}
			if (is == null) {
				System.err.println("WARNUNG: moneyball-config.yaml konnte nicht gefunden werden!");
				return new AppConfig();
			}
			cachedConfig = mapper.readValue(is, AppConfig.class);
			return cachedConfig;
		} catch (Exception e) {
			e.printStackTrace();
			return new AppConfig();
		}
	}
}