package com.taller.movimiento.dto.response;

import com.taller.movimiento.entity.MovimientoInventario;
import com.taller.movimiento.enums.TipoMovimiento;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimientoResponse(
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
        Long consumoId,
        Long usuarioId
) {

    public static MovimientoResponse from(MovimientoInventario movimiento) {
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getFecha(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getVariacion(),
                movimiento.getStockAnterior(),
                movimiento.getStockPosterior(),
                movimiento.getCostoUnitario(),
                movimiento.getMotivo(),
                movimiento.getProducto().getId(),
                movimiento.getConsumoId(),
                movimiento.getUsuario().getId());
    }
}
