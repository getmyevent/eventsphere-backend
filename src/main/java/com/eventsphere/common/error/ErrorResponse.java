package com.eventsphere.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        Map<String, String> validationErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path, String correlationId) {
        return new ErrorResponse(Instant.now(), status, error, message, path, correlationId, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path, String correlationId, Map<String, String> validationErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, path, correlationId, validationErrors);
    }
}
