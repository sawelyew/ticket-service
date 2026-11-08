package project.ticket_service.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> errors,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, null, OffsetDateTime.now());
    }

    public static ErrorResponse of(int status, String error, String message, Map<String, String> errors) {
        return new ErrorResponse(status, error, message, errors, OffsetDateTime.now());
    }
}