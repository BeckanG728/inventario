package com.taller.producto.dto.response;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String codigo,
        String sku,
        String nombre,
        String descripcion,
        String unidadMedida,
        Integer stockActual,
        Integer stockMinimo,
        boolean stockCritico,
        BigDecimal costoUltimaCompra,
        boolean activo,
        Long categoriaId,
        String categoriaNombre,
        Long proveedorId,
        String proveedorRazonSocial
) {
}
