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

public class UpdateChecker {

		private static final String CURRENT_VERSION = "1.0.0";
		private static final String GITHUB_API_URL = "https://api.github.com/repos/DonCerebro1/Moneyball_FM/releases/latest";
		public static void checkForUpdatesAsync() {
				Thread updateThread = new Thread(() -> {
						try {
								HttpClient client = HttpClient.newBuilder()
												.connectTimeout(Duration.ofSeconds(4))
												.build();

								HttpRequest request = HttpRequest.newBuilder()
												.uri(URI.create(GITHUB_API_URL))
												.header("Accept", "application/vnd.github+json")
												.GET()
												.build();

								HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

								if (response.statusCode() == 200) {
										ObjectMapper mapper = new ObjectMapper();
										JsonNode root = mapper.readTree(response.body());

										String latestTag = root.path("tag_name").asText("").replace("v", "");
										String releaseUrl = root.path("html_url").asText();

										if (isNewerVersion(latestTag, CURRENT_VERSION)) {
												Platform.runLater(() -> showUpdateAlert(latestTag, releaseUrl));
										}
								}
						} catch (Exception ignored) {
								// Keine Internetverbindung oder API-Fehler -> normal fortfahren
						}
				});

				updateThread.setDaemon(true);
				updateThread.setName("UpdateCheckerThread");
				updateThread.start();
		}

		private static boolean isNewerVersion(String latest, String current) {
				// Simpler Vergleich oder semantischer Version-Split
				return !latest.isBlank() && !latest.equalsIgnoreCase(current);
		}

		private static void showUpdateAlert(String newVersion, String url) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Update verfügbar");
				alert.setHeaderText("Neue Moneyball-Version verfügbar!");
				alert.setContentText("Version " + newVersion + " ist da. Möchtest du sie herunterladen?");

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