package com.taller.auth.security;

import com.taller.common.error.ErrorCode;
import com.taller.common.error.ErrorDefinition;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
        ErrorCode code = ErrorCode.desdeAtributoJwt(String.valueOf(request.getAttribute(JWT_ERROR_ATTRIBUTE)));
        ErrorDefinition definition = ErrorDefinition.of(code);

        errorResponseWriter.write(request, response,
                definition.status(), definition.code(), definition.message());
    }
}
