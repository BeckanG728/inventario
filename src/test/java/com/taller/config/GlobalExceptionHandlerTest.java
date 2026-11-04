package com.taller.config;

import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.auth.exception.InvalidRoleException;
import com.taller.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void dataIntegrityViolation_returnsConflictWithoutSqlMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/register");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("ERROR: duplicate key value violates unique constraint"),
                request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("DATA_INTEGRITY_VIOLATION", response.getBody().error());
        assertEquals("La operación viola una restricción de integridad de datos", response.getBody().message());
    }

    @Test
    void userAlreadyExists_returnsConflict() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/register");

        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExists(
                new UserAlreadyExistsException("Username already in use"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("USER_ALREADY_EXISTS", response.getBody().error());
    }

    @Test
    void invalidRole_returnsBadRequestWithInvalidRoleCode() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/register");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidRole(
                new InvalidRoleException("El rol ADMIN no puede asignarse mediante el registro"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_ROLE", response.getBody().error());
        assertEquals("El rol ADMIN no puede asignarse mediante el registro", response.getBody().message());
    }

    @Test
    void illegalArgument_returnsValidationError() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/register");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("El rol ADMIN no puede asignarse mediante el registro"),
                request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().error());
        assertEquals("El rol ADMIN no puede asignarse mediante el registro", response.getBody().message());
    }

    @Test
    void methodArgumentTypeMismatch_returnsValidationErrorWithFieldDetail() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/reportes/movimientos");

        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", LocalDate.class, "desde", null, new IllegalArgumentException("formato inválido"));

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().error());
        assertEquals("El parámetro 'desde' no tiene un formato válido", response.getBody().message());
        assertEquals("desde", response.getBody().details().get(0).field());
    }

    @Test
    void duplicateResource_returnsConflictWithDuplicateResourceCode() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/productos");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicate(
                new DuplicateResourceException("Ya existe un producto con el código: FIL-001"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("DUPLICATE_RESOURCE", response.getBody().error());
        assertEquals("Ya existe un producto con el código: FIL-001", response.getBody().message());
    }

    @Test
    void noResourceFound_returnsNotFoundWithContract() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/inexistente");

        ResponseEntity<ErrorResponse> response = handler.handleNoResource(
                new NoResourceFoundException(HttpMethod.GET, "api/inexistente", "No static resource api/inexistente."), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND", response.getBody().error());
        assertEquals("/api/inexistente", response.getBody().endpoint());
    }

    @Test
    void methodNotSupported_returnsMethodNotAllowedWithAllowHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/productos");

        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("DELETE", List.of("GET", "POST")), request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("METHOD_NOT_ALLOWED", response.getBody().error());
        assertEquals(2, response.getHeaders().getAllow().size());
        assertTrue(response.getHeaders().getAllow().contains(HttpMethod.GET));
    }

    @Test
    void mediaTypeNotSupported_returnsUnsupportedMediaTypeWithContract() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/productos");

        ResponseEntity<ErrorResponse> response = handler.handleMediaTypeNotSupported(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)),
                request);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        assertEquals("UNSUPPORTED_MEDIA_TYPE", response.getBody().error());
    }
}
