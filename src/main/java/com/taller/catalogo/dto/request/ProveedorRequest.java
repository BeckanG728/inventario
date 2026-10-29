package com.taller.catalogo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
        @NotBlank @Size(max = 20) String ruc,
        @NotBlank @Size(max = 150) String razonSocial,
        @Size(max = 100) String contacto,
        @Size(max = 30) String telefono,
        @Email @Size(max = 150) String correo,
        @Size(max = 255) String direccion) {
}