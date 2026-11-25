package com.taller.catalogo.producto.dto.response;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Integer stockActual,
        Integer stockMinimo,
        boolean stockCritico,
        BigDecimal costoUltimaCompra,
        boolean activo,
        Long categoriaId,
        String categoriaNombre,
        Long proveedorId,
        String proveedorRazonSocial,
        Long unidadBaseId,
        String unidadBaseCodigo,
        String unidadBaseNombre,
        Long unidadPresentacionId,
        String unidadPresentacionCodigo,
        Integer factorPresentacion
) {
}
