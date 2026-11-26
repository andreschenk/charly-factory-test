package ch.noveit.ai.charly.factory.test;

import java.time.Instant;
import java.util.List;

/**
 * Problem-Details-Antwort nach RFC 9457.
 */
record ProblemDetail(String type, String title, int status, String detail, List<FieldError> errors) {

    /**
     * Ein betroffenes Feld bei Validierungsfehlern.
     */
    record FieldError(String field, String message) {
    }
}
