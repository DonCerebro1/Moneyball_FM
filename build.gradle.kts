plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "3.0.1"
}

group = "org.moneyballfm"
version = "1.0.0"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    // Zwingt badass-jlink dazu, die Log4j-JARs sauber zusammenzuführen:
    forceMerge("log4j-api", "log4j-to-slf4j")

    launcher {
        name = "FMMoneyball"
    }

    jpackage {
        // jpackage holt sich das Launcher-Icon direkt aus dem Ressourcenordner:
        resourceDir = file("src/main/package/windows")

        installerOptions = listOf(
            "--win-shortcut",
            "--win-menu"
        )
    }
}

dependencies {
    // Schicke UI-Komponenten & Moderne Icons
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("org.kordamp.ikonli:ikonli-feather-pack:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")

    // Moneyball Core-Tools
    implementation("org.jsoup:jsoup:1.17.2")                                          // FM HTML-Export Parser
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.1") // YAML-Parser
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")             // Objekt-Mapping
    implementation("org.apache.poi:poi-ooxml:5.2.5")                                  // Excel (.xlsx) Erstellung

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    implementation("com.h2database:h2:2.2.224")
}

// Kopiert den tactics-Ordner nach der Erstellung des jpackage-Images direkt neben die .exe
tasks.named("jpackageImage") {
    doLast {
        val imageDir = layout.buildDirectory.dir("jpackage/FMMoneyball").get().asFile
        val targetTactics = File(imageDir, "tactics")
        val sourceTactics = file("src/main/resources/tactics")

        if (sourceTactics.exists()) {
            copy {
                from(sourceTactics)
                into(targetTactics)
            }
            println("Tactics wurden erfolgreich nach ${targetTactics.absolutePath} kopiert.")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}