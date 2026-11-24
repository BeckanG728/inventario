package com.taller.reportes.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Valor de inventario paginado para la interfaz. Los totales cubren todos los productos activos,
 * no solo la página; {@code detalle} contiene únicamente los registros de la página.
 */
public record ValorInventarioPaginaResponse(
        BigDecimal totalGeneral,
        int totalProductosConCosto,
        int productosSinCosto,
        List<ValorInventarioDetalle> detalle,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
