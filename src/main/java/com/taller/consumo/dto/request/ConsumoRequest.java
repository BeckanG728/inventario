package com.taller.consumo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ConsumoRequest(
        @NotBlank(message = "La finalidad es obligatoria")
        @Size(max = 255, message = "La finalidad admite hasta 255 caracteres")
        String finalidad,

        @NotEmpty(message = "Debe registrar al menos un producto")
        @Valid
        List<ConsumoItemRequest> items,

        @Size(max = 150, message = "La referencia admite hasta 150 caracteres")
        String referenciaVehiculoCliente
) {
}
