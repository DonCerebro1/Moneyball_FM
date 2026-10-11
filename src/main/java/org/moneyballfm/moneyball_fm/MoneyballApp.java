package org.moneyballfm.moneyball_fm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.moneyballfm.moneyball_fm.util.UpdateChecker;

import java.io.IOException;
import java.io.InputStream;

public class MoneyballApp extends Application {
		@Override
		public void start(Stage stage) throws IOException {
				FXMLLoader fxmlLoader = new FXMLLoader(MoneyballApp.class.getResource("main-view.fxml"));
				Scene scene = new Scene(fxmlLoader.load(), 1100, 700);

				// App-Icon für Titelleiste und Windows-Taskleiste setzen
				InputStream iconStream = MoneyballApp.class.getResourceAsStream("/icons/tactic.png");
				if (iconStream != null) {
						stage.getIcons().add(new Image(iconStream));
				}

				stage.setTitle("FM Moneyball Suite");
				stage.setScene(scene);
				stage.show();

				// Asynchroner Update-Check nach dem Anzeigen des Hauptfensters
				UpdateChecker.checkForUpdatesAsync();
		}

		public static void main(String[] args) {
				launch();
		}
}