package com.taller.movimiento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AjusteRequest(
        @NotNull
        Long productoId,

        @NotNull
        @PositiveOrZero
        Integer cantidadFisica,

        @NotBlank
        @Size(max = 255)
        String justificacion
) {
}
