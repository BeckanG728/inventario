package com.taller.inventario.movimiento.dto.response;

import java.util.List;

/**
 * Reporte de movimientos paginado. Los totales por tipo cubren todo el filtro, no solo la página.
 */
public record ReporteMovimientosPaginaResponse(
        long totalEntradas,
        long totalConsumos,
        long totalAjustes,
        List<MovimientoReporteResponse> movimientos,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
