package com.taller.config;

import com.taller.auth.exception.InvalidCredentialsException;
import com.taller.auth.exception.InvalidRoleException;
import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.producto.exception.ProductoNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex,
                                                                 HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS",
                "Ya existe un usuario con ese username", req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "La operación viola una restricción de integridad de datos", req);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                  HttpServletRequest req) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "Usuario o contraseña incorrectos", req);
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRole(InvalidRoleException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_ROLE", ex.getMessage(), req);
    }

    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductoNotFound(ProductoNotFoundException ex,
                                                                HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex,
                                                              HttpServletRequest req) {
        List<ErrorResponse.FieldDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErrorResponse.FieldDetail(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "VALIDATION_ERROR", "La petición contiene campos no válidos",
                details, req.getRequestURI(), Instant.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "La petición contiene un cuerpo JSON no válido", req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), req);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message,
                                                HttpServletRequest req) {
        return ResponseEntity.status(status).body(ErrorResponse.of(code, message, req.getRequestURI()));
    }
}
