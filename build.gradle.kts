plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "org.moneyballfm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("org.moneyballfm.moneyball_fm")
    mainClass.set("org.moneyballfm.moneyball_fm.MoneyballApp")
}

javafx {
    version = "17.0.10"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    // Schicke UI-Komponenten & Moderne Icons
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-feather-pack:12.3.1")

    // Moneyball Core-Tools
    implementation("org.jsoup:jsoup:1.17.2")                                  // FM HTML-Export Parser
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.1") // YAML-Parser
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")     // Objekt-Mapping
    implementation("org.apache.poi:poi-ooxml:5.2.5")                          // Excel (.xlsx) Erstellung

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}