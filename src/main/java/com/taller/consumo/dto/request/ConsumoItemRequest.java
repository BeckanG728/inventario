package com.taller.consumo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConsumoItemRequest(
        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El producto debe ser válido")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad
) {
}
