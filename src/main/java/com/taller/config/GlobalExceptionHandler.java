package com.taller.config;

import com.taller.common.error.ErrorDefinition;
import com.taller.common.error.ErrorPolicyResolver;
import com.taller.common.error.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorPolicyResolver errorPolicyResolver;
    private final ErrorResponseFactory errorResponseFactory;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
        ErrorDefinition definition = errorPolicyResolver.resolveMvc(ex);

        return ResponseEntity.status(definition.status())
                .headers(definition.headers())
                .body(errorResponseFactory.crear(definition, request.getRequestURI()));
    }
}
