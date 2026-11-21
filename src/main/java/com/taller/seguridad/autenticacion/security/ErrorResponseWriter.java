package com.taller.seguridad.autenticacion.security;

import com.taller.compartido.error.ErrorDefinition;
import com.taller.compartido.error.ErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
}
