package com.taller.config;

import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.auth.exception.InvalidRoleException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

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
}
