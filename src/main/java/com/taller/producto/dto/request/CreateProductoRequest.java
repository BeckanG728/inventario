package com.taller.producto.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateProductoRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 50, message = "El código no puede superar los 50 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @NotBlank(message = "La unidad de medida es obligatoria")
        @Size(max = 30, message = "La unidad de medida no puede superar los 30 caracteres")
        String unidadMedida,

        @NotNull(message = "El stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo,

        @NotNull(message = "La categoría es obligatoria")
        @Positive(message = "El id de categoría debe ser mayor que cero")
        @JsonProperty("id_categoria")
        @JsonAlias({"idCategoria", "categoriaId"})
        Long idCategoria,

        @NotNull(message = "El proveedor es obligatorio")
        @Positive(message = "El id de proveedor debe ser mayor que cero")
        @JsonProperty("id_proveedor")
        @JsonAlias({"idProveedor", "proveedorId"})
        Long idProveedor
) {
}
