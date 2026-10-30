package com.taller.reporte.dto.response;

import com.taller.movimiento.dto.response.MovimientoResponse;
import java.util.List;

public record ReporteMovimientosResponse(
        long totalEntradas,
        long totalConsumos,
        long totalAjustes,
        List<MovimientoResponse> movimientos
) {
}
