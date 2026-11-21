package com.taller.compartido.error;

import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** Política técnica de framework: integridad de datos, acceso denegado y argumentos inválidos. */
@Component
@Order(50)
public class FrameworkErrorPolicy implements ErrorPolicy {

    @Override
    public boolean supports(Throwable error) {
        return error instanceof DataIntegrityViolationException
                || error instanceof AccessDeniedException
                || error instanceof IllegalArgumentException;
    }

    @Override
    public ErrorDefinition resolve(Throwable error) {
        if (error instanceof DataIntegrityViolationException) {
            return ErrorDefinition.of(ErrorCode.DATA_INTEGRITY_VIOLATION);
        }
        if (error instanceof AccessDeniedException) {
            return ErrorDefinition.of(ErrorCode.FORBIDDEN);
        }
        return ErrorDefinition.of(ErrorCode.VALIDATION_ERROR, error.getMessage());
    }
}
