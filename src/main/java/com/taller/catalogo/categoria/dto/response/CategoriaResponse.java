package com.taller.catalogo.categoria.dto.response;

import com.taller.catalogo.categoria.entity.Categoria;
import com.taller.catalogo.unidad.dto.response.UnidadResponse;

import java.util.List;

public record CategoriaResponse(Long id, String nombre, String descripcion, boolean activo,
                               List<UnidadResponse> unidades) {

    public static CategoriaResponse from(Categoria c) {
        List<UnidadResponse> unidades = c.getUnidades() == null
                ? List.of()
                : c.getUnidades().stream().map(UnidadResponse::from).toList();
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.isActivo(), unidades);
    }
}
