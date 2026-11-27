package ch.noveit.ai.charly.factory.test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

/**
 * Globale Ausnahmebehandlung für REST-Controller.
 * Fängt ResponseStatusException ab und antwortet im RFC 9457 Problem Details Format.
 */
@ControllerAdvice
class GlobalExceptionHandler {

    /**
     * Behandelt ungültige Eingaben (400) mit Feldinformationen.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex) {

        HttpStatusCode statusCode = ex.getStatusCode();
        Map<String, Object> problem = new LinkedHashMap<>();
        problem.put("type", "about:blank");
        problem.put("title", ex.getReason() != null ? ex.getReason() : "Fehler");
        problem.put("status", statusCode.value());
        problem.put("detail", ex.getReason());
        problem.put("instance", "/products");
        problem.put("timestamp", Instant.now().toString());

        // Bei 400-Fehlern die betroffenen Felder mitliefern
        if (statusCode == HttpStatus.BAD_REQUEST) {
            String reason = ex.getReason() != null ? ex.getReason() : "";
            Map<String, Object> violations = new LinkedHashMap<>();

            if (reason.contains("Name") || reason.contains("name")) {
                Map<String, Object> nameViolation = new LinkedHashMap<>();
                nameViolation.put("field", "name");
                nameViolation.put("message", reason);
                violations.put("name", nameViolation);
            }

            if (reason.contains("Preis") || reason.contains("price")) {
                Map<String, Object> priceViolation = new LinkedHashMap<>();
                priceViolation.put("field", "price");
                priceViolation.put("message", reason);
                violations.put("price", priceViolation);
            }

            // Bei Paginierungsfehlern
            if (reason.contains("Seitengrösse") || reason.contains("size")) {
                Map<String, Object> sizeViolation = new LinkedHashMap<>();
                sizeViolation.put("field", "size");
                sizeViolation.put("message", reason);
                violations.put("size", sizeViolation);
            }

            if (reason.contains("Seitennummer") || reason.contains("page")) {
                Map<String, Object> pageViolation = new LinkedHashMap<>();
                pageViolation.put("field", "page");
                pageViolation.put("message", reason);
                violations.put("page", pageViolation);
            }

            if (reason.contains("Sortierfeld") || reason.contains("sort")) {
                Map<String, Object> sortViolation = new LinkedHashMap<>();
                sortViolation.put("field", "sort");
                sortViolation.put("message", reason);
                violations.put("sort", sortViolation);
            }

            if (!violations.isEmpty()) {
                problem.put("violations", violations);
            }
        }

        return ResponseEntity.status(statusCode)
                .header("Content-Type", "application/problem+json")
                .body(problem);
    }
}
