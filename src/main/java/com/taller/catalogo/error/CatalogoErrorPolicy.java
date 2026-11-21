package com.taller.catalogo.error;

import com.taller.compartido.error.ErrorCode;
import com.taller.compartido.error.ErrorDefinition;
import com.taller.compartido.error.ErrorPolicy;
import com.taller.compartido.exception.DuplicateResourceException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Política de errores del catálogo: recursos duplicados (categorías, proveedores, productos). */
@Component
@Order(30)
public class CatalogoErrorPolicy implements ErrorPolicy {

    @Override
    public boolean supports(Throwable error) {
        return error instanceof DuplicateResourceException;
    }

    @Override
    public ErrorDefinition resolve(Throwable error) {
        return ErrorDefinition.of(ErrorCode.DUPLICATE_RESOURCE, error.getMessage());
    }
}
