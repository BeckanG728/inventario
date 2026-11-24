package com.taller.inventario.error;

import com.taller.compartido.error.ErrorCode;
import com.taller.compartido.error.ErrorDefinition;
import com.taller.compartido.error.ErrorPolicy;
import com.taller.compartido.exception.StockInsuficienteException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Política de errores de inventario: stock insuficiente en entradas, consumos y ajustes. */
@Component
@Order(31)
public class InventarioErrorPolicy implements ErrorPolicy {

    @Override
    public boolean supports(Throwable error) {
        return error instanceof StockInsuficienteException || error instanceof ConflictoIdempotenciaException;
    }

    @Override
    public ErrorDefinition resolve(Throwable error) {
        ErrorCode code = error instanceof ConflictoIdempotenciaException
                ? ErrorCode.DUPLICATE_RESOURCE : ErrorCode.STOCK_INSUFICIENTE;
        return ErrorDefinition.of(code, error.getMessage());
    }
}
