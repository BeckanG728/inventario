package com.taller.inventario.consumo.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ConsumoItemRequest(
        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El producto debe ser válido")
        Long productoId,

        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad,

        String unidadRegistro,

        @Positive(message = "El factor debe ser mayor que cero")
        Integer factorAplicado,

        @Positive(message = "La cantidad exacta debe ser mayor que cero")
        @Digits(integer = 10, fraction = 4, message = "La cantidad exacta admite hasta 4 decimales")
        BigDecimal cantidadExacta
) {
}
