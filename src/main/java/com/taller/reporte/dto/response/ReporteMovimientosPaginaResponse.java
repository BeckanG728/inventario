package com.taller.reporte.dto.response;

import com.taller.inventario.movimiento.dto.response.MovimientoResponse;
import java.util.List;

/**
 * Reporte de movimientos paginado. Los totales por tipo cubren todo el filtro, no solo la página.
 */
public record ReporteMovimientosPaginaResponse(
        long totalEntradas,
        long totalConsumos,
        long totalAjustes,
        List<MovimientoResponse> movimientos,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
