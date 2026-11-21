package com.taller.seguridad.autenticacion.security;

import com.taller.compartido.error.ErrorCode;
import com.taller.compartido.error.ErrorDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationEntryPointTest {

    @Mock
    private ErrorResponseWriter errorResponseWriter;

    @InjectMocks
    private TokenAuthenticationEntryPoint entryPoint;

    @Test
    void missingToken_returnsUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("unauth"));

        verify(errorResponseWriter).write(request, response,
                ErrorDefinition.of(ErrorCode.UNAUTHORIZED));
    }

    @Test
    void expiredToken_returnsTokenExpired() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(TokenAuthenticationEntryPoint.JWT_ERROR_ATTRIBUTE,
                TokenAuthenticationEntryPoint.TOKEN_EXPIRED);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("unauth"));

        verify(errorResponseWriter).write(request, response,
                ErrorDefinition.of(ErrorCode.TOKEN_EXPIRED));
    }

    @Test
    void invalidToken_returnsTokenInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(TokenAuthenticationEntryPoint.JWT_ERROR_ATTRIBUTE,
                TokenAuthenticationEntryPoint.TOKEN_INVALID);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("unauth"));

        verify(errorResponseWriter).write(request, response,
                ErrorDefinition.of(ErrorCode.TOKEN_INVALID));
    }

    @Test
    void disabledAccount_returnsAccountDisabled() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(TokenAuthenticationEntryPoint.JWT_ERROR_ATTRIBUTE,
                TokenAuthenticationEntryPoint.ACCOUNT_DISABLED);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("unauth"));

        verify(errorResponseWriter).write(request, response,
                ErrorDefinition.of(ErrorCode.ACCOUNT_DISABLED));
    }
}
