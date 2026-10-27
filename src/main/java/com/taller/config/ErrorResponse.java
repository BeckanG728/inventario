package com.taller.config;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String error,
        String message,
        List<FieldDetail> details,
        String endpoint,
        Instant timestamp) {

    public record FieldDetail(String field, String reason) {
    }

    public static ErrorResponse of(String error, String message, String endpoint) {
        return new ErrorResponse(error, message, List.of(), endpoint, Instant.now());
    }
}