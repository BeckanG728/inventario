package com.taller.inventario.movimiento.dto.response;

import com.taller.inventario.api.MovimientoDetalle;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Movimiento en el reporte: incluye el producto y el usuario resueltos, a diferencia de
 * {@code MovimientoResponse}, que solo expone sus identificadores en las respuestas de escritura.
 */
public record MovimientoReporteResponse(
        Long id,
        Instant fecha,
        TipoMovimiento tipo,
        Integer cantidad,
        Integer variacion,
        Integer stockAnterior,
        Integer stockPosterior,
        BigDecimal costoUnitario,
        String motivo,
        Long productoId,
        String productoCodigo,
        String productoNombre,
        Long consumoId,
        Long usuarioId,
        String usuarioUsername
) {

    public static MovimientoReporteResponse from(MovimientoDetalle movimiento) {
        return new MovimientoReporteResponse(
                movimiento.id(),
                movimiento.fecha(),
                movimiento.tipo(),
                movimiento.cantidad(),
                movimiento.variacion(),
                movimiento.stockAnterior(),
                movimiento.stockPosterior(),
                movimiento.costoUnitario(),
                movimiento.motivo(),
                movimiento.productoId(),
                movimiento.productoCodigo(),
                movimiento.productoNombre(),
                movimiento.consumoId(),
                movimiento.usuarioId(),
                movimiento.usuarioUsername());
    }
}
