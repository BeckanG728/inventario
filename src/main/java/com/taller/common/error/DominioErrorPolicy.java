package com.taller.common.error;

import com.taller.common.exception.DuplicateResourceException;
import com.taller.common.exception.StockInsuficienteException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
public class DominioErrorPolicy implements ErrorPolicy {

    @Override
    public boolean supports(Throwable error) {
        return error instanceof DuplicateResourceException
                || error instanceof StockInsuficienteException;
    }

    @Override
    public ErrorDefinition resolve(Throwable error) {
        if (error instanceof DuplicateResourceException) {
            return ErrorDefinition.of(ErrorCode.DUPLICATE_RESOURCE, error.getMessage());
        }
        return ErrorDefinition.of(ErrorCode.STOCK_INSUFICIENTE, error.getMessage());
    }
}
