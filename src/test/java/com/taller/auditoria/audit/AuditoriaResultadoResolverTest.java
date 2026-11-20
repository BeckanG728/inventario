package com.taller.auditoria.audit;

import com.taller.auth.exception.InvalidCredentialsException;
import com.taller.auth.exception.InvalidRoleException;
import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.auth.exception.UsuarioNotFoundException;
import com.taller.common.exception.DuplicateResourceException;
import com.taller.common.exception.StockInsuficienteException;
import com.taller.producto.exception.ProductoNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AuditoriaResultadoResolverTest {

    @Test
    void sinExcepcionYEstadoOk_devuelveOk() {
        assertEquals("OK", AuditoriaResultadoResolver.resolver(null, 200));
    }

    @Test
    void sinExcepcionPeroEstadoDeError_devuelveHttpDelEstado() {
        assertEquals("HTTP_500", AuditoriaResultadoResolver.resolver(null, 500));
    }

    @Test
    void excepcionesDeDominio_seTraducenACodigosConocidos() {
        assertEquals("USER_ALREADY_EXISTS",
                AuditoriaResultadoResolver.resolver(new UserAlreadyExistsException("user"), 409));
        assertEquals("INVALID_ROLE",
                AuditoriaResultadoResolver.resolver(new InvalidRoleException("rol"), 400));
        assertEquals("INVALID_CREDENTIALS",
                AuditoriaResultadoResolver.resolver(new InvalidCredentialsException("cred"), 401));
        assertEquals("NOT_FOUND",
                AuditoriaResultadoResolver.resolver(new ProductoNotFoundException(1L), 404));
        assertEquals("NOT_FOUND",
                AuditoriaResultadoResolver.resolver(new UsuarioNotFoundException(2L), 404));
        assertEquals("STOCK_INSUFICIENTE",
                AuditoriaResultadoResolver.resolver(new StockInsuficienteException("stock"), 409));
        assertEquals("DUPLICATE_RESOURCE",
                AuditoriaResultadoResolver.resolver(new DuplicateResourceException("dup"), 409));
        assertEquals("DATA_INTEGRITY_VIOLATION",
                AuditoriaResultadoResolver.resolver(new DataIntegrityViolationException("sql"), 409));
        assertEquals("FORBIDDEN",
                AuditoriaResultadoResolver.resolver(new AccessDeniedException("denegado"), 403));
    }

    @Test
    void excepcionesDeValidacion_seTraducenAValidationError() {
        assertEquals("VALIDATION_ERROR",
                AuditoriaResultadoResolver.resolver(new IllegalArgumentException("x"), 400));
        assertEquals("VALIDATION_ERROR",
                AuditoriaResultadoResolver.resolver(mock(HttpMessageNotReadableException.class), 400));
        assertEquals("VALIDATION_ERROR",
                AuditoriaResultadoResolver.resolver(mock(MethodArgumentNotValidException.class), 400));
    }

    @Test
    void excepcionNoConocida_devuelveElHttpDelEstado() {
        assertEquals("HTTP_503", AuditoriaResultadoResolver.resolver(new RuntimeException(), 503));
    }
}
