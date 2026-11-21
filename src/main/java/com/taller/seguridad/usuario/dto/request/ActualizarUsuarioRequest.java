package com.taller.seguridad.usuario.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ActualizarUsuarioRequest(

        @NotNull
        @Pattern(regexp = "^[A-Z_]+$", message = "Formato de rol invalido. Solo se admiten letras mayusculas y guion bajo.")
        String role,

        @NotNull
        Boolean activo
) {
}
