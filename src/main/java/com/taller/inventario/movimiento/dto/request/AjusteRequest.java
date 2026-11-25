package com.taller.inventario.movimiento.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AjusteRequest(
        @NotNull
        Long productoId,

        @PositiveOrZero
        Integer cantidadFisica,

        @NotBlank
        @Size(max = 255)
        String justificacion,

        String unidadRegistro,

        @Positive(message = "El factor debe ser mayor que cero")
        Integer factorAplicado,

        @PositiveOrZero
        @Digits(integer = 10, fraction = 4, message = "La cantidad exacta admite hasta 4 decimales")
        BigDecimal cantidadFisicaExacta
) {
}
