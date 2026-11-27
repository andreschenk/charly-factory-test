package ch.noveit.ai.charly.factory.test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Problem Details für HTTP-Fehlerantworten nach RFC 9457.
 */
class ProblemDetails {

    private final String type;
    private final String title;
    private final int status;
    private final String detail;
    private final String instance;
    private final Map<String, Object> properties;

    ProblemDetails(String type, String title, int status, String detail, String instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.properties = new LinkedHashMap<>();
    }

    /**
     * Fügt ein betroffenes Feld hinzu (für Validierungsfehler).
     */
    ProblemDetails withProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    String getType() {
        return type;
    }

    String getTitle() {
        return title;
    }

    int getStatus() {
        return status;
    }

    String getDetail() {
        return detail;
    }

    String getInstance() {
        return instance;
    }

    Map<String, Object> getProperties() {
        return properties;
    }
}
