package com.taller.catalogo.producto.error;

import com.taller.compartido.error.ErrorCode;
import com.taller.compartido.error.ErrorDefinition;
import com.taller.compartido.error.ErrorPolicy;
import com.taller.catalogo.producto.exception.ProductoNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class ProductoErrorPolicy implements ErrorPolicy {

    @Override
    public boolean supports(Throwable error) {
        return error instanceof ProductoNotFoundException;
    }

    @Override
    public ErrorDefinition resolve(Throwable error) {
        return ErrorDefinition.of(ErrorCode.NOT_FOUND, error.getMessage());
    }
}
