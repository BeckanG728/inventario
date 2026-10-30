package com.taller.config;

import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.auth.exception.InvalidRoleException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
