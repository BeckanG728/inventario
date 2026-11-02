package com.taller.reporte.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * US-18 — Consumo agrupado por producto y mecánico.
 */
public record ReporteConsumoItem(
        Long productoId,
        String productoCodigo,
        String productoNombre,
        String mecanico,
        List<String> finalidades,
        long cantidadTotal,
        List<Instant> fechas
) {
}
