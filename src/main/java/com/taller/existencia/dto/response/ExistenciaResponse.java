package com.taller.existencia.dto.response;

import com.taller.producto.entity.Producto;

public record ExistenciaResponse(
        Long productoId,
        String codigo,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        boolean stockCritico
) {

    /** RN302: stockCritico cuando stockActual <= stockMinimo. */
    public static ExistenciaResponse from(Producto producto) {
        return new ExistenciaResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getStockActual(),
                producto.getStockMinimo(),
                producto.getStockActual() <= producto.getStockMinimo());
    }
}