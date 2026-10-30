package com.taller.producto.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockMinimoRequest(
        @NotNull(message = "El nuevo stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer nuevoStockMinimo
) {
}
