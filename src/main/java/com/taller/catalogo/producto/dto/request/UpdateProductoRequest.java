package com.taller.catalogo.producto.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @NotNull(message = "La unidad base es obligatoria")
        @Positive(message = "El id de unidad base debe ser mayor que cero")
        Long unidadBaseId,

        @Positive(message = "El id de unidad de presentación debe ser mayor que cero")
        Long unidadPresentacionId,

        @Positive(message = "El factor de presentación debe ser mayor que cero")
        Integer factorPresentacion,

        @NotNull(message = "El stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo,

        Boolean activo,

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
