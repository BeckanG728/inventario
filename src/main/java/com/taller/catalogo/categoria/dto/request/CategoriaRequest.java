package com.taller.catalogo.categoria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Las unidades de la categoría no se envían: las relaciones base las siembra V10 y toda categoría nueva recibe UND. */
public record CategoriaRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 255) String descripcion) {
}