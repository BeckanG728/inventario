package com.taller.catalogo.unidad.dto.response;

import com.taller.catalogo.unidad.entity.Unidad;

/** Unidad de inventario expuesta al frontend. */
public record UnidadResponse(
        Long id,
        String codigo,
        String nombre,
        boolean permiteFragmentacion
) {
    public static UnidadResponse from(Unidad unidad) {
        return new UnidadResponse(
                unidad.getId(),
                unidad.getCodigo(),
                unidad.getNombre(),
                unidad.isPermiteFragmentacion());
    }
}
