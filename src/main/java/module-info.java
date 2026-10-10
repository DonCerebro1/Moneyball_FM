module org.moneyballfm.moneyball_fm {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;

	requires org.controlsfx.controls;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.feather;

	requires org.jsoup;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires org.apache.poi.poi;
	requires org.apache.poi.ooxml;
	requires java.desktop;

	opens org.moneyballfm.moneyball_fm to javafx.fxml;
	opens org.moneyballfm.moneyball_fm.controller to javafx.fxml;
	opens org.moneyballfm.moneyball_fm.config to com.fasterxml.jackson.databind;
	opens org.moneyballfm.moneyball_fm.model to javafx.base;

	exports org.moneyballfm.moneyball_fm;
	exports org.moneyballfm.moneyball_fm.controller;
	exports org.moneyballfm.moneyball_fm.model;
	exports org.moneyballfm.moneyball_fm.config;
}