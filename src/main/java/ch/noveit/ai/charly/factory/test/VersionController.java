package ch.noveit.ai.charly.factory.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller für den Version-Endpunkt. Liefert Name und Version der Anwendung als JSON.
 */
@RestController
public class VersionController {

    /**
     * Gibt den Namen und die Version der Anwendung zurück.
     * Die Werte werden aus der pom.xml (artifactId und version) gelesen.
     *
     * @return ein Objekt mit den Feldern 'name' und 'version'
     */
    @GetMapping("/version")
    public VersionInfo version() {
        return new VersionInfo(App.name(), App.version());
    }

    /**
     * DTO für die Versionsinformation.
     */
    static class VersionInfo {

        private final String name;
        private final String version;

        VersionInfo(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }
    }
}
