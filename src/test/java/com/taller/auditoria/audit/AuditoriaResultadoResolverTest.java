package com.taller.auditoria.audit;

import com.taller.auth.error.AuthErrorPolicy;
import com.taller.auth.exception.InvalidCredentialsException;
import com.taller.auth.exception.InvalidRoleException;
import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.auth.exception.UsuarioNotFoundException;
import com.taller.common.error.DominioErrorPolicy;
import com.taller.common.error.ErrorPolicyResolver;
import com.taller.common.error.GenericErrorPolicy;
import com.taller.common.error.WebErrorPolicy;
import com.taller.common.exception.DuplicateResourceException;
import com.taller.common.exception.StockInsuficienteException;
import com.taller.producto.error.ProductoErrorPolicy;
import com.taller.producto.exception.ProductoNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AuditoriaResultadoResolverTest {

    private final ErrorPolicyResolver errores = new ErrorPolicyResolver(List.of(
            new AuthErrorPolicy(),
            new ProductoErrorPolicy(),
            new DominioErrorPolicy(),
            new WebErrorPolicy(),
            new GenericErrorPolicy()));
    private final AuditoriaResultadoResolver resolver = new AuditoriaResultadoResolver(errores);

    @Test
    void sinExcepcionYEstadoOk_devuelveOk() {
        assertEquals("OK", resolver.resolver(null, 200));
    }

    @Test
    void sinExcepcionPeroEstadoDeError_devuelveHttpDelEstado() {
        assertEquals("HTTP_500", resolver.resolver(null, 500));
    }

    @Test
    void excepcionesDeDominio_seTraducenACodigosConocidos() {
        assertEquals("USER_ALREADY_EXISTS",
                resolver.resolver(new UserAlreadyExistsException("user"), 409));
        assertEquals("INVALID_ROLE",
                resolver.resolver(new InvalidRoleException("rol"), 400));
        assertEquals("INVALID_CREDENTIALS",
                resolver.resolver(new InvalidCredentialsException("cred"), 401));
        assertEquals("NOT_FOUND",
                resolver.resolver(new ProductoNotFoundException(1L), 404));
        assertEquals("NOT_FOUND",
                resolver.resolver(new UsuarioNotFoundException(2L), 404));
        assertEquals("STOCK_INSUFICIENTE",
                resolver.resolver(new StockInsuficienteException("stock"), 409));
        assertEquals("DUPLICATE_RESOURCE",
                resolver.resolver(new DuplicateResourceException("dup"), 409));
        assertEquals("DATA_INTEGRITY_VIOLATION",
                resolver.resolver(new DataIntegrityViolationException("sql"), 409));
        assertEquals("FORBIDDEN",
                resolver.resolver(new AccessDeniedException("denegado"), 403));
    }

    @Test
    void excepcionesDeValidacion_seTraducenAValidationError() {
        assertEquals("VALIDATION_ERROR",
                resolver.resolver(new IllegalArgumentException("x"), 400));
        assertEquals("VALIDATION_ERROR",
                resolver.resolver(mock(HttpMessageNotReadableException.class), 400));
        assertEquals("VALIDATION_ERROR",
                resolver.resolver(mock(MethodArgumentNotValidException.class), 400));
        assertEquals("VALIDATION_ERROR",
                resolver.resolver(new MissingServletRequestParameterException("desde", "LocalDate"), 400));
    }

    @Test
    void erroresHttpConCodigoPropio_seAuditanConEseCodigo() {
        assertEquals("METHOD_NOT_ALLOWED",
                resolver.resolver(new HttpRequestMethodNotSupportedException(
                        "DELETE", List.of("GET")), 405));
        assertEquals("UNSUPPORTED_MEDIA_TYPE",
                resolver.resolver(new HttpMediaTypeNotSupportedException(
                        MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)), 415));
    }

    @Test
    void excepcionNoConocida_devuelveElHttpDelEstado() {
        assertEquals("HTTP_503", resolver.resolver(new RuntimeException(), 503));
    }
}
