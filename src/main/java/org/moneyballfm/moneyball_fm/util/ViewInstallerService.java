package org.moneyballfm.moneyball_fm.util;

import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ViewInstallerService {

	// Liste der Dateinamen, die in src/main/resources/views/ liegen
	private static final List<String> VIEW_FILES = List.of(
			"Kader_Analyse_Tabelle_Moneyball.fmf",
			"Scouting_Tabelle_Moneyball.fmf"
	);

	public static void installViews(Window parentWindow) {
		Path targetDir = getDefaultFmViewsDirectory();

		// Falls der Ordner nicht existiert (z. B. FM auf anderer Partition oder OneDrive-Verbiegung)
		if (!Files.exists(targetDir)) {
			try {
				Files.createDirectories(targetDir);
			} catch (Exception e) {
				// Fallback: Benutzer selbst wählen lassen
				DirectoryChooser chooser = new DirectoryChooser();
				chooser.setTitle("Football Manager 'views' Ordner auswählen");
				File selected = chooser.showDialog(parentWindow);
				if (selected != null) {
					targetDir = selected.toPath();
				} else {
					return; // Abgebrochen
				}
			}
		}

		int copiedCount = 0;
		try {
			for (String fileName : VIEW_FILES) {
				// Liest die Datei direkt aus der JAR/den Resources
				try (InputStream in = ViewInstallerService.class.getResourceAsStream("/views/" + fileName)) {
					if (in != null) {
						Path destination = targetDir.resolve(fileName);
						Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
						copiedCount++;
					} else {
						System.err.println("[ViewInstaller] Resource nicht gefunden: /views/" + fileName);
					}
				}
			}

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.initOwner(parentWindow);
			alert.setTitle("FM-Ansichten installiert");
			alert.setHeaderText("Installation erfolgreich!");
			alert.setContentText(copiedCount + " Ansichten wurden nach:\n"
					+ targetDir.toAbsolutePath()
					+ "\nkopiert.\n\nDu kannst sie jetzt im FM über 'Ansichten -> Importieren' auswählen.");
			alert.showAndWait();

		} catch (Exception ex) {
			Alert error = new Alert(Alert.AlertType.ERROR);
			error.initOwner(parentWindow);
			error.setTitle("Fehler");
			error.setHeaderText("Kopieren fehlgeschlagen");
			error.setContentText("Konnte Ansichten nicht kopieren: " + ex.getMessage());
			error.showAndWait();
		}
	}

	private static Path getDefaultFmViewsDirectory() {
		String userHome = System.getProperty("user.home");

		// 1. Standard-Windows-Pfad
		Path standardPath = Paths.get(userHome, "Documents", "Sports Interactive", "Football Manager 2024", "views");
		if (Files.exists(standardPath.getParent())) {
			return standardPath;
		}

		// 2. OneDrive-Dokumente (falls Windows OneDrive-Synchronisierung nutzt)
		Path oneDrivePath = Paths.get(userHome, "OneDrive", "Documents", "Sports Interactive", "Football Manager 2024", "views");
		if (Files.exists(oneDrivePath.getParent())) {
			return oneDrivePath;
		}

		// Fallback: Standard
		return standardPath;
	}
}