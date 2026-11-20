package com.taller.auth.error;

import com.taller.auth.exception.InvalidCredentialsException;
import com.taller.auth.exception.InvalidRoleException;
import com.taller.auth.exception.UltimoAdminActivoException;
import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.auth.exception.UsuarioNotFoundException;
import com.taller.common.error.ErrorCode;
import com.taller.common.error.ErrorDefinition;
import com.taller.common.error.ErrorPolicy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class AuthErrorPolicy implements ErrorPolicy {

    @Override
    public boolean supports(Throwable error) {
        return error instanceof InvalidCredentialsException
                || error instanceof InvalidRoleException
                || error instanceof UltimoAdminActivoException
                || error instanceof UserAlreadyExistsException
                || error instanceof UsuarioNotFoundException;
    }

    @Override
    public ErrorDefinition resolve(Throwable error) {
        if (error instanceof UserAlreadyExistsException) {
            return ErrorDefinition.of(ErrorCode.USER_ALREADY_EXISTS,
                    "Ya existe un usuario con ese username");
        }
        if (error instanceof InvalidCredentialsException) {
            return ErrorDefinition.of(ErrorCode.INVALID_CREDENTIALS);
        }
        if (error instanceof InvalidRoleException) {
            return ErrorDefinition.of(ErrorCode.INVALID_ROLE, error.getMessage());
        }
        if (error instanceof UsuarioNotFoundException) {
            return ErrorDefinition.of(ErrorCode.NOT_FOUND, error.getMessage());
        }
        return ErrorDefinition.of(ErrorCode.ULTIMO_ADMIN_ACTIVO, error.getMessage());
    }
}
