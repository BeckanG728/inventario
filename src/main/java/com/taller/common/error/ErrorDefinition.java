package com.taller.common.error;

import com.taller.config.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.List;

/** Resultado de traducir una excepción o un estado HTTP al contrato de errores. */
public record ErrorDefinition(
        HttpStatus status,
        String code,
        String message,
        List<ErrorResponse.FieldDetail> details,
        HttpHeaders headers) {

    public static ErrorDefinition of(ErrorCode code, String message) {
        return new ErrorDefinition(code.status(), code.name(), message, List.of(), new HttpHeaders());
    }

    public static ErrorDefinition of(ErrorCode code) {
        return of(code, code.mensaje());
    }

    public static ErrorDefinition of(HttpStatus status, String code, String message) {
        return new ErrorDefinition(status, code, message, List.of(), new HttpHeaders());
    }
}
