package ch.noveit.ai.charly.factory.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller für Health-Endpunkt.
 */
@RestController
public class HealthController {

    /**
     * Gibt den Health-Status des Services zurück.
     *
     * @return ein Objekt mit dem Status
     */
    @GetMapping("/health")
    public HealthStatus health() {
        return new HealthStatus("UP", "Charly Factory Test");
    }

    /**
     * DTO für Health-Status.
     */
    public static class HealthStatus {
        private final String status;
        private final String application;

        public HealthStatus(String status, String application) {
            this.status = status;
            this.application = application;
        }

        public String getStatus() {
            return status;
        }

        public String getApplication() {
            return application;
        }
    }
}
