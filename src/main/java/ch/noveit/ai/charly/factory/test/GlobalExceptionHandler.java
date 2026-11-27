package ch.noveit.ai.charly.factory.test;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Fängt Exceptions ab und wandelt sie in Problem Details (RFC 9457) um.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Behandelt ResponseStatusException (z. B. aus der Validierung im Controller).
     */
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ProblemDetails> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = toHttpStatus(ex.getStatusCode());
        String detail = ex.getReason();

        // RFC 9457: type ist eine URI, die den Fehlertyp beschreibt
        String typeUri = "about:blank";
        String title;

        if (status == HttpStatus.BAD_REQUEST) {
            title = "Bad Request";
        } else if (status == HttpStatus.NOT_FOUND) {
            title = "Not Found";
        } else {
            title = status.getReasonPhrase();
        }

        ProblemDetails problem = new ProblemDetails(
                typeUri,
                title,
                status.value(),
                detail,
                "/products"
        );

        // Bei 400-Fehlern: betroffene Felder aus der Detail-Nachricht extrahieren
        if (status == HttpStatus.BAD_REQUEST && detail != null) {
            if (detail.contains("Name") || detail.toLowerCase().contains("name")) {
                problem.withProperty("name", "ungültig");
            }
            if (detail.contains("Preis") || detail.toLowerCase().contains("price")) {
                problem.withProperty("price", "ungültig");
            }
        }

        return ResponseEntity.status(status)
                .header("Content-Type", "application/problem+json")
                .body(problem);
    }

    /**
     * Konvertiert HttpStatusCode zu HttpStatus für den Vergleich.
     */
    private HttpStatus toHttpStatus(org.springframework.http.HttpStatusCode code) {
        try {
            return HttpStatus.valueOf(code.value());
        } catch (IllegalArgumentException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
