package ch.noveit.ai.charly.factory.test;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Globale Ausnahmebehandlung für REST-Controller. Liefert Fehlerantworten im
 * Problem-Details-Format nach RFC 9457 (application/problem+json).
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Fängt ResponseStatusException ab und liefert eine Problem-Details-Antwort.
     * Bei 400 werden die betroffenen Felder extrahiert.
     */
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ProblemDetail> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String detail = ex.getReason();

        // Prüfen, ob es sich um einen Validierungsfehler handelt und Felder extrahieren
        List<ProblemDetail.FieldError> fieldErrors = extractFieldErrors(detail);

        ProblemDetail problem = new ProblemDetail(
                "about:blank",
                status.getReasonPhrase(),
                status.value(),
                detail,
                fieldErrors);

        return ResponseEntity.status(status)
                .header("Content-Type", "application/problem+json")
                .body(problem);
    }

    /**
     * Versucht aus der Detail-Nachricht Validierungsfelder zu extrahieren.
     */
    private List<ProblemDetail.FieldError> extractFieldErrors(String detail) {
        if (detail == null) {
            return List.of();
        }
        // Prüfen, ob die Nachricht ein bekanntes Validierungsmuster enthält
        if (detail.contains("Name") || detail.toLowerCase().contains("name")) {
            return List.of(new ProblemDetail.FieldError("name", detail));
        }
        if (detail.contains("Preis") || detail.toLowerCase().contains("preis")) {
            return List.of(new ProblemDetail.FieldError("price", detail));
        }
        return List.of();
    }
}
