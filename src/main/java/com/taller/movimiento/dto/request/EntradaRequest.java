package com.taller.movimiento.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record EntradaRequest(
        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull(message = "El proveedor es obligatorio")
        Long proveedorId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "Cantidad debe ser mayor que cero")
        Integer cantidad,

        @NotNull(message = "El costo unitario es obligatorio")
        @PositiveOrZero(message = "Costo unitario no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Costo unitario admite hasta 10 enteros y 2 decimales")
        BigDecimal costoUnitario
) {
}
