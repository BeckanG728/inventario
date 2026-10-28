package com.taller.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String JWT_ERROR_ATTRIBUTE = "jwt.error";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String TOKEN_INVALID = "TOKEN_INVALID";
    public static final String ACCOUNT_DISABLED = "ACCOUNT_DISABLED";

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String errorCode = String.valueOf(request.getAttribute(JWT_ERROR_ATTRIBUTE));

        switch (errorCode) {
            case TOKEN_EXPIRED -> errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                    TOKEN_EXPIRED, "El token ha expirado");
            case TOKEN_INVALID -> errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                    TOKEN_INVALID, "El token de autenticación no es válido");
            case ACCOUNT_DISABLED -> errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                    ACCOUNT_DISABLED, "La cuenta está desactivada");
            default -> errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED", "Falta el token de autenticación");
        }
    }
}
