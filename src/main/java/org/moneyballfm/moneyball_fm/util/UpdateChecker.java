package org.moneyballfm.moneyball_fm.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

	private static final String CURRENT_VERSION = "1.0.1";
	// Nutzt /tags als primäre Quelle, damit reine Git-Tags und Releases gleichermaßen funktionieren
	private static final String GITHUB_TAGS_API = "https://api.github.com/repos/DonCerebro1/Moneyball_FM/tags";
	private static final String GITHUB_REPO_URL = "https://github.com/DonCerebro1/Moneyball_FM/releases";

	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+(\\.\\d+)?)");

	public static void checkForUpdatesAsync() {
		Thread updateThread = new Thread(() -> {
			try {
				HttpClient client = HttpClient.newBuilder()
						.connectTimeout(Duration.ofSeconds(5))
						.build();

				// GitHub REST API verlangt zwingend einen User-Agent-Header!
				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create(GITHUB_TAGS_API))
						.header("Accept", "application/vnd.github+json")
						.header("User-Agent", "MoneyballFM-App")
						.GET()
						.build();

				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				if (response.statusCode() == 200) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode root = mapper.readTree(response.body());

					// Erste Tag-Position ist das neueste Tag
					if (root.isArray() && !root.isEmpty()) {
						String rawTagName = root.get(0).path("name").asText("");
						String latestVersion = extractVersionNumbers(rawTagName);

						if (isNewerVersion(latestVersion, CURRENT_VERSION)) {
							Platform.runLater(() -> showUpdateAlert(rawTagName, GITHUB_REPO_URL));
						}
					}
				} else {
					System.err.println("[UpdateChecker] GitHub API Antwort: HTTP " + response.statusCode());
				}
			} catch (Exception e) {
				System.err.println("[UpdateChecker] Fehler beim Prüfen: " + e.getMessage());
			}
		});

		updateThread.setDaemon(true);
		updateThread.setName("UpdateCheckerThread");
		updateThread.start();
	}

	/**
	 * Filtert Versionsnummern auch aus Strings wie "Version 1.0.1" oder "v1.0.1" heraus.
	 */
	private static String extractVersionNumbers(String raw) {
		if (raw == null) return "";
		Matcher m = VERSION_PATTERN.matcher(raw);
		if (m.find()) {
			return m.group(1);
		}
		return raw.replaceAll("[^0-9.]", "").trim();
	}

	/**
	 * Semantischer Versionsvergleich ("1.0.1" > "1.0.0").
	 */
	private static boolean isNewerVersion(String latest, String current) {
		if (latest.isBlank() || current.isBlank()) return false;

		String[] latestParts = latest.split("\\.");
		String[] currentParts = current.split("\\.");

		int length = Math.max(latestParts.length, currentParts.length);
		for (int i = 0; i < length; i++) {
			int vLatest = i < latestParts.length ? parsePart(latestParts[i]) : 0;
			int vCurrent = i < currentParts.length ? parsePart(currentParts[i]) : 0;

			if (vLatest > vCurrent) return true;
			if (vLatest < vCurrent) return false;
		}
		return false;
	}

	private static int parsePart(String part) {
		try {
			String num = part.replaceAll("[^0-9]", "");
			return num.isEmpty() ? 0 : Integer.parseInt(num);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static void showUpdateAlert(String newVersion, String url) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Update verfügbar");
		alert.setHeaderText("Neue Moneyball-Version verfügbar!");
		alert.setContentText("Version " + newVersion + " ist auf GitHub verfügbar. Möchtest du sie herunterladen?");

		ButtonType btnDownload = new ButtonType("Herunterladen");
		ButtonType btnLater = new ButtonType("Später");
		alert.getButtonTypes().setAll(btnDownload, btnLater);

		alert.showAndWait().ifPresent(type -> {
			if (type == btnDownload) {
				try {
					Desktop.getDesktop().browse(URI.create(url));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}