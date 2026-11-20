package com.taller.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        String error,
        String message,
        List<FieldDetail> details,
        String endpoint,
        Instant timestamp) {

    public record FieldDetail(String field, String reason) {
    }
}
