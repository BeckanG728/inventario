package com.taller.auditoria.audit;

import com.taller.auth.exception.InvalidCredentialsException;
import com.taller.auth.exception.InvalidRoleException;
import com.taller.auth.exception.UltimoAdminActivoException;
import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.auth.exception.UsuarioNotFoundException;
import com.taller.common.exception.DuplicateResourceException;
import com.taller.common.exception.StockInsuficienteException;
import com.taller.producto.exception.ProductoNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

public final class AuditoriaResultadoResolver {

    private AuditoriaResultadoResolver() {
    }

    public static String resolver(Throwable ex, int status) {
        if (ex == null) {
            return status >= 400 ? "HTTP_" + status : "OK";
        }
        if (ex instanceof UserAlreadyExistsException) {
            return "USER_ALREADY_EXISTS";
        }
        if (ex instanceof InvalidRoleException) {
            return "INVALID_ROLE";
        }
        if (ex instanceof InvalidCredentialsException) {
            return "INVALID_CREDENTIALS";
        }
        if (ex instanceof UsuarioNotFoundException || ex instanceof ProductoNotFoundException
                || ex instanceof NoResourceFoundException) {
            return "NOT_FOUND";
        }
        if (ex instanceof UltimoAdminActivoException) {
            return "ULTIMO_ADMIN_ACTIVO";
        }
        if (ex instanceof StockInsuficienteException) {
            return "STOCK_INSUFICIENTE";
        }
        if (ex instanceof DuplicateResourceException) {
            return "DUPLICATE_RESOURCE";
        }
        if (ex instanceof DataIntegrityViolationException) {
            return "DATA_INTEGRITY_VIOLATION";
        }
        if (ex instanceof AccessDeniedException) {
            return "FORBIDDEN";
        }
        if (ex instanceof IllegalArgumentException
                || ex instanceof MethodArgumentNotValidException
                || ex instanceof MethodArgumentTypeMismatchException
                || ex instanceof HttpMessageNotReadableException
                || ex instanceof MissingServletRequestParameterException
                || ex instanceof HttpMediaTypeNotSupportedException
                || ex instanceof HttpRequestMethodNotSupportedException) {
            return "VALIDATION_ERROR";
        }
        return status >= 400 ? "HTTP_" + status : "ERROR";
    }
}
