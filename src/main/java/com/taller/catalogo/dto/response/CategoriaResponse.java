package com.taller.catalogo.dto.response;

import com.taller.catalogo.entity.Categoria;

public record CategoriaResponse(Long id, String nombre, String descripcion, boolean activo) {

    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.isActivo());
    }
}