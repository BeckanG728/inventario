package com.taller.common.error;

import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@Order(50)
public class GenericErrorPolicy implements ErrorPolicy {

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
