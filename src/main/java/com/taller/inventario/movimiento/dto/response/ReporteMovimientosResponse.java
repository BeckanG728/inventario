package com.taller.inventario.movimiento.dto.response;

import java.util.List;

public record ReporteMovimientosResponse(
        long totalEntradas,
        long totalConsumos,
        long totalAjustes,
        List<MovimientoReporteResponse> movimientos
) {
}
