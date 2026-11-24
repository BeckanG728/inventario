package com.taller.monitoreo.dto.response;

import com.taller.catalogo.producto.dto.response.ProductoResponse;

public record ExistenciaResponse(
        Long productoId,
        String codigo,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        boolean stockCritico
) {

    /** RN302: stockCritico cuando stockActual <= stockMinimo. */
    public static ExistenciaResponse from(ProductoResponse producto) {
        return new ExistenciaResponse(
                producto.id(),
                producto.codigo(),
                producto.nombre(),
                producto.stockActual(),
                producto.stockMinimo(),
                producto.stockActual() <= producto.stockMinimo());
    }
}
