package com.taller.seguridad.autenticacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]{3,20}$",
                message = "Formato de username invalido. Solo se admiten letras, numeros y guión bajo."
        )
        String username,

        @NotBlank
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
                message = "La contraseña debe contener al menos una mayúscula y un número"
        )
        String password,

        @NotNull
        @Pattern(
                regexp = "^[A-Z_]+$",
                message = "Formato de rol invalido. Solo se admiten letras mayusculas y guion bajo."
        )
        String role
) {
}
