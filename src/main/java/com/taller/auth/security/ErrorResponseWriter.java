package com.taller.auth.security;

import com.taller.common.error.ErrorDefinition;
import com.taller.common.error.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;
    private final ErrorResponseFactory errorResponseFactory;

    public void write(HttpServletRequest request, HttpServletResponse response,
                      ErrorDefinition definition) throws IOException {
        definition.headers().forEach((nombre, valores) ->
                valores.forEach(valor -> response.addHeader(nombre, valor)));

        String json = objectMapper.writeValueAsString(
                errorResponseFactory.crear(definition, request.getRequestURI()));

        response.setStatus(definition.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    /**
     * Firma anterior, conservada mientras los filtros de rate limiting siguen usando el catálogo directamente.
     */
    @Deprecated
    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
                      String error, String message) throws IOException {
        write(request, response, ErrorDefinition.of(status, error, message));
    }
}
