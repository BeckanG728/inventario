package com.taller.config;

import com.taller.auth.error.AuthErrorPolicy;
import com.taller.auth.exception.InvalidRoleException;
import com.taller.auth.exception.UltimoAdminActivoException;
import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.auth.exception.UsuarioNotFoundException;
import com.taller.common.error.DominioErrorPolicy;
import com.taller.common.error.ErrorPolicyResolver;
import com.taller.common.error.ErrorResponseFactory;
import com.taller.common.error.GenericErrorPolicy;
import com.taller.common.error.WebErrorPolicy;
import com.taller.common.exception.DuplicateResourceException;
import com.taller.producto.error.ProductoErrorPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final ErrorPolicyResolver errores = new ErrorPolicyResolver(List.of(
            new AuthErrorPolicy(),
            new ProductoErrorPolicy(),
            new DominioErrorPolicy(),
            new WebErrorPolicy(),
            new GenericErrorPolicy()));
    private final ErrorResponseFactory fabrica = new ErrorResponseFactory(
            Clock.fixed(Instant.parse("2026-11-19T18:00:00Z"), ZoneOffset.UTC));
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(errores, fabrica);

    @Test
    void dataIntegrityViolation_returnsConflictWithoutSqlMessage() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new DataIntegrityViolationException("ERROR: duplicate key value violates unique constraint"),
                request("/api/auth/register"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("DATA_INTEGRITY_VIOLATION", response.getBody().error());
        assertEquals("La operación viola una restricción de integridad de datos", response.getBody().message());
    }

    @Test
    void userAlreadyExists_returnsConflict() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new UserAlreadyExistsException("Username already in use"), request("/api/auth/register"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("USER_ALREADY_EXISTS", response.getBody().error());
    }

    @Test
    void invalidRole_returnsBadRequestWithInvalidRoleCode() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new InvalidRoleException("El rol ADMIN no puede asignarse mediante el registro"),
                request("/api/auth/register"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_ROLE", response.getBody().error());
        assertEquals("El rol ADMIN no puede asignarse mediante el registro", response.getBody().message());
    }

    @Test
    void usuarioNotFound_returnsNotFoundWithContract() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new UsuarioNotFoundException(99L), request("/api/usuarios/99"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND", response.getBody().error());
        assertEquals("/api/usuarios/99", response.getBody().endpoint());
    }

    @Test
    void ultimoAdminActivo_returnsConflictWithOwnCode() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new UltimoAdminActivoException(), request("/api/usuarios/1"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("ULTIMO_ADMIN_ACTIVO", response.getBody().error());
    }

    @Test
    void illegalArgument_returnsValidationError() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new IllegalArgumentException("El rol ADMIN no puede asignarse mediante el registro"),
                request("/api/auth/register"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().error());
        assertEquals("El rol ADMIN no puede asignarse mediante el registro", response.getBody().message());
    }

    @Test
    void methodArgumentTypeMismatch_returnsValidationErrorWithFieldDetail() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", LocalDate.class, "desde", null, new IllegalArgumentException("formato inválido"));

        ResponseEntity<ErrorResponse> response = handler.handle(ex, request("/api/reportes/movimientos"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().error());
        assertEquals("El parámetro 'desde' no tiene un formato válido", response.getBody().message());
        assertEquals("desde", response.getBody().details().get(0).field());
    }

    @Test
    void missingServletRequestParameter_returnsValidationErrorWithContract() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new MissingServletRequestParameterException("desde", "LocalDate"),
                request("/api/reportes/movimientos"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().error());
        assertEquals("desde", response.getBody().details().get(0).field());
    }

    @Test
    void duplicateResource_returnsConflictWithDuplicateResourceCode() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new DuplicateResourceException("Ya existe un producto con el código: FIL-001"),
                request("/api/productos"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("DUPLICATE_RESOURCE", response.getBody().error());
        assertEquals("Ya existe un producto con el código: FIL-001", response.getBody().message());
    }

    @Test
    void noResourceFound_returnsNotFoundWithContract() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new NoResourceFoundException(HttpMethod.GET, "api/inexistente", "No static resource api/inexistente."),
                request("/api/inexistente"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND", response.getBody().error());
        assertEquals("/api/inexistente", response.getBody().endpoint());
    }

    @Test
    void methodNotSupported_returnsMethodNotAllowedWithAllowHeader() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new HttpRequestMethodNotSupportedException("DELETE", List.of("GET", "POST")),
                request("/api/productos"));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("METHOD_NOT_ALLOWED", response.getBody().error());
        assertEquals(2, response.getHeaders().getAllow().size());
        assertTrue(response.getHeaders().getAllow().contains(HttpMethod.GET));
    }

    @Test
    void mediaTypeNotSupported_returnsUnsupportedMediaTypeWithContract() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)),
                request("/api/productos"));

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        assertEquals("UNSUPPORTED_MEDIA_TYPE", response.getBody().error());
    }

    @Test
    void excepcionNoConocida_usaInternalError() {
        ResponseEntity<ErrorResponse> response = handler.handle(
                new IllegalStateException("boom"), request("/api/productos"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().error());
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }
}
