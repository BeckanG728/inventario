package com.taller.compartido.error;

import org.springframework.http.HttpStatus;

/** Catálogo único de códigos de error, estados y mensajes por defecto. */
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "La petición contiene campos no válidos"),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "El rol indicado no es válido"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Falta el token de autenticación"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "El token ha expirado"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "El token de autenticación no es válido"),
    ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "La cuenta está desactivada"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta acción"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "El recurso solicitado no existe"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "El método HTTP no está soportado para este endpoint"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Ya existe un usuario con ese username"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "La operación viola una restricción de integridad de datos"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "La operación entra en conflicto con un recurso existente"),
    ULTIMO_ADMIN_ACTIVO(HttpStatus.CONFLICT, "No se puede desactivar al último ADMIN activo"),
    STOCK_INSUFICIENTE(HttpStatus.CONFLICT, "Stock insuficiente"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "El tipo de contenido no está soportado; se espera application/json"),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Demasiadas peticiones; intente nuevamente más tarde"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno no previsto");

    private final HttpStatus status;
    private final String mensaje;

    ErrorCode(HttpStatus status, String mensaje) {
        this.status = status;
        this.mensaje = mensaje;
    }

    public HttpStatus status() {
        return status;
    }

    public String mensaje() {
        return mensaje;
    }

    public static ErrorCode desdeAtributoJwt(String valor) {
        return switch (valor) {
            case "TOKEN_EXPIRED" -> TOKEN_EXPIRED;
            case "TOKEN_INVALID" -> TOKEN_INVALID;
            case "ACCOUNT_DISABLED" -> ACCOUNT_DISABLED;
            default -> UNAUTHORIZED;
        };
    }
}
